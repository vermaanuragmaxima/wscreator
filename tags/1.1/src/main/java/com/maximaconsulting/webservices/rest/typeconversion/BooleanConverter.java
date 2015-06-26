package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Boolean datatype
 * 
 * @author anurag.verma
 */
public class BooleanConverter implements Converter<Boolean> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Boolean convert(Object object) {
		return Boolean.valueOf(object.toString());
	}
}
