package com.hzn.awsopensearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
@Configuration
public class BeanConfig {

	@Bean
	public ObjectMapper objectMapper () {
		return new ObjectMapper ();
	}
}
