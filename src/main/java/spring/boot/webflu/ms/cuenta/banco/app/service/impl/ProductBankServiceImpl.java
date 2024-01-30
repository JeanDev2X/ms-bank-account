package spring.boot.webflu.ms.cuenta.banco.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.client.ClientClient;
import spring.boot.webflu.ms.cuenta.banco.app.client.CreditoClient;
import spring.boot.webflu.ms.cuenta.banco.app.dao.ProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.dao.TypeProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.dto.Client;
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaCreditoDto;
import spring.boot.webflu.ms.cuenta.banco.app.service.ProductBankService;
import spring.boot.webflu.ms.cuenta.banco.app.exception.RequestException;

@Service
public class ProductBankServiceImpl implements ProductBankService {

	private static final Logger log = LoggerFactory.getLogger(ProductBankServiceImpl.class);
	
	@Autowired
	public ProductBankDao productoDao;

	@Autowired
	public TypeProductBankDao tipoProductoDao;
	
	@Autowired
	private ClientClient clientClient;
	
	@Autowired
	private CreditoClient creditoClient;
	
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
	
	@Override
	public Mono<ProductBank> retiro(Double monto, String numTarjeta, Double comision, String codigo_bancario) {
		//BUSCA EL NUMERO DE LA CUENTA-TARJETA CON SU BANCO CORRESPONDIENTE
		//PARA OBTERNER TODOS LOS DATOS PARA QUITAR EL MONTO
		log.debug("Llego desde el controlador");
		return productoDao.viewNumCuenta(numTarjeta,codigo_bancario).flatMap(c -> {

			System.out.println(c.toString());
			
			if (monto < c.getSaldo()) {
				c.setSaldo((c.getSaldo() - monto) - comision);

				return productoDao.save(c);
			}
			return Mono.error(new InterruptedException("SALDO INSUFICIENTE"));
		});
	}

	@Override
	public Mono<ProductBank> depositos(Double monto, String numTarjeta, Double comision, String codigo_bancario) {
		
		return productoDao.viewNumCuenta(numTarjeta,codigo_bancario).flatMap(c -> {
			
			System.out.println("El monto es : " +  monto);
			System.out.println("El monto es : " +  comision);
			
			c.setSaldo((c.getSaldo() + monto) - comision);
			return productoDao.save(c);
		});
		
	}

