package spring.boot.webflu.ms.cuenta.banco.app.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;

public interface ProductBankDao extends ReactiveMongoRepository<ProductBank, String> {
	
	//BUSCA EL NUMERO DE CUENTA - TARGETA CON SU BANCO
	@Query("{ 'numeroCuenta' : ?0, 'codigoBanco': ?1}")
	Mono<ProductBank> viewNumCuenta(String numeroCuenta,String codigo_bancario);
		
	//busca por numero de documento y tipo de producto si ya esta registrado
	@Query("{ 'dni' : ?0 , 'tipoProducto.id' : ?1, 'codigoBanco': ?2 }")
	Flux<ProductBank> buscarPorDocTipoCuentaBanco(String dni, String idTipo, String codigo_bancario);
	
	Flux<ProductBank> findByDni(String dni);
		
}
