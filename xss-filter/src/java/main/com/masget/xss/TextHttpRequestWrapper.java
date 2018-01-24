package com.masget.xss;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.masget.xss.rejector.InjectedRejectorUtils;

/**
 * <b>function:</b> 文本过滤
 * 
 * @author hoojo
 * @createDate 2018年1月10日 下午4:56:15
 * @file XssHttpRequestWrapper.java
 * @package com.masget.xss
 * @project xss
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class TextHttpRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(TextHttpRequestWrapper.class);
	
	private String url;
	private Map<String, String[]> escapedParametersValuesMap = new HashMap<String, String[]>();

	public TextHttpRequestWrapper(HttpServletRequest req) {
		super(req);
		url = req.getRequestURI();
	}

	@Override
	public String getParameter(String name) {
		String[] escapedParameterValues = escapedParametersValuesMap.get(name);

		String escapedParameterValue = null;
		if (escapedParameterValues != null) {
			escapedParameterValue = escapedParameterValues[0];
		} else {
			String parameterValue = super.getParameter(name);

			try {
				escapedParameterValue = InjectedRejectorUtils.invokeAll(url, name, parameterValue);
			} catch (Exception e) {
				logger.error("过滤特殊字符串异常：", e);
			}

			escapedParametersValuesMap.put(name, new String[] { escapedParameterValue });
		} // end-else

		return escapedParameterValue;
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] escapedParameterValues = escapedParametersValuesMap.get(name);

		if (escapedParameterValues == null) {
			String[] parametersValues = super.getParameterValues(name);
			escapedParameterValues = new String[parametersValues.length];

			for (int i = 0; i < parametersValues.length; i++) {
				String parameterValue = parametersValues[i];
				String escapedParameterValue = parameterValue;

				try {
					escapedParameterValue = InjectedRejectorUtils.invokeAll(url, name, escapedParameterValue);
				} catch (Exception e) {
					logger.error("过滤特殊字符串异常：", e);
				}

				escapedParameterValues[i] = escapedParameterValue;
			} // end-for

			escapedParametersValuesMap.put(name, escapedParameterValues);
		} // end-else

		return escapedParameterValues;
	}

	@Override
	public String getHeader(String name) {
		String value = super.getHeader(name);

		try {
			value = InjectedRejectorUtils.invokeAll(url, name, value);
		} catch (Exception e) {
			logger.error("过滤特殊字符串异常：", e);
		}
		return value;
	}
}
