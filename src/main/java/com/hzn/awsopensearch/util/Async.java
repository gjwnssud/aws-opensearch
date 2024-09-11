package com.hzn.awsopensearch.util;

import com.hzn.awsopensearch.exception.AwsOpensearchException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>non-blocking, async util</p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
@Slf4j
public class Async {
	private static final ExecutorService executor = Executors.newFixedThreadPool (Runtime.getRuntime ().availableProcessors ());

	static {
		Runtime.getRuntime ().addShutdownHook (new Thread (Async::shutdown));
	}

	private Async () {
	}

	public static <T> CompletableFuture<T> supplyAsync (Callable<T> callable) {
		CompletableFuture<T> result = new CompletableFuture<> ();
		CompletableFuture.runAsync (() -> {
			try {
				result.complete (callable.call ());
			} catch (Throwable t) {
				result.completeExceptionally (t);
			}
		}, executor);
		return result;
	}

	public static void runAsync (Runnable runnable) {
		CompletableFuture.runAsync (runnable, executor).exceptionally (t -> {
			if (t instanceof AwsOpensearchException) {
				throw (AwsOpensearchException) t;
			} else {
				throw new AwsOpensearchException (ExceptionLog.getMessage (t));
			}
		});
	}

	public static void runAsync (Runnable runnable, Consumer<Void> callback) {
		CompletableFuture.runAsync (runnable, executor).exceptionally (t -> {
			if (t instanceof AwsOpensearchException) {
				throw (AwsOpensearchException) t;
			} else {
				throw new AwsOpensearchException (ExceptionLog.getMessage (t));
			}
		}).thenAccept (callback);
	}

	public static <T> void runAsync (Callable<T> callable) {
		runAsync (callable, null, null);
	}

	public static <T> void runAsync (Callable<T> callable, Consumer<T> callback) {
		runAsync (callable, null, callback);
	}

	public static <T> void runAsync (Callable<T> callable, Function<Throwable, T> exceptionally, Consumer<T> callback) {
		CompletableFuture<T> result = new CompletableFuture<> ();

		if (exceptionally != null) {
			result.exceptionally (exceptionally);
		}

		if (callback != null) {
			result.thenAccept (callback);
		}

		CompletableFuture.runAsync (() -> {
			try {
				result.complete (callable.call ());
			} catch (Throwable t) {
				result.completeExceptionally (t);
			}
		}, executor);
	}

	private static void shutdown () {
		executor.shutdown ();
		try {
			if (!executor.awaitTermination (60L, TimeUnit.SECONDS)) {
				executor.shutdownNow ();
				if (!executor.awaitTermination (60L, TimeUnit.SECONDS)) {
					log.error ("Thread pool did not terminate");
				}
			}
		} catch (InterruptedException e) {
			executor.shutdownNow ();
			Thread.currentThread ().interrupt ();
		}
	}
}
