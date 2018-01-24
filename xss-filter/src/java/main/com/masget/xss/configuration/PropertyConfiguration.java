package com.masget.xss.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.owasp.esapi.ESAPI;

import com.masget.xss.configuration.loader.PropertyLoader;
import com.masget.xss.configuration.manager.PropertyManager;

/**
 * 默认配置文件
 * 
 * @author hoojo
 * @createDate 2018年1月12日 下午3:44:15
 * @file PropertyConfiguration.java
 * @package com.masget.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class PropertyConfiguration extends AbstractResourceStreamLoader implements PropertyLoader {

	private static volatile PropertyConfiguration instance;
	private static final String DEFAULT_RESOURCE_FILE = "xss.properties";

	private PropertyManager propertyManager;
	private Properties properties;
	private String resourceFile;

	/**
	 * Instantiates a new configuration, using the provided property file name
	 * @param resourceFile
	 *            The name of the property file to load
	 */
	PropertyConfiguration(String resourceFile) {
		super();
		
		this.resourceFile = resourceFile;
		this.propertyManager = new PropertyManager();
		
		// load xss configuration
		try {
			loadConfiguration();
		} catch (IOException e) {
			error("加载默认文件配置失败：", e);
			throw new RuntimeException("加载默认文件配置失败", e);
		}
	}

	public PropertyConfiguration(Properties properties) {
		resourceFile = DEFAULT_RESOURCE_FILE;
		this.properties = properties;
	}

	/**
	 * Instantiates a new configuration.
	 */
	public PropertyConfiguration() {
		this(DEFAULT_RESOURCE_FILE);
	}

	public static PropertyConfiguration getInstance() {
		if (instance == null) {
			synchronized (PropertyConfiguration.class) {
				if (instance == null) {
					instance = new PropertyConfiguration();
				}
			}
		}
		return instance;
	}

	protected void loadConfiguration() throws IOException {
		try {
			// first attempt file IO loading of properties
			info("尝试通过文件IO 加载文件：{}", resourceFile);
			
			properties = loadPropertiesFromStream(getResourceStream(resourceFile), resourceFile);
		} catch (Exception iae) {
			// if file I/O loading fails, attempt classpath based loading next
			error("尝试通过文件IO 加载文件:{}， Exception was: {}", resourceFile, iae);
			
			info("尝试从classpath下加载配置文件：{}", resourceFile);
			try {
				properties = loadPropertiesFromStream(loadConfigurationFromClasspath(resourceFile), resourceFile);
			} catch (Exception e) {
				error("无法以任何方式加载配置：{}. Fail.", resourceFile, e);
				throw new RuntimeException("无法以任何方式加载配置：" + resourceFile + ". Fail.", e);
			}
		}
	}

	private Properties loadPropertiesFromStream(InputStream is, String name) throws IOException {
		Properties config = new Properties();
		try {
			config.load(is);
			info("加载配置文件：{}", name);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (Exception e) {
				}
		}
		return config;
	}

	@Override
	public int getIntProp(String propertyName) throws RuntimeException {
		try {
			return propertyManager.getIntProp(propertyName);
		} catch (RuntimeException ex) {
			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " not found in " + resourceFile);
			}
			try {
				return Integer.parseInt(property);
			} catch (NumberFormatException e) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " has incorrect type");
			}
		}
	}

	/**
	 * {@inheritDoc} Looks for property in three configuration files in following order: 1.) In file defined as
	 * com.masget.xss.opsteam system property 2.) In file defined as com.masget.xss.devteam system property 3.) In
	 * xss.properties
	 */
	@Override
	public byte[] getByteArrayProp(String propertyName) throws RuntimeException {
		try {
			return propertyManager.getByteArrayProp(propertyName);
		} catch (RuntimeException ex) {
			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " not found in " + resourceFile);
			}
			try {
				return ESAPI.encoder().decodeFromBase64(property);
			} catch (IOException e) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " has incorrect type");
			}
		}
	}

	/**
	 * {@inheritDoc} Looks for property in three configuration files in following order: 
	 * 1.) In file defined as com.masget.xss.opsteam system property 
	 * 2.) In file defined as com.masget.xss.devteam system property 
	 * 3.) In xss.properties
	 */
	@Override
	public Boolean getBooleanProp(String propertyName) throws RuntimeException {
		try {
			return propertyManager.getBooleanProp(propertyName);
		} catch (RuntimeException ex) {
			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " not found in " + resourceFile);
			}
			if (property.equalsIgnoreCase("true") || property.equalsIgnoreCase("yes")) {
				return true;
			}
			if (property.equalsIgnoreCase("false") || property.equalsIgnoreCase("no")) {
				return false;
			}
			throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " has incorrect type");
		}
	}

	/**
	 * Looks for property in three configuration files in following order: 
	 * 1.) In file defined ascom.masget.xss.opsteam system property 
	 * 2.) In file defined as com.masget.xss.devteam system property 
	 * 3.) In xss.properties
	 */
	@Override
	public String getStringProp(String propertyName) throws RuntimeException {
		try {
			return propertyManager.getStringProp(propertyName);
		} catch (RuntimeException ex) {
			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " not found in " + resourceFile);
			}
			return property;
		}
	}
	
	@Override
	public Object getObjectProp(String propertyName) throws RuntimeException {
		try {
			return propertyManager.getObjectProp(propertyName);
		} catch (RuntimeException ex) {
			Object property = properties.get(propertyName);
			if (property == null) {
				throw new RuntimeException("DefaultPropertyConfiguration for " + propertyName + " not found in " + resourceFile);
			}
			return property;
		}
	}

	public String getProp(String key, String def) {
		String value = properties.getProperty(key);
		if (value == null) {
			warn("DefaultPropertyConfiguration for " + key + " not found in " + resourceFile + ". Using default: " + def);
			return def;
		}
		return value;
	}

	public boolean getProp(String key, boolean def) {
		String property = properties.getProperty(key);
		if (property == null) {
			warn("DefaultPropertyConfiguration for " + key + " not found in " + resourceFile + ". Using default: " + def);
			return def;
		}
		if (property.equalsIgnoreCase("true") || property.equalsIgnoreCase("yes")) {
			return true;
		}
		if (property.equalsIgnoreCase("false") || property.equalsIgnoreCase("no")) {
			return false;
		}
		warn("DefaultPropertyConfiguration for " + key + " not either \"true\" or \"false\" in " + resourceFile + ". Using default: " + def);
		return def;
	}

	public byte[] getProp(String key, byte[] def) {
		String property = properties.getProperty(key);
		if (property == null) {
			warn("DefaultPropertyConfiguration for " + key + " not found in " + resourceFile + ". Using default: " + def);
			return def;
		}
		try {
			return ESAPI.encoder().decodeFromBase64(property);
		} catch (IOException e) {
			warn("DefaultPropertyConfiguration for " + key + " not properly Base64 encoded in " + resourceFile + ". Using default: " + def);
			return null;
		}
	}

	public int getProp(String key, int def) {
		String property = properties.getProperty(key);
		if (property == null) {
			warn("DefaultPropertyConfiguration for " + key + " not found in " + resourceFile + ". Using default: " + def);
			return def;
		}
		try {
			return Integer.parseInt(property);
		} catch (NumberFormatException e) {
			warn("DefaultPropertyConfiguration for " + key + " not an integer in " + resourceFile + ". Using default: " + def);
			return def;
		}
	}

	public List<String> getProp(String key, List<String> def) {
		String property = properties.getProperty(key);
		if (property == null) {
			warn("DefaultPropertyConfiguration for " + key + " not found in " + resourceFile + ". Using default: " + def);
			return def;
		}
		String[] parts = property.split(",");
		return Arrays.asList(parts);
	}

	public Properties getProperties() {
		return properties;
	}
	
	public static void main(String[] args) {
		System.out.println(PropertyConfiguration.getInstance().getBooleanProp("upload.file.include"));
	}
}
