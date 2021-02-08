package com.source.hot.ioc.book.ann.imported;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ConfigurationA.class)
public class ImportedBeans {
}
