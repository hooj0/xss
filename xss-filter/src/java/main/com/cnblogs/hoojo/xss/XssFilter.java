package com.cnblogs.hoojo.xss;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.util.AntPathMatcher;

import com.cnblogs.hoojo.xss.configuration.FilterConfiguration;
import com.cnblogs.hoojo.xss.log.ApplicationLogging;

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
	
	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;

		String url = request.getRequestURI();
		debug("当前请求URL：{}", url);
		
		// 白名单路径直接放行
		if (FilterConfiguration.getInstance().isExcludeProp(url)) {
			debug("“白名单”直接放行URL：{}", url);
			chain.doFilter(request, response);
		} else {
			
			// 如果是上传文件
			if (isMultipartRequest(request)) {
				debug("“文件”过滤请求URL：{}", url);
				try {
					chain.doFilter(new FileHttpRequestWrapper(request), response);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new ServletException(e.getMessage());
				}
			} else {
				debug("“文本”过滤请求URL：{}", url);
				chain.doFilter(new TextHttpRequestWrapper(request), response);
			}
		}
	}
	
	// 是否是上传文件
	private boolean isMultipartRequest(HttpServletRequest request) {
		
		boolean multipart = ServletFileUpload.isMultipartContent(request);
		if (multipart) {
			return multipart;
		}
		
		multipart = request.getHeader(MULTIPART_HEADER) != null && request.getHeader(MULTIPART_HEADER).startsWith("multipart/form-data");
		if (multipart) {
			return multipart;
		}
		
		String contentType = request.getContentType();
		if ((contentType != null) && (contentType.toLowerCase().startsWith("multipart/"))) {
			multipart = true;
		}

		return multipart;
	}

	@Override
	public void destroy() {
	}
	
	public static void main(String[] args) {
		/*System.out.println(FilterConfiguration.getInstance().isExcludeProp("cnblogs.hoojo/pay/weixin.do"));
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("cnblogs.hoojo/pay/alipay.do"));
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("cnblogs.hoojo/pay/unionpay.do"));
		System.out.println(FilterConfiguration.getInstance().isExcludeProp("applepay.do"));

		//System.out.println(FilterConfiguration.getInstance().getCheckProp("cnblogs.hoojo/pay/alipay_upload.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileSize("cnblogs.hoojo/pay/applepay.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileSize("cnblogs.hoojo/pay/alipay_upload.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileType("cnblogs.hoojo/pay/alipay_upload.do"));
		 **/

		System.out.println(new AntPathMatcher().match("*/cnblogs.hoojo/*", "a/cnblogs.hoojo/pay"));
		System.out.println(new AntPathMatcher().match("a/cnblogs.hoojo/pay", "a/cnblogs.hoojo/pay"));
		System.out.println(new AntPathMatcher().isPattern("a/*"));
		
		System.out.println(FilterConfiguration.getInstance().getMapProp("/chehuotongweb/xss/example/alipay_upload.do"));
		System.out.println(FilterConfiguration.getInstance().isCheckFileType("/chehuotongweb/xss/example/alipay_upload.do"));
		
	}
}
