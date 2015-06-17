package com.maximaconsulting.webservices.rest.typeconversion;

/**
 * converter for Character datatype
 * 
 * @author anurag.verma
 */
public class CharacterConverter implements Converter<Character> {
	/*
	 * (non-Javadoc)
	 * @see
	 * com.maximaconsulting.webservices.rest.typeconversion.Converter#convert
	 * (java.lang.Object)
	 */
	@Override
	public Character convert(Object object) {
		return object.toString().charAt(0);
	}
}
