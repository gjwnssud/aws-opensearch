package com.hzn.awsopensearch.exception;

import com.hzn.awsopensearch.enums.Status;
import lombok.Getter;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Getter
public class AwsOpensearchException extends RuntimeException {
	private final int code;

	public AwsOpensearchException (String message) {
		super (message);
		this.code = Status.FAIL.getCode ();
	}

	public AwsOpensearchException (Status status) {
		super (status.getMessage ());
		this.code = status.getCode ();
	}
}
