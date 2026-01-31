package com.novatech.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NovatechAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(NovatechAgentApplication.class, args);

		SpringApplication.run(NovatechAgentApplication.class, args);
		System.out.println("✅ NovaTech Knowledge Agent is running!");
		System.out.println("📚 It will automatically load your PDFs from ./novatech-kb");
		System.out.println("🌐 API available at: http://localhost:8080/api/ask");

	}

}
