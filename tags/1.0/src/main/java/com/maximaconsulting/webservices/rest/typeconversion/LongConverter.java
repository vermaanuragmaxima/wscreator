package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Long datatype
 * 
 * @author anurag.verma
 */
public class LongConverter implements Converter<Long> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Long convert(Object object) {
		return Long.valueOf(object.toString());
	}
}
