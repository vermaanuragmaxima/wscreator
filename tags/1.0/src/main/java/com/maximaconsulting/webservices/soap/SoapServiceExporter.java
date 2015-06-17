package com.maximaconsulting.webservices.soap;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServiceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.spring.EndpointDefinitionParser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.maximaconsulting.webservices.WebServicesScanner;

/**
 * This will export SOAP web-services for all the interfaces annotated with
 * {@link WebService} annotation.
 * 
 * @author anurag.verma
 */
public class SoapServiceExporter implements ApplicationContextAware, BeanFactoryAware, InitializingBean, DisposableBean {
	public static final String DEFAULT_BASE_ADDRESS = "http://localhost:8080/";
	private final Log logger = LogFactory.getLog(getClass());
	private String baseAddress = DEFAULT_BASE_ADDRESS;
	private Map<String, Object> endpointProperties;
	private Executor executor;
	private String bindingType;
	private Object[] webServiceFeatures;
	private ListableBeanFactory beanFactory;
	private ApplicationContext applicationContext;
	private final Set<Endpoint> publishedEndpoints = new LinkedHashSet<Endpoint>();

	/**
	 * Immediately publish all endpoints when fully configured.
	 * 
	 * @see #publishEndpoints()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			Bus bus = (Bus) beanFactory.getBean("cxf");
			final Set<Class<?>> services = WebServicesScanner.getWebServices();
			SOAPProxyCreator soapProxyCreator = new SOAPProxyCreator(beanFactory);
			for (Class<?> serviceInterface : services) {
				Object proxy = soapProxyCreator.getProxyWS(serviceInterface);
				EndpointDefinitionParser.SpringEndpointImpl endpoint = new EndpointDefinitionParser.SpringEndpointImpl(bus, proxy);
				endpoint.setAddress("/" + WebServicesScanner.getServiceName(serviceInterface));
				endpoint.setApplicationContext(applicationContext);
				endpoint.publish();
				addClassAsEndpoint(proxy.getClass(), proxy);
			}
		}
		catch (Exception e) {
			logger.fatal("Error while exporting SOAP endpoints: " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void destroy() {
		for (Endpoint endpoint : this.publishedEndpoints) {
			endpoint.stop();
		}
	}

	/**
	 * Publish all {@link javax.jws.WebService} annotated beans in the
	 * containing BeanFactory.
	 * 
	 * @see #publishEndpoint
	 */
	public void publishEndpoints() {
		Set<String> beanNames = new LinkedHashSet<String>(this.beanFactory.getBeanDefinitionCount());
		beanNames.addAll(Arrays.asList(this.beanFactory.getBeanDefinitionNames()));
		if (this.beanFactory instanceof ConfigurableBeanFactory) {
			beanNames.addAll(Arrays.asList(((ConfigurableBeanFactory) this.beanFactory).getSingletonNames()));
		}
		for (String beanName : beanNames) {
			try {
				Class<?> type = this.beanFactory.getType(beanName);
				addClassAsEndpoint(type, this.beanFactory.getBean(beanName));
			}
			catch (CannotLoadBeanClassException ex) {
				// ignore beans where the class is not resolvable
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setBaseAddress(String baseAddress) {
		this.baseAddress = baseAddress;
	}

	/**
	 * Obtains all web service beans and publishes them as JAX-WS endpoints.
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalStateException(getClass().getSimpleName() + " requires a ListableBeanFactory");
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	/**
	 * Specify the binding type to use, overriding the value of the JAX-WS
	 * {@link javax.xml.ws.BindingType} annotation.
	 */
	public void setBindingType(String bindingType) {
		this.bindingType = bindingType;
	}

	/**
	 * Set the property bag for the endpoint, including properties such as
	 * "javax.xml.ws.wsdl.service" or "javax.xml.ws.wsdl.port".
	 * 
	 * @see javax.xml.ws.Endpoint#setProperties
	 * @see javax.xml.ws.Endpoint#WSDL_SERVICE
	 * @see javax.xml.ws.Endpoint#WSDL_PORT
	 */
	public void setEndpointProperties(Map<String, Object> endpointProperties) {
		this.endpointProperties = endpointProperties;
	}

	/**
	 * Set the JDK concurrent executor to use for dispatching incoming requests
	 * to exported service instances.
	 * 
	 * @see javax.xml.ws.Endpoint#setExecutor
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * Allows for providing JAX-WS 2.2 WebServiceFeature specifications: in the
	 * form of actual {@link javax.xml.ws.WebServiceFeature} objects,
	 * WebServiceFeature Class references, or WebServiceFeature class names.
	 */
	public void setWebServiceFeatures(Object[] webServiceFeatures) {
		this.webServiceFeatures = webServiceFeatures;
	}

	protected String calculateEndpointAddress(Endpoint endpoint, String serviceName) {
		String fullAddress = this.baseAddress + serviceName;
		if (endpoint.getClass().getName().startsWith("weblogic.")) {
			// Workaround for WebLogic 10.3
			fullAddress = fullAddress + "/";
		}
		return fullAddress;
	}

	/**
	 * Create the actual Endpoint instance.
	 * 
	 * @param bean
	 *            the service object to wrap
	 * @return the Endpoint instance
	 * @see Endpoint#create(Object)
	 * @see Endpoint#create(String, Object)
	 */
	protected Endpoint createEndpoint(Object bean) {
		if (this.webServiceFeatures != null) {
			return new FeatureEndpointProvider().createEndpoint(this.bindingType, bean, this.webServiceFeatures);
		} else {
			return Endpoint.create(this.bindingType, bean);
		}
	}

	protected void publishEndpoint(Endpoint endpoint, WebService annotation) {
		endpoint.publish(calculateEndpointAddress(endpoint, annotation.serviceName()));
	}

	protected void publishEndpoint(Endpoint endpoint, WebServiceProvider annotation) {
		endpoint.publish(calculateEndpointAddress(endpoint, annotation.serviceName()));
	}

	private void addClassAsEndpoint(Class<?> type, Object bean) {
		if (type != null && !type.isInterface()) {
			WebService wsAnnotation = type.getAnnotation(WebService.class);
			WebServiceProvider wsProviderAnnotation = type.getAnnotation(WebServiceProvider.class);
			if (wsAnnotation != null || wsProviderAnnotation != null) {
				Endpoint endpoint = createEndpoint(bean);
				if (this.endpointProperties != null) {
					endpoint.setProperties(this.endpointProperties);
				}
				if (this.executor != null) {
					endpoint.setExecutor(this.executor);
				}
				if (wsAnnotation != null) {
					publishEndpoint(endpoint, wsAnnotation);
				} else {
					publishEndpoint(endpoint, wsProviderAnnotation);
				}
				this.publishedEndpoints.add(endpoint);
			}
		}
	}

	/**
	 * Inner class in order to avoid a hard-coded JAX-WS 2.2 dependency. JAX-WS
	 * 2.0 and 2.1 didn't have WebServiceFeatures for endpoints yet...
	 */
	private class FeatureEndpointProvider {
		public Endpoint createEndpoint(String bindingType, Object implementor, Object[] features) {
			WebServiceFeature[] wsFeatures = new WebServiceFeature[features.length];
			for (int i = 0; i < features.length; i++) {
				wsFeatures[i] = convertWebServiceFeature(features[i]);
			}
			try {
				Method create = Endpoint.class.getMethod("create", String.class, Object.class, WebServiceFeature[].class);
				return (Endpoint) ReflectionUtils.invokeMethod(create, null, bindingType, implementor, wsFeatures);
			}
			catch (NoSuchMethodException ex) {
				throw new IllegalStateException("JAX-WS 2.2 not available - cannot create feature endpoints", ex);
			}
		}

		private WebServiceFeature convertWebServiceFeature(Object feature) {
			Assert.notNull(feature, "WebServiceFeature specification object must not be null");
			if (feature instanceof WebServiceFeature) {
				return (WebServiceFeature) feature;
			} else if (feature instanceof Class) {
				return (WebServiceFeature) BeanUtils.instantiate((Class<?>) feature);
			} else if (feature instanceof String) {
				try {
					Class<?> featureClass = getBeanClassLoader().loadClass((String) feature);
					return (WebServiceFeature) BeanUtils.instantiate(featureClass);
				}
				catch (ClassNotFoundException ex) {
					throw new IllegalArgumentException("Could not load WebServiceFeature class [" + feature + "]");
				}
			} else {
				throw new IllegalArgumentException("Unknown WebServiceFeature specification type: " + feature.getClass());
			}
		}

		private ClassLoader getBeanClassLoader() {
			return (beanFactory instanceof ConfigurableBeanFactory ? ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader() : ClassUtils.getDefaultClassLoader());
		}
	}
}
