package com.source.hot.ioc.book.ioc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ResourcePatternResolverTest {
	@Test
	void testResourcePatternResolver() throws IOException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		String basePackage = "com.source.hot.ioc.book.ioc";
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				resolveBasePackage(basePackage) + "/" + "**/*.class";
		Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
		System.out.println();
	}


	@Test
	void testGetResource() {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		String basePackage = resolveBasePackage("com.source.hot.ioc.book.ioc.ResourcePatternResolverTest.class");
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				basePackage;
		Resource resource = resourcePatternResolver.getResource(packageSearchPath);
		System.out.println();
	}

	@Test
	void testJarFile() throws IOException {
		File f = new File("D:\\jar_repo\\org\\springframework\\spring-core\\5.2.3.RELEASE\\spring-core-5.2.3.RELEASE.jar");
		JarFile jarFile = new JarFile(f);

		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
			JarEntry entry = entries.nextElement();
			String entryPath = entry.getName();
			if (match(entryPath)) {
				System.out.println(entryPath);
			}
		}
	}

	private boolean match(String entryPath) {
		return entryPath.endsWith(".class");
	}

	private String resolveBasePackage(String basePackage) {
		return basePackage.replace(".", "/");
	}
}
