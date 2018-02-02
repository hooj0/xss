package com.cnblogs.hoojo.xss;

import com.cnblogs.hoojo.xss.configuration.PropertyConfiguration;

public class Test {

	@org.junit.Test
	public void test1() {
		
		PropertyConfiguration.getInstance().getStringProp("test");
	}
}
