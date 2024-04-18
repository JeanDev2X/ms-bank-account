package spring.boot.webflu.ms.cuenta.banco.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.service.ProductBankService;
import spring.boot.webflu.ms.cuenta.banco.app.service.TypeProductBankService;
import spring.boot.webflu.ms.cuenta.banco.app.dto.*;

@RequestMapping("/api/ProductBank")
@RestController
public class ProductBankControllers {

	private static final Logger log = LoggerFactory.getLogger(ProductBankControllers.class);
	
	@Autowired
	private ProductBankService productoService;
	
	@Autowired
	private TypeProductBankService tipoProductoService;
	
	//LISTA LAS CUENTAS DE BANCO EXISTENTES
	@GetMapping
	public Mono<ResponseEntity<Flux<ProductBank>>> findAll() {
		return Mono.just(
				ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(productoService.findAllProductoBanco())

		);
	}

	//TODAS CUENTAS BANCARIAS POR ID
	@GetMapping("/{id}")
	public Mono<ResponseEntity<ProductBank>> viewId(@PathVariable String id) {
		return productoService.findByIdProductoBanco(id)
				.map(p -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	//ACTUALIZAR LA CUENTA
	@PutMapping
	public Mono<ProductBank> updateProducto(@RequestBody ProductBank producto) {

		return productoService.saveProductoBanco(producto);
	}
	
	//GUARDA CUENTA PRODUCTO BANCO - SIN VALIDAR
	@PostMapping("/guardarProductoBanco")
	public Mono<ProductBank> guardarProBanco(@RequestBody ProductBank cuentaBanco) {
		return productoService.saveProductoBanco(cuentaBanco);
	}
	
	//REGISTRAR UN PRODUCTO DE BANCO
	@PostMapping("/guardarProductoBancoValidType")
	public Mono<ProductBank> registerProductBankValidType(@RequestBody ProductBank pro) {
		// BUSCA SI EL TIPO DE CREDITO EXISTE		
		log.info("producto" + pro);
		return tipoProductoService.findByIdTipoProducto("1")
	            .switchIfEmpty(Mono.error(new InterruptedException("NO EXISTE ESTE TIPO DE PRODUCTO")))
	            .flatMap(tipo -> {
	                pro.setTipoProducto(tipo);
	                return productoService.saveProductoBanco(pro);
	            });
		
	}
	
	//ELIMINA CLIENTE POR ID
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
		System.out.println("Eliminar la cuenta");
		return productoService.findByIdProductoBanco(id)
				.flatMap(s -> {
			return productoService.deleteProductoBanco(s).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
	}
	
	//-----------------------------------------------------------------------------
	
	//REGISTRAR UN PRODUCTO DE BANCO
	
	@PostMapping
	public Flux<ProductBank> guardarProductoBanco(@RequestBody ProductBank pro) {
		log.debug("En el controlador-crear producto");
		//EL DNI DEBE DE EXISTIR EN EL MS-CREDITO PARA QUE PUEDA VERIFICAR
		return productoService.saveProductoBancoCliente(pro);
	}
	/*
	Un cliente puede hacer depósitos y retiros de sus cuentas bancarias
	*/
	@PutMapping("/retiro/{numero_cuenta}/{monto}/{comision}/{codigo_bancario}")
	public Mono<ProductBank> retiroBancario(@PathVariable String numero_cuenta,@PathVariable Double monto,
			@PathVariable Double comision, @PathVariable String codigo_bancario) {

		System.out.println("LLEGO DESDE MS-OP-BANCOS --->>>");
		return productoService.retiro(monto, numero_cuenta, comision,codigo_bancario);
	}
	
	/*
	Un cliente puede hacer depósitos y retiros de sus cuentas bancarias
	*/
	@PutMapping("/deposito/{numero_Cuenta}/{monto}/{comision}/{codigo_bancario}")
	public Mono<ProductBank> despositoBancario(@PathVariable Double monto, @PathVariable String numero_Cuenta,
			@PathVariable Double comision,@PathVariable String codigo_bancario) {
		
		System.out.println("LLEGO DESDE MS-OP-BANCOS --->>>");
		return productoService.depositos(monto, numero_Cuenta, comision,codigo_bancario);
	}
	//MUESTRA LA CUENTA BANCARIA POR EL NUMERO DE CUENTA Y EL CODIGO DE BANCO
	
	@GetMapping("/numero_cuenta/{num}/{codigo_bancario}")
	public Mono<ProductBank> productosBancoPorBancos(@PathVariable String num, @PathVariable String codigo_bancario) {
		Mono<ProductBank> producto = productoService.listProdNumTarj(num, codigo_bancario);
		producto.subscribe(o -> System.out.println("Cliente[" + o.toString()));
		return producto;
	}
	
	//MUESTRA LAS CUENTAS DE LOS CLIENTES -{TRANSACCIONES REALIZADAS Y TODAS LAS CUENTAS ASOCIADAS CON ESE CLIENTE}
	@GetMapping("/dni/{dni}")
	public Flux<ProductBank> mostrarProductoBancoCliente(@PathVariable String dni) {
		Flux<ProductBank> producto = productoService.findAllProductoByDniCliente(dni);
		return producto;
	}
	
	//MUESTRA LOS SALDOS DE LA CUENTAS DE BANCO - CON EL NUMERO DE CUENTA Y EL CODIGO DE BANCO
	/*
	El sistema debe permitir consultar los saldos disponibles en sus productos como: cuentas bancarias y tarjetas de crédito 
	*/
	@GetMapping("/SaldosBancarios/{numero_cuenta}/{codigo_bancario}")
	public Mono<CuentaBancoDto> saldosClienteBancos(@PathVariable String numero_cuenta,@PathVariable String codigo_bancario) {
		
		System.out.println("Saldos Bancarios: " + numero_cuenta + " cod banco: " + codigo_bancario);
		
		return productoService.listProdNumTarj(numero_cuenta, codigo_bancario)
	            .flatMap(this::mapToCuentaBancoDto)
	            .switchIfEmpty(Mono.error(new RuntimeException("La cuenta no existe"))); // Manejar cuenta inexistente si es necesario

	}
	
	private Mono<CuentaBancoDto> mapToCuentaBancoDto(ProductBank c) {
	    /*
	    Esto permite que la creación del objeto se retrase hasta que se suscriba un observador
	    */	    
	    return Mono.fromSupplier(() -> {
	        CuentaBancoDto pp = new CuentaBancoDto();
	        TipoCuentaBancoDto tp = new TipoCuentaBancoDto();

	        tp.setId(c.getTipoProducto().getId());
	        tp.setDescripcion(c.getTipoProducto().getDescripcion());

	        pp.setDni(c.getDni());
	        pp.setNumero_cuenta(c.getNumeroCuenta());
	        pp.setSaldo(c.getSaldo());
	        pp.setTipoProducto(tp);

	        return pp;
	    });
	    
	}
	
	@GetMapping("/saldopromedio/{dni}")
	public Mono<CuentaSaldoPromedio> saldosPromedio(@PathVariable String dni) {		
		Mono<CuentaSaldoPromedio> saldos = productoService.saldos(dni);
		return saldos;		
	}
	
	//=============operaciones yanki
	
	@GetMapping("/numeroCelular/{numeroCelular}")
	public Mono<ProductBank> viewCuentaYanki(@PathVariable String numeroCelular) {
		return productoService.viewCuentaYanki(numeroCelular)
	            .doOnNext(o -> log.info("Cliente[" + o.toString()));
	}
	
	@GetMapping("/saldoyanki/{numeroCelular}")
	public Mono<ProductBank> saldoYanki(@PathVariable String numeroCelular) {
		return productoService.saldoYanki(numeroCelular)
	            .doOnNext(o -> log.info("Cliente[" + o.toString()));				
	}
	
	@PutMapping("/retiroyk/{numeroCelular}/{monto}")
	public Mono<ProductBank> retiroCuentaYanki(@PathVariable String numeroCelular,@PathVariable Double monto) {
		log.info("Desde ms-op-bancos");		
		return productoService.retiroYanki(monto, numeroCelular);
	}
	
	@PutMapping("/depositoyk/{numeroCelular}/{monto}")
	public Mono<ProductBank> despositoCuentaYanki(@PathVariable Double monto, @PathVariable String numeroCelular) {		
		log.info("Desde ms-op-bancos");
		return productoService.depositoYanki(monto, numeroCelular);
	}
	
}
