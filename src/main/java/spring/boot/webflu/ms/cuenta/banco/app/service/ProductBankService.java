package spring.boot.webflu.ms.cuenta.banco.app.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;

public interface ProductBankService {

	Flux<ProductBank> findAllProductoBanco();	
	Mono<ProductBank> findByIdProductoBanco(String id); //findByIdProducto	
	Mono<ProductBank> saveProductoBanco(ProductBank producto); //saveProducto
	Mono<Void> deleteProductoBanco(ProductBank producto);
	//============
	Mono<ProductBank> retiro(Double monto, String numero_cuenta, Double comision, String codigo_bancario);
	Mono<ProductBank> depositos(Double monto, String numero_Cuenta, Double comision, String codigo_bancario);
	Flux<ProductBank> saveProductoBancoCliente(ProductBank producto);
	Mono<ProductBank> listProdNumTarj(String num, String codigo_bancario);
	Flux<ProductBank> findAllProductoByDniCliente(String dniCliente);
	
}
