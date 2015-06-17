package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Integer Datatype
 * 
 * @author anurag.verma
 */
public class IntegerConverter implements Converter<Integer> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Integer convert(Object object) {
		return Integer.valueOf(object.toString());
	}
}
