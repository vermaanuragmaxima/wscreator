package com.maximaconsulting.webservices;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.maximaconsulting.dummy.DummyWebService;

/**
 * @author anurag.verma
 */
public class WebServicesScannerTest {
	@Before
	public void setup() {
		WebServicesScanner.init(Arrays.asList(new String[] {
			"com.maximaconsulting.dummy"
		}));
	}

	@Test
	public void scanTest() {
		Assert.assertEquals(new HashSet<Class<?>>(), WebServicesScanner.getWebServices());
	}

	@Test
	public void getServiceNameTest() {
		Assert.assertEquals("DummyWebService", WebServicesScanner.getServiceName(DummyWebService.class));
	}
}
