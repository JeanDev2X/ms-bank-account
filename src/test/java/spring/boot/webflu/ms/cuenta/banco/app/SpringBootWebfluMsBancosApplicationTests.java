package spring.boot.webflu.ms.cuenta.banco.app;

import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.service.ProductBankService;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluMsBancosApplicationTests {
	
	@Autowired
	private ProductBankService productoService;
	
	@Autowired
	private WebTestClient client;

	@Test
	void contextLoads() {
	}
	
	@Test
	public void cantiadCuentaBanco() {
		client.get().uri("/api/ProductBank")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk() 
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(ProductBank.class)
		.hasSize(8);
	}

	@Test
	public void listarCuentaBanco() {
		client.get().uri("/api/ProductBank")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk() 
		.expectHeader().contentType(MediaType.APPLICATION_JSON) //.hasSize(2);
		.expectBodyList(ProductBank.class).consumeWith(response -> {
			
			List<ProductBank> cuentaBanco = response.getResponseBody();
			
			cuentaBanco.forEach(p -> {
				System.out.println(p.getNumeroCuenta());
			});
			
			Assertions.assertThat(cuentaBanco.size() > 0).isTrue();
		});
	}
	
	@Test
	void crearCuentaBanco() {
		
		TypeProductBank ahorro = new TypeProductBank("1","personal");		
		ProductBank ctBanco = new ProductBank("47305710","900033","963791433","4557880460332733",ahorro,10000.0,"bcp");	
		
		client.post()
		.uri("/api/ProductBank/guardarProductoBanco")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(ctBanco), ProductBank.class)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.dni").isEqualTo("47305710")
		.jsonPath("$.numeroCuenta").isEqualTo("900033");
	}
	
//	@Test
//	void crearCuentaBancoValidado() {
//		
//		TypeProductBank ahorro = new TypeProductBank("1","personal");		
//		ProductBank ctBanco = new ProductBank("47305710","900033","963791433","4557880460332733",ahorro,10000.0,"bcp");	
//		
//		client.post()
//		.uri("/api/ProductBank")
//		.contentType(MediaType.APPLICATION_JSON)
//		.accept(MediaType.APPLICATION_JSON)
//		.body(Mono.just(ctBanco), ProductBank.class)
//		.exchange()
//		.expectStatus().isOk()
//		.expectHeader().contentType(MediaType.APPLICATION_JSON)
//		.expectBody()
//		.jsonPath("$.dni").isEqualTo("47305710")
//		.jsonPath("$.numeroCuenta").isEqualTo("900033");
//	}
	
	@Test
	void eliminarAccount() {
		ProductBank prod = productoService.findAllProductoByDniCliente("47305710").blockFirst();
		client.delete()
		.uri("/api/ProductBank/{id}",Collections.singletonMap("id",prod.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody()
		.isEmpty();
		
		client.get()
		.uri("/api/ProductBank/{id}",Collections.singletonMap("id",prod.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody()
		.isEmpty();
		
	}
	
}