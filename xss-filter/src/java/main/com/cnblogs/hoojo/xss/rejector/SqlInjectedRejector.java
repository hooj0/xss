package com.cnblogs.hoojo.xss.rejector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.MySQLCodec.Mode;

/**
 * 防止sql注入
 * 
 * @author hoojo
 * @createDate 2018年1月17日 下午4:58:52
 * @file SqlInjectedRejector.java
 * @package com.cnblogs.hoojo.xss.rejector
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class SqlInjectedRejector extends AbstractInjectedRejector {

	private static final String KEY_WORDS = "'|and|exec|execute|insert|create|drop|grant|from|use|group_concat|column_name|xp_cmdshell|table_schema|union|where|select|delete|update|chr|char|master|truncate|declare|or|;|-|--|\\+|//|/|%|#";
	private static final String REPLACE_WORDS = "‘|ａnd|ｅxec|ｅxecute|ｉnsert|ｃreate|ｄrop|ｇrant|ｆrom|ｕｓｅ|ｇroup_concat|ｃolumn_name|ｘp_cmdshell|ｔable_schema|ｕnion|ｗhere|ｓelect|ｄelete|ｕpdate|ｃhr|ｃhar|ｍaster|ｔruncate|ｄeclare|ｏr|；|－|－－|＋|／／|／|％|#";

	private SqlInjectedRejector() {
		super("sql");
	}
	
	private static class SingletonHolder {
		private static final AbstractInjectedRejector INSTANCE = new SqlInjectedRejector();
	}

	public static final AbstractInjectedRejector getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	protected String doRejector(String target) throws Exception {
		String[] keyWords = KEY_WORDS.split("\\|");
		String[] replaceWords = REPLACE_WORDS.split("\\|");
		
		String result = StringUtils.replace(target.toLowerCase(), "\r", ""); // 剔除换行
		result = StringUtils.replace(result, "\n", ""); // 剔除换行
		
		result = StringUtils.replace(result, "/*", " ／ *"); // 剔除注释
		result = StringUtils.replace(result, "*/", "*／ ");
		result = StringUtils.replace(result, "--", "——");
		
		//result = StringUtils.replace(result, "'", "''");

		for (int i = 0; i < keyWords.length; i++) {
			if (result.matches("^(.*\\s){0,1}(" + keyWords[i] + ")(\\s.*){0,1}$")) {
				result = result.replaceAll("(?i)(.*?\\s){0,1}(" + keyWords[i] + ")(\\s.*?){0,1}", "$1" + replaceWords[i] + "$3");
			}
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		SqlInjectedRejector rejector = new SqlInjectedRejector();
		String sql = "select count(*) from a where a = b and c = '1' --查询 /* sash */";
		System.out.println(rejector.doRejector(sql));
		System.out.println(rejector.doRejector("' or 1 = 1"));
		
		String value = StringEscapeUtils.escapeSql(sql);
		System.out.println(value);
		value = ESAPI.encoder().encodeForSQL(new MySQLCodec(Mode.ANSI), sql);
		System.out.println(value);
	}
}
