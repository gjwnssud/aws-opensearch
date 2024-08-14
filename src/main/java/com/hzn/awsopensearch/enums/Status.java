package com.hzn.awsopensearch.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Getter
@RequiredArgsConstructor
public enum Status {
	OK (200, "success."), BAD_REQUEST (400, "bad request."),

	NOT_FOUND (404, "not found."), INTERNAL_SERVER_ERROR (500, "internal server error."),

	FAIL (9999, "fail.");

	private final int code;
	private final String message;
}
