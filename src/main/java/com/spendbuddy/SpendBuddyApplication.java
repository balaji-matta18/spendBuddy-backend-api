package com.spendbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpendBuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpendBuddyApplication.class, args);
	}

}
