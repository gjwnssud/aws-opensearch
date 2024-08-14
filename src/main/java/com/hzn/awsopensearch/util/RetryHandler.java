package com.hzn.awsopensearch.util;

import java.util.concurrent.Callable;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
public class RetryHandler {
	private static final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial (() -> 0);

	public static void resetRetryCount () {
		retryCount.set (0);
	}

	public static <T> T retry (Callable<T> operation, int retryLimit) throws Exception {
		try {
			return operation.call ();
		} catch (Exception e) {
			retryCount.set (retryCount.get () + 1);
			if (retryCount.get () < retryLimit) {
				return retry (operation, retryLimit);
			} else {
				throw e;
			}
		} finally {
			retryCount.remove ();
		}
	}

	public static void retry (Runnable operation, int retryLimit) {
		try {
			operation.run ();
		} catch (Exception e) {
			retryCount.set (retryCount.get () + 1);
			if (retryCount.get () < retryLimit) {
				retry (operation, retryLimit);
			} else {
				throw e;
			}
		} finally {
			retryCount.remove ();
		}
	}
}
