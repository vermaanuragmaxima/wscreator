package com.maximaconsulting.webservices.rest.typeconversion;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import com.maximaconsulting.webservices.rest.javassist.TypeConverterCreator;

/**
 * A central place to find all the type converters
 * 
 * @author anurag.verma
 */
public class Converters {
	private static final Map<String, Converter<?>> converters;
	static {
		converters = new HashMap<String, Converter<?>>();
		addConverter(Boolean.class.getName(), new BooleanConverter());
		addConverter(Character.class.getName(), new CharacterConverter());
		addConverter(Double.class.getName(), new DoubleConverter());
		addConverter(Float.class.getName(), new FloatConverter());
		addConverter(Integer.class.getName(), new IntegerConverter());
		addConverter(Long.class.getName(), new LongConverter());
		addConverter(Short.class.getName(), new ShortConverter());
		addConverter(String.class.getName(), new StringConverter());
		addConverter(Date.class.getName(), new DateConverter());
		addConverter("boolean", new BooleanConverter());
		addConverter("char", new CharacterConverter());
		addConverter("double", new DoubleConverter());
		addConverter("float", new FloatConverter());
		addConverter("int", new IntegerConverter());
		addConverter("long", new LongConverter());
		addConverter("short", new ShortConverter());
	}

	/**
	 * include a converter
	 * 
	 * @param typeName
	 * @param converter
	 */
	public static void addConverter(final String typeName, final Converter<?> converter) {
		converters.put(typeName, converter);
	}

	/**
	 * makes sure that a converter for desired type exists, creates a new one if
	 * unavailable
	 * 
	 * @param clazz
	 * @throws CannotCompileException
	 * @throws RuntimeException
	 * @throws NotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void ensureConverterPresence(Class<?> clazz) throws CannotCompileException, RuntimeException, NotFoundException, InstantiationException, IllegalAccessException {
		if (!converters.containsKey(clazz.getName())) {
			converters.put(clazz.getName(), new TypeConverterCreator().create(clazz));
		}
	}

	/**
	 * get a converter for specified datatype
	 * 
	 * @param targetTypeName
	 * @return the converter
	 */
	public static Converter<?> getConverter(String targetTypeName) {
		return converters.get(targetTypeName);
	}
}
