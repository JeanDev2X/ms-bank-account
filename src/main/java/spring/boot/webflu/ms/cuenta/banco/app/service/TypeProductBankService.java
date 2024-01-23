package spring.boot.webflu.ms.cuenta.banco.app.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;

public interface TypeProductBankService {
	
	Flux<TypeProductBank> findAllTipoproducto();
	Mono<TypeProductBank> findByIdTipoProducto(String id);
	Mono<TypeProductBank> saveTipoProducto(TypeProductBank tipoProducto);
	Mono<Void> deleteTipo(TypeProductBank tipoProducto);
	
}
