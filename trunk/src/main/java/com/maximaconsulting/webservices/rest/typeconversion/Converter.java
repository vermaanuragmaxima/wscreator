package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * Common interface for all datatype converters
 * 
 * @author anurag.verma
 * @param <T>
 */
public interface Converter<T> {
	/**
	 * method for datatype conversion
	 * 
	 * @param object
	 * @return data converted into desired type object
	 */
	T convert(Object object);
}
