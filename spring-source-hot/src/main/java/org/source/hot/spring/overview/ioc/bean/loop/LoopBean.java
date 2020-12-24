package org.source.hot.spring.overview.ioc.bean.loop;

import java.io.File;

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
//		traverseFolder("D:\\desktop\\git_repo\\spring-ebk\\spring-framework-read\\doc\\book");
	}


	public static  void traverseFolder(String path) {

		File file = new File(path);
		if (file.exists()) {
			File[] files = file.listFiles();
			if (null == files || files.length == 0) {
				return;
			} else {
				for (File file2 : files) {
					if (file2.isDirectory()) {
						traverseFolder(file2.getAbsolutePath());
					} else {
//						System.out.println("文件:" + file2.getAbsolutePath());
						if (file2.getName().contains("未完成")) {
							String name = file2.getName();
							System.out.println(name);
						}
					}
				}
			}
		} else {
			System.out.println("文件不存在!");
		}
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
