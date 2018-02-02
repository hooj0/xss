package com.cnblogs.hoojo.xss.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.cnblogs.hoojo.xss.log.ApplicationLogging;

/**
 * 配置文件资源加载器
 * @author hoojo
 * @createDate 2018年1月16日 上午11:31:09
 * @file AbstractResourceStreamLoader.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public abstract class AbstractResourceStreamLoader extends ApplicationLogging {
	
	private static final String userHome = StringUtils.defaultIfBlank(System.getProperty("user.home"), "");
	private static final String customDirectory = StringUtils.defaultIfBlank(System.getProperty("com.cnblogs.hoojo.xss.resources"), "");
	protected String globalResourceDirectory = ".xss"; // For backward compatibility (vs. "xss")
	protected String resourceDirectory = "xss"; 

	protected InputStream getResourceStream(String filename) throws IOException {
		if (filename == null) {
			warn("filter配置文件名为空");
			return null;
		}

		try {
			File f = getResourceFile(filename);
			if (f != null && f.exists()) {
				return new FileInputStream(f);
			}
		} catch (Exception e) {
		}

		throw new FileNotFoundException(filename + "filter配置文件不存在");
	}

	protected File getResourceFile(String filename) {
		debug("通过FileIO加载配置文件：{}/{}", customDirectory, filename);

		if (filename == null) {
			warn("无法加载配置文件，文件名称为空");
			return null; 
		}

		File file = new File(customDirectory, filename);
		if (customDirectory != null && file.canRead()) {
			info("找到配置文件: {}", file.getAbsolutePath());
			return file;
		} else {
			warn("未找到配置文件或配置文件目录不可读: {}", file.getAbsolutePath());
		}

		URL fileUrl = ClassLoader.getSystemResource(globalResourceDirectory + "/" + filename);
		if (fileUrl == null) {
			warn("未找到配置文件: {}", globalResourceDirectory + "/" + filename);
			info("重新从系统资源目录下加载到配置文件：{}", resourceDirectory + "/" + filename);

			fileUrl = ClassLoader.getSystemResource(resourceDirectory + "/" + filename);
		} 

		if (fileUrl != null) {
			file = new File(fileUrl.getFile());
			info("从系统资源目录下加载到配置文件：{}", fileUrl.getPath());
			
			if (file.exists()) {
				warn("从系统资源目录下找到配置文件: {}", file.getAbsolutePath());
				return file;
			} else {
				warn("从系统资源目录下未找到配置文件: {}", file.getAbsolutePath());
			}
		} 

		// First look under ".xss" (for reasons of backward compatibility).
		file = new File(userHome + "/" + globalResourceDirectory, filename);
		if (file.exists() && file.canRead()) {
			warn("在 'user.home' 找到配置文件: {}", file.getAbsolutePath());
			return file;
		} else {
			// Didn't find it under old directory ".xss" so now look under the "xss" directory.
			file = new File(userHome + "/" + resourceDirectory, filename);
			if (file.exists() && file.canRead()) {
				warn("在 'user.home' 找到配置文件: {}", file.getAbsolutePath());
				return file;
			} else {
				warn("在 'user.home' ({}) 目录没有找到配置文件: {}", userHome, file.getAbsolutePath());
			}
		}

		// return null if not found
		return null;
	}

	protected InputStream loadConfigurationFromClasspath(String fileName) throws IllegalArgumentException {
		InputStream in = null;

		ClassLoader[] loaders = new ClassLoader[] { 
				Thread.currentThread().getContextClassLoader(),
		        ClassLoader.getSystemClassLoader(), 
		        getClass().getClassLoader() 
		};
		String[] classLoaderNames = { 
				"当前线程上下文类加载器", 
				"系统类加载器",
		        "当前Class类加载器" 
		};

		ClassLoader currentLoader = null;
		for (int i = 0; i < loaders.length; i++) {
			if (loaders[i] != null) {
				currentLoader = loaders[i];
				try {
					// try root
					String currentClasspathSearchLocation = "/";
					in = loaders[i].getResourceAsStream(fileName);
					

					// try resourceDirectory folder
					if (in == null) {
						currentClasspathSearchLocation = globalResourceDirectory + "/";
						in = currentLoader.getResourceAsStream(currentClasspathSearchLocation + fileName);
					} 

					// try .xss folder. Look here first for backward compatibility.
					if (in == null) {
						currentClasspathSearchLocation = ".xss/";
						in = currentLoader.getResourceAsStream(currentClasspathSearchLocation + fileName);
					} 

					// try xss folder (new directory)
					if (in == null) {
						currentClasspathSearchLocation = resourceDirectory + "/";
						in = currentLoader.getResourceAsStream(currentClasspathSearchLocation + fileName);
					} 

					// try resources folder
					if (in == null) {
						currentClasspathSearchLocation = "src/main/resources/";
						in = currentLoader.getResourceAsStream(currentClasspathSearchLocation + fileName);
					} 

					// now load the properties
					if (in != null) {
						info("成功加载文件 " + fileName + " 在 CLASSPATH 目录 '" + currentClasspathSearchLocation + "' 使用 " + classLoaderNames[i] + "!");
						break; // Outta here since we've found and loaded it.
					} 
				} catch (Exception e) {
					in = null;
				}
			}
		}

		if (in == null) {
			// CHECKME: This is odd...why not RuntimeException?
			throw new IllegalArgumentException("类加载器加载配置资源文件失败： " + fileName);
		}

		return in;
	}
}
