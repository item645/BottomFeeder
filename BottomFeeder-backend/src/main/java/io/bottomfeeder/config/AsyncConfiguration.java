package io.bottomfeeder.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async/scheduling configuration.
 */
@Configuration
@EnableScheduling
@EnableAsync
class AsyncConfiguration implements AsyncConfigurer {

	private final Environment environment;
	
	public AsyncConfiguration(Environment environment) {
		this.environment = environment;
	}


	@Override
	@Bean(name = "taskExecutor")
	public ThreadPoolTaskExecutor getAsyncExecutor() {
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(environment.getRequiredProperty("bf.async.core-pool-size", Integer.class));
		executor.setMaxPoolSize(environment.getRequiredProperty("bf.async.max-pool-size", Integer.class));
		executor.setThreadNamePrefix(environment.getProperty("bf.async.thread-name-prefix", "BF-Async-Executor-"));
		executor.setDaemon(true);
		return executor;
	}

	
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
	
}
