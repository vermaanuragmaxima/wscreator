package com.maximaconsulting.webservices.soap;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import com.maximaconsulting.webservices.annotations.HiddenMethod;
import com.maximaconsulting.webservices.rest.proxycreation.ByteCodeWriter;

/**
 * SOAP proxy creator, method signature is similar to the actual method.
 * 
 * @author anurag.verma
 */
public class SOAPProxyMethodCreator extends ByteCodeWriter {
	/**
	 * adds a method to class
	 * 
	 * @param clazz
	 * @param method
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	void create(CtClass clazz, Method method) throws CannotCompileException, NotFoundException {
		final String code = getCode(method);
		CtMethod ctMethod = CtNewMethod.make(code, clazz);
		if (method.getAnnotation(HiddenMethod.class) != null) {
			addHiddenWebMethodAnnotation(clazz, ctMethod);
		}
		clazz.addMethod(ctMethod);
	}

	/**
	 * adds up the code for method parameters.
	 * 
	 * @param code
	 * @param variableNames
	 * @param type
	 * @param paramCount
	 */
	private void addParameterCode(StringBuilder code, StringBuilder variableNames, Class<?> type, int paramCount) {
		final String paramName = "arg" + paramCount;
		if (variableNames.length() > 0) {
			code.append(", ");
			variableNames.append(", ");
		}
		code.append(type.getName() + " " + paramName);
		variableNames.append(paramName);
	}

	/**
	 * writes the code for a method.
	 * 
	 * @param method
	 * @return code for a method.
	 */
	private String getCode(Method method) {
		final StringBuilder code = new StringBuilder("public " + method.getReturnType().getName());
		code.append(" " + method.getName() + "(");
		StringBuilder variableNames = new StringBuilder();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			addParameterCode(code, variableNames, parameterTypes[i], i);
		}
		code.append(") ");
		if (method.getExceptionTypes() != null && method.getExceptionTypes().length > 0) {
			code.append(" throws Exception ");
		}
		code.append("{ ");
		if (!method.getReturnType().toString().equals("void")) {
			code.append("return ");
		}
		code.append("service.");
		code.append(method.getName());
		code.append("(");
		code.append(variableNames.toString());
		code.append(");}");
		return code.toString();
	}
}
