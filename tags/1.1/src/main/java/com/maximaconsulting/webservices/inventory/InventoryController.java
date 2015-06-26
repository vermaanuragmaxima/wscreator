package com.maximaconsulting.webservices.inventory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.maximaconsulting.webservices.WebServicesScanner;
import com.maximaconsulting.webservices.annotations.HiddenMethod;

/**
 * Controller for Inventory & Descriptor pages.
 * 
 * @author anurag.verma
 */
@Controller
public class InventoryController {
	/**
	 * handles the requests for the Service Inventory Page
	 * 
	 * @param className
	 * @param methodName
	 * @param request
	 * @return name of jsp that needs to be used as view
	 */
	@RequestMapping(value = "/")
	@ResponseBody
	public String serviceInventory(@RequestParam(value = "c", required = false) String className, @RequestParam(value = "m", required = false) String methodName, HttpServletRequest request, HttpServletResponse response) {
		String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
		response.setContentType("text/html");
		if (className != null && methodName != null) {
			try {
				final Class<?> serviceInterface = Class.forName(className);
				Method method = null;
				for (Method m : serviceInterface.getMethods()) {
					if (m.getName().equals(methodName)) {
						method = m;
					}
				}
				if (method == null) {
					throw new Exception();
				}
				return getDescriptorPageCode(baseUrl, serviceInterface, method);
			}
			catch (Exception e) {}
		}
		return getInventoryPage(baseUrl);
	}

	/**
	 * prepares html for the descriptor page
	 * 
	 * @param baseUrl
	 * @param serviceInterface
	 * @param method
	 * @return html for descriptor
	 */
	private String getDescriptorPageCode(String baseUrl, Class<?> serviceInterface, Method method) {
		StringBuilder page = new StringBuilder("<html><head><title>");
		DescriptorBean descriptor = new DescriptorBean(baseUrl, serviceInterface, method, page);
		page.append("");
		page.append(descriptor.getPageTitle());
		page.append("</title>");
		page.append("</head><body><br />");
		page.append("<table border=\"0px\" cellpadding=\"10\" cellspacing=\"10\" width=\"100%\">");
		page.append("<tr><td><h3>Service :</h3>");
		page.append(descriptor.getServiceName());
		page.append("</td></tr><tr>");
		page.append("<td><b>URL : </b> &nbsp; &nbsp; ");
		page.append(descriptor.getServiceUrl());
		page.append("</td></tr><tr>");
		page.append("<td><b>Methods Allowed : </b> ");
		page.append(descriptor.getAllowedMethods());
		page.append("</td></tr><tr><td><b>XML</b></td></tr><tr><td>");
		descriptor.writeParamXml();
		page.append("</td></tr><tr><td><b>JSON</b></td></tr><tr><td>");
		descriptor.writeParamJson();
		page.append("</td></tr></table></body></html>");
		return page.toString();
	}

	/**
	 * prepares the inventory for Inventory page
	 * 
	 * @param baseUrl
	 * @return html for Inventory
	 */
	private String getInventoryPage(String baseUrl) {
		StringBuilder page = new StringBuilder("<html>");
		page.append("<head><title>WSCreator Web Services.</title></head>");
		page.append("<body>");
		page.append("<h3>Available SOAP and REST services:</h3>");
		page.append("<table cellpadding=\"1\" cellspacing=\"1\" border=\"0\" width=\"100%\">");
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.addAll(WebServicesScanner.getWebServices());
		Collections.sort(list, new Comparator<Class<?>>() {
			@Override
			public int compare(java.lang.Class<?> c1, java.lang.Class<?> c2) {
				return WebServicesScanner.getServiceName(c1).compareTo(WebServicesScanner.getServiceName(c2));
			}
		});
		for (Class<?> serviceInterface : list) {
			String serviceName = WebServicesScanner.getServiceName(serviceInterface);
			page.append("<tr>");
			page.append("<td><b>");
			page.append(serviceName);
			page.append(" &nbsp; (<a href=\"");
			page.append(baseUrl + serviceName);
			page.append("?wsdl\">wsdl</a>)</b>");
			page.append("<table border=\"0\" cellpadding=\"1\" cellspacing=\"1\">");
			for (Method method : serviceInterface.getDeclaredMethods()) {
				if (method.getAnnotation(HiddenMethod.class) == null) {
					page.append("<tr><td>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; #&nbsp; &nbsp;</td>");
					page.append("<td><a href=\"");
					page.append(baseUrl + "?c=" + serviceInterface.getName() + "&m=" + method.getName());
					page.append("\">" + method.getName() + "</a>");
					page.append(" &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</td>");
					page.append("</tr>");
				}
			}
			page.append("</table><br /></td></tr>");
		}
		page.append("</table></body></html>");
		return page.toString();
	}
}
