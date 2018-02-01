package com.masget.xss.rejector;

import org.apache.commons.lang.StringUtils;

/**
 * html 过滤处理
 * @author hoojo
 * @createDate 2018年1月19日 上午11:46:16
 * @file HTMLInjectedRejector.java
 * @package com.masget.xss.rejector
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class HTMLInjectedRejector extends AbstractInjectedRejector {

	private static final String[] KEYWORDS = { "<", ">", "\'",  "\"", "\\(", "\\)" }; 
	private static final String[] REPLACE_WORDS = { "&lt;", "&gt;", "&#39;", "&quot;", "&#40;", "&#41;" }; 

	private HTMLInjectedRejector() {
		super("html");
	}
	
	private static class SingletonHolder {
		private static final AbstractInjectedRejector INSTANCE = new HTMLInjectedRejector();
	}

	public static final AbstractInjectedRejector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	protected String doRejector(String target) throws Exception {

		/*DefaultEncoder encoder = new DefaultEncoder(Arrays.asList(
		    HTMLEntityCodec.class.getName(),
		    PercentCodec.class.getName(),
		    JavaScriptCodec.class.getName()
		));
		*/
		
		//String result = ESAPI.encoder().canonicalize(target);// 注意：若前端使用get方式提交经过encodeURI的中文，此处会乱码
		//result = ESAPI.encoder().encodeForHTML(target); // 此方法中文会被转码
		
		return StringUtils.replaceEach(target, KEYWORDS, REPLACE_WORDS);
	}
}
