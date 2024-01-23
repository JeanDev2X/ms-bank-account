package spring.boot.webflu.ms.cuenta.banco.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import reactor.core.publisher.Flux;
import spring.boot.webflu.ms.cuenta.banco.app.service.ProductBankService;
import spring.boot.webflu.ms.cuenta.banco.app.service.TypeProductBankService;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;

@SpringBootApplication
public class SpringBootWebfluMsCuentaBancoApplication implements CommandLineRunner{

	@Autowired
	private ProductBankService serviceProducto;
	
	@Autowired
	private TypeProductBankService serviceTipoProducto;
	
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluMsCuentaBancoApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluMsCuentaBancoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		mongoTemplate.dropCollection("ProductBank").subscribe();
		mongoTemplate.dropCollection("TypeProductBank").subscribe();
		
		TypeProductBank ahorro = new TypeProductBank("1","ahorro");
		TypeProductBank corriente = new TypeProductBank("2","corriente");
		TypeProductBank plazoFijo = new TypeProductBank("3","plazoFijo");
		
		//
		Flux.just(ahorro,corriente,plazoFijo)
		.flatMap(serviceTipoProducto::saveTipoProducto)
		.doOnNext(c -> {
			log.info("Tipo cliente creado: " +  c.getDescripcion() + ", Id: " + c.getId());
		}).thenMany(					
				Flux.just(
						
						new ProductBank("47305710","900001",ahorro,10000.0,"bcp"),
						new ProductBank("47305711","900003",corriente,30000.0,"bcp"),
						new ProductBank("07091424","900005",plazoFijo,50000.0,"bcp")
						)					
					.flatMap(producto -> {
						return serviceProducto.saveProductoBanco(producto);
					})					
				).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNumeroCuenta()));
		
		
	}

}
