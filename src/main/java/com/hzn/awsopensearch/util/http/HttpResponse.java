package com.hzn.awsopensearch.util.http;

import com.hzn.awsopensearch.enums.Status;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 9. 24.
 */
public class HttpResponse<T> {
	private final int code;
	private final String message;
	private final T data;

	private HttpResponse (int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public int getCode () {
		return code;
	}

	public String getMessage () {
		return message;
	}

	public T getData () {
		return data;
	}

	public static <T> HttpResponse<T> of (int code, String message) {
		return of (code, message, null);
	}

	public static <T> HttpResponse<T> of (T responseBody) {
		return of (Status.OK, responseBody);
	}

	public static <T> HttpResponse<T> of (Status status) {
		return of (status.getCode (), status.getMessage (), null);
	}

	public static <T> HttpResponse<T> of (Status status, T data) {
		return of (status.getCode (), status.getMessage (), data);
	}

	public static <T> HttpResponse<T> of (int code, String message, T data) {
		return new HttpResponse<> (code, message, data);
	}
}
