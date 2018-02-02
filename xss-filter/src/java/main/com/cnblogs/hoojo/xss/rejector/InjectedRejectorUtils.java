package com.cnblogs.hoojo.xss.rejector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cnblogs.hoojo.xss.configuration.FilterConfiguration;
import com.cnblogs.hoojo.xss.configuration.PropertyConfiguration;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * 放注入工具类
 * @author hoojo
 * @createDate 2018年1月18日 下午5:45:24
 * @file InjectedRejectorManager.java
 * @package com.cnblogs.hoojo.xss.rejector
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public abstract class InjectedRejectorUtils {

	private static final Logger logger = LoggerFactory.getLogger(InjectedRejectorUtils.class);
	
	private static final String DEFAULT_FILTER_CONFIG_KEY = "default.filter";
	private static final Map<String, AbstractInjectedRejector> rejectores = Maps.newLinkedHashMap();
	
	static {
		rejectores.put(CSSInjectedRejector.getInstance().getName(), CSSInjectedRejector.getInstance());
		rejectores.put(HTMLInjectedRejector.getInstance().getName(), HTMLInjectedRejector.getInstance());
		rejectores.put(JavaScriptInjectedRejector.getInstance().getName(), JavaScriptInjectedRejector.getInstance());
		rejectores.put(SqlInjectedRejector.getInstance().getName(), SqlInjectedRejector.getInstance());
	}
	
	/**
	 * 执行配置在 filter-exclude.xml 中所配置的filter
	 * @author hoojo
	 * @createDate 2018年1月17日 下午5:42:19
	 * @param targetURL 目标URL
	 * @param name 参数名称
	 * @param value 参数值
	 * @return 返回过滤剔除后的字符串
	 * @throws Exception
	 */
	public static String invokeAll(String targetURL, String name, String value) throws Exception {
		if (value == null) {
			return null;
		}
		logger.debug("XSS处理URL：{}，参数：{}，参数值：{}", new Object[] { targetURL, name, value });

		
		List<String> defaultFilters = PropertyConfiguration.getInstance().getProp(DEFAULT_FILTER_CONFIG_KEY, getFilters());
		
		List<String> filters = FilterConfiguration.getInstance().getFilterProp(targetURL);
		List<String> excludes = FilterConfiguration.getInstance().getExcludeProp(targetURL);
		List<String> includes = FilterConfiguration.getInstance().getIncludeProp(targetURL);
		
		boolean emptyFilters = filters == null || filters.isEmpty();
		boolean emptyExcludes = excludes == null || excludes.isEmpty();
		boolean emptyIncludes = includes == null || includes.isEmpty();
		
		// 1、filter、exclude、include 为空，表示该url不被过滤，即不做任何处理
		// 2、filter为空、exclude不为空，表示该url的exclude中的参数不被过滤处理（所有filter都处理）
		// 3、filter为空、include不为空，表示该url的include中的参数被处理过滤（所有filter都处理）
		// 4、filter不为空，表示该url只处理filter中的过滤
		if (FilterConfiguration.getInstance().getMapProp(targetURL) == null) {
			filters = defaultFilters;
		} else if (filters.isEmpty() && excludes.isEmpty() && includes.isEmpty()) {
			// 该URL位于白名单，忽略处理
			return value;
		} else if (emptyFilters) {
			filters = defaultFilters;
		}
		
		fixedSort(filters);
		
		logger.warn("目标URL：{} 配置的filter：{}", targetURL, filters);
		for (String filter : filters) {
			
			AbstractInjectedRejector rejector = rejectores.get(filter);
			if (rejector != null) {
				if (emptyExcludes && emptyIncludes) {
					value = rejector.doRejector(value);
				} else if (excludes.contains(name)) {
					// 不过滤不处理
				} else {
					value = rejector.doRejector(value);
				}
			} else {
				logger.warn("目标URL：{} 配置的filter：{} 不存在，请检查配置", targetURL, filter);
			}
		} 
		
		logger.debug("XSS处理URL：{}，参数：{}，返回参数值：{}", new Object[] { targetURL, name, value });
		return value;
	}
	
	private static void fixedSort(List<String> filters) {
		filters.sort(Ordering.explicit(getFilters()));
	}
	
	public static List<String> getFilters() {
		String[] filters = new String[rejectores.keySet().size()];
		return Arrays.asList(rejectores.keySet().toArray(filters));
	}
	
	public static void main(String[] args) throws Exception {
		String sql = "select count(*) from a where a = b and c = '1' --查询 /* sash */";
		sql += "<script>alert(1); window.onload = function () { alert('%E4%B8%AD%E5%9B%BD');}</script>";
		
		System.out.println("##" + InjectedRejectorUtils.invokeAll("cnblogs.hoojo/pay/weixin.do", "name", sql));
		System.out.println("##" + InjectedRejectorUtils.invokeAll("cnblogs.hoojo/pay/weixin.do", "remark", sql));
	}
}
