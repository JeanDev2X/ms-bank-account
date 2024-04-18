package spring.boot.webflu.ms.cuenta.banco.app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.dao.TypeProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.service.TypeProductBankService;


@Service
public class TypeProductBankServiceImpl implements TypeProductBankService{

	
	@Autowired
	public TypeProductBankDao  tipoProductoDao;
	
	private static final Logger log = LoggerFactory.getLogger(TypeProductBankServiceImpl.class);
	
	@Override
	public Flux<TypeProductBank> findAllTipoproducto()
	{
	return tipoProductoDao.findAll();
	
	}
	@Override
	public Mono<TypeProductBank> findByIdTipoProducto(String id)
	{		
		log.info("id["+id+"]");
		return tipoProductoDao.findById(id);
	}
	
	@Override
	public Mono<TypeProductBank> saveTipoProducto(TypeProductBank typeProductBank)
	{
		System.out.println("tipos producto [" + typeProductBank);
		return tipoProductoDao.save(typeProductBank);
	}
	
	@Override
	public Mono<Void> deleteTipo(TypeProductBank tipoProducto) {
		return tipoProductoDao.delete(tipoProducto);
	}
	
}
