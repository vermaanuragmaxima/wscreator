package com.maximaconsulting.webservices.rest.proxycreation;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import com.maximaconsulting.webservices.rest.typeconversion.ArrayConverter;
import com.maximaconsulting.webservices.rest.typeconversion.CollectionConverter;
import com.maximaconsulting.webservices.rest.typeconversion.Converter;
import com.maximaconsulting.webservices.rest.typeconversion.Converters;

/**
 * This class creates Parser which parses the input map to target type object.
 * 
 * @author anurag.verma
 */
public class TypeConverterCreator extends ByteCodeWriter {
	/**
	 * creates a converter/parser for the custom type.
	 * 
	 * @param clazz
	 * @return the converter instance
	 * @throws CannotCompileException
	 * @throws RuntimeException
	 * @throws NotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> Converter<T> create(Class<T> clazz) throws CannotCompileException, RuntimeException, NotFoundException, InstantiationException, IllegalAccessException {
		final String className = clazz.getSimpleName() + "TypeConverter";
		final ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.makeClass(className);
		addMethod(ctClass, getConvertMethodCode(clazz));
		ctClass.setInterfaces(new CtClass[] {
			classPool.getCtClass(Converter.class.getName())
		});
		final Class<Converter<T>> converterClass = ctClass.toClass();
		return converterClass.newInstance();
	}

	/**
	 * prepares code for the actual convert method for the custom type.
	 * 
	 * @param clazz
	 * @return code for the convert method
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> String getConvertMethodCode(Class<T> clazz) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException {
		final StringBuilder code = new StringBuilder("public " + clazz.getName() + " convert(" + Object.class.getName() + " obj) {");
		code.append(clazz.getName() + " target = new " + clazz.getName() + "();");
		code.append(Map.class.getName() + " map = (" + Map.class.getName() + ") obj;");
		Class<?> t = clazz;
		final List<Field> fields = new ArrayList<Field>();
		while (t != null && !t.equals(Object.class)) {
			fields.addAll(Arrays.asList(t.getDeclaredFields()));
			t = t.getSuperclass();
		}
		for (Field field : fields) {
			String fieldName = field.getName();
			String capsCaseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			final Class<?> type = field.getType();
			code.append("if(map.containsKey(\"" + fieldName + "\")) {");
			if (type.isEnum()) {
				code.append("target.set" + capsCaseName + "(" + type.getName() + ".valueOf((" + String.class.getName() + ")map.get(\"" + fieldName + "\")));");
			} else if (Collection.class.isAssignableFrom(type)) {
				code.append(Converter.class.getName() + " converter = new " + CollectionConverter.class.getName() + "(" + type.getName() + ".class);");
				code.append("target.set" + capsCaseName + "((" + type.getName() + ")converter.convert(map.get(\"" + fieldName + "\")));");
			} else if (type.isArray()) {
				final String targetTypeName = type.getComponentType().getName();
				code.append(Converter.class.getName() + " converter = new " + ArrayConverter.class.getName() + "(" + targetTypeName + ".class, new " + targetTypeName + "[" + Array.class.getName() + ".getLength(map.get(\"" + fieldName + "\"))]);");
				code.append("target.set" + capsCaseName + "((" + targetTypeName + "[])converter.convert(map.get(\"" + fieldName + "\")));");
			} else {
				Converters.ensureConverterPresence(type);
				code.append(Converter.class.getName() + " converter = " + Converters.class.getName() + ".getConverter(\"" + type.getName() + "\");");
				code.append("target.set" + capsCaseName + "((" + type.getName() + ")converter.convert(map.get(\"" + fieldName + "\")));");
			}
			code.append("}");
		}
		code.append("return target;");
		code.append("}");
		return code.toString();
	}
}
