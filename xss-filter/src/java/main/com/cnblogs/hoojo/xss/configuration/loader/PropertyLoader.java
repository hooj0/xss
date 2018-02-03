package com.cnblogs.hoojo.xss.configuration.loader;

/**
 * 配置文件加载器
 * 
 * @author hoojo
 * @createDate 2018年1月12日 上午10:10:55
 * @file PropertyLoader.java
 * @package com.cnblogs.hoojo.xss.configuration
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public interface PropertyLoader {

	/**
     * Get any int type property from security configuration.
     *
     * @return property value.
     * @throws RuntimeException when property does not exist in configuration or has incorrect type.
     */
    public int getIntProp(String propertyName) throws RuntimeException;

    /**
     * Get any byte array type property from security configuration.
     *
     * @return property value.
     * @throws RuntimeException when property does not exist in configuration or has incorrect type.
     */
    public byte[] getByteArrayProp(String propertyName) throws RuntimeException;

    /**
     * Get any Boolean type property from security configuration.
     *
     * @return property value.
     * @throws RuntimeException when property does not exist in configuration or has incorrect type.
     */
    public Boolean getBooleanProp(String propertyName) throws RuntimeException;

    /**
     * Get any property from security configuration. As every property can be returned as string, this method
     * throws exception only when property does not exist.
     *
     * @return property value.
     * @throws RuntimeException when property does not exist in configuration.
     */
    public String getStringProp(String propertyName) throws RuntimeException;
    
    /**
     * 获取对象
     * @author hoojo
     * @createDate 2018年1月16日 上午11:18:57
     */
    public Object getObjectProp(String propertyName) throws RuntimeException;
}
