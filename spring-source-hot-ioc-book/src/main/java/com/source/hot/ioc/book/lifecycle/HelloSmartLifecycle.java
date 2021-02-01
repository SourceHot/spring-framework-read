package com.source.hot.ioc.book.lifecycle;

import org.springframework.context.SmartLifecycle;

public class HelloSmartLifecycle implements SmartLifecycle {
	@Override
	public boolean isAutoStartup() {
		return false;
	}

	@Override
	public void stop(Runnable callback) {
		System.out.println("com.source.hot.ioc.book.lifecycle.HelloSmartLifecycle.stop(java.lang.Runnable)");
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public void start() {
		System.out.println("com.source.hot.ioc.book.lifecycle.HelloSmartLifecycle.start");
	}

	@Override
	public void stop() {
		System.out.println("com.source.hot.ioc.book.lifecycle.HelloSmartLifecycle.stop()");
	}

	@Override
	public boolean isRunning() {
		return false;
	}
}
