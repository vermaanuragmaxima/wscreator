package com.maximaconsulting.webservices.rest.endpoint;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Wrapper of Servlet Config to hold Rest specific configurations
 * 
 * @author anurag.verma
 */
public class RestConfig implements ServletConfig {
	private ServletConfig config;

	/**
	 * @param config
	 */
	public RestConfig(ServletConfig config) {
		this.config = config;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String name) {
		if (name.equals("contextConfigLocation")) {
			return "classpath:ws-servlet.xml";
		}
		return config.getInitParameter(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		final Set<String> names = new HashSet<String>();
		names.add("contextConfigLocation");
		names.addAll(EnumUtil.getValues(config.getInitParameterNames()));
		return EnumUtil.getEnumFor(names);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		return config.getServletContext();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletName()
	 */
	@Override
	public String getServletName() {
		return config.getServletName();
	}
}
