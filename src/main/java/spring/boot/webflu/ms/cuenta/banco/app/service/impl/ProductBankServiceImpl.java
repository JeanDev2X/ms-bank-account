package spring.boot.webflu.ms.cuenta.banco.app.service.impl;

import java.time.LocalDate;
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
import spring.boot.webflu.ms.cuenta.banco.app.config.Constantes;
import spring.boot.webflu.ms.cuenta.banco.app.config.UtilisCode;
import spring.boot.webflu.ms.cuenta.banco.app.dao.ProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.dao.TypeProductBankDao;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.dto.Client;
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaBancoDto;
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaCreditoDto;
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaSaldoPromedio;
import spring.boot.webflu.ms.cuenta.banco.app.dto.TipoCuentaBancoDto;
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
	public Mono<ProductBank> retiro(Double monto, String numero_cuenta, Double comision, String codigo_bancario) {
		//BUSCA EL NUMERO DE LA CUENTA-TARJETA CON SU BANCO CORRESPONDIENTE
		//PARA OBTERNER TODOS LOS DATOS PARA QUITAR EL MONTO
		log.info("Llego desde el controlador");
		return productoDao.viewNumCuenta(numero_cuenta,codigo_bancario).flatMap(c -> {

			System.out.println(c.toString());
			
			if (monto < c.getSaldo()) {
				c.setSaldo((c.getSaldo() - monto) - comision);

				return productoDao.save(c);
			}
			return Mono.error(new InterruptedException("SALDO INSUFICIENTE"));
		});
	}

	@Override
	public Mono<ProductBank> depositos(Double monto, String numero_Cuenta, Double comision, String codigo_bancario) {
		
		return productoDao.viewNumCuenta(numero_Cuenta,codigo_bancario).flatMap(c -> {
			
			System.out.println("El monto es : " +  monto);
			System.out.println("El monto es : " +  comision);
			
			c.setSaldo((c.getSaldo() + monto) - comision);
			return productoDao.save(c);
		});
		
	}

	@Override
	public Flux<ProductBank> saveProductoBancoCliente(ProductBank producto) {
		
		log.info("CREAR PRODUCTO BANCARIO");		
		log.info("Producto["+producto+"]");
		
		List<ProductBank> listProducto = new ArrayList<ProductBank>();
		listProducto.add(producto);
		
		Flux<ProductBank> fMono = Flux.fromIterable(listProducto);
		
		/*
		TIPO PRODUCTO
		Cuentas Ahorro = 1
		Cuentas corrientes  = 2
		Cuentas a plazo fijo = 3			
		*/
		return fMono.filter(ff -> {
			//VERIFICAR QUE TIPO PRODUCTO CUENTA SE ESTA MANDO A CREAR
			if (ff.getTipoProducto().getId().equalsIgnoreCase("1")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("2")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("3")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("4")
					|| ff.getTipoProducto().getId().equalsIgnoreCase("5")
					)
			{
				return true;
			}
			return false;
		}).flatMap(f -> {
			//BUSCA SI TINE UNA DEUDA DE UN PRODUCTO DE CREDITO	
			log.info("BUSCA SI TINE UNA DEUDA DE UN PRODUCTO DE CREDITO");
			Flux<CuentaCreditoDto> cred = creditoClient.findByNumDoc(f.getDni());
			
			cred.subscribe(m -> log.info("CuentaCreditoDtoXXXX["+ m.getNumeroCuenta() +"]"));
			
			return cred.defaultIfEmpty(new CuentaCreditoDto()).flatMap(n->{
				
				//SI NO TIENE UNA CUENTA SIGNIFICA QUE NO TIENE DEUDA
				log.info("NUMERO CUENTA : " + n.getNumeroCuenta());
				log.info("credito : " + n.toString());
				if(n.getNumeroCuenta() == null) {
					CuentaCreditoDto cdrt = new CuentaCreditoDto();
					System.out.println("credito null");
					cdrt.setCodigoBanco(f.getCodigoBanco());
					cdrt.setConsumo(0.0);
					
					System.out.println(cdrt.toString());
					//throw new RequestException("NO EXISTE CUENTA - NO TIENE CUENTA CREDITO");
				}
				//System.out.println(cred.toString());								
				cred.subscribe(c -> log.info("CuentaCreditoDto["+ c +"]"));
				return cred.defaultIfEmpty(new CuentaCreditoDto()).flatMap(deuda -> {							
					if(deuda.getCodigoBanco() == null) {
						log.info("NO TIENE DEUDA");
						deuda.setCodigoBanco(f.getCodigoBanco());
						deuda.setConsumo(0.0);
					}
					
					if(deuda.getConsumo() > 0) {
						throw new RequestException("TIENES UNA DEUDA - NO PUEDES ADQUIRIR UN PRODUCTO");
					}
					
					//BUSCAR EL NUMERO DE DOCUMENTO
					log.info("ProductBank[" + f+"]");					
					//OBTENIENDO LOS DATOS DEL CLIENTE
					Mono<Client> cli = clientClient.findByNumDoc(f.getDni());
					cli.subscribe(c -> log.info("CuentaCreditoDto["+ c +"]"));
										
					return cli.flatMap(p -> {
						log.info("client[" + f+"]");
						//COMPARA EL CODIGO DE BANCO DEL CLIENTE CON
						//EL CODIGO DE QUE ESTA MANDANDO DEL BANCO
						if(!p.getCodigoBanco().equalsIgnoreCase(f.getCodigoBanco())) {							
							log.info("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO");
							throw new RequestException("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO");
						
						}else{							
							/*							  
							tipo cliente
							personal = 1
							empresarial= 2																					
							*/							
							//VERIFIANDO EL TIPO DE CLIENTE
							log.info("LA CUENTA-PRODUCTO DEL CLIENTE --> PERTENECE AL BANCO");
							log.info("VERIFIANDO EL TIPO DE CLIENTE");
							if (p.getTipoCliente().getId().equalsIgnoreCase("1")) { //cliente personal = 1
								//BUSCA SI EL CLIENTE PERSONAL TIENE UN PRODUCTO YA CREADO
								log.info("CLIENTE PERSONAL : VALIDAR SI YA TIENE UNA CUENTA(AHORRO, CORRIENTE, PLAZO FIJO) CREADA - NO PUEDE SER CREADA OTRA");
								/*
								Un cliente personal solo puede tener un máximo de una cuenta de ahorro, 
								una cuenta corriente o cuentas a plazo fijo.
								*/
								Mono<Long> valor = productoDao
										.buscarPorDocTipoCuentaBanco(f.getDni(),f.getCodigoBanco()).count();
																					
								valor.subscribe(v -> log.info("PRODUCTO :["+ v +"]"));
								
								return valor.flatMap(p1 -> {									
									if (p1 >= 1) {
										
										log.info("CLIENTE PERSONAL : TIENE AL MENOS UNA CUENTA CREADA - SOLO PUEDE TENER UN PRODUCTO - NO PUEDE TENER MAS DE UNA CUENTA");																																	
										throw new RequestException("CLIENTE PERSONAL : YA TIENE UNA CUENTA(AHORRO, CORRIENTE, PLAZO FIJO) CREADA - NO PUEDE SER CREADA OTRA");
										
									}else {
										log.info("CLIENTE PERSONAL : CUENTA NUEVA CREADA");										
										ProductBank f1 = new ProductBank();
										
										f1.setDni(f.getDni());
										f1.setNumeroCuenta(f.getNumeroCuenta());
										f1.setNumeroTarjeta(Constantes.NUMERO_TARJETA +UtilisCode.numTarjeta());
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
								
							}else if (p.getTipoCliente().getId().equalsIgnoreCase("2")) { //empresarial= 2
								/*
								Un cliente empresarial no puede tener una cuenta de ahorro o de plazo fijo, 
								pero sí múltiples cuentas corrientes
								*/
								log.info("CLIENTE EMPRESARIA SOLO PUEDE TENER CUENTAS DE TIPO CORRIENTE");
								if (!f.getTipoProducto().getId().equalsIgnoreCase("2")) {//corriente
									throw new RequestException("CLIENTE EMPRESARIAL : NO PUEDE TENER CUENTA DE ESTE TIPO");
								}
//								
								log.info("CREA LA CUENTA EMPRESARIAL");
								ProductBank f1 = new ProductBank();

								f1.setDni(f.getDni());
								f1.setNumeroCuenta(f.getNumeroCuenta());
								f1.setNumeroTarjeta(Constantes.NUMERO_TARJETA +UtilisCode.numTarjeta());
								f1.setFecha_afiliacion(f.getFecha_afiliacion());
								f1.setFecha_caducidad(f.getFecha_caducidad());
								f1.setSaldo(f.getSaldo());								
								f1.setCodigoBanco(f.getCodigoBanco());

								TypeProductBank t = new TypeProductBank();
								t.setId(f.getTipoProducto().getId());
								t.setDescripcion(f.getTipoProducto().getDescripcion());
								f1.setTipoProducto(t);

								return productoDao.save(f1);
								
							}else if (p.getTipoCliente().getId().equalsIgnoreCase("3")) { //personal vip = 3
								
								if(!(f.getSaldo() >= 500)) { 							
									throw new RequestException("DEBE TENER SALDO MINIMO S/.500.00");
								}else {
									
									if(p.isCredito()==false) {
										throw new RequestException("CLIENTE EMPRESARIAL : NO TIENE TARJETA DE CREDITO");
									}
									
									//TODO : Adicionalmente, para solicitar este producto el cliente debe tener una tarjeta de crédito 
									//con el banco al momento de la creación de la cuenta.
									System.out.println("CREA LA CUENTA PERSONAL VIP");
									log.info("CREA LA CUENTA PERSONAL VIP");						
									ProductBank f1 = new ProductBank();
									f1.setDni(f.getDni());
									f1.setNumeroCuenta(f.getNumeroCuenta());
									f1.setNumeroTarjeta(Constantes.NUMERO_TARJETA +UtilisCode.numTarjeta());
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
							}else if (p.getTipoCliente().getId().equalsIgnoreCase("4")) { //empresarial pyme = 4
								
								//TODO : Como requisito debe de tener una cuenta corriente. 
								//TODO : Como requisito, el cliente debe tener una tarjeta de crédito con el banco al momento de la creación de la cuenta.
								
								Mono<Long> cuentasCorrientes = productoDao
										.cuentasCorrientes(p.getNumdoc());
								
								return cuentasCorrientes.flatMap(p1 -> {
									if(p1<0) {
										throw new RequestException("CLIENTE EMPRESARIAL : NO TIENE CUENTA CORRIENTE");																			
									}else if(p.isCredito()==false){
										throw new RequestException("CLIENTE EMPRESARIAL : NO TIENE TARJETA DE CREDITO");
									}else {
										//tiene cuenta corriente, puedes crear una cuenta pyme
										System.out.println("CREANDO CUENTA PYME EMPRESARIAL");
										log.info("CREANDO CUENTA PYME EMPRESARIAL");
										ProductBank f1 = new ProductBank();
										f1.setDni(f.getDni());
										f1.setNumeroCuenta(f.getNumeroCuenta());
										f1.setNumeroTarjeta(Constantes.NUMERO_TARJETA +UtilisCode.numTarjeta());
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
									
								});
								
							}
						}
						System.out.println("FINAL");
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

	@Override
	public Mono<ProductBank> listProdNumTarj(String num, String codigo_bancario) {
		System.out.println("lista productos por numero de cuenta");
		return productoDao.viewNumCuenta(num, codigo_bancario);
	}
	
	

	@Override
	public Flux<ProductBank> findAllProductoByDniCliente(String dniCliente) {
		return productoDao.findByDni(dniCliente);
	}
	
	public Flux<ProductBank> buscarPorDni(String dniCliente) {
		return productoDao.findByDni(dniCliente);
	}
	
	double saldoPromedio = 0.0;
	double sumaPromedio = 0.0;
	int cantidad = 0;

	@Override
	public Mono<CuentaSaldoPromedio> saldos(String dniCliente) {
		
		System.out.println("num_doc-->"+dniCliente);
		Flux<ProductBank> producto = buscarPorDni(dniCliente);
		
		//Usamos el collect, para acumular los resultados en un solo objeto CuentaSaldoPromedio
		return producto.collect(CuentaSaldoPromedio::new,(saldos,pd) -> {
			
			// Cálculo del saldo promedio            
            sumaPromedio += pd.getSaldo();
            cantidad++;
            saldoPromedio = sumaPromedio / cantidad;
            System.out.println("saldoPromedio["+saldoPromedio);
            // Configuración de los valores en el objeto CuentaSaldoPromedio
            saldos.setDni(dniCliente);
            saldos.setNumero_cuenta(pd.getNumeroCuenta());
            TipoCuentaBancoDto tp = new TipoCuentaBancoDto();
            tp.setId(pd.getTipoProducto().getId());
            tp.setDescripcion(pd.getTipoProducto().getDescripcion());
            saldos.setTipoProducto(tp);
            saldos.setSaldoPromedio(saldoPromedio);  
            saldos.setFechaSaldo(LocalDate.now());
			
		});
	}
	
	
	
}
