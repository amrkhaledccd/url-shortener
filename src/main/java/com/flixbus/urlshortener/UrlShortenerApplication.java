package com.flixbus.urlshortener;

import com.flixbus.urlshortener.util.LoggingInterceptor;
import com.mongodb.client.MongoClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "100")
@EnableAsync
public class UrlShortenerApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerApplication.class, args);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoggingInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public LockProvider lockProvider(MongoClient mongo) {
		return new MongoLockProvider(mongo.getDatabase("url-shortener"));
	}
}
