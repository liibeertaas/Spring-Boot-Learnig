package com.example.sales_report_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Вимикає StartupReportMailer під час тестів — інакше кожен запуск mvn test
// намагався б реально надіслати email через SMTP.
@SpringBootTest(properties = "app.startup-mail.enabled=false")
class SalesReportServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
