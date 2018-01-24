package com.masget.xss.configuration.loader.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.owasp.esapi.ESAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.masget.xss.configuration.loader.AbstractPrioritizedPropertyLoader;

/**
 * xml 配置文件加载器
 * 
 * @author hoojo
 * @createDate 2018年1月12日 上午10:51:21
 * @file XmlPropertyLoader.java
 * @package com.masget.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class XmlPropertyLoader extends AbstractPrioritizedPropertyLoader {

	public XmlPropertyLoader(String filename, int priority) {
		super(filename, priority);
	}
	
	@Override
	public int getIntProp(String propertyName) throws RuntimeException {
		String property = properties.getProperty(propertyName);
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
		String property = properties.getProperty(propertyName);
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
		String property = properties.getProperty(propertyName);
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
		String property = properties.getProperty(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		return property;
	}
	
	@Override
	public Object getObjectProp(String propertyName) throws RuntimeException {
		Object property = properties.get(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + " not found in default configuration");
		}
		return property;
	}

	/**
	 * Methods loads configuration from .xml file.
	 * 
	 * @param is
	 */
	protected void loadPropertiesFromInputStream(InputStream is) throws RuntimeException {
		try {
			validateAgainstXSD(is);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("property");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String propertyKey = element.getAttribute("name");
					String propertyValue = element.getTextContent();
					properties.put(propertyKey, propertyValue);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Configuration file : " + filename + " has invalid schema." + e.getMessage(), e);
		}
	}
	
	/**
	 * schema 验证xml文件格式
	 * @author hoojo
	 * @createDate 2018年1月12日 上午10:55:20
	 * @param xml
	 * @throws Exception
	 */
	private void validateAgainstXSD(InputStream xml) throws Exception {
		InputStream xsd = getClass().getResourceAsStream("/xss-properties.xsd");
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new StreamSource(xsd));
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(xml));
	}
}
