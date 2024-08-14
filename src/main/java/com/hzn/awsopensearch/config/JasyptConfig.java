package com.hzn.awsopensearch.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Configuration
public class JasyptConfig {

	@Value ("${jasypt.encryptor.key}")
	private String jasyptKey;

	@Bean (name = "jasyptStringEncryptor")
	public StringEncryptor stringEncryptor () {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor ();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig ();
		config.setPassword (jasyptKey);
		config.setAlgorithm ("PBEWithMD5AndDES");
		config.setKeyObtentionIterations ("1000");
		config.setPoolSize ("1");
		config.setProviderName ("SunJCE");
		config.setSaltGeneratorClassName ("org.jasypt.salt.RandomSaltGenerator");
		config.setStringOutputType ("base64");
		encryptor.setConfig (config);
		return encryptor;
	}
}
