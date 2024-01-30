package spring.boot.webflu.ms.cuenta.banco.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {
	
	@Bean
	@Qualifier("client")
	public WebClient registrarWebClient() {
		return WebClient.create("http://localhost:8020/api/Customer");
	}
	
	@Bean
	@Qualifier("credito")
	public WebClient registrarWebCreditClient() {
		return WebClient.create("http://localhost:8022/api/ProductCredit");
	}

}
