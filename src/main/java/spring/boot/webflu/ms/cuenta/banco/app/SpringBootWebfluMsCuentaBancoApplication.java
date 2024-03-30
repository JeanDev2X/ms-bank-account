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
		TypeProductBank ahorroVip = new TypeProductBank("4","ahorroVip");
		TypeProductBank ahorroPyme = new TypeProductBank("5","ahorroPyme");
		
		//
		Flux.just(ahorro,corriente,plazoFijo,ahorroVip,ahorroPyme)
		.flatMap(serviceTipoProducto::saveTipoProducto)
		.doOnNext(c -> {
			log.info("Tipo producto creado: " +  c.getDescripcion() + ", Id: " + c.getId());
		}).thenMany(					
				Flux.just(
						
						new ProductBank("47305710","900001","963791402","4557880460332750",ahorro,10000.0,"bcp"),
						new ProductBank("47305711","900003","","4557880460330001",corriente,30000.0,"bcp"),
						new ProductBank("47305711","900044","","4557880460330044",corriente,80000.0,"bcp"),
						new ProductBank("47305712","900005","963791420","4557880460338888",plazoFijo,50000.0,"bcp"),
						new ProductBank("47305712","900055","","4557880460338855",plazoFijo,9000.0,"bcp"),
						new ProductBank("47305713","900006","963791445","4557880460334500",ahorroVip,50000.0,"bcp"),
						new ProductBank("99091450","900099","","4557880460339001",ahorroPyme,45000.0,"bcp"),
						new ProductBank("99091440","900007","","4557880460339000",ahorroPyme,50000.0,"bcp")
						)					
					.flatMap(producto -> {
						return serviceProducto.saveProductoBanco(producto);
					})					
				).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNumeroCuenta()));
		
		
	}

}
