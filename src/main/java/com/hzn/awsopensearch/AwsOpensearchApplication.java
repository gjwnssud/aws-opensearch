package com.hzn.awsopensearch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan ("com.hzn.awsopensearch.mapper")
@SpringBootApplication
public class AwsOpensearchApplication {

	public static void main (String[] args) {
		SpringApplication.run (AwsOpensearchApplication.class, args);
	}

}
