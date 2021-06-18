package com.github.source.hot.data.oxxm;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

public class SpringOXMApp {
	private static final String FILE_NAME = "Customer.xml";
	private Customer customer = new Customer();
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	public void saveSettings() throws IOException {
		try (FileOutputStream os = new FileOutputStream(FILE_NAME)) {
			this.marshaller.marshal(customer, new StreamResult(os));
		}
	}

	public void loadSettings() throws IOException {
		try (FileInputStream is = new FileInputStream(FILE_NAME)) {
			this.customer = (Customer) this.unmarshaller.unmarshal(new StreamSource(is));
		}
	}

	public static void main(String[] args) throws IOException {
		ApplicationContext appContext =
				new ClassPathXmlApplicationContext("spring-oxm.xml");
		SpringOXMApp application = appContext.getBean("application",SpringOXMApp.class);
		application.saveSettings();
//		application.loadSettings();
		System.out.println();
	}
}
