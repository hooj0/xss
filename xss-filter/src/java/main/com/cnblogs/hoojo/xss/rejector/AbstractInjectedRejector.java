package com.cnblogs.hoojo.xss.rejector;

import com.cnblogs.hoojo.xss.log.ApplicationLogging;

/**
 * 剔除http请求注入的敏感信息数据：sql关键字、js关键字、html关键字、css关键字
 * @author hoojo
 * @createDate 2018年1月17日 下午4:30:42
 * @file AbstractInjectedRejector.java
 * @package com.cnblogs.hoojo.xss
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public abstract class AbstractInjectedRejector extends ApplicationLogging {

	protected String name;
	
	public AbstractInjectedRejector(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * 替换业务规则
	 * @author hoojo
	 * @createDate 2018年1月17日 下午5:41:51
	 * @param target 目标字符串
	 * @return 替换后字符串
	 * @throws Exception
	 */
	protected abstract String doRejector(String target) throws Exception;

}
