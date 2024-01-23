package spring.boot.webflu.ms.cuenta.banco.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.dao.ProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.dao.TypeProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.service.ProductBankService;
import spring.boot.webflu.ms.cuenta.banco.app.service.TypeProductBankService;

@Service
public class ProductBankServiceImpl implements ProductBankService {

	private static final Logger log = LoggerFactory.getLogger(ProductBankServiceImpl.class);
	
	@Autowired
	public ProductBankDao productoDao;

	@Autowired
	public TypeProductBankDao tipoProductoDao;
	
	@Override
	public Flux<ProductBank> findAllProductoBanco() {
		return productoDao.findAll();

	}

	@Override
	public Mono<ProductBank> findByIdProductoBanco(String id) {
		return productoDao.findById(id);

	}
	
	@Override
	public Mono<ProductBank> saveProductoBanco(ProductBank producto) {		
		return productoDao.save(producto);
	}
	
	@Override
	public Mono<Void> deleteProductoBanco(ProductBank producto) {
		return productoDao.delete(producto);
	}
	
	//--------------------------------------------*************************
	
}
