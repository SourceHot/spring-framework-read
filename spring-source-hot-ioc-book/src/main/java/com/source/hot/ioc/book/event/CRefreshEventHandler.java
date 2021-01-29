package com.source.hot.ioc.book.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

public class CRefreshEventHandler
   implements ApplicationListener<ContextRefreshedEvent>{

   public void onApplicationEvent(ContextRefreshedEvent event) {
      System.out.println("ContextRefreshedEvent Received");
   }
}