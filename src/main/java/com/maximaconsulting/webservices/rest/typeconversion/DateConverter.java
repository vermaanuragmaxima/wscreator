package com.maximaconsulting.webservices.rest.typeconversion;

import java.util.Date;

/**
 * converter for Date datatype
 * 
 * @author anurag.verma
 */
public class DateConverter implements Converter<Date> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Date convert(Object object) {
		return javax.xml.bind.DatatypeConverter.parseDateTime(object.toString()).getTime();
	}
}
