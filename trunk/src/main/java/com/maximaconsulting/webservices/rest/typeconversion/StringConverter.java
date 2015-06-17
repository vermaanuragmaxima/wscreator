package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for String datatype
 * 
 * @author anurag.verma
 */
public class StringConverter implements Converter<String> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public String convert(Object object) {
		return object.toString();
	}
}
