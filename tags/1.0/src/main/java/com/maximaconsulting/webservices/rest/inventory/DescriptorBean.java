package com.maximaconsulting.webservices.rest.inventory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jws.WebParam;

import com.maximaconsulting.webservices.WebServicesScanner;
import com.maximaconsulting.webservices.rest.javassist.RestServiceCreator;

/**
 * Backing Bean for descriptor.jsp
 * 
 * @author anurag.verma
 */
public class DescriptorBean {
	private String baseUrl;
	private Class<?> service;
	private StringBuilder page;
	private Method method;
	private Annotation[][] annotations;
	private Class<?>[] types;

	/**
	 * @param baseUrl
	 * @param service
	 * @param method
	 * @param page
	 */
	public DescriptorBean(String baseUrl, Class<?> service, Method method, StringBuilder page) {
		this.baseUrl = baseUrl;
		this.service = service;
		this.page = page;
		this.method = method;
		annotations = method.getParameterAnnotations();
		types = method.getParameterTypes();
	}

	/**
	 * finds all the HTTP methods which are allowed to use for a method
	 * 
	 * @return allowed http methods
	 */
	public String getAllowedMethods() {
		if (areAllParamsPrimitives()) {
			return "GET, POST";
		}
		return "POST";
	}

	/**
	 * prepares the title of page
	 * 
	 * @return page title
	 */
	public String getPageTitle() {
		return WebServicesScanner.getServiceName(service) + "." + method.getName();
	}

	/**
	 * finds name of Service
	 * 
	 * @return service name
	 */
	public String getServiceName() {
		return "/" + WebServicesScanner.getServiceName(service) + "/" + method.getName();
	}

	/**
	 * url of service for invocation
	 * 
	 * @return service Url
	 */
	public String getServiceUrl() {
		return baseUrl + WebServicesScanner.getServiceName(service) + "/" + method.getName();
	}

	/**
	 * prepare a sample JSON content to invoke the method
	 */
	public void writeParamJson() {
		page.append("{");
		for (int i = 0; i < types.length; i++) {
			Class<?> type = types[i];
			String paramName = getParamName(i);
			if (i != 0) {
				page.append(",");
			}
			page.append("&quot;" + paramName + "&quot;:");
			writeTypeJson(type);
		}
		page.append("}");
	}

	/**
	 * prepares sample XML content to call the service
	 */
	public void writeParamXml() {
		page.append("&lt;" + method.getName() + "&gt;");
		for (int i = 0; i < types.length; i++) {
			Class<?> type = types[i];
			String paramName = getParamName(i);
			page.append("&lt;" + paramName + "&gt;");
			writeTypeXml(type);
			page.append("&lt;/" + paramName + "&gt;");
		}
		page.append("&lt;/" + method.getName() + "&gt;");
	}

	/**
	 * checks whether all the parameters of the method are primitive.
	 * 
	 * @return true if all parameters are primitive, false otherwise
	 */
	private boolean areAllParamsPrimitives() {
		boolean areAllPrimitives = true;
		for (Class<?> parameterType : method.getParameterTypes()) {
			if (!RestServiceCreator.primitiveTypeNames.contains(parameterType.getName())) {
				areAllPrimitives = false;
				break;
			}
		}
		return areAllPrimitives;
	}

	/**
	 * get the name of method parameter at specified index
	 * 
	 * @param index
	 * @return name of method parameter
	 */
	private String getParamName(int index) {
		String paramName = null;
		for (java.lang.annotation.Annotation annotation : annotations[index]) {
			if (annotation instanceof WebParam) {
				paramName = ((WebParam) annotation).name();
				break;
			}
		}
		return paramName;
	}

	/**
	 * get the types of fields in the specified class
	 * 
	 * @param type
	 * @return map of field name & its type
	 */
	private Map<String, Class<?>> getTypeMap(Class<?> type) {
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		while (type != null && !type.equals(Object.class)) {
			for (Field field : type.getDeclaredFields()) {
				map.put(field.getName(), field.getType());
			}
			type = type.getSuperclass();
		}
		return map;
	}

	/**
	 * write sample JSON for the specified type
	 * 
	 * @param type
	 */
	private void writeTypeJson(Class<?> type) {
		if (!RestServiceCreator.primitiveTypeNames.contains(type.getName()) && (type.getSuperclass() == null || !type.getSuperclass().equals(Enum.class))) {
			page.append("{");
			Iterator<Map.Entry<String, Class<?>>> itr = getTypeMap(type).entrySet().iterator();
			for (int j = 0; itr.hasNext(); j++) {
				Map.Entry<String, Class<?>> entry = itr.next();
				if (j != 0) {
					page.append(",");
				}
				page.append("&quot;" + entry.getKey() + "&quot;:");
				writeTypeJson(entry.getValue());
			}
			page.append("}");
		} else {
			page.append("&quot;&quot;");
		}
	}

	/**
	 * write sample XML for the specified type
	 * 
	 * @param type
	 */
	private void writeTypeXml(Class<?> type) {
		if (!RestServiceCreator.primitiveTypeNames.contains(type.getName()) && (type.getSuperclass() == null || !type.getSuperclass().equals(Enum.class))) {
			Iterator<Map.Entry<String, Class<?>>> itr = getTypeMap(type).entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, Class<?>> entry = itr.next();
				page.append("&lt;" + entry.getKey() + "&gt;");
				writeTypeXml(entry.getValue());
				page.append("&lt;/" + entry.getKey() + "&gt;");
			}
		}
	}
}
