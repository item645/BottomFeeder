package io.bottomfeeder.util;

import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helper service providing a simple way to execute arbitrary code in a context of transaction.
 * Useful when you need to call {@code @Transactional}-annotated method from same class
 * or execute a non-public method inside transaction.
 */
@Service
public class TransactionalRunner {

	@Transactional
	public void run(Runnable runnable) {
		runnable.run();
	}
	
	@Transactional
	public <T> T call(Supplier<T> supplier) {
		return supplier.get();
	}
}
