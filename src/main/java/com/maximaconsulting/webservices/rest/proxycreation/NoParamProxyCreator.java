package com.maximaconsulting.webservices.rest.proxycreation;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import com.maximaconsulting.webservices.rest.Response;

/**
 * Proxy method creator for methods which have no parameters. GET or POST is not
 * specified.
 * 
 * @author anurag.verma
 */
public class NoParamProxyCreator extends ByteCodeWriter {
	/**
	 * adds Spring MVC method for service methods with no parameters.
	 * 
	 * @param clazz
	 * @param method
	 * @param variableName
	 * @param pattern
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	void create(CtClass clazz, Method method, String variableName, String pattern) throws CannotCompileException, NotFoundException {
		final String envelopedMethodCode = getCode(method, method.getName() + System.currentTimeMillis(), variableName, pattern);
		CtMethod ctMethod = CtNewMethod.make(envelopedMethodCode, clazz);
		addRequestMappingAnnotation(clazz, ctMethod, pattern, null);
		clazz.addMethod(ctMethod);
	}

	/**
	 * prepares the implementation code of method.
	 * 
	 * @param method
	 * @param methodName
	 * @param variableName
	 * @param pattern
	 * @return the code
	 */
	private String getCode(Method method, String methodName, String variableName, String pattern) {
		final StringBuilder code = new StringBuilder("public " + Response.class.getName());
		code.append(" " + methodName + "() { " + Response.class.getName() + " response = new " + Response.class.getName() + "(\"" + pattern + "\");");
		code.append("try {");
		if (!method.getReturnType().toString().equals("void")) {
			code.append("response.setResult(");
		}
		code.append(variableName + ".");
		code.append(method.getName());
		code.append("(");
		if (!method.getReturnType().toString().equals("void")) {
			code.append(")");
		}
		code.append(");}catch(" + Exception.class.getName() + " e){response.setError(e.getMessage());} return response;}");
		return code.toString();
	}
}