package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Short Datatype
 * 
 * @author anurag.verma
 */
public class ShortConverter implements Converter<Short> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Short convert(Object object) {
		return Short.valueOf(object.toString());
	}
}
