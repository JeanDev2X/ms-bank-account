package spring.boot.webflu.ms.cuenta.banco.app.client;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaCreditoDto;

@Service
public class CreditoClient {

	private static final Logger log = LoggerFactory.getLogger(ClientClient.class);
	
	@Autowired
	@Qualifier("credito")
	private WebClient creditoClient;

	public Flux<CuentaCreditoDto> findByNumDoc(String dni) {
		log.debug("PRODUCTO DE CREDITO - findByNumDoc");
		log.debug("PRODUCTO DE CREDITO - findByNumDoc" + dni);
		return creditoClient.get()
				.uri("/dni/{dni}",Collections.singletonMap("dni",dni))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(CuentaCreditoDto.class);		    	
	}
	
}
