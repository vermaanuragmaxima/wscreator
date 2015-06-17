package com.maximaconsulting.webservices.rest.typeconversion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * converter for collections
 * 
 * @author anurag.verma
 * @param <T>
 */
public class CollectionConverter<T> implements Converter<Collection<T>> {
	private Class<T> targetType;

	public CollectionConverter(Class<T> targetType) {
		this.targetType = targetType;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<T> convert(Object object) {
		Collection<T> collection = (Collection<T>) object;
		try {
			Collection<T> corrected = collection.getClass().newInstance();
			final Converter<?> converter = Converters.getConverter(targetType.getName());
			final Method convertMethod = converter.getClass().getDeclaredMethod("convert", Object.class);
			for (Object source : collection) {
				corrected.add((T) convertMethod.invoke(converter, source));
			}
			return corrected;
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
