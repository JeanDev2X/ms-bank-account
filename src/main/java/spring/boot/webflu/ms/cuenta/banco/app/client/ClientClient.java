package spring.boot.webflu.ms.cuenta.banco.app.client;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.dto.Client;

@Service
public class ClientClient {

	private static final Logger log = LoggerFactory.getLogger(Client.class);
	
	@Autowired
	@Qualifier("client")
	private WebClient clientClient;
	
	public Mono<Client> findByNumDoc(String dni) {
		log.debug("findByNumDoc - " + dni);
		System.out.println("findByNumDoc - " + dni);
		return clientClient.get()
				.uri("/dni/{dni}",Collections.singletonMap("dni",dni))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Client.class);
		    	
	}
	
}
