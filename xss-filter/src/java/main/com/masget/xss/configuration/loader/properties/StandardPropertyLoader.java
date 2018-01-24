package com.masget.xss.configuration.loader.properties;

import java.io.IOException;
import java.io.InputStream;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.ConfigurationException;

import com.masget.xss.configuration.loader.AbstractPrioritizedPropertyLoader;

/**
 * 标准的配置文件加载器实现
 * 
 * @author hoojo
 * @createDate 2018年1月12日 上午10:19:34
 * @file StandardPropertyLoader.java
 * @package com.masget.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class StandardPropertyLoader extends AbstractPrioritizedPropertyLoader {

	public StandardPropertyLoader(String filename, int priority) {
		super(filename, priority);
	}

	@Override
	public int getIntProp(String propertyName) throws RuntimeException {
		String property = properties.getProperty(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + "not found in configuration");
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
			throw new RuntimeException("Property : " + propertyName + "not found in default configuration");
		}
		try {
			return ESAPI.encoder().decodeFromBase64(property);
		} catch (IOException e) {
			throw new ConfigurationException("Incorrect type of : " + propertyName + ". Value " + property + "cannot be converted to byte array", e);
		}
	}

	@Override
	public Boolean getBooleanProp(String propertyName) throws RuntimeException {
		String property = properties.getProperty(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + "not found in default configuration");
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
			throw new RuntimeException("Property : " + propertyName + "not found in default configuration");
		}
		return property;
	}
	
	@Override
	public Object getObjectProp(String propertyName) throws RuntimeException {
		Object property = properties.get(propertyName);
		if (property == null) {
			throw new RuntimeException("Property : " + propertyName + "not found in default configuration");
		}
		return property;
	}

	/**
	 * Methods loads configuration from .properties file.
	 * 
	 * @param is
	 */
	protected void loadPropertiesFromInputStream(InputStream is) {
		try {
			properties.load(is);
		} catch (IOException ex) {
			System.err.println("Loading " + filename + " via file I/O failed. Exception was: " + ex);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					System.err.println("Could not close stream");
				}
			}
		}
	}
}
