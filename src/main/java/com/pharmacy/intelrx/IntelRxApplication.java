package com.pharmacy.intelrx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class IntelRxApplication {

	public static void main(String[] args) {
		loadEnvironmentVariables();
		SpringApplication.run(IntelRxApplication.class, args);

	}

	private static void loadEnvironmentVariables() {
		//Dotenv dotenv = Dotenv.load();
		Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
		if (dotenv.entries().isEmpty()) {
			// Fallback to the production .env file if the default one is empty
			dotenv = Dotenv.configure()
					.directory("/var/www/html/dev") // specify the directory for production
					.load();
		}
		String[] envVariables = {
				"SERVER_PORT",
				"AWS_ACCESS_KEY",
				"AWS_SECRET_KEY",
				"AWS_BUCKET_NAME",
				"DATABASE_URL",
				"DATABASE_USERNAME",
				"DATABASE_PASSWORD",
				"JWT_SECRET_KEY",
				"JWT_EXPIRATION",
				"JWT_REFRESH_TOKEN_EXPIRATION",
				"INTELRX_URL",
				"OPENAI_MODEL",
				"OPENAI_API_URL",
				"OPENAI_API_KEY",
				"TERMII_API_URL",
				"TERMII_API_KEY",
				"TERMII_SMS_FROM",
				"EMAIL_URL",
				"EMAIL_USERNAME",
				"EMAIL_PASSWORD",
				"ADMIN_EMAIL",
				"ADMIN_PASSWORD"
		};

		for (String envVar : envVariables) {
			String value = dotenv.get(envVar);
			if (value != null) {
				System.setProperty(envVar, value);
			} else {
				System.out.println("Environment variable " + envVar + " is not set in .env");
			}
		}

	}


}
