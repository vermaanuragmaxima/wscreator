package com.maximaconsulting.webservices.rest.proxycreation;

import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.jws.WebMethod;
import javax.jws.WebParam;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Class with reusable methods for all other proxy creaters.
 * 
 * @author anurag.verma
 */
public abstract class ByteCodeWriter {
	/**
	 * adds the specified annotation to the new javassist class and adds all the
	 * annotation attributes provided.
	 * 
	 * @param clazz
	 * @param annotationClass
	 * @param attributes
	 */
	protected void addAnnotationToClass(CtClass clazz, Class<?> annotationClass, Map<String, Object> attributes) {
		ConstPool constPool = clazz.getClassFile().getConstPool();
		AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		final Annotation annotation = new Annotation(annotationClass.getName(), constPool);
		if (attributes != null) {
			for (Map.Entry<String, Object> entry : attributes.entrySet()) {
				final StringMemberValue value = new StringMemberValue(constPool);
				value.setValue((String) entry.getValue());
				annotation.addMemberValue(entry.getKey(), value);
			}
		}
		annotationsAttribute.addAnnotation(annotation);
		clazz.getClassFile().addAttribute(annotationsAttribute);
	}

	/**
	 * adds annotation of specified type to a field in javassist class
	 * 
	 * @param clazz
	 * @param field
	 * @param annotationClass
	 */
	protected void addAnnotationToField(CtClass clazz, CtField field, Class<?> annotationClass) {
		ConstPool constPool = clazz.getClassFile().getConstPool();
		AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		annotationsAttribute.addAnnotation(new Annotation(annotationClass.getName(), constPool));
		field.getFieldInfo().addAttribute(annotationsAttribute);
	}

	/**
	 * adds a field to class
	 * 
	 * @param clazz
	 * @param parameterType
	 * @param paramName
	 * @throws CannotCompileException
	 */
	protected void addField(CtClass clazz, Class<?> parameterType, String paramName) throws CannotCompileException {
		CtField serviceField = CtField.make(parameterType.getName() + " " + paramName + ";", clazz);
		clazz.addField(serviceField);
	}

	/**
	 * adds a field in the class and adds a setter method for that field.
	 * 
	 * @param proxy
	 * @param fieldName
	 * @param type
	 * @throws CannotCompileException
	 */
	protected void addFieldWithSetter(CtClass proxy, final String fieldName, Class<?> type) throws CannotCompileException {
		CtField serviceField = CtField.make("private " + type.getName() + " " + fieldName + ";", proxy);
		proxy.addField(serviceField);
		CtMethod setter = CtMethod.make("public void set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "(" + type.getName() + " param){this." + fieldName + "=param;}", proxy);
		proxy.addMethod(setter);
	}

	/**
	 * adds the {@link WebMethod}(exclude=true) annotation to methods which
	 * should not be exposed as a SOAP web-service
	 * 
	 * @param clazz
	 * @param ctMethod
	 * @throws NotFoundException
	 */
	protected void addHiddenWebMethodAnnotation(CtClass clazz, CtMethod ctMethod) throws NotFoundException {
		ConstPool constPool = clazz.getClassFile().getConstPool();
		AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		final Annotation annotation = new Annotation(WebMethod.class.getName(), constPool);
		annotation.addMemberValue("exclude", new BooleanMemberValue(true, constPool));
		annotationsAttribute.addAnnotation(annotation);
		ctMethod.getMethodInfo().addAttribute(annotationsAttribute);
	}

	/**
	 * adds a method to a javassist class
	 * 
	 * @param clazz
	 * @param code
	 * @throws CannotCompileException
	 */
	protected void addMethod(CtClass clazz, String code) throws CannotCompileException {
		CtMethod getMethod = CtNewMethod.make(code, clazz);
		clazz.addMethod(getMethod);
	}

	/**
	 * adds {@link RequestMapping} annotation to a method with specified
	 * attributes
	 * 
	 * @param clazz
	 * @param ctMethod
	 * @param pattern
	 * @param httpMethod
	 * @throws NotFoundException
	 */
	protected void addRequestMappingAnnotation(CtClass clazz, CtMethod ctMethod, String pattern, String httpMethod) throws NotFoundException {
		ConstPool constPool = clazz.getClassFile().getConstPool();
		AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		final Annotation annotation = new Annotation(RequestMapping.class.getName(), constPool);
		addArrayMemberParamToAnnotation(constPool, annotation, "value", new StringMemberValue(pattern, constPool));
		if (httpMethod != null) {
			final EnumMemberValue memberValue = new EnumMemberValue(constPool);
			memberValue.setType(RequestMethod.class.getName());
			memberValue.setValue(httpMethod);
			addArrayMemberParamToAnnotation(constPool, annotation, "method", memberValue);
		}
		annotationsAttribute.addAnnotation(annotation);
		ctMethod.getMethodInfo().addAttribute(annotationsAttribute);
	}

	/**
	 * finds the name of parameter of a method through the name attribute of
	 * {@link WebParam} annotation
	 * 
	 * @param parameterAnnotations
	 * @return parameter name
	 */
	protected String getParamName(java.lang.annotation.Annotation[] parameterAnnotations) {
		String paramName = null;
		for (java.lang.annotation.Annotation annotation : parameterAnnotations) {
			if (annotation instanceof WebParam) {
				paramName = ((WebParam) annotation).name();
				break;
			}
		}
		return paramName;
	}

	/**
	 * adds an attribute of type Array to an annotation
	 * 
	 * @param constPool
	 * @param annotation
	 * @param name
	 * @param memberValue
	 */
	private void addArrayMemberParamToAnnotation(ConstPool constPool, final Annotation annotation, final String name, final MemberValue memberValue) {
		final ArrayMemberValue value = new ArrayMemberValue(constPool);
		value.setValue(new MemberValue[] {
			memberValue
		});
		annotation.addMemberValue(name, value);
	}
}
