package com.hzn.awsopensearch.exception;

import com.hzn.awsopensearch.dto.Response;
import com.hzn.awsopensearch.enums.Status;
import com.hzn.awsopensearch.util.BeanValidator;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
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
	public ResponseEntity<Response<Void>> handleAwsOpensearchException (AwsOpensearchException e) {
		return ResponseEntity.ok (Response.of (e.getCode (), e.getMessage ()));
	}

	@ExceptionHandler (value = BindException.class)
	public ResponseEntity<Response<Map<String, String>>> handleBindException (BindException e) {
		return ResponseEntity.ok (Response.of (Status.FAIL, BeanValidator.getErrorMap (e.getBindingResult ())));
	}

	@ExceptionHandler (value = Exception.class)
	public ResponseEntity<Response<Void>> handleException (Exception e) {
		return ResponseEntity.ok (Response.of (Status.FAIL.getCode (), e.getMessage ()));
	}
}
