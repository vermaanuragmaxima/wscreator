package com.maximaconsulting.webservices.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.NotFoundException;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.maximaconsulting.webservices.WebServicesScanner;
import com.maximaconsulting.webservices.rest.proxycreation.RestServiceCreator;

/**
 * This will be exporting the REST services for all the interfaces annotated
 * with {@link WebService} annotation.
 * 
 * @author anurag.verma
 */
public class RestServiceExporter {
	private final Log logger = LogFactory.getLog(getClass());
	private final RestServiceCreator restServiceCreator = new RestServiceCreator();

	/**
	 * Exports REST WebServices for all the Service Interfaces in
	 */
	public void exportRestServices() {
		Set<Class<?>> serviceInterfaces = WebServicesScanner.getWebServices();
		importToJavassistEnvironment(serviceInterfaces);
		restServiceCreator.createRestProxy(serviceInterfaces);
	}

	/**
	 * finds the classpath for a Particular class
	 * 
	 * @param clazz
	 * @return the classpath
	 * @throws UnsupportedEncodingException
	 */
	private String getClassPath(Class<?> clazz) throws UnsupportedEncodingException {
		String classpath;
		String resourcePath = clazz.getResource(clazz.getSimpleName() + ".class").toString();
		classpath = resourcePath.split("/" + (clazz.getPackage().getName().replaceAll("\\.", "/")))[0];
		classpath = classpath.endsWith("!") ? classpath.substring(0, classpath.length() - 1) : classpath;
		classpath = classpath.startsWith("jar:") ? classpath.substring(4) : classpath;
		classpath = classpath.startsWith("file:") ? classpath.substring(5) : classpath;
		classpath = classpath.charAt(2) == ':' ? classpath.substring(1) : classpath;
		classpath = URLDecoder.decode(classpath, "UTF-8");
		return classpath;
	}

	/**
	 * imports a class into the Javassist Environment so that Javassist context
	 * could use the class
	 * 
	 * @param serviceInterfaces
	 * @throws NotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void importToJavassistEnvironment(Set<Class<?>> serviceInterfaces) {
		try {
			Set<String> classPaths = new HashSet<String>();
			for (Class<?> clazz : serviceInterfaces) {
				classPaths.add(getClassPath(clazz));
				final XmlSeeAlso xmlSeeAnnotation = clazz.getAnnotation(XmlSeeAlso.class);
				if (xmlSeeAnnotation != null) {
					final Class<?>[] usedTypes = xmlSeeAnnotation.value();
					if (usedTypes != null) {
						for (Class<?> usedType : usedTypes) {
							classPaths.add(getClassPath(usedType));
						}
					}
				}
			}
			classPaths.add(getClassPath(Response.class));
			for (String packageName : classPaths) {
				ClassPool.getDefault().insertClassPath(packageName);
			}
		}
		catch (Exception e) {
			logger.fatal("Error occured while importing service Interfaces to javassist classpath.", e);
			throw new RuntimeException(e);
		}
	}
}
