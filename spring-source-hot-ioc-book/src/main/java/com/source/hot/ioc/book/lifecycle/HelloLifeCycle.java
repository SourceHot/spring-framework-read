package com.source.hot.ioc.book.lifecycle;

import org.springframework.context.Lifecycle;
import org.springframework.context.LifecycleProcessor;

public class HelloLifeCycle implements LifecycleProcessor {
    private volatile boolean running = false;

	@Override
	public void onRefresh() {
		System.out.println("lifycycle onRefresh");

	}

	@Override
	public void onClose() {
		System.out.println("lifycycle onClose");
	}

	@Override
    public void start() {
        System.out.println("lifycycle start");
        running = true;

    }
   @Override
    public void stop() {
        System.out.println("lifycycle stop");
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}