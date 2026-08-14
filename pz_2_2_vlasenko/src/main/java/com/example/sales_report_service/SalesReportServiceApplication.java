package com.example.sales_report_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalesReportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesReportServiceApplication.class, args);
		System.out.println("SalesReportServiceApplication started");
	}

}
