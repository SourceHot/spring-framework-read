# Spring BeanDefinitionParserDelegate

- 全路径`org.springframework.beans.factory.xml.BeanDefinitionParserDelegate`
- 解析 xml 中标签的委托类



- 在这个类中定义常量如下，为后续解析提供帮助

```java
	public static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";	

	public static final String TRUE_VALUE = "true";

	public static final String FALSE_VALUE = "false";

	public static final String DEFAULT_VALUE = "default";

	public static final String DESCRIPTION_ELEMENT = "description";

	public static final String AUTOWIRE_NO_VALUE = "no";

	public static final String AUTOWIRE_BY_NAME_VALUE = "byName";

	public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";

	public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";

	public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String BEAN_ELEMENT = "bean";

	public static final String META_ELEMENT = "meta";

	public static final String ID_ATTRIBUTE = "id";

	public static final String PARENT_ATTRIBUTE = "parent";

	public static final String CLASS_ATTRIBUTE = "class";

	public static final String ABSTRACT_ATTRIBUTE = "abstract";

	public static final String SCOPE_ATTRIBUTE = "scope";

	public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";

	public static final String AUTOWIRE_ATTRIBUTE = "autowire";

	public static final String AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate";

	public static final String PRIMARY_ATTRIBUTE = "primary";

	public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";

	public static final String INIT_METHOD_ATTRIBUTE = "init-method";

	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

	public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";

	public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

	public static final String INDEX_ATTRIBUTE = "index";

	public static final String TYPE_ATTRIBUTE = "type";

	public static final String VALUE_TYPE_ATTRIBUTE = "value-type";

	public static final String KEY_TYPE_ATTRIBUTE = "key-type";

	public static final String PROPERTY_ELEMENT = "property";

	public static final String REF_ATTRIBUTE = "ref";

	public static final String VALUE_ATTRIBUTE = "value";

	public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

	public static final String REPLACED_METHOD_ELEMENT = "replaced-method";

	public static final String REPLACER_ATTRIBUTE = "replacer";

	public static final String ARG_TYPE_ELEMENT = "arg-type";

	public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

	public static final String REF_ELEMENT = "ref";

	public static final String IDREF_ELEMENT = "idref";

	public static final String BEAN_REF_ATTRIBUTE = "bean";

	public static final String PARENT_REF_ATTRIBUTE = "parent";

	public static final String VALUE_ELEMENT = "value";

	public static final String NULL_ELEMENT = "null";

	public static final String ARRAY_ELEMENT = "array";

	public static final String LIST_ELEMENT = "list";

	public static final String SET_ELEMENT = "set";

	public static final String MAP_ELEMENT = "map";

	public static final String ENTRY_ELEMENT = "entry";

	public static final String KEY_ELEMENT = "key";

	public static final String KEY_ATTRIBUTE = "key";

	public static final String KEY_REF_ATTRIBUTE = "key-ref";

	public static final String VALUE_REF_ATTRIBUTE = "value-ref";

	public static final String PROPS_ELEMENT = "props";

	public static final String PROP_ELEMENT = "prop";

	public static final String MERGE_ATTRIBUTE = "merge";

	public static final String QUALIFIER_ELEMENT = "qualifier";

	public static final String QUALIFIER_ATTRIBUTE_ELEMENT = "attribute";

	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";

	public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";

	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

	public static final String DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE = "default-autowire-candidates";

	public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";

	public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";

	private static final String SINGLETON_ATTRIBUTE = "singleton";
```







## populateDefaults

- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#populateDefaults`方法解析属性赋值给`DocumentDefaultsDefinition`对象



- 代码逻辑如下
  1. 读取属性
  2. 判断是否默认值
  3. 判断是否存在属性
  4. 赋值

```java
protected void populateDefaults(DocumentDefaultsDefinition defaults, @Nullable DocumentDefaultsDefinition parentDefaults, Element root) {
   // 获取 default-lazy-init 属性值
   String lazyInit = root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE);
   // 判断是否是默认值
   if (isDefaultValue(lazyInit)) {
      // Potentially inherited from outer <beans> sections, otherwise falling back to false.
      lazyInit = (parentDefaults != null ? parentDefaults.getLazyInit() : FALSE_VALUE);
   }
   defaults.setLazyInit(lazyInit);

   String merge = root.getAttribute(DEFAULT_MERGE_ATTRIBUTE);
   if (isDefaultValue(merge)) {
      // Potentially inherited from outer <beans> sections, otherwise falling back to false.
      merge = (parentDefaults != null ? parentDefaults.getMerge() : FALSE_VALUE);
   }
   defaults.setMerge(merge);

   String autowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
   if (isDefaultValue(autowire)) {
      // Potentially inherited from outer <beans> sections, otherwise falling back to 'no'.
      autowire = (parentDefaults != null ? parentDefaults.getAutowire() : AUTOWIRE_NO_VALUE);
   }
   defaults.setAutowire(autowire);

   if (root.hasAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE)) {
      defaults.setAutowireCandidates(root.getAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE));
   }
   else if (parentDefaults != null) {
      defaults.setAutowireCandidates(parentDefaults.getAutowireCandidates());
   }

   if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
      defaults.setInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
   }
   else if (parentDefaults != null) {
      defaults.setInitMethod(parentDefaults.getInitMethod());
   }

   if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
      defaults.setDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
   }
   else if (parentDefaults != null) {
      defaults.setDestroyMethod(parentDefaults.getDestroyMethod());
   }

   defaults.setSource(this.readerContext.extractSource(root));
}
```



