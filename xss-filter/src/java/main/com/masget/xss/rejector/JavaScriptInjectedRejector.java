package com.masget.xss.rejector;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

/**
 * js 防注入处理
 * 
 * @author hoojo
 * @createDate 2018年1月17日 下午6:02:05
 * @file JavaScriptInjectedRejector.java
 * @package com.masget.xss.rejector
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class JavaScriptInjectedRejector extends AbstractInjectedRejector {

	private static final String EVENT_KEYWORDS = "onmouseover|onmouseout|onmousedown|onmouseup|onmousemove|onclick|ondblclick|onkeypress|onkeydown|onkeyup|ondragstart|onerrorupdate|onhelp|onreadystatechange|onrowenter|onrowexit|onselectstart|onload|onunload|onbeforeunload|onblur|onerror|onfocus|onresize|onscroll|oncontextmenu";
	private static final PatternCompiler COMPILER = new Perl5Compiler();
	private static final Perl5Matcher MATCHER = new Perl5Matcher();
	
	private JavaScriptInjectedRejector() {
		super("js");
	}
	
	private static class SingletonHolder {
		private static final AbstractInjectedRejector INSTANCE = new JavaScriptInjectedRejector();
	}

	public static final AbstractInjectedRejector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	protected String doRejector(String target) throws Exception {

		target = stripEvent(target);
		return removeJavaScript(target);
	}
	
	private static String stripEvent(String content) throws Exception {
		
		String[] events = StringUtils.split(EVENT_KEYWORDS, "\\|");
		for (String event : events) {
			
			org.apache.oro.text.regex.Pattern pattern = COMPILER.compile("(<[^>]*)(" + event + ")([^>]*>)", 1);
			if (null != pattern) {
				content = Util.substitute(MATCHER, pattern, new Perl5Substitution("$1" + event.substring(2) + "$3"), content, -1);
			}
		}

		return content;
	}

	private String removeJavaScript(String value) {
		// Avoid null characters
		value = value.replaceAll("", "");

		// Avoid anything between script tags
		Pattern scriptPattern = Pattern.compile("<script(.*?)</script>", Pattern.CASE_INSENSITIVE);
		value = scriptPattern.matcher(value).replaceAll("");

		// Avoid anything in a src='...' type of expression
		scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		// Remove any lonesome </script> tag
		scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
		value = scriptPattern.matcher(value).replaceAll("");

		// Remove any lonesome <script ...> tag
		scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		// Avoid eval(...) expressions
		scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		// Avoid expression(...) expressions
		scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		// Avoid javascript:... expressions
		scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
		value = scriptPattern.matcher(value).replaceAll("");

		// Avoid vbscript:... expressions
		scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
		value = scriptPattern.matcher(value).replaceAll("");

		// Avoid onload= expressions
		scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		value = scriptPattern.matcher(value).replaceAll("");

		return value;
	}
	
	public static void main(String[] args) throws Exception {
		JavaScriptInjectedRejector rejector = new JavaScriptInjectedRejector();

		String js = "<script>alert(1); window.onload = function () { alert('%E4%B8%AD%E5%9B%BD');}</script>";
		System.out.println(rejector.doRejector(js));
		
		System.out.println(stripEvent("<script>alert(1); window.onload = function () { alert('%E4%B8%AD%E5%9B%BD');}</script>"));
		System.out.println(stripEvent("<body onload='aaa()'>"));
		System.out.println(rejector.doRejector("<body onload='aaa()'>"));
	}
}
