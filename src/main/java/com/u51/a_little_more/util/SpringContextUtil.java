package com.u51.a_little_more.util;

import org.springframework.context.ApplicationContext;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: SpringContextUtil.java, v 0.1 2018年01月11日 下午6:53:53 alexsong Exp $
 */
public class SpringContextUtil {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static Object getBean(String name){
        return applicationContext.getBean(name);
    }

    public static Object getBean(Class<?> requiredType){
        return applicationContext.getBean(requiredType);
    }
}
