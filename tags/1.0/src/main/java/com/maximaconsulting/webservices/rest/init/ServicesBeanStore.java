package com.maximaconsulting.webservices.rest.init;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.maximaconsulting.webservices.WebServicesScanner;

/**
 * All the rest controllers will be fetching actual service implementation beans
 * from here.
 * 
 * @author anurag.verma
 */
public class ServicesBeanStore implements ApplicationContextAware, InitializingBean {
	private Map<String, Class<?>> service = new HashMap<String, Class<?>>();
	private ApplicationContext context;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		for (Class<?> serviceInterface : WebServicesScanner.getWebServices()) {
			service.put(WebServicesScanner.getServiceName(serviceInterface), serviceInterface);
		}
	}

	/**
	 * gets the Bean of specified type from Spring Context
	 * 
	 * @param serviceName
	 * @return the bean
	 */
	public Object getBeanForService(String serviceName) {
		return context.getBean(service.get(serviceName));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
}
