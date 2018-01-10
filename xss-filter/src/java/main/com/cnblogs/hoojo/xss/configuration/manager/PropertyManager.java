package com.cnblogs.hoojo.xss.configuration.manager;

import java.util.TreeSet;

import com.cnblogs.hoojo.xss.configuration.consts.ConfigurationMode;
import com.cnblogs.hoojo.xss.configuration.factory.PropertyLoaderFactory;
import com.cnblogs.hoojo.xss.configuration.loader.AbstractPrioritizedPropertyLoader;
import com.cnblogs.hoojo.xss.configuration.loader.PropertyLoader;

/**
 * 配置文件管理器
 * 
 * @author hoojo
 * @createDate 2018年1月12日 上午11:05:01
 * @file PropertyManager.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class PropertyManager implements PropertyLoader {

	protected TreeSet<AbstractPrioritizedPropertyLoader> loaders;

    public PropertyManager() {
        initLoaders();
    }

    @Override
    public int getIntProp(String propertyName) throws RuntimeException {
        for (AbstractPrioritizedPropertyLoader loader : loaders) {
            try {
                return loader.getIntProp(propertyName);
            } catch (RuntimeException e) {
                System.err.println("Property not found in " + loader.name());
            }
        }
        throw new RuntimeException("Could not find property " + propertyName + " in configuration");
    }

    @Override
    public byte[] getByteArrayProp(String propertyName) throws RuntimeException {
        for (AbstractPrioritizedPropertyLoader loader : loaders) {
            try {
                return loader.getByteArrayProp(propertyName);
            } catch (RuntimeException e) {
                System.err.println("Property not found in " + loader.name());
            }
        }
        throw new RuntimeException("Could not find property " + propertyName + " in configuration");
    }

    @Override
    public Boolean getBooleanProp(String propertyName) throws RuntimeException {
        for (AbstractPrioritizedPropertyLoader loader : loaders) {
            try {
                return loader.getBooleanProp(propertyName);
            } catch (RuntimeException e) {
                System.err.println("Property not found in " + loader.name());
            }
        }
        throw new RuntimeException("Could not find property " + propertyName + " in configuration");
    }

    @Override
    public String getStringProp(String propertyName) throws RuntimeException {
        for (AbstractPrioritizedPropertyLoader loader : loaders) {
            try {
                return loader.getStringProp(propertyName);
            } catch (RuntimeException e) {
                System.err.println("Property : " + propertyName + " not found in " + loader.name());
            }
        }
        throw new RuntimeException("Could not find property " + propertyName + " in configuration");
    }
    
    @Override
	public Object getObjectProp(String propertyName) throws RuntimeException {
    	for (AbstractPrioritizedPropertyLoader loader : loaders) {
            try {
                return loader.getObjectProp(propertyName);
            } catch (RuntimeException e) {
                System.err.println("Property : " + propertyName + " not found in " + loader.name());
            }
        }
        throw new RuntimeException("Could not find property " + propertyName + " in configuration");
	}

    private void initLoaders() {
        loaders = new TreeSet<AbstractPrioritizedPropertyLoader>();
        
        try {
            loaders.add(PropertyLoaderFactory.createPropertyLoader(ConfigurationMode.OPSTEAM_CFG));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        try {
            loaders.add(PropertyLoaderFactory.createPropertyLoader(ConfigurationMode.DEVTEAM_CFG));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
