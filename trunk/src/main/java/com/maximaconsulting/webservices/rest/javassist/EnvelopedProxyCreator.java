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

import org.springframework.web.bind.annotation.RequestBody;

import com.maximaconsulting.webservices.rest.Response;

/**
 * Proxy method creator for POST calls. request body will have XML or JSON
 * content.
 * 
 * @author anurag.verma
 */
public class EnvelopedProxyCreator extends ByteCodeWriter {
	private static final RequestEnvelopeClassCreator requestEnvelopeClassCreator = new RequestEnvelopeClassCreator();

	/**
	 * creates REST variant of service method with all its parameters wrapped
	 * into a new class
	 * 
	 * @param clazz
	 * @param method
	 * @param variableName
	 * @param pattern
	 * @param serviceName
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	void create(CtClass clazz, Method method, String variableName, String pattern, String serviceName) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException {
		final Class<?> requestEnvelopeClass = requestEnvelopeClassCreator.create(method, serviceName);
		final String envelopedMethodCode = getCode(method, method.getName() + "Post" + System.currentTimeMillis(), requestEnvelopeClass, variableName, pattern);
		CtMethod ctMethod = CtNewMethod.make(envelopedMethodCode, clazz);
		addRequestMappingAnnotation(clazz, ctMethod, pattern, "POST");
		addRequestBodyAnnotation(clazz, ctMethod);
		clazz.addMethod(ctMethod);
	}

	/**
	 * adds the {@link RequestBody} annotation to the parameter
	 * 
	 * @param clazz
	 * @param ctMethod
	 */
	private void addRequestBodyAnnotation(CtClass clazz, CtMethod ctMethod) {
		ConstPool constPool = clazz.getClassFile().getConstPool();
		ParameterAnnotationsAttribute annotationsAttribute = new ParameterAnnotationsAttribute(constPool, ParameterAnnotationsAttribute.visibleTag);
		final Annotation[][] annotations = new Annotation[1][1];
		final Annotation annotation = new Annotation(RequestBody.class.getName(), constPool);
		annotations[0] = new Annotation[] {
			annotation
		};
		annotationsAttribute.setAnnotations(annotations);
		ctMethod.getMethodInfo().addAttribute(annotationsAttribute);
	}

	/**
	 * writes implementation code of service method.
	 * 
	 * @param method
	 * @param methodName
	 * @param requestEnvelopeClass
	 * @param variableName
	 * @param pattern
	 * @return the code
	 */
	private String getCode(Method method, String methodName, Class<?> requestEnvelopeClass, String variableName, String pattern) {
		final StringBuilder code = new StringBuilder("public " + Response.class.getName());
		code.append(" " + methodName + "(");
		code.append(requestEnvelopeClass.getName() + " request");
		code.append(") { " + Response.class.getName() + " response = new " + Response.class.getName() + "(\"" + pattern + "\");");
		code.append("try {");
		if (!method.getReturnType().toString().equals("void")) {
			code.append("response.setResult(");
		}
		code.append(variableName + ".");
		code.append(method.getName());
		code.append("(");
		java.lang.annotation.Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		for (int i = 0; i < parameterAnnotations.length; i++) {
			if (i > 0) {
				code.append(", ");
			}
			final String paramName = getParamName(parameterAnnotations[i]);
			String capsCaseName = Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
			code.append("request.get" + capsCaseName + "()");
		}
		if (!method.getReturnType().toString().equals("void")) {
			code.append(")");
		}
		code.append(");}catch(" + Exception.class.getName() + " e){response.setError(e.getMessage());} return response;}");
		return code.toString();
	}
}