	@Override
	public Flux<ProductBank> saveProductoBancoCliente(ProductBank producto) {
		
		log.debug("Entro al metodo crear producto");
		System.out.println("Entro al metodo crear producto");
		System.out.println(producto.toString());
		
		List<ProductBank> listProducto = new ArrayList<ProductBank>();
		listProducto.add(producto);
		
		Flux<ProductBank> fMono = Flux.fromIterable(listProducto);
		
		/*
		TIPO PRODUCTO
		Ahorro = 1
		Cuentas corrientes  = 2
		Cuentas a plazo fijo = 3
		cuenta ahorro personal VIP 0 = 4
		cuenta corriente personal VIP = 5
		//------------------------------------------------------
		empresarial PYME  = 6 
		empresarial Corporative = 7
		cuenta plazo fijo VIP = 8
		 */
		
		return fMono.filter(ff -> {
			//VERIFICAR QUE TIPO PRODUCTO CUENTA SE ESTA MANDO A CREAR
			if (ff.getTipoProducto().getId().equalsIgnoreCase("1")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("2")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("3")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("4")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("5"))
			{
				return true;
			}
			return false;
		}).flatMap(f -> {

			log.debug("DNI : " + f.getDni());
			System.out.println(f.getDni());
			
			//BUSCA SI TINE UNA DEUDA DE UN PRODUCTO DE CREDITO	
			log.debug("BUSCA SI TINE UNA DEUDA DE UN PRODUCTO DE CREDITO");
			Flux<CuentaCreditoDto> cred = creditoClient.findByNumDoc(f.getDni());
			
			cred.subscribe(c -> System.out.println(c));
			
			return cred.defaultIfEmpty(new CuentaCreditoDto()).flatMap(n->{
				
				//SI NO TIENE UNA CUENTA SIGNIFICA QUE NO TIENE DEUDA
				log.debug("El numero de cuenta es : " + n.getNumeroCuenta());
				System.out.println("El numero de cuenta es : " + n.getNumeroCuenta());
				
				return cred.flatMap(deuda -> {
					
					if(deuda.getCodigoBanco() == null) {
						deuda.setCodigoBanco(f.getCodigoBanco());
						deuda.setConsumo(0.0);
					}
					
					if(deuda.getConsumo() > 0) {
						throw new RequestException("TIENES UNA DEUDA - NO PUEDES ADQUIRIR UN PRODUCTO");
					}
					
					//BUSCAR EL NUMERO DE DOCUMENTO
					log.debug("El DNI es : --->" + f.getDni());
					System.out.println("El DNI es : --->" + f.getDni());
					System.out.println("ProductBank--[" + f);
					//OBTENIENDO LOS DATOS DEL CLIENTE
					Mono<Client> cli = clientClient.findByNumDoc(f.getDni());
					cli.subscribe(c -> System.out.println(c));
					
					log.info("datos cliente --->> "+cli.map(c-> "DNI : " + c.getNumdoc()));
					System.out.println("datos cliente --->> "+cli.map(c-> "DNI : " + c.getNumdoc()));
					System.out.println("datos cliente --->> "+cli.map(c-> c.getTipoCliente().toString()));
					return cli.flatMap(p -> {
						System.out.println("client--[" + f);
						//COMPARA EL CODIGO DE BANCO DEL CLIENTE CON
						//EL CODIGO DE QUE ESTA MANDANDO DEL BANCO
						if(!p.getCodigoBanco().equalsIgnoreCase(f.getCodigoBanco())) {
							System.out.println("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO");
							log.info("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO");
							throw new RequestException("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO");
						
						}else{							
							/*							  
							tipo cliente
							personal = 1
							empresarial= 2							
							empresarial corporativo = 5 							
							*/							
							//VERIFIANDO EL TIPO DE CLIENTE
							System.out.println("VERIFIANDO EL TIPO DE CLIENTE");
							if (p.getTipoCliente().getId().equalsIgnoreCase("1")) { //personal = 1
								//BUSCA SI EL CLIENTE PERSONAL TIENE UN PRODUCTO YA CREADO
								System.out.println("BUSCA SI EL CLIENTE PERSONAL TIENE UN PRODUCTO YA CREADO");
								Mono<Long> valor = productoDao
										.buscarPorDocTipoCuentaBanco(f.getDni(), f.getTipoProducto().getId(),f.getCodigoBanco()).count();
																
								log.info("clientes ---> " + valor);
								System.out.println("clientes ---> " + valor);
								
								return valor.flatMap(p1 -> {									
									if (p1 >= 1) {
										log.info("TIENE ALMNOS UNA CUENTA CREADA");
										System.out.println("TIENE ALMNOS UNA CUENTA CREADA");
										if (!f.getTipoProducto().getId().equalsIgnoreCase("1")&& 
												!f.getTipoProducto().getId().equalsIgnoreCase("2")&& 
												!f.getTipoProducto().getId().equalsIgnoreCase("3")) {
											System.out.println("CLIENTE PERSONAL SOLO PUEDE TENER UN PRODUCTO");
											System.out.println("VERIFICA QUE NO TENGA CREADO UNA DE CUENTA : AHORRO, CORRIENTE, PLAZO FIJO");
											log.info("CLIENTE PERSONAL SOLO PUEDE TENER UN PRODUCTO");
											log.info("VERIFICA QUE NO TENGA CREADO UNA DE CUENTA : AHORRO, CORRIENTE, PLAZO FIJO");
											
											ProductBank f1 = new ProductBank();
											
											f1.setDni(f.getDni());
											f1.setNumeroCuenta(f.getNumeroCuenta());
											f1.setFecha_afiliacion(f.getFecha_afiliacion());
											f1.setFecha_caducidad(f.getFecha_caducidad());
											f1.setSaldo(f.getSaldo());
											
											f1.setCodigoBanco(f.getCodigoBanco());

											TypeProductBank t = new TypeProductBank();
											t.setId(f.getTipoProducto().getId());
											t.setDescripcion(f.getTipoProducto().getDescripcion());

											f1.setTipoProducto(t);
											return productoDao.save(f1);
										}else {
											throw new RequestException("PERSONAL TIENE UNA CUENTA BANCARIA DE ESTE TIPO");
										}
									}else {
										System.out.println("CUENTA CREADA");
										ProductBank f1 = new ProductBank();
										
										f1.setDni(f.getDni());
										f1.setNumeroCuenta(f.getNumeroCuenta());
										f1.setFecha_afiliacion(f.getFecha_afiliacion());
										f1.setFecha_caducidad(f.getFecha_caducidad());
										f1.setSaldo(f.getSaldo());										
										f1.setCodigoBanco(f.getCodigoBanco());

										TypeProductBank t = new TypeProductBank();
										t.setId(f.getTipoProducto().getId());
										t.setDescripcion(f.getTipoProducto().getDescripcion());

										f1.setTipoProducto(t);

										return productoDao.save(f1);
									}
									//return null;
								});
								
							}
						}
						return Mono.empty();
						//return null;
					});
					
					//return null;
				});
				
				//return null;
			});
			
			//return fMono;
			
		
		});
		
//		return null;
	}
	
}
