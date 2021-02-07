package com.source.hot.ioc.book.ann.importselector;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MyDeferredImportSelector implements DeferredImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		System.out.println("in MyDeferredImportSelector");
		return new String[0];
	}
}
