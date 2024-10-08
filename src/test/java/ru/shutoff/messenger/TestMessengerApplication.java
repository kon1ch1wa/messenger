package ru.shutoff.messenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestMessengerApplication {

	public static void main(String[] args) {
		SpringApplication.from(MessengerApplication::main).with(TestMessengerApplication.class).run(args);
	}

}
