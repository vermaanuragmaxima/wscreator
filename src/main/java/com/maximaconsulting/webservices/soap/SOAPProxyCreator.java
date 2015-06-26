package com.maximaconsulting.webservices.soap;

import java.lang.reflect.Method;
import java.util.HashMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import com.maximaconsulting.webservices.WebServicesScanner;
import com.maximaconsulting.webservices.rest.proxycreation.ByteCodeWriter;

/**
 * Creates proxy Implementation classes for SOAP.
 * 
 * @author anurag.verma
 */
public class SOAPProxyCreator extends ByteCodeWriter {
	private final Log logger = LogFactory.getLog(getClass());
	private BeanFactory beanFactory;

	/**
	 * @param beanFactory
	 */
	public SOAPProxyCreator(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * @param serviceInterface
	 * @return returns a proxy Object for the service interface passed in
	 */
	public Object getProxyWS(Class<?> serviceInterface) {
		try {
			logger.info("Creating SOAP proxy for " + serviceInterface.getName());
			Class<?> proxyClass = getProxyClass(serviceInterface);
			Object proxy = proxyClass.newInstance();
			Method method = proxyClass.getDeclaredMethod("setService", serviceInterface);
			method.invoke(proxy, beanFactory.getBean(serviceInterface));
			logger.info("Successfully created SOAP proxy for " + serviceInterface.getName());
			return proxy;
		}
		catch (Exception e) {
			logger.fatal("Error creating SOAP proxy for " + serviceInterface.getName(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * creates a javassist class for the interface passed in
	 * 
	 * @param serviceInterface
	 * @return the ready to code javassist class.
	 * @throws Exception
	 */
	private CtClass createClass(Class<?> serviceInterface) throws Exception {
		String serviceName = WebServicesScanner.getServiceName(serviceInterface);
		ClassPool pool = ClassPool.getDefault();
		pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
		final CtClass ctServiceInterface = pool.get(serviceInterface.getName());
		CtClass proxy = pool.makeClass("SOAP" + serviceName);
		proxy.addInterface(ctServiceInterface);
		addAnnotationToClass(proxy, WebService.class, getAttributes(serviceInterface));
		return proxy;
	}

	/**
	 * pulls out the values configured in {@link WebService} annotation.
	 * Populates defaults if nothing found.
	 * 
	 * @param serviceInterface
	 * @return {@link HashMap} with attribute values
	 */
	private HashMap<String, Object> getAttributes(Class<?> serviceInterface) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		WebService annotation = serviceInterface.getAnnotation(WebService.class);
		attributes.put("name", annotation.name() == null || annotation.name().isEmpty() ? WebServicesScanner.getServiceName(serviceInterface) : annotation.name());
		attributes.put("serviceName", WebServicesScanner.getServiceName(serviceInterface));
		attributes.put("targetNamespace", getNamespace(serviceInterface, annotation));
		if (annotation.endpointInterface() != null && !annotation.endpointInterface().isEmpty()) {
			attributes.put("endpointInterface", annotation.endpointInterface());
		}
		if (annotation.portName() != null && !annotation.portName().isEmpty()) {
			attributes.put("portName", annotation.portName());
		}
		if (annotation.wsdlLocation() != null && !annotation.wsdlLocation().isEmpty()) {
			attributes.put("wsdlLocation", annotation.wsdlLocation());
		}
		return attributes;
	}

	/**
	 * get value of namespace fropm {@link WebService} annotation or return a
	 * default value.
	 * 
	 * @param serviceInterface
	 * @param annotation
	 * @return namespace to be used
	 */
	private String getNamespace(Class<?> serviceInterface, WebService annotation) {
		if (annotation.targetNamespace() == null || annotation.targetNamespace().isEmpty()) {
			StringBuilder ns = new StringBuilder("http://");
			String[] pkg = serviceInterface.getPackage().getName().split("\\.");
			for (int i = pkg.length - 1; i >= 0; i--) {
				ns.append(pkg[i]);
				if (i > 0) {
					ns.append(".");
				}
			}
			ns.append("/");
			return ns.toString();
		} else {
			return annotation.targetNamespace();
		}
	}

	/**
	 * Create a java class which will act as a proxy to the actual SOAP
	 * implementation.
	 * 
	 * @param serviceInterface
	 * @return
	 * @throws Exception
	 */
	private Class<?> getProxyClass(Class<?> serviceInterface) throws Exception {
		CtClass ctClass = createClass(serviceInterface);
		addFieldWithSetter(ctClass, "service", serviceInterface);
		SOAPProxyMethodCreator methodCreator = new SOAPProxyMethodCreator();
		for (Method method : serviceInterface.getDeclaredMethods()) {
			methodCreator.create(ctClass, method);
		}
		return ctClass.toClass();
	}
}