### DocumentDefaultsDefinition

- 全路径:`org.springframework.beans.factory.xml.DocumentDefaultsDefinition`
- 下面放出类的属性标记

```java
public class DocumentDefaultsDefinition implements DefaultsDefinition {

   /**
    * true or false
    */
   @Nullable
   private String lazyInit;

   /**
    * true or false
    */
   @Nullable
   private String merge;

   /**
    * no or byName or byType
    */
   @Nullable
   private String autowire;

   /**
    * default-autowire-candidates 属性值
    */
   @Nullable
   private String autowireCandidates;

   /**
    * 实例化方法
    */
   @Nullable
   private String initMethod;

   /**
    * 摧毁方法
    */
   @Nullable
   private String destroyMethod;

   @Nullable
   private Object source;
}
```







## checkNameUniqueness

- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#checkNameUniqueness`

- 判断 beanName 是否被使用, bean 别名是否被使用

```java
/**
 * Validate that the specified bean name and aliases have not been used already
 * within the current level of beans element nesting.
 *
 * 判断 beanName 是否被使用, bean 别名是否被使用
 */
protected void checkNameUniqueness(String beanName, List<String> aliases, Element beanElement) {
   // 当前寻找的name
   String foundName = null;

   // 是否有 beanName
   // 使用过的name中是否存在
   if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
      foundName = beanName;
   }
   if (foundName == null) {
      // 寻找匹配的第一个
      foundName = CollectionUtils.findFirstMatch(this.usedNames, aliases);
   }
   // 抛出异常
   if (foundName != null) {
      error("Bean name '" + foundName + "' is already used in this <beans> element", beanElement);
   }

   // 加入使用队列
   this.usedNames.add(beanName);
   this.usedNames.addAll(aliases);
}
```





## createBeanDefinition

- `org.springframework.beans.factory.support.BeanDefinitionReaderUtils#createBeanDefinition`
- 创建具有基本信息的**BeanDefinition**
  1. parent bean name 
  2. bean clsss
  3. bean class name 

```java
public static AbstractBeanDefinition createBeanDefinition(
      @Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {

   GenericBeanDefinition bd = new GenericBeanDefinition();
   // 设置 父bean
   bd.setParentName(parentName);
   if (className != null) {
      if (classLoader != null) {
         // 设置 class
         // 内部是通过反射创建 class
         bd.setBeanClass(ClassUtils.forName(className, classLoader));
      }
      else {
         // 设置 class name
         bd.setBeanClassName(className);
      }
   }
   return bd;
}
```





## parseBeanDefinitionElement

- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseBeanDefinitionElement(org.w3c.dom.Element, org.springframework.beans.factory.config.BeanDefinition)`
- 该方法用来解析 `<bean/>` 标签信息







## 	

- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseBeanDefinitionElement(org.w3c.dom.Element, java.lang.String, org.springframework.beans.factory.config.BeanDefinition)`





```java
@Nullable
public AbstractBeanDefinition parseBeanDefinitionElement(
      Element ele, String beanName, @Nullable BeanDefinition containingBean) {

   this.parseState.push(new BeanEntry(beanName));

   String className = null;
   // 是否包含属性 class
   if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
      className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
   }
   String parent = null;
   // 是否包含属性 parent
   if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
      parent = ele.getAttribute(PARENT_ATTRIBUTE);
   }

   try {
      // 创建 bean definition
      AbstractBeanDefinition bd = createBeanDefinition(className, parent);

      // bean definition 属性设置
      parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
      bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));
      // 元信息设置
      parseMetaElements(ele, bd);
      // lookup-override 标签解析
      parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
      // replaced-method sub-elements 标签解析
      parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

      // constructor arg 标签解析
      parseConstructorArgElements(ele, bd);
      // property 标签解析
      parsePropertyElements(ele, bd);
      // qualifier 标签解析
      parseQualifierElements(ele, bd);
      // 资源设置
      bd.setResource(this.readerContext.getResource());
      // source 设置
      bd.setSource(extractSource(ele));

      return bd;
   }
   catch (ClassNotFoundException ex) {
      error("Bean class [" + className + "] not found", ele, ex);
   }
   catch (NoClassDefFoundError err) {
      error("Class that bean class [" + className + "] depends on not found", ele, err);
   }
   catch (Throwable ex) {
      error("Unexpected failure during bean definition parsing", ele, ex);
   }
   finally {
      this.parseState.pop();
   }

   return null;
}
```

### parseBeanDefinitionAttributes

- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseBeanDefinitionAttributes`



- 将 xml 标签的数据读取到内存中设置给`AbstractBeanDefinition`



```JAVA
public AbstractBeanDefinition parseBeanDefinitionAttributes(Element ele, String beanName,
      @Nullable BeanDefinition containingBean, AbstractBeanDefinition bd) {

   // 是否存在 singleton 属性
   if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
      error("Old 1.x 'singleton' attribute in use - upgrade to 'scope' declaration", ele);
   }
   // 是否存在 scope 属性
   else if (ele.hasAttribute(SCOPE_ATTRIBUTE)) {
      // 设置 scope 属性
      bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE));
   }
   // bean 定义是否为空
   else if (containingBean != null) {
      // Take default from containing bean in case of an inner bean definition.
      // 设置 bean definition 中的 scope
      bd.setScope(containingBean.getScope());
   }

   // 是否存在 abstract 属性
   if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
      // 设置 abstract 属性
      bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
   }

   // 获取 lazy-init 属性
   String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
   // 是否是默认的 lazy-init 属性
   if (isDefaultValue(lazyInit)) {
      // 获取默认值
      lazyInit = this.defaults.getLazyInit();
   }
   // 设置 lazy-init 属性
   bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

   // 获取注入方式
   String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
   // 设置注入方式
   bd.setAutowireMode(getAutowireMode(autowire));

   // 依赖的bean
   if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
      String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
      bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, MULTI_VALUE_ATTRIBUTE_DELIMITERS));
   }

   // autowire-candidate 是否自动注入判断
   String autowireCandidate = ele.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE);
   if (isDefaultValue(autowireCandidate)) {
      String candidatePattern = this.defaults.getAutowireCandidates();
      if (candidatePattern != null) {
         String[] patterns = StringUtils.commaDelimitedListToStringArray(candidatePattern);
         // * 匹配 设置数据
         bd.setAutowireCandidate(PatternMatchUtils.simpleMatch(patterns, beanName));
      }
   }
   else {
      bd.setAutowireCandidate(TRUE_VALUE.equals(autowireCandidate));
   }

   // 获取 primary 书信
   if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
      bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
   }

   // 获取 init-method 属性
   if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
      String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
      bd.setInitMethodName(initMethodName);
   }
   // 没有 init-method 的情况处理
   else if (this.defaults.getInitMethod() != null) {
      bd.setInitMethodName(this.defaults.getInitMethod());
      bd.setEnforceInitMethod(false);
   }

   // 获取 destroy-method 属性
   if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
      String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
      bd.setDestroyMethodName(destroyMethodName);
   }
   // 没有 destroy-method 的情况处理
   else if (this.defaults.getDestroyMethod() != null) {
      bd.setDestroyMethodName(this.defaults.getDestroyMethod());
      bd.setEnforceDestroyMethod(false);
   }

   // 获取 factory-method 属性
   if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
      bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
   }
   // 获取 factory-bean 属性
   if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
      bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
   }

   return bd;
}
```





### parseMetaElements



- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseMetaElements`

- 设置元数据. 

  标签`meta`的解析

```java
public void parseMetaElements(Element ele, BeanMetadataAttributeAccessor attributeAccessor) {
   // 获取下级标签
   NodeList nl = ele.getChildNodes();
   // 循环子标签
   for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      // 设置数据
      if (isCandidateElement(node) && nodeNameEquals(node, META_ELEMENT)) {
         Element metaElement = (Element) node;
         // 获取 key 属性
         String key = metaElement.getAttribute(KEY_ATTRIBUTE);
         // 获取 value 属性
         String value = metaElement.getAttribute(VALUE_ATTRIBUTE);
         // 元数据对象设置
         BeanMetadataAttribute attribute = new BeanMetadataAttribute(key, value);
         // 设置 source
         attribute.setSource(extractSource(metaElement));
         // 信息添加
         attributeAccessor.addMetadataAttribute(attribute);
      }
   }
}
```



使用案例

```xml
	<bean id="apple" class="org.source.hot.spring.overview.ioc.bean.lookup.Apple">
		<meta key="meta-key" value="meta-value"/>
	</bean>
```



```java
ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/beans/spring-lookup-method.xml");

ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
BeanDefinition apple = beanFactory.getBeanDefinition("apple");
Object attribute = apple.getAttribute("meta-key");
System.out.println(attribute);
```



### parseLookupOverrideSubElements

- `org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseLookupOverrideSubElements`

- 解析标签

  `lookup-method`



使用案例

```xml
<bean id="apple" class="org.source.hot.spring.overview.ioc.bean.lookup.Apple">
		<meta key="meta-key" value="meta-value"/>
	</bean>

	<bean id="shop" class="org.source.hot.spring.overview.ioc.bean.lookup.Shop">
		<lookup-method name="getFruits" bean="apple"/>
	</bean>

```

```java
public class LookupMain {
   public static void main(String[] args) {
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/beans/spring-lookup-method.xml");
      Shop shop = context.getBean("shop", Shop.class);
      System.out.println(shop.getFruits().getName());
   }
}
```