package com.cnblogs.hoojo.xss.configuration.consts;

/**
 * 配置文件类型
 * 
 * @author hoojo
 * @createDate 2018年1月12日 上午11:19:44
 * @file ConfigurationType.java
 * @package com.cnblogs.hoojo.xss.configuration.consts
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public enum ConfigurationType {

	PROPERTIES("properties"), XML("xml");

    String typeName;

    ConfigurationType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
