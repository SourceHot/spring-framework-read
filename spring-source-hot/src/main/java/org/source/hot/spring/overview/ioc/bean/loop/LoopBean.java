package org.source.hot.spring.overview.ioc.bean.loop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author huifer
 */
@Component
public class LoopBean {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(LoopBean.class);
		B bean = ctx.getBean(B.class);
		System.out.println();
	}


	@Component(value = "a")
	public class A {

		@Autowired
		@Qualifier("b")
		private B b;

		public B getB() {
			return b;
		}

		public void setB(B b) {
			this.b = b;
		}
	}

	@Component(value = "b")
	public class B {
//		@Autowired
//		private B b;
//
//		public B getB() {
//			return b;
//		}
//
//		public void setB(B b) {
//			this.b = b;
//		}
		@Autowired
		@Qualifier("a")
		private A a;

		public A getA() {
			return a;
		}

		public void setA(A a) {
			this.a = a;
		}
	}
}
