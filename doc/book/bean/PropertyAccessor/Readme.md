# Spring PropertyAccessor 阅读指南
本节围绕`PropertyAccessor`接口进行展开, 重点介绍接口的作用及其实现类. 下面是`PropertyAccessor`的类图
![PropertyAccessor](./images/PropertyAccessor.png)

- 围绕着类图将进行下面这些类的分析
    - PropertyAccessor
        - ConfigurablePropertyAccessor
            - BeanWrapper
                - BeanWrapperImpl
            - AbstractPropertyAccessor
                - AbstractNestablePropertyAccessor
                    - DirectFieldAccessor
                    - BeanWrapperImpl