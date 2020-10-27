# Spring DefaultFormattingConversionService

- 类全路径: `org.springframework.format.support.DefaultFormattingConversionService`



- `DefaultFormattingConversionService` 围绕`javax.money`和`org.joda.time.LocalDate` 开发

- 有关format对象
  - `CurrencyUnitFormatter`
  - `MonetaryAmountFormatter`
  - `Jsr354NumberFormatAnnotationFormatterFactory`



- 添加 jsr 354 中 金钱和时间的formatter对象



```java
public static void addDefaultFormatters(FormatterRegistry formatterRegistry) {
   // Default handling of number values
   formatterRegistry.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

   // Default handling of monetary values
   if (jsr354Present) {
      // 添加  jsr 354 中时间和金钱的format
      formatterRegistry.addFormatter(new CurrencyUnitFormatter());
      formatterRegistry.addFormatter(new MonetaryAmountFormatter());
      formatterRegistry.addFormatterForFieldAnnotation(new Jsr354NumberFormatAnnotationFormatterFactory());
   }

   // Default handling of date-time values

   // just handling JSR-310 specific date and time types
   // 追加 format register
   new DateTimeFormatterRegistrar().registerFormatters(formatterRegistry);

   if (jodaTimePresent) {
      // handles Joda-specific types as well as Date, Calendar, Long
      new JodaTimeFormatterRegistrar().registerFormatters(formatterRegistry);
   }
   else {
      // regular DateFormat-based Date, Calendar, Long converters
      new DateFormatterRegistrar().registerFormatters(formatterRegistry);
   }
}
```