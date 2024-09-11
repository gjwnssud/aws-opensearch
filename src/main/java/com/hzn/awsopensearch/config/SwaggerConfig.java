package com.hzn.awsopensearch.config;

import com.hzn.awsopensearch.enums.Status;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI () {
		return new OpenAPI ().info (new Info ().title ("AWS OpenSearch API").description ("AWS OpenSearch API 명세").version ("v1.0"));
	}

	@Bean
	public OpenApiCustomizer removePathsCustomizer () {
		return openApi -> {
			Paths paths = openApi.getPaths ();
			paths.keySet ().removeIf (path -> !path.startsWith ("/api"));
		};
	}

	@Bean
	public OperationCustomizer operationCustomizer () {
		return (operation, handlerMethod) -> {
			// 공통 응답 생성
			ApiResponses responses = operation.getResponses ();
			for (Status status : Status.values ()) {
				int code = status.getCode ();

				Schema<?> commonResponse;
				if (code == 200) {
					if (handlerMethod.isVoid ()) {
						commonResponse = new Schema<> ().type ("object").title ("공통 응답 객체");
						commonResponse.addProperty ("code", new IntegerSchema ().title ("응답 코드").example (String.valueOf (code)));
					} else {
						commonResponse = new Schema<> ().$ref ("CommonResponse");
					}
				} else {
					commonResponse = new Schema<> ().type ("object").title ("공통 응답 객체");
					commonResponse.addProperty ("code", new IntegerSchema ().title ("응답 코드").example (String.valueOf (code)));
					commonResponse.addProperty ("message", new StringSchema ().title ("응답 메시지").example (status.getMessage ()));
				}

				responses.addApiResponse (String.valueOf (code), new ApiResponse ().description (status.getMessage ())
				                                                                   .content (new Content ().addMediaType (
						                                                                   org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
						                                                                   new MediaType ().schema (commonResponse))));
			}
			return operation;
		};
	}
}
