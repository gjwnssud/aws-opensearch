package com.hzn.awsopensearch.aop;

import com.hzn.awsopensearch.util.ExceptionLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * <p></p>
 *
 * @author hzn
 * @date 2024. 8. 14.
 */
@Component
@Aspect
@Slf4j
@Order (Integer.MIN_VALUE)
public class ServiceLog {

	@Around ("execution(public * com.hzn.awsopensearch..service..*(..))")
	public Object serviceLog (ProceedingJoinPoint point) throws Throwable {
		Signature signature = point.getSignature ();
		Logger logger = LoggerFactory.getLogger (signature.getDeclaringTypeName ());
		String className = signature.getDeclaringType ().getSimpleName ();
		String methodName = signature.getName ();
		logger.info ("[{}.{}] start.", className, methodName);

		StopWatch stopWatch = new StopWatch ();
		stopWatch.start ();
		Object object;
		try {
			object = point.proceed ();
		} catch (Throwable t) {
			ExceptionLog.print (methodName, t, logger);
			throw t;
		} finally {
			stopWatch.stop ();
			logger.info ("[{}.{}] end. elapsed time : {} sec", className, methodName, (double) stopWatch.getTotalTimeMillis () / 1000);
		}

		return object;
	}
}
