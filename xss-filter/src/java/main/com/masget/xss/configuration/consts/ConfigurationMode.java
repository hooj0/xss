package com.masget.xss.configuration.consts;

/**
 * 配置文件模式
 * @author hoojo
 * @createDate 2018年1月12日 上午11:14:59
 * @file ConfigurationMode.java
 * @package com.masget.xss.configuration.consts
 * @project xss-filter
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public enum ConfigurationMode {
	
	/** 线上部署配置 */
	OPSTEAM_CFG("com.masget.xss.opsteam", 1),
	/** 开发团队配置 */
    DEVTEAM_CFG("com.masget.xss.devteam", 2);

    /**
     * Key of system property pointing to path esapi to configuration file. 
     */
    String configName;

    /**
     * Priority of configuration (higher numer - higher priority).
     */
    int priority;

    ConfigurationMode(String configName, int priority) {
        this.configName = configName;
        this.priority = priority;
    }

    public String getConfigName() {
        return configName;
    }

    public int getPriority() {
        return priority;
    }
}
