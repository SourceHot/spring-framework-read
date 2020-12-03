# JavaBean
- 本节将介绍分析`java.beans`下的一些接口类的使用

- 首当其冲的第一个接口就是`BeanInfo`

```java
public interface BeanInfo {

    /**
     * 获取        
     */
    BeanDescriptor getBeanDescriptor();

    /**
     */
    EventSetDescriptor[] getEventSetDescriptors();

    /**
     */
    int getDefaultEventIndex();

    /**
     */
    PropertyDescriptor[] getPropertyDescriptors();

    /**
     */
    int getDefaultPropertyIndex();

    /**
     */
    MethodDescriptor[] getMethodDescriptors();

    /**
     */
    BeanInfo[] getAdditionalBeanInfo();

    /**
     */
    Image getIcon(int iconKind);

    /**
     * Constant to indicate a 16 x 16 color icon.
     */
    final static int ICON_COLOR_16x16 = 1;

    /**
     * Constant to indicate a 32 x 32 color icon.
     */
    final static int ICON_COLOR_32x32 = 2;

    /**
     * Constant to indicate a 16 x 16 monochrome icon.
     */
    final static int ICON_MONO_16x16 = 3;

    /**
     * Constant to indicate a 32 x 32 monochrome icon.
     */
    final static int ICON_MONO_32x32 = 4;
}
```