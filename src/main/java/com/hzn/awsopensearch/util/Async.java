package com.hzn.awsopensearch.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>non-blocking, async util</p>
 *
 * @author hzn
 * @date 2024. 9. 11.
 */
public class Async {
	private static final Logger log = LoggerFactory.getLogger (Async.class);
	private static final ExecutorService executor = Executors.newFixedThreadPool (Runtime.getRuntime ().availableProcessors ());

	static {
		Runtime.getRuntime ().addShutdownHook (new Thread (Async::shutdown));
	}

	private Async () {
	}

	public static <T> void run (Callable<T> callable) {
		run (callable, null, null);
	}

	public static <T> void run (Callable<T> callable, Consumer<T> callback) {
		run (callable, null, callback);
	}

	public static <T> void run (Callable<T> callable, Function<Throwable, T> exceptionally, Consumer<T> callback) {
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

	public static void scheduleWithFixedDelay (Callable<Boolean> condition, Runnable task, Runnable callback) {
		scheduleWithFixedDelay (condition, task, callback, 0, 1, TimeUnit.SECONDS);
	}

	public static void scheduleWithFixedDelay (Callable<Boolean> condition, Runnable task, Runnable callback, long initialDelay, long delay, TimeUnit unit) {
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor ();
		scheduledExecutor.scheduleWithFixedDelay (() -> {
			try {
				if (condition.call ()) {
					task.run ();
				} else {
					callback.run ();
					scheduledExecutor.shutdown ();
				}
			} catch (Throwable t) {
				callback.run ();
				scheduledExecutor.shutdown ();
				ExceptionLog.print (t, log);
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private static void shutdown () {
		executor.shutdown ();
		try {
			if (!executor.awaitTermination (60L, TimeUnit.SECONDS)) {
				executor.shutdownNow ();
				if (!executor.awaitTermination (60L, TimeUnit.SECONDS)) {
					log.error ("Executor Thread pool did not terminate");
				}
			}
		} catch (InterruptedException e) {
			executor.shutdownNow ();
			Thread.currentThread ().interrupt ();
			ExceptionLog.print (e, log);
		}
	}
}
