package com.maximaconsulting.webservices.rest.init;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import org.springframework.web.servlet.view.xml.MarshallingView;

/**
 * Initializer code for Rest Views & Object Mapper properties.
 * 
 * @author anurag.verma
 */
public class RestInitializer {
	private MappingJacksonJsonView jsonView;
	private MarshallingView xmlView;
	private List<View> viewList;

	/**
	 * Initialization of elements on which REST web-services of Spring MVC are
	 * dependent.
	 */
	public void init() {
		final ObjectMapper objectMapper = new ObjectMapper();
		final SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		serializationConfig.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		@SuppressWarnings("serial")
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
			@Override
			public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
				StringBuffer toFix = super.format(date, toAppendTo, pos);
				return toFix.insert(toFix.length() - 2, ':');
			};
		};
		serializationConfig.setDateFormat(dateFormat);
		jsonView.setObjectMapper(objectMapper);
		viewList.clear();
		viewList.add(jsonView);
		viewList.add(xmlView);
	}

	/**
	 * @param jsonView
	 */
	public void setJsonView(MappingJacksonJsonView jsonView) {
		this.jsonView = jsonView;
	}

	/**
	 * @param viewList
	 */
	public void setViewList(List<View> viewList) {
		this.viewList = viewList;
	}

	/**
	 * @param xmlView
	 */
	public void setXmlView(MarshallingView xmlView) {
		this.xmlView = xmlView;
	}
}
