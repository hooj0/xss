package com.cnblogs.hoojo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.masget.xss.log.ApplicationLogging;

/**
 * examples xss
 * @author hoojo
 * @createDate 2018年1月24日 下午5:24:33
 * @file XssExampleController.java
 * @package com.cnblogs.hoojo.controller
 * @project xss-filter-web
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
@Controller
@RequestMapping("/xss/example")
public class XssExampleController extends ApplicationLogging {

	public String index() {
		
		return "redirect:index";
	}
	
	public String form(String name, String remark) {
		
		logger.info("name: {}, remark: {}", name, remark);
		
		return "redirect:index";
	}
}
