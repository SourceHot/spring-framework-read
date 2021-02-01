package com.source.hot.ioc.book.lifecycle;

import org.springframework.context.Lifecycle;

public class HelloLifeCycle implements Lifecycle {
    private volatile boolean running = false;
   
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