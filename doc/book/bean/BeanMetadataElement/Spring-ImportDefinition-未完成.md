# Spring ImportDefinition 

- `<import>` 标签定义

```xml
<xsd:element name="import">
   <xsd:annotation>
      <xsd:documentation source="java:org.springframework.core.io.Resource"><![CDATA[
Specifies an XML bean definition resource to import.
      ]]></xsd:documentation>
   </xsd:annotation>
   <xsd:complexType>
      <xsd:complexContent>
         <xsd:restriction base="xsd:anyType">
            <xsd:attribute name="resource" type="xsd:string" use="required">
               <xsd:annotation>
                  <xsd:documentation><![CDATA[
The relative resource location of the XML (bean definition) file to import,
for example "myImport.xml" or "includes/myImport.xml" or "../myImport.xml".
                  ]]></xsd:documentation>
               </xsd:annotation>
            </xsd:attribute>
         </xsd:restriction>
      </xsd:complexContent>
   </xsd:complexType>
</xsd:element>
```



```java
public class ImportDefinition implements BeanMetadataElement {

   /**
    * import 标签的resource 值
    */
   private final String importedResource;

   /**
    * 资源数组
    */
   @Nullable
   private final Resource[] actualResources;

   /**
    * 源
    */
   @Nullable
   private final Object source;
}
```