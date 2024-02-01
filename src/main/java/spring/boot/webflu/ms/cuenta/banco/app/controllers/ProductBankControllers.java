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
		System.out.println("producto" + pro);
		System.out.println("productoID" + pro.getTipoProducto().getId());
		Mono<TypeProductBank> tipo = tipoProductoService.findByIdTipoProducto("1");
		return tipo.defaultIfEmpty(new TypeProductBank()).flatMap(c -> {
			if (c.getId() == null) {
				return Mono.error(new InterruptedException("NO EXISTE ESTE TIPO"));
			}
			return Mono.just(c);
		}).flatMap(t -> {
			pro.setTipoProducto(t);
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
	
	@PutMapping("/retiro/{numero_cuenta}/{monto}/{comision}/{codigo_bancario}")
	public Mono<ProductBank> retiroBancario(@PathVariable String numero_cuenta,@PathVariable Double monto,
			@PathVariable Double comision, @PathVariable String codigo_bancario) {

		System.out.println("LLEGO DESDE MS-OP-BANCOS --->>>");
		return productoService.retiro(monto, numero_cuenta, comision,codigo_bancario);
	}
	
	
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
		return producto;
	}
	
	//MUESTRA LAS CUENTAS DE LOS CLIENTES -{TRANSACCIONES REALIZADAS Y TODAS LAS CUENTAS ASOCIADAS CON ESE CLIENTE}
	@GetMapping("/dni/{dni}")
	public Flux<ProductBank> mostrarProductoBancoCliente(@PathVariable String dni) {
		Flux<ProductBank> producto = productoService.findAllProductoByDniCliente(dni);
		return producto;
	}
	
	//MUESTRA LOS SALDOS DE LA CUENTAS DE BANCO - CON EL NUMERO DE CUENTA Y EL CODIGO DE BANCO
	@GetMapping("/SaldosBancarios/{numero_cuenta}/{codigo_bancario}")
	public Mono<CuentaBancoDto> saldosClienteBancos(@PathVariable String numero_cuenta,@PathVariable String codigo_bancario) {
		
		System.out.println("Saldos Bancarios : --->> " + numero_cuenta + " cod banco -->> " + codigo_bancario);
		
		//BUSCAR LA TARGETA-CUENTA
		Mono<ProductBank> oper = productoService.listProdNumTarj(numero_cuenta, codigo_bancario);
		
		oper.map(o->o).subscribe(u -> log.info(u.toString()));
		
		return oper.flatMap(c -> {
			
			//TODO:Agregar cuando la cuenta no existe.
			
			CuentaBancoDto pp = new CuentaBancoDto();
			TipoCuentaBancoDto tp = new TipoCuentaBancoDto();

			
			tp.setId(c.getTipoProducto().getId());
			tp.setDescripcion(c.getTipoProducto().getDescripcion());
			
			
			pp.setDni(c.getDni());
			pp.setNumero_cuenta(c.getNumeroCuenta());
			pp.setSaldo(c.getSaldo());
			pp.setTipoProducto(tp);

			return Mono.just(pp);
		});

	}
	
}
