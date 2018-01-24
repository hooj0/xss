package com.masget.xss;

import com.masget.xss.configuration.PropertyConfiguration;

public class Test {

	@org.junit.Test
	public void test1() {
		
		PropertyConfiguration.getInstance().getStringProp("test");
	}
}
