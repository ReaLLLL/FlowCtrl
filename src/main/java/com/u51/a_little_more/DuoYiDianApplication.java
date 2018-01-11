package com.u51.a_little_more;

import com.u51.a_little_more.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations={"classpath:META-INF/spring-context.xml"})
public class DuoYiDianApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(DuoYiDianApplication.class, args);
		SpringContextUtil.setApplicationContext(ctx);
	}
}
