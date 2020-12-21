# Spring PropertiesLoaderUtils
- 类全路径: `org.springframework.core.io.support.PropertiesLoaderUtils`


## loadAllProperties
- 方法签名: `org.springframework.core.io.support.PropertiesLoaderUtils.loadAllProperties(java.lang.String, java.lang.ClassLoader)`
- 方法作用: 加载资源文件转换成 properties 对象

```java
	public static Properties loadAllProperties(String resourceName, @Nullable ClassLoader classLoader) throws IOException {
		Assert.notNull(resourceName, "Resource name must not be null");
		// 类加载器
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = ClassUtils.getDefaultClassLoader();
		}
		// 资源信息列表
		Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
				ClassLoader.getSystemResources(resourceName));
		// 存储资源信息的容器
		Properties props = new Properties();
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			URLConnection con = url.openConnection();
			ResourceUtils.useCachesIfNecessary(con);
			InputStream is = con.getInputStream();
			try {
				// 是否是 xml 文件
				if (resourceName.endsWith(XML_FILE_EXTENSION)) {
					props.loadFromXML(is);
				}
				else {
					props.load(is);
				}
			}
			finally {
				is.close();
			}
		}
		return props;
	}

```