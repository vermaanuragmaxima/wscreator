package com.maximaconsulting.webservices.rest.typeconversion;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * converter for Array datatype
 * 
 * @author anurag.verma
 * @param <T>
 */
public class ArrayConverter<T> implements Converter<T[]> {
	private Class<T> targetType;
	private T[] targetArray;

	public ArrayConverter(Class<T> targetType, T[] targetArray) {
		this.targetType = targetType;
		this.targetArray = targetArray;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] convert(Object object) {
		try {
			final Converter<?> converter = Converters.getConverter(targetType.getName());
			final Method convertMethod = converter.getClass().getDeclaredMethod("convert", Object.class);
			for (int i = 0; i < targetArray.length; i++) {
				targetArray[i] = (T) convertMethod.invoke(converter, Array.get(object, i));
			}
			return targetArray;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
