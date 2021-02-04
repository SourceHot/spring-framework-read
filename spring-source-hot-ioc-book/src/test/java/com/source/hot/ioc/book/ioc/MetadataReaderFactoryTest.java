package com.source.hot.ioc.book.ioc;

import java.io.IOException;

import com.source.hot.ioc.book.ann.AnnBeans;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class MetadataReaderFactoryTest {

	@Test
	void testSimpleMetadataReaderFactory() throws IOException {
		MetadataReaderFactory factory = new SimpleMetadataReaderFactory();
		MetadataReader metadataReader = factory.getMetadataReader(MetadataReaderFactoryTest.class.getName());
		System.out.println();
	}

	@Test
	void testInnerName() {
		String className = MetadataReaderFactoryTest.A.class.getName();
		int lastDotIndex = className.lastIndexOf('.');
		String innerClassName =
				className.substring(0, lastDotIndex) + '$' + className.substring(lastDotIndex + 1);
		String innerClassResourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath(innerClassName) + ClassUtils.CLASS_FILE_SUFFIX;
		System.out.println(innerClassResourcePath);
	}

	@Test
	void me() throws IOException {
		MetadataReaderFactory factory = new SimpleMetadataReaderFactory();
		MetadataReader metadataReader = factory.getMetadataReader(AnnBeans.class.getName());
		System.out.println();
	}

	class A {

	}
}
