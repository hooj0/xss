package com.cnblogs.hoojo.xss.configuration.factory;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.cnblogs.hoojo.xss.configuration.consts.ConfigurationMode;
import com.cnblogs.hoojo.xss.configuration.consts.ConfigurationType;
import com.cnblogs.hoojo.xss.configuration.loader.AbstractPrioritizedPropertyLoader;
import com.cnblogs.hoojo.xss.configuration.loader.properties.StandardPropertyLoader;
import com.cnblogs.hoojo.xss.configuration.loader.xml.FilterXmlPropertyLoader;
import com.cnblogs.hoojo.xss.configuration.loader.xml.XmlPropertyLoader;

/**
 * 配置文件工厂类
 * @author hoojo
 * @createDate 2018年1月12日 上午11:07:04
 * @file PropertyLoaderFactory.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public abstract class PropertyLoaderFactory {

	public static AbstractPrioritizedPropertyLoader createPropertyLoader(ConfigurationMode mode) throws RuntimeException, FileNotFoundException {
        String cfgPath = System.getProperty(mode.getConfigName());
        if (cfgPath == null) {
            throw new RuntimeException("System property [" + mode.getConfigName() + "] is not set");
        }
        
        String fileExtension = cfgPath.substring(cfgPath.lastIndexOf('.') + 1);
        if (ConfigurationType.XML.getTypeName().equals(fileExtension)) {
            return new XmlPropertyLoader(cfgPath, mode.getPriority());
        } else if (ConfigurationType.PROPERTIES.getTypeName().equals(fileExtension)) {
            return new StandardPropertyLoader(cfgPath, mode.getPriority());
        } else {
            throw new RuntimeException("Configuration storage type [" + fileExtension + "] is not supported");
        }
    }
	
	public static AbstractPrioritizedPropertyLoader createFilterLoader(String fileName) throws RuntimeException, FileNotFoundException {
        if (fileName == null) {
            throw new RuntimeException("filter execlude xml config [" + fileName + "] is not set");
        }
        
        return new FilterXmlPropertyLoader(fileName, 0);
    }
	
	public static AbstractPrioritizedPropertyLoader createFilterLoader(InputStream is) throws RuntimeException, FileNotFoundException {
        if (is == null) {
            throw new RuntimeException("filter execlude xml config is not set");
        }
        
        return new FilterXmlPropertyLoader(is);
    }
}
