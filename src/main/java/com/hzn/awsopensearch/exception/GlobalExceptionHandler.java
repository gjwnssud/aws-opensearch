package com.hzn.awsopensearch.exception;

import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.util.RCH;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler (value = AwsOpensearchException.class)
	public ResponseEntity<Response<?>> handleAwsOpensearchException (AwsOpensearchException e) {
		return ResponseEntity.ok (Response.of (e.getCode (), e.getMessage ()));
	}

	@ExceptionHandler (value = Exception.class)
	public ResponseEntity<Response<?>> handleException (Exception e) {
		return ResponseEntity.ok (Response.of (RCH.getResponse ().getStatus (), e.getMessage ()));
	}
}
