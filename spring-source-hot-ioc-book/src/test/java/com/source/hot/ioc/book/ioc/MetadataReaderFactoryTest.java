package com.source.hot.ioc.book.ioc;

import java.io.IOException;

import com.source.hot.ioc.book.ann.AnnBeans;
import org.junit.jupiter.api.Test;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

public class MetadataReaderFactoryTest {

	@Test
	void testSimpleMetadataReaderFactory() throws IOException {
		MetadataReaderFactory factory = new SimpleMetadataReaderFactory();
		MetadataReader metadataReader = factory.getMetadataReader(AnnBeans.class.getName());
		System.out.println();
	}
}
