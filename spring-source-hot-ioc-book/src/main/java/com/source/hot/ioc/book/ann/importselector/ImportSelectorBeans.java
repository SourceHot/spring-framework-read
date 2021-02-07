package com.source.hot.ioc.book.ann.importselector;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import(
		value = {
				MyDeferredImportSelector.class,
				MyImportBeanDefinitionRegistrar.class,
				MyImportSelector.class
		}
)
@Configuration
public class ImportSelectorBeans {

}
