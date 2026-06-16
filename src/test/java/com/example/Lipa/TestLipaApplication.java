package com.example.Lipa;

import org.springframework.boot.SpringApplication;

public class TestLipaApplication {

	public static void main(String[] args) {
		SpringApplication.from(LipaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
