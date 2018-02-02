package com.cnblogs.hoojo.xss.rejector;

import java.util.regex.Pattern;

/**
 * css 过滤处理
 * @author hoojo
 * @createDate 2018年1月19日 上午11:46:16
 * @file CSSInjectedRejector.java
 * @package com.cnblogs.hoojo.xss.rejector
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class CSSInjectedRejector extends AbstractInjectedRejector {

	private CSSInjectedRejector() {
		super("css");
	}
	
	private static class SingletonHolder {
		private static final AbstractInjectedRejector INSTANCE = new CSSInjectedRejector();
	}

	public static final AbstractInjectedRejector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	protected String doRejector(String target) throws Exception {

		// Avoid anything between script tags
		Pattern scriptPattern = Pattern.compile("<style(.*?)</style>", Pattern.CASE_INSENSITIVE);
		target = scriptPattern.matcher(target).replaceAll("");

		// Avoid anything in a src='...' type of expression
		scriptPattern = Pattern.compile("href[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		target = scriptPattern.matcher(target).replaceAll("");

		scriptPattern = Pattern.compile("href[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		target = scriptPattern.matcher(target).replaceAll("");

		// Remove any lonesome </script> tag
		scriptPattern = Pattern.compile("</link>", Pattern.CASE_INSENSITIVE);
		target = scriptPattern.matcher(target).replaceAll("");

		// Remove any lonesome <script ...> tag
		scriptPattern = Pattern.compile("<link(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		target = scriptPattern.matcher(target).replaceAll("");

		// Avoid javascript:... expressions
		scriptPattern = Pattern.compile("style=", Pattern.CASE_INSENSITIVE);
		target = scriptPattern.matcher(target).replaceAll("css=");

		return target;
	}

	public static void main(String[] args) throws Exception {
		
		
		String value = "<a href='dd'>dd</href><div style='width: 2px;' name='d'></div> <style link='a.css'></style> <link rel='stylesheet' href='bootstrap.min.css'>";
		
		System.out.println(getInstance().doRejector("<div style='width: 2px;' name='d'></div>"));
		System.out.println(getInstance().doRejector("<style link='a.css' />"));
		System.out.println(getInstance().doRejector(value));
	}
}
