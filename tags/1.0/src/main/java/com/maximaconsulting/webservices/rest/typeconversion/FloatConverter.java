package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Float Datatype
 * 
 * @author anurag.verma
 */
public class FloatConverter implements Converter<Float> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Float convert(Object object) {
		return Float.valueOf(object.toString());
	}
}
