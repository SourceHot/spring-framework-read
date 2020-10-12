# Spring FormatterRegistry 
- 类全路径: `org.springframework.format.FormatterRegistry`

- 完整代码


```java
public interface FormatterRegistry extends ConverterRegistry {

	/**
	 * 添加输出接口
	 */
	void addPrinter(Printer<?> printer);

	/**
	 * 添加解析接口
	 */
	void addParser(Parser<?> parser);

	/**
	 * 添加格式化接口
	 */
	void addFormatter(Formatter<?> formatter);

	/**
	 * 添加类型对应的格式化接口
	 */
	void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter);

	/**
	 * 添加类型对应的输出接口和解析接口
	 */
	void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser);

	/**
	 * 添加注解格式化工厂
	 */
	void addFormatterForFieldAnnotation(AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory);

}
```