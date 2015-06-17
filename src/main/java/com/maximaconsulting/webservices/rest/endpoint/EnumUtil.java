package com.maximaconsulting.webservices.rest.endpoint;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Util class for enum operations while reading/writing rest request/response
 * 
 * @author anurag.verma
 */
public class EnumUtil {
	/**
	 * creates an enumeration for the list of String passed
	 * 
	 * @param list
	 * @return enumeration
	 */
	public static Enumeration<String> getEnumFor(final Collection<String> list) {
		return new Enumeration<String>() {
			Iterator<String> itr = list.iterator();

			/*
			 * (non-Javadoc)
			 * @see java.util.Enumeration#hasMoreElements()
			 */
			@Override
			public boolean hasMoreElements() {
				return itr.hasNext();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Enumeration#nextElement()
			 */
			@Override
			public String nextElement() {
				return itr.next();
			}
		};
	}

	/**
	 * gives the Values of an Enumeration in a Set of String
	 * 
	 * @param enumeration
	 * @return Set of String with all values
	 */
	@SuppressWarnings("rawtypes")
	public static Set<String> getValues(Enumeration enumeration) {
		final Set<String> values = new HashSet<String>();
		while (enumeration.hasMoreElements()) {
			values.add((String) enumeration.nextElement());
		}
		return values;
	}
}
