package ru.molchanov.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankAppApplication {

	public static void main(String[] args) {
		Thread thread = new Thread(()->{
		System.out.println("ID ="+ Thread.currentThread().getId());
			System.out.println("ID ="+ Thread.currentThread().getId());
			System.out.println("ID ="+ Thread.currentThread().getName());
			System.out.println("ID ="+ Thread.currentThread().getPriority());
			System.out.println("ID ="+ Thread.currentThread().isDaemon());
		});

		thread.start();
	}

}
