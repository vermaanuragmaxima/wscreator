package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Double Datatype
 * 
 * @author anurag.verma
 */
public class DoubleConverter implements Converter<Double> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Double convert(Object object) {
		return Double.valueOf(object.toString());
	}
}
