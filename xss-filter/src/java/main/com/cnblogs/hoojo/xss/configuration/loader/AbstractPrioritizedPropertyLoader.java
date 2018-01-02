package com.cnblogs.hoojo.xss.configuration.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 优先加载配置抽象类
 * @author hoojo
 * @createDate 2018年1月12日 上午10:14:18
 * @file AbstractPrioritizedPropertyLoader.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public abstract class AbstractPrioritizedPropertyLoader implements PropertyLoader, Comparable<AbstractPrioritizedPropertyLoader> {

	protected Properties properties;
	protected final String filename;
	protected final InputStream is;
	private final int priority;

	public AbstractPrioritizedPropertyLoader(String filename, int priority) {
		this.priority = priority;
		this.filename = filename;
		this.is = null;
		initProperties();
	}
	
	public AbstractPrioritizedPropertyLoader(InputStream is) {
		this.priority = 0;
		this.filename = null;
		this.is = is;
		initProperties();
	}

	/**
	 * Get priority of this property loader. If two and more loaders can return value for the same property key, the one
	 * with highest priority will be chosen.
	 * 
	 * @return priority of this property loader
	 */
	public int priority() {
		return priority;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 比较优先级
	 * @author hoojo
	 * @createDate 2018年1月12日 上午10:17:42
	 */
	@Override
	public int compareTo(AbstractPrioritizedPropertyLoader compared) {
		if (this.priority > compared.priority()) {
			return 1;
		} else if (this.priority < compared.priority()) {
			return -1;
		}
		return 0;
	}

	public String name() {
		return filename;
	}

	/**
	 * Initializes properties object and fills it with data from configuration file.
	 */
	protected void initProperties() {
		properties = new Properties();
		
		InputStream is = null;
		if (filename != null) {
			
			File file = new File(filename);
			if (file.exists() && file.isFile()) {
				try {
					is = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					System.err.println("Configuration file " + filename + " does not exist");
				}
			} else {
				System.err.println("Configuration file " + filename + " does not exist");
			}
		} else {
			is = this.is;
		}
		
		loadPropertiesFromInputStream(is);
	}

	/**
	 * Method that loads the data from configuration file to properties object.
	 * 
	 * @param is
	 */
	protected abstract void loadPropertiesFromInputStream(InputStream is);
}
