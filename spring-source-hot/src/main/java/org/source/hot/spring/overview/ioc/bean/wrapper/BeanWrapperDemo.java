package org.source.hot.spring.overview.ioc.bean.wrapper;

import java.beans.PropertyDescriptor;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;

/**
 *
 *
 * @author huifer
 */
public class BeanWrapperDemo {

	public static final String d = "";

	public static void main(String[] args) {

		BeanWrapperImpl beanWrapperImpl = new BeanWrapperImpl(People.class);
		beanWrapperImpl.setAutoGrowNestedPaths(true);

		beanWrapperImpl.setAutoGrowNestedPaths(true);
//		beanWrapperImpl.setPropertyValue("name", "张三");
//		beanWrapperImpl.setPropertyValue("addresses[0].name", "a1");
		beanWrapperImpl.setPropertyValue("addresses[1].name", "a2");
		beanWrapperImpl.setPropertyValue("addresses[1].id", 2);
//		beanWrapperImpl.setPropertyValue("addresses[2]", new Address("bbbbbb"));
//		beanWrapperImpl.setPropertyValue("address.name", "ads");
		beanWrapperImpl.setPropertyValue("map['t1']", "zzzzzzzzzz");
		PropertyDescriptor[] propertyDescriptors = beanWrapperImpl.getPropertyDescriptors();
		System.out.println();
	}

	private static class People {
		public Address[] addresses;

		public Address address;

		private Map<String, Address> map;

		private String name;

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public Map<String, Address> getMap() {
			return map;
		}

		public void setMap(Map<String, Address> map) {
			this.map = map;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Address[] getAddresses() {
			return addresses;
		}

		public void setAddresses(Address[] addresses) {
			this.addresses = addresses;
		}
	}

	private static class Address {
		public String name;

		private Integer id;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Address() {
		}

		public Address(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
