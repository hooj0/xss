package com.cnblogs.hoojo.xss.configuration.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.springframework.util.AntPathMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.cnblogs.hoojo.xss.configuration.loader.AbstractPrioritizedPropertyLoader;

/**
 * 排除过滤xml 配置文件加载器
 * @author hoojo
 * @createDate 2018年1月12日 上午10:51:21
 * @file FilterXmlPropertyLoader.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class FilterXmlPropertyLoader extends AbstractPrioritizedPropertyLoader {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
	
	public FilterXmlPropertyLoader(InputStream is) {
		super(is);
	}
	
	public FilterXmlPropertyLoader(String filename, int priority) {
		super(filename, priority);
	}

	@Override
	public int getIntProp(String propertyName) throws RuntimeException {
		String property = getProp(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		try {
			return Integer.parseInt(property);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Incorrect type of : " + propertyName + ". Value " + property + "cannot be converted to integer", e);
		}
	}

	@Override
	public byte[] getByteArrayProp(String propertyName) throws RuntimeException {
		String property = getProp(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		try {
			return ESAPI.encoder().decodeFromBase64(property);
		} catch (IOException e) {
			throw new RuntimeException("Incorrect type of : " + propertyName + ". Value " + property + "cannot be converted to byte array", e);
		}
	}

	@Override
	public Boolean getBooleanProp(String propertyName) throws RuntimeException {
		String property = getProp(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		if (property.equalsIgnoreCase("true") || property.equalsIgnoreCase("yes")) {
			return true;
		}
		if (property.equalsIgnoreCase("false") || property.equalsIgnoreCase("no")) {
			return false;
		} else {
			throw new RuntimeException("Incorrect type of : " + propertyName + ". Value " + property + "cannot be converted to boolean");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStringProp(String propertyName) throws RuntimeException {
		String property = getProp(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		return property;
	}
	
	@Override
	public Object getObjectProp(String propertyName) throws RuntimeException {
		Object property = getProp(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		return property;
	}
	
	@SuppressWarnings("unchecked")
	public <V> V getProp(String propertyName) throws RuntimeException {
		Object property = properties.get(propertyName);
		if (property == null) {
			Enumeration<?> enumeration = properties.propertyNames();
			while (enumeration.hasMoreElements()) {
				Object key = enumeration.nextElement();
				if (PATH_MATCHER.isPattern(key.toString()) && PATH_MATCHER.match(key.toString(), propertyName)) {
					return (V) properties.get(key);
				}
			}
		}
		return (V) property;
	}

	/**
	 * Methods loads configuration from .xml file.
	 * 
	 * @param is
	 */
	protected void loadPropertiesFromInputStream(InputStream is) throws RuntimeException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList roots = doc.getElementsByTagName("filter-excludes");
			if (roots == null || roots.getLength() <= 0) {
				throw new RuntimeException("XML配置节点错误，没有找到节点filter-excludes");
			} 
			
			for (int idx = 0; idx < roots.getLength(); idx++) {
				
				Element filterExcludes = (Element) roots.item(idx);
				NodeList nodeList = filterExcludes.getElementsByTagName("exclude");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						String url = element.getAttribute("url");
						
						String checkSize = element.getAttribute("check-size");
						String checkType = element.getAttribute("check-type");
						
						List<String> fileChecks = Lists.newArrayList();
						if ("true".equalsIgnoreCase(checkSize) || "yes".equalsIgnoreCase(checkSize)) {
							fileChecks.add("check-size=true");
						} else if ("false".equalsIgnoreCase(checkSize) || "no".equalsIgnoreCase(checkSize)) {
							fileChecks.add("check-size=false");
						}
						if ("true".equalsIgnoreCase(checkType) || "yes".equalsIgnoreCase(checkType)) {
							fileChecks.add("check-type=true");
						} else if ("false".equalsIgnoreCase(checkType) || "no".equalsIgnoreCase(checkType)) {
							fileChecks.add("check-type=false");
						}
						
						List<String> filters = Arrays.asList(StringUtils.split(StringUtils.remove(element.getAttribute("filter"), " "), ","));
						List<String> excludeParams = parseElement(element, "exclude-param");
						List<String> includeParams = parseElement(element, "include-param");
						
						properties.put(url, ImmutableMap.<String, List<String>>of("filter", filters, "exclude", excludeParams, "include", includeParams, "check", fileChecks));
					}
				}
			}
			
			System.out.println(properties);
		} catch (Exception e) {
			throw new RuntimeException("Configuration file : " + filename + " has invalid schema." + e.getMessage(), e);
		}
	}
	
	private List<String> parseElement(Element el, String tagName) {
		List<String> list = Lists.newArrayList();
		
		NodeList nodeList = el.getElementsByTagName(tagName);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				list.addAll(Splitter.on(",").trimResults().splitToList(element.getTextContent()));
			}
		}
		
		return list;
	}
}
