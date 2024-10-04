package com.hzn.awsopensearch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hzn.awsopensearch.enums.Status;
import com.hzn.awsopensearch.util.http.HttpResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Getter
@AllArgsConstructor
@JsonInclude (JsonInclude.Include.NON_NULL)
@Schema (title = "공통 응답 객체", name = "CommonResponse")
public class ResponseDto<T> {
	@Schema (title = "응답 코드", example = "200")
	private int code;
	@Schema (title = "응답 메시지", example = "success.")
	private String message;
	@Schema (title = "응답 데이터")
	private T data;

	public static <T> ResponseDto<T> of (int code, String message) {
		return of (code, message, null);
	}

	public static <T> ResponseDto<T> of (T data) {
		return of (Status.OK, data);
	}

	public static <T> ResponseDto<T> of (Status status) {
		return of (status.getCode (), status.getMessage (), null);
	}

	public static <T> ResponseDto<T> of (Status status, T data) {
		return of (status.getCode (), status.getMessage (), data);
	}

	public static <T> ResponseDto<T> of (int code, String message, T data) {
		return new ResponseDto<> (code, message, data);
	}

	public static <T> ResponseDto<T> from (HttpResponse<T> httpResponse) {
		return new ResponseDto<> (httpResponse.getCode (), httpResponse.getMessage (), httpResponse.getData ());
	}
}
