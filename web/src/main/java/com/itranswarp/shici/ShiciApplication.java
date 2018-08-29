package com.itranswarp.shici;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.itranswarp.shici.util.Utils;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.spring.PebbleViewResolver;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

/**
 * Shici Application.
 * 
 * @author liaoxuefeng
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class ShiciApplication {

	final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(ShiciApplication.class);
	}

	@Bean
	public PebbleViewResolver pebbleViewResolver() throws IOException {
		boolean dev = isDevMode();
		logger.info("init pebble as {} mode...", dev ? "development" : "production");
		PebbleViewResolver viewResolver = new PebbleViewResolver();
		viewResolver.setPrefix("templates/");
		viewResolver.setSuffix("");
		viewResolver.setPebbleEngine(new PebbleEngine.Builder().cacheActive(!dev).extension(createExtension())
				.loader(new ClasspathLoader()).build());
		return viewResolver;
	}

	Extension createExtension() {
		return new AbstractExtension() {
			@Override
			public Map<String, Filter> getFilters() {
				Map<String, Filter> map = new HashMap<>();
				map.put("lines", new Filter() {
					@Override
					public List<String> getArgumentNames() {
						return null;
					}

					@Override
					public Object apply(Object input, Map<String, Object> args, PebbleTemplate self,
							EvaluationContext context, int lineNumber) throws PebbleException {
						if (input == null) {
							return null;
						}
						String text = input.toString();
						String[] lines = text.split("\n");
						StringBuilder sb = new StringBuilder(text.length() + 100);
						for (String line : lines) {
							sb.append("<p>").append(line).append("</p>");
						}
						return sb.toString();
					}
				});
				return map;
			}
		};
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		/**
		 * Keep "/static/" prefix
		 */
		return new WebMvcConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
			}
		};
	}

	boolean isDevMode() throws IOException {
		Resource[] resources = Utils.loadResources("classpath*:META-INF/spring-devtools.properties");
		return resources.length > 0;
	}
}
