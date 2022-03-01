package com.source.hot.ioc.book.ann;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan("com")
public class Xunhuan {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(Xunhuan.class);
		context.close();

	}

	@Component
	public class A {
		@Autowired
		public B b;

		public B getB() {
			return b;
		}

		public void setB(B b) {
			this.b = b;
		}
	}

	@Component
	public class B {
		@Autowired
		public A a;

		public A getA() {
			return a;
		}

		public void setA(A a) {
			this.a = a;
		}
	}
}
