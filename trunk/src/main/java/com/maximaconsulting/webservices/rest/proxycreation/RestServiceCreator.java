package com.maximaconsulting.webservices.rest.proxycreation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;

import com.maximaconsulting.webservices.WebServicesScanner;
import com.maximaconsulting.webservices.annotations.HiddenMethod;
import com.maximaconsulting.webservices.rest.init.ServicesBeanStore;

/**
 * This will create a class which will actually be a Rest {@link Controller} for
 * a particular service.
 * 
 * @author anurag.verma
 */
public class RestServiceCreator extends ByteCodeWriter {
	public static final List<String> primitiveTypeNames = new ArrayList<String>();
	static {
		primitiveTypeNames.add("boolean");
		primitiveTypeNames.add("int");
		primitiveTypeNames.add("short");
		primitiveTypeNames.add("long");
		primitiveTypeNames.add("double");
		primitiveTypeNames.add("float");
		primitiveTypeNames.add("byte");
		primitiveTypeNames.add("char");
		primitiveTypeNames.add(Boolean.class.getName());
		primitiveTypeNames.add(Integer.class.getName());
		primitiveTypeNames.add(Short.class.getName());
		primitiveTypeNames.add(Long.class.getName());
		primitiveTypeNames.add(Double.class.getName());
		primitiveTypeNames.add(Float.class.getName());
		primitiveTypeNames.add(Byte.class.getName());
		primitiveTypeNames.add(Character.class.getName());
		primitiveTypeNames.add(String.class.getName());
		primitiveTypeNames.add(Date.class.getName());
	}
	private final Log logger = LogFactory.getLog(getClass());
	private final EnvelopedProxyCreator envelopedProxyCreator = new EnvelopedProxyCreator();
	private final SimilarSignatureProxyCreator similarSignatureProxyCreator = new SimilarSignatureProxyCreator();
	private final NoParamProxyCreator noParamProxyCreator = new NoParamProxyCreator();

	/**
	 * creates a Spring MVC Controller class which has all the rest variants of
	 * all methods in all the Service Interfaces.
	 * 
	 * @param serviceInterfaces
	 * @return the Controller class.
	 */
	public Class<?> createRestProxy(Set<Class<?>> serviceInterfaces) {
		try {
			CtClass proxy = createClass("WSCreatorRestProxyController");
			addServiceFields(proxy, serviceInterfaces);
			for (Class<?> serviceInterface : serviceInterfaces) {
				validateMethodNames(serviceInterface);
				addProxyMethods(proxy, serviceInterface);
			}
			return proxy.toClass();
		}
		catch (Exception e) {
			logger.fatal("Error creating rest proxy, " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * adds all the REST variant methods for the methods of Service Interface to
	 * proxy
	 * 
	 * @param proxy
	 * @param serviceInterface
	 */
	public void addProxyMethods(CtClass proxy, Class<?> serviceInterface) {
		String serviceName = WebServicesScanner.getServiceName(serviceInterface);
		try {
			for (Method method : serviceInterface.getDeclaredMethods()) {
				if (method.getAnnotation(HiddenMethod.class) == null) {
					String variableName = "obj" + serviceInterface.getSimpleName();
					createProxyMethods(proxy, method, variableName, serviceName);
				}
			}
		}
		catch (Exception e) {
			logger.fatal("Error creating rest proxy for " + serviceInterface.getName(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * adds fields in the proxy class to hold the actual implementation class
	 * object, Service Name & {@link ServicesBeanStore} to get the relevant
	 * services if needed.
	 * 
	 * @param proxy
	 * @param serviceInterfaces
	 * @throws CannotCompileException
	 */
	private void addServiceFields(CtClass proxy, Set<Class<?>> serviceInterfaces) throws CannotCompileException {
		addFieldWithSetter(proxy, "servicesBeanStore", ServicesBeanStore.class);
		StringBuilder initMethodCode = new StringBuilder("public void init(){");
		for (Class<?> serviceInterface : serviceInterfaces) {
			String variableName = "obj" + serviceInterface.getSimpleName();
			addFieldWithSetter(proxy, variableName, serviceInterface);
			String serviceName = WebServicesScanner.getServiceName(serviceInterface);
			initMethodCode.append("\nthis." + variableName + " = (" + serviceInterface.getName() + ") servicesBeanStore.getBeanForService(\"" + serviceName + "\");");
		}
		initMethodCode.append("\n}");
		CtMethod initMethod = CtMethod.make(initMethodCode.toString(), proxy);
		proxy.addMethod(initMethod);
	}

	/**
	 * checks whether all the parameters of a method are of primitive datatype.
	 * 
	 * @param method
	 * @return true if all params of method are primitive, false otherwise
	 */
	private boolean areAllParametersPrimitive(Method method) {
		boolean areAllPrimitives = true;
		for (Class<?> parameterType : method.getParameterTypes()) {
			if (!primitiveTypeNames.contains(parameterType.getName())) {
				areAllPrimitives = false;
				break;
			}
		}
		return areAllPrimitives;
	}

	/**
	 * creates the controller class & adds the {@link Controller} annotation
	 * 
	 * @param serviceName
	 * @return the Javassist class for rest proxy of Service Interface
	 */
	private CtClass createClass(String serviceName) {
		CtClass proxy = ClassPool.getDefault().makeClass(serviceName);
		addAnnotationToClass(proxy, Controller.class, null);
		return proxy;
	}

	/**
	 * adds all possible REST variants possible for an actual service Method.
	 * 
	 * @param proxy
	 * @param method
	 * @param variableName
	 * @param serviceName
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void createProxyMethods(CtClass proxy, Method method, String variableName, String serviceName) throws CannotCompileException, NotFoundException, IllegalAccessException, InstantiationException {
		try {
			final String pattern = "/" + serviceName + "/" + method.getName();
			if (hasParams(method)) {
				envelopedProxyCreator.create(proxy, method, variableName, pattern, serviceName);
				if (areAllParametersPrimitive(method)) {
					similarSignatureProxyCreator.create(proxy, method, variableName, pattern);
				}
			} else {
				noParamProxyCreator.create(proxy, method, variableName, pattern);
			}
		}
		catch (CannotCompileException e) {
			logger.fatal("Error creating proxy method for " + method.getDeclaringClass().getName() + "." + method.getName(), e);
			throw e;
		}
		catch (InstantiationException e) {
			logger.fatal("Error creating proxy method for " + method.getDeclaringClass().getName() + "." + method.getName(), e);
			throw e;
		}
		catch (IllegalAccessException e) {
			logger.fatal("Error creating proxy method for " + method.getDeclaringClass().getName() + "." + method.getName(), e);
			throw e;
		}
	}

	/**
	 * checks whether a method has any parameters.
	 * 
	 * @param method
	 * @return true if method has atleast one parameter.
	 */
	private boolean hasParams(Method method) {
		return method.getParameterTypes().length > 0;
	}

	/**
	 * As first round of validation verifies that a service class has no 2
	 * methods of same name.
	 * 
	 * @param serviceInterface
	 */
	private void validateMethodNames(Class<?> serviceInterface) {
		Set<String> names = new HashSet<String>();
		for (Method method : serviceInterface.getDeclaredMethods()) {
			names.add(method.getName());
		}
		if (names.size() != serviceInterface.getDeclaredMethods().length) {
			throw new RuntimeException("Duplicate Methods Found: One service can not have multiple methods of same name (even with different parameters & return type)");
		}
	}
}
