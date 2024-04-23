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
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaCreditoDto;
import spring.boot.webflu.ms.cuenta.banco.app.dto.CuentaSaldoPromedio;
import spring.boot.webflu.ms.cuenta.banco.app.dto.TipoCuentaBancoDto;
import spring.boot.webflu.ms.cuenta.banco.app.exception.RequestException;
import spring.boot.webflu.ms.cuenta.banco.app.service.ProductBankService;

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

	// --------------------------------------------*************************

	@Override
	public Mono<ProductBank> retiro(Double monto, String numero_cuenta, Double comision, String codigo_bancario) {
		// BUSCA EL NUMERO DE LA CUENTA-TARJETA CON SU BANCO CORRESPONDIENTE
		// PARA OBTERNER TODOS LOS DATOS PARA QUITAR EL MONTO
		log.info("Llego desde el controlador");
		return productoDao.viewNumCuenta(numero_cuenta, codigo_bancario).flatMap(c -> {
			log.info("PRODUCTO:" + c);
			if (monto < c.getSaldo()) {
				c.setSaldo((c.getSaldo() - monto) - comision);
				return productoDao.save(c);
			}
			return Mono.error(new InterruptedException("SALDO INSUFICIENTE"));
		});
	}

	@Override
	public Mono<ProductBank> depositos(Double monto, String numero_Cuenta, Double comision, String codigo_bancario) {

		return productoDao.viewNumCuenta(numero_Cuenta, codigo_bancario).flatMap(c -> {
			log.info("PRODUCTO:" + c);
			c.setSaldo((c.getSaldo() + monto) - comision);
			return productoDao.save(c);
		});

	}

	@Override
	public Flux<ProductBank> saveProductoBancoCliente(ProductBank producto) {

		log.info("CREAR PRODUCTO BANCARIO - " + producto);	    

	    return Mono.just(producto)
	            .flatMapMany(p -> {
	                // Verificar que el tipo de producto de la cuenta se está mandando a crear
	                String tipoProductoId = p.getTipoProducto().getId();
	                boolean tipoValido = tipoProductoId.equals("1") || tipoProductoId.equals("2") ||
	                        tipoProductoId.equals("3") || tipoProductoId.equals("4") ||
	                        tipoProductoId.equals("5");

	                if (!tipoValido) {
	                    return Flux.error(new RequestException("TIPO DE PRODUCTO NO VÁLIDO"));
	                }

	                // Buscar si tiene una deuda de un producto de crédito
	                log.info("BUSCA SI TINE UNA DEUDA DE UN PRODUCTO DE CRÉDITO");
	                return creditoClient.findByNumDoc(p.getDni())
	                        .defaultIfEmpty(new CuentaCreditoDto())
	                        .flatMap(credito -> {
	                            if (credito.getNumeroCuenta() == null) {
	                                log.info("NO TIENE DEUDA");
	                                credito.setCodigoBanco(p.getCodigoBanco());
	                                credito.setConsumo(0.0);
	                            }

	                            if (credito.getConsumo() > 0) {
	                                return Mono.error(new RequestException("TIENE UNA DEUDA - NO PUEDES ADQUIRIR UN PRODUCTO"));
	                            }

	                            // Buscar el número de documento
	                            log.info("ProductBank[" + p + "]");
	                            return clientClient.findByNumDoc(p.getDni())
	                                    .flatMap(cliente -> {
	                                        log.info("client[" + cliente + "]");
	                                        // Comparar el código de banco del cliente con el código que se está mandando del banco
	                                        if (!cliente.getCodigoBanco().equalsIgnoreCase(p.getCodigoBanco())) {
	                                            log.info("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO");
	                                            return Mono.error(new RequestException("LA CUENTA-PRODUCTO DEL CLIENTE NO PERTENECE AL BANCO"));
	                                        }

	                                        // Verificar el tipo de cliente
	                                        log.info("VERIFIANDO EL TIPO DE CLIENTE");
	                                        String tipoClienteId = cliente.getTipoCliente().getId();

	                                        if (tipoClienteId.equalsIgnoreCase("1")) { // Cliente personal
	                                            return productoDao.buscarPorDocTipoCuentaBanco(p.getDni(), p.getCodigoBanco())
	                                                    .count()
	                                                    .flatMap(count -> {
	                                                        if (count >= 1) {
	                                                            return Mono.error(new RequestException("CLIENTE PERSONAL YA TIENE UNA CUENTA CREADA"));
	                                                        } else {
	                                                            log.info("CLIENTE PERSONAL: CUENTA NUEVA CREADA");
	                                                            return productoDao.save(crearProducto(p));
	                                                        }
	                                                    });
	                                        } else if (tipoClienteId.equalsIgnoreCase("2")) { // Cliente empresarial
	                                            if (!tipoProductoId.equalsIgnoreCase("2")) { // Cuenta corriente
	                                                return Mono.error(new RequestException("CLIENTE EMPRESARIAL: NO PUEDE TENER CUENTA DE ESTE TIPO"));
	                                            }
	                                            log.info("CREA LA CUENTA EMPRESARIAL");
	                                            return productoDao.save(crearProducto(p));
	                                        } else if (tipoClienteId.equalsIgnoreCase("3")) { // Cliente personal VIP
	                                            if (p.getSaldo() < 500) {
	                                                return Mono.error(new RequestException("DEBE TENER SALDO MÍNIMO S/.500.00"));
	                                            } else if (!cliente.isCredito()) {
	                                                return Mono.error(new RequestException("CLIENTE PERSONAL VIP: NO TIENE TARJETA DE CRÉDITO"));
	                                            }
	                                            log.info("CREA LA CUENTA PERSONAL VIP");
	                                            return productoDao.save(crearProducto(p));
	                                        } else if (tipoClienteId.equalsIgnoreCase("4")) { // Cliente empresarial PYME
	                                            return productoDao.cuentasCorrientes(cliente.getNumdoc())
	                                                    .flatMap(cuentas -> {
	                                                        if (cuentas < 0) {
	                                                            return Mono.error(new RequestException("CLIENTE EMPRESARIAL PYME: NO TIENE CUENTA CORRIENTE"));
	                                                        } else if (!cliente.isCredito()) {
	                                                            return Mono.error(new RequestException("CLIENTE EMPRESARIAL PYME: NO TIENE TARJETA DE CRÉDITO"));
	                                                        }
	                                                        log.info("CREANDO CUENTA PYME EMPRESARIAL");
	                                                        return productoDao.save(crearProducto(p));
	                                                    });
	                                        } else {
	                                            return Mono.empty();
	                                        }
	                                    });
	                        });
	            });
		
	}
	
	private ProductBank crearProducto(ProductBank p) {
	    ProductBank producto = new ProductBank();
	    producto.setDni(p.getDni());
	    producto.setNumeroCuenta(Constantes.NUMERO_CUENTA+UtilisCode.numCuenta());
	    producto.setNumeroTarjeta(Constantes.NUMERO_TARJETA + UtilisCode.numTarjeta());
	    producto.setFecha_afiliacion(p.getFecha_afiliacion());
	    producto.setFecha_caducidad(p.getFecha_caducidad());
	    producto.setSaldo(0.0);
	    producto.setCodigoBanco(p.getCodigoBanco());

	    TypeProductBank tipoProducto = new TypeProductBank();
	    tipoProducto.setId(p.getTipoProducto().getId());
	    tipoProducto.setDescripcion(p.getTipoProducto().getDescripcion());
	    producto.setTipoProducto(tipoProducto);

	    return producto;
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


	@Override
	public Mono<CuentaSaldoPromedio> saldos(String dniCliente) {
		
		log.info("NUM-DOC=" + dniCliente);
		// Usamos el collect, para acumular los resultados en un solo objeto
		// CuentaSaldoPromedio
		return buscarPorDni(dniCliente)
	            .collect(CuentaSaldoPromedio::new, (saldos, pd) -> {
	                double sumaPromedio = saldos.getSaldoPromedio() * saldos.getCantidad() + pd.getSaldo();
	                double nuevaCantidad = saldos.getCantidad() + 1;
	                double nuevoSaldoPromedio = sumaPromedio / nuevaCantidad;

	                saldos.setDni(dniCliente);
	                saldos.setNumero_cuenta(pd.getNumeroCuenta());
	                TipoCuentaBancoDto tp = new TipoCuentaBancoDto();
	                tp.setId(pd.getTipoProducto().getId());
	                tp.setDescripcion(pd.getTipoProducto().getDescripcion());
	                saldos.setTipoProducto(tp);
	                saldos.setSaldoPromedio(nuevoSaldoPromedio);
	                saldos.setCantidad(nuevaCantidad);
	                saldos.setFechaSaldo(LocalDate.now());
	            });
		
	}

	//=========================================
	
	@Override
	public Mono<ProductBank> viewCuentaYanki(String numeroCelular) {		
		log.info("lista productos por numero de cuenta");
		return productoDao.viewCuenta(numeroCelular);
	}
	
	@Override
	public Mono<ProductBank> saldoYanki(String numeroCelular) {
		log.info("lista productos por numero de cuenta");
		return productoDao.viewCuenta(numeroCelular);
	}
	
	@Override
	public Mono<ProductBank> retiroYanki(Double monto, String numeroCelular) {
		
		// BUSCA EL NUMERO DE LA CUENTA-TARJETA CON SU BANCO CORRESPONDIENTE
		// PARA OBTERNER TODOS LOS DATOS PARA QUITAR EL MONTO
		log.info("Llego desde el controlador");
		return productoDao.viewCuenta(numeroCelular)
	        .flatMap(cuenta -> {	            
	            log.info("Cuenta : " + cuenta);
	            if (monto < cuenta.getSaldo()) {
	                cuenta.setSaldo(cuenta.getSaldo() - monto);
	                return productoDao.save(cuenta);
	            } else {
	                return Mono.error(new InterruptedException("SALDO INSUFICIENTE"));
	            }
	        });
	}

	@Override
	public Mono<ProductBank> depositoYanki(Double monto, String numeroCelular) {
		return productoDao.viewCuenta(numeroCelular)
	        .flatMap(cuenta -> {	            
	            log.info("El monto es : " + monto);
	            cuenta.setSaldo(cuenta.getSaldo() + monto);
	            return productoDao.save(cuenta);
	        });
	}

}
