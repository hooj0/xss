package com.masget.xss;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.masget.xss.configuration.FilterConfiguration;
import com.masget.xss.log.ApplicationLogging;

/**
 * <b>function:</b> xss 过滤器，防止HTML、JavaScript、SQL、CSS 注入
 * 
 * @author hoojo
 * @createDate 2018年1月10日 下午4:52:24
 * @file XssFilter.java
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class XssFilter extends ApplicationLogging implements Filter {

	// 文件头类型
	private static final String MULTIPART_HEADER = "Content-type";
	// 是否是上传文件
	private boolean multipart;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;

		String url = request.getRequestURI();
		debug("当前请求URL：{}", url);
		
		// 白名单路径直接放行
		if (FilterConfiguration.getInstance().isExcludeProp(url)) {
			debug("直接放行URL：{}", url);
			chain.doFilter(request, response);
		} else {
			
			// 判断是否是上传文件
			multipart = request.getHeader(MULTIPART_HEADER) != null && request.getHeader(MULTIPART_HEADER).startsWith("multipart/form-data");
			// 如果是上传文件
			if (multipart) {
				debug("“文件”过滤放行URL：{}", url);
				chain.doFilter(new FileHttpRequestWrapper(request), response);
			} else {
				debug("“文本”过滤放行URL：{}", url);
				chain.doFilter(new TextHttpRequestWrapper(request), response);
			}
		}
	}

	@Override
	public void destroy() {
	}
	
	public static void main(String[] args) {
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("masget/pay/weixin.do"));
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("masget/pay/alipay.do"));
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("masget/pay/unionpay.do"));
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("applepay.do"));

		//System.out.println(FilterConfiguration.getInstance().getCheckProp("masget/pay/alipay_upload.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileSize("masget/pay/applepay.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileSize("masget/pay/alipay_upload.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileType("masget/pay/alipay_upload.do"));
	}
}
