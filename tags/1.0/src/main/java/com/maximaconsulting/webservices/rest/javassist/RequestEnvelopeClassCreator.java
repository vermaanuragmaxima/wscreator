package com.maximaconsulting.webservices.rest.javassist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import javax.jws.WebParam;
import javax.xml.bind.annotation.XmlRootElement;

import com.maximaconsulting.webservices.rest.typeconversion.CollectionConverter;
import com.maximaconsulting.webservices.rest.typeconversion.Converters;

/**
 * This will create the class which will act as a request pojo for REST POST
 * requests.
 * 
 * @author anurag.verma
 */
class RequestEnvelopeClassCreator extends ByteCodeWriter {
	/**
	 * creates a class which has all the parameters of a method as fields. this
	 * will be used in making POST variants for actual service methods.
	 * 
	 * @param method
	 * @param serviceName
	 * @return the wrapper class
	 * @throws CannotCompileException
	 * @throws RuntimeException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NotFoundException
	 */
	Class<?> create(Method method, String serviceName) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException {
		validateMethodForParamAnnotations(method);
		final String envelopeClassName = serviceName + Character.toUpperCase(method.getName().charAt(0)) + method.getName().substring(1);
		CtClass clazz = ClassPool.getDefault().makeClass(envelopeClassName);
		final HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("name", method.getName());
		addAnnotationToClass(clazz, XmlRootElement.class, attributes);
		Type[] parameterTypes = method.getGenericParameterTypes();
		java.lang.annotation.Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (int i = 0; i < parameterTypes.length; i++) {
			addProperty(clazz, parameterTypes[i], parameterAnnotations[i]);
		}
		return clazz.toClass();
	}

	/**
	 * add getter method of a field.
	 * 
	 * @param clazz
	 * @param parameterType
	 * @param type
	 * @param paramName
	 * @param capsCaseName
	 * @throws CannotCompileException
	 */
	private void addGetter(CtClass clazz, Class<?> parameterType, Class<?> innerType, String paramName, String capsCaseName) throws CannotCompileException {
		String code;
		if (innerType == null) {
			code = "public " + parameterType.getName() + " get" + capsCaseName + "() { return " + paramName + "; }";
		} else {
			code = "public " + parameterType.getName() + " get" + capsCaseName + "() { return new " + CollectionConverter.class.getName() + "(" + innerType.getName() + ".class).convert(" + paramName + "); }";
		}
		addMethod(clazz, code);
	}

	/**
	 * add one parameter of actual service method as a field in Envelope class
	 * 
	 * @param clazz
	 * @param type
	 * @param parameterAnnotations
	 * @throws CannotCompileException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NotFoundException
	 * @throws RuntimeException
	 */
	private void addProperty(CtClass clazz, Type type, java.lang.annotation.Annotation[] parameterAnnotations) throws CannotCompileException, RuntimeException, NotFoundException, InstantiationException, IllegalAccessException {
		String paramName = getParamName(parameterAnnotations);
		Class<?> rawType;
		Class<?> innerType = null;
		if (type instanceof ParameterizedType) {
			rawType = (Class<?>) ((ParameterizedType) type).getRawType();
			innerType = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
			Converters.ensureConverterPresence(innerType);
		} else {
			rawType = (Class<?>) type;
		}
		addField(clazz, rawType, paramName);
		String capsCaseName = Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
		addGetter(clazz, rawType, innerType, paramName, capsCaseName);
		addSetter(clazz, rawType, paramName, capsCaseName);
	}

	/**
	 * adds setter of a field.
	 * 
	 * @param clazz
	 * @param parameterType
	 * @param paramName
	 * @param capsCaseName
	 * @throws CannotCompileException
	 */
	private void addSetter(CtClass clazz, Class<?> parameterType, String paramName, String capsCaseName) throws CannotCompileException {
		final String code = "public void set" + capsCaseName + "(" + parameterType.getName() + " x) { this." + paramName + " = x; }";
		addMethod(clazz, code);
	}

	/**
	 * verifies whether all parameters of the service method have the
	 * {@link WebParam} annotation & a name is given to them.
	 * 
	 * @param method
	 */
	private void validateMethodForParamAnnotations(Method method) {
		final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (int i = 0; i < parameterAnnotations.length; i++) {
			Annotation[] annotations = parameterAnnotations[i];
			final String paramName = getParamName(annotations);
			if (paramName == null) {
				throw new RuntimeException("Parameter name missing for class " + method.getDeclaringClass().getName() + " method " + method.getName() + " parameter " + (i + 1) + ". Did you forget to add @WebParam annotation to parameter?");
			}
			if (paramName.isEmpty()) {
				throw new RuntimeException("Parameter name missing for class " + method.getDeclaringClass().getName() + " method " + method.getName() + " parameter " + (i + 1) + ". Did you forget to add name attribute to @WebParam annotation?");
			}
		}
	}
}
