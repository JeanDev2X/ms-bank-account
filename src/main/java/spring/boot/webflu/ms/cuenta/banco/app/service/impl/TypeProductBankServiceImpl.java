package spring.boot.webflu.ms.cuenta.banco.app.service.impl;

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
	
	@Override
	public Flux<TypeProductBank> findAllTipoproducto()
	{
	return tipoProductoDao.findAll();
	
	}
	@Override
	public Mono<TypeProductBank> findByIdTipoProducto(String id)
	{
		System.out.println("ID_TIPE-->" + id);
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
