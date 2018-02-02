package com.cnblogs.hoojo.xss.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.cnblogs.hoojo.xss.configuration.factory.PropertyLoaderFactory;
import com.cnblogs.hoojo.xss.configuration.loader.PropertyLoader;

/**
 * 过滤拦截XML配置文件
 * 
 * @author hoojo
 * @createDate 2018年1月12日 下午3:44:15
 * @file FilterConfiguration.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class FilterConfiguration extends AbstractResourceStreamLoader implements PropertyLoader {

	private static volatile FilterConfiguration instance;
	private static final String DEFAULT_RESOURCE_FILE = "filter-exclude.xml";

	private PropertyLoader filterPropertyLoader;
	private String resourceFile;

	FilterConfiguration(String resourceFile) {
		super();
		this.resourceFile = resourceFile;
		
		try {
			this.filterPropertyLoader = PropertyLoaderFactory.createFilterLoader(resourceFile);
		} catch (Exception e) {
			warn("加载默认filter配置文件失败：{}", resourceFile);

			InputStream is = null;
			try {
				is = loadConfiguration();
				this.filterPropertyLoader = PropertyLoaderFactory.createFilterLoader(is);
			} catch (IOException e2) {
				error("重试加载默认filter配置文件失败：{}", resourceFile, e);
				throw new RuntimeException("重试加载默认filter配置文件失败", e);
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception e3) {
				}
			}
		}
	}

	/**
	 * Instantiates a new configuration.
	 */
	public FilterConfiguration() {
		this(DEFAULT_RESOURCE_FILE);
	}

	public static FilterConfiguration getInstance() {
		if (instance == null) {
			synchronized (FilterConfiguration.class) {
				if (instance == null) {
					instance = new FilterConfiguration();
				}
			}
		}
		return instance;
	}

	protected InputStream loadConfiguration() throws IOException {
		InputStream is = null;
		try {
			info("尝试通过文件IO 加载文件：{}", resourceFile);
			is = getResourceStream(resourceFile);
		} catch (Exception iae) {
			error("尝试通过文件IO 加载文件:{}， Exception was: {}", resourceFile, iae);

			info("尝试从classpath下加载配置文件：{}", resourceFile);
			try {
				is = loadConfigurationFromClasspath(resourceFile);
			} catch (Exception e) {
				error("无法以任何方式加载配置：{}. Fail.", resourceFile, e);
				throw new RuntimeException("无法以任何方式加载配置：" + resourceFile + ". Fail.", e);
			}
		}

		return is;
	}

	@Override
	public int getIntProp(String propertyName) throws RuntimeException {
		try {
			return filterPropertyLoader.getIntProp(propertyName);
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + " has incorrect type");
		}
	}

	@Override
	public byte[] getByteArrayProp(String propertyName) throws RuntimeException {
		try {
			return filterPropertyLoader.getByteArrayProp(propertyName);
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + " has incorrect type");
		}
	}

	@Override
	public Boolean getBooleanProp(String propertyName) throws RuntimeException {
		try {
			return filterPropertyLoader.getBooleanProp(propertyName);
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + " has incorrect type");
		}
	}

	@Override
	public String getStringProp(String propertyName) throws RuntimeException {
		try {
			return filterPropertyLoader.getStringProp(propertyName);
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + " has incorrect type");
		}
	}

	@Override
	public Object getObjectProp(String propertyName) throws RuntimeException {
		try {
			return filterPropertyLoader.getObjectProp(propertyName);
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + " has incorrect type");
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getMapProp(String propertyName) throws RuntimeException {
		try {
			Object data = filterPropertyLoader.getObjectProp(propertyName);
			if (data == null) {
				return null;
			}
			if (data instanceof Map) {
				return (Map<String, List<String>>) data;
			}
			return null;
		} catch (RuntimeException ex) {
			return null;
		}
	}
	
	public boolean isExcludeProp(String propertyName) throws RuntimeException {
		try {
			try {
				if (this.getMapProp(propertyName) == null) {
					return false;
				}
				
				List<String> filters = this.getFilterProp(propertyName);
				List<String> excludes = this.getExcludeProp(propertyName);
				List<String> includes = this.getIncludeProp(propertyName);
				
				boolean emptyFilters = (filters == null || filters.isEmpty());
				boolean emptyExcludes = (excludes == null || excludes.isEmpty());
				boolean emptyIncludes = (includes == null || includes.isEmpty());
				
				if (emptyFilters && emptyExcludes && emptyIncludes) {
					return true;
				} 
				return false;
			} catch (Exception e) {
				return false;
			}
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + " has incorrect type");
		}
	}
	
	public List<String> getFilterProp(String propertyName) throws RuntimeException {
		try {
			return this.getMapProp(propertyName).get("filter");
		} catch (RuntimeException ex) {
			return null;
		}
	}
	
	public List<String> getIncludeProp(String propertyName) throws RuntimeException {
		try {
			return this.getMapProp(propertyName).get("include");
		} catch (RuntimeException ex) {
			return null;
		}
	}
	
	public List<String> getExcludeProp(String propertyName) throws RuntimeException {
		try {
			return this.getMapProp(propertyName).get("exclude");
		} catch (RuntimeException ex) {
			return null;
		}
	}
	
	private List<String> getCheckProp(String propertyName) throws RuntimeException {
		try {
			return this.getMapProp(propertyName).get("check");
		} catch (RuntimeException ex) {
			return null;
		}
	}
	
	public boolean isCheckFileSize(String propertyName) throws RuntimeException {
		try {
			this.getMapProp(propertyName);
			
			List<String> checks = this.getCheckProp(propertyName);
			if (checks == null || checks.isEmpty()) {
				return PropertyConfiguration.getInstance().getProp("file.check.size", true);
			}

			if (checks.contains("check-size=true")) {
				return true;
			} else if (checks.contains("check-size=false")) {
				return false;
			}
			
			return PropertyConfiguration.getInstance().getProp("file.check.size", true); 
		} catch (Exception e) {
			return PropertyConfiguration.getInstance().getProp("file.check.size", true);
		}
	}
	
	public boolean isCheckFileType(String propertyName) throws RuntimeException {
		try {
			try {
				this.getMapProp(propertyName);
				
				List<String> checks = this.getCheckProp(propertyName);
				if (checks == null || checks.isEmpty()) {
					return PropertyConfiguration.getInstance().getProp("file.check.type", true);
				}
				
				if (checks.contains("check-type=true")) {
					return true;
				} else if (checks.contains("check-type=false")) {
					return false;
				}
				
				return PropertyConfiguration.getInstance().getProp("file.check.type", true); 
			} catch (Exception e) {
				return PropertyConfiguration.getInstance().getProp("file.check.type", true);
			}
		} catch (RuntimeException ex) {
			throw new RuntimeException("FilterConfiguration for " + propertyName + ".exclude has incorrect type");
		}
	}

	public static void main(String[] args) {
		
		System.out.println(FilterConfiguration.getInstance().getObjectProp("cnblogs/hoojo/pay/weixin.do"));
		System.out.println(FilterConfiguration.getInstance().getMapProp("cnblogs/hoojo/pay/weixin.do"));
		System.out.println(FilterConfiguration.getInstance().getFilterProp("cnblogs/hoojo/pay/weixin.do"));
		System.out.println(FilterConfiguration.getInstance().getIncludeProp("cnblogs/hoojo/pay/weixin.do"));
		System.out.println(FilterConfiguration.getInstance().getExcludeProp("cnblogs/hoojo/pay/weixin.do"));
	}
}
