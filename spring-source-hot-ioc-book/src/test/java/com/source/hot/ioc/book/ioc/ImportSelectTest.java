package com.source.hot.ioc.book.ioc;

import com.source.hot.ioc.book.ann.importselector.ImportSelectorBeans;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ImportSelectTest {
	@Test
	void testImportSelect() {
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(ImportSelectorBeans.class);
	}
}
