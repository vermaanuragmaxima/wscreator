package com.maximaconsulting.webservices;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.web.servlet.DispatcherServlet;

import com.maximaconsulting.webservices.rest.RestServiceExporter;
import com.maximaconsulting.webservices.rest.endpoint.RequestWrapper;
import com.maximaconsulting.webservices.rest.endpoint.RestConfig;

/**
 * Extending the Dispatcher Servlet just to inject the contextConfigLocation
 * property & other initializations.
 * 
 * @author anurag.verma
 */
@SuppressWarnings("serial")
public class WSServlet extends DispatcherServlet {
	/**
	 * Wrapped ServletConfig
	 */
	private RestConfig restConfig;
	private CXFServlet soapService = new CXFServlet();

	@Override
	public ServletConfig getServletConfig() {
		return restConfig;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 * Initializing ServiceExporters & WebServiceScanner.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		final File folder = new File(config.getServletContext().getRealPath("/WEB-INF/classes/wscreator"));
		folder.mkdirs();
		final List<String> packagesToScan = Arrays.asList(config.getInitParameter("scanPackages").split(","));
		WebServicesScanner.init(packagesToScan);
		new RestServiceExporter().exportRestServices();
		restConfig = new RestConfig(config);
		super.init(restConfig);
		soapService.init(config);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getHeader("SOAPAction") != null || isWsdlRequest(request) || request.getHeader("soapaction") != null) {
			soapService.service(request, response);
		} else {
			super.service(new RequestWrapper(request), response);
		}
	}

	/**
	 * figures out whether the request is for WSDL of a Service
	 * 
	 * @param request
	 * @return
	 */
	private boolean isWsdlRequest(HttpServletRequest request) {
		Map<?, ?> parameterMap = request.getParameterMap();
		return parameterMap.size() == 1 && parameterMap.containsKey("wsdl");
	}
}
