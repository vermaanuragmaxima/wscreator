package com.maximaconsulting.webservices.rest.javassist;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import org.springframework.web.bind.annotation.RequestParam;

import com.maximaconsulting.webservices.rest.Response;

/**
 * Get proxy creator, method signature is similar to the actual method.
 * 
 * @author anurag.verma
 */
public class SimilarSignatureProxyCreator extends ByteCodeWriter {
	/**
	 * creates a Spring MVC method with same signature as the actual method.
	 * 
	 * @param clazz
	 * @param method
	 * @param variableName
	 * @param pattern
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	void create(CtClass clazz, Method method, String variableName, String pattern) throws CannotCompileException, NotFoundException {
		final String sameSignatureMethodCode = getCode(method, method.getName() + "Get" + System.currentTimeMillis(), variableName, pattern);
		CtMethod ctMethod = CtNewMethod.make(sameSignatureMethodCode, clazz);
		addRequestMappingAnnotation(clazz, ctMethod, pattern, "GET");
		if (method.getParameterTypes().length > 0) {
			addParameterAnnotations(clazz, method, ctMethod);
		}
		clazz.addMethod(ctMethod);
	}

	/**
	 * adds {@link RequestParam} annotation to the new method being created.
	 * 
	 * @param clazz
	 * @param method
	 * @param ctMethod
	 */
	private void addParameterAnnotations(CtClass clazz, Method method, CtMethod ctMethod) {
		final java.lang.annotation.Annotation[][] actualParameterAnnotations = method.getParameterAnnotations();
		ConstPool constPool = clazz.getClassFile().getConstPool();
		ParameterAnnotationsAttribute annotationsAttribute = new ParameterAnnotationsAttribute(constPool, ParameterAnnotationsAttribute.visibleTag);
		final Annotation[][] annotations = new Annotation[actualParameterAnnotations.length][1];
		for (int i = 0; i < actualParameterAnnotations.length; i++) {
			java.lang.annotation.Annotation[] paramAnnotations = actualParameterAnnotations[i];
			final Annotation annotation = new Annotation(RequestParam.class.getName(), constPool);
			annotations[i] = new Annotation[] {
				annotation
			};
			annotation.addMemberValue("value", new StringMemberValue(getParamName(paramAnnotations), constPool));
		}
		annotationsAttribute.setAnnotations(annotations);
		ctMethod.getMethodInfo().addAttribute(annotationsAttribute);
	}

	/**
	 * adds method parameter code to the method code
	 * 
	 * @param code
	 * @param variableNames
	 * @param type
	 * @param annotations
	 */
	private void addParameterCode(StringBuilder code, StringBuilder variableNames, Class<?> type, java.lang.annotation.Annotation[] annotations) {
		final String paramName = getParamName(annotations);
		if (variableNames.length() > 0) {
			code.append(", ");
			variableNames.append(", ");
		}
		code.append(type.getName() + " " + paramName);
		variableNames.append(paramName);
	}

	/**
	 * writes code implementation for a method.
	 * 
	 * @param method
	 * @param methodName
	 * @param variableName
	 * @param pattern
	 * @return code
	 */
	private String getCode(Method method, String methodName, String variableName, String pattern) {
		final StringBuilder code = new StringBuilder("public " + Response.class.getName());
		code.append(" " + methodName + "(");
		StringBuilder variableNames = new StringBuilder();
		Class<?>[] parameterTypes = method.getParameterTypes();
		java.lang.annotation.Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (int i = 0; i < parameterTypes.length; i++) {
			addParameterCode(code, variableNames, parameterTypes[i], parameterAnnotations[i]);
		}
		code.append(") { " + Response.class.getName() + " response = new " + Response.class.getName() + "(\"" + pattern + "\");");
		code.append("try {");
		if (!method.getReturnType().toString().equals("void")) {
			code.append("response.setResult(");
		}
		code.append(variableName + ".");
		code.append(method.getName());
		code.append("(");
		code.append(variableNames.toString());
		if (!method.getReturnType().toString().equals("void")) {
			code.append(")");
		}
		code.append(");}catch(" + Exception.class.getName() + " e){response.setError(e.getMessage());} return response;}");
		return code.toString();
	}
}
