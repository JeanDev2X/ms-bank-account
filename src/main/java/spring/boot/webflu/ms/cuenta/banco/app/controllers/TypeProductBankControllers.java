package spring.boot.webflu.ms.cuenta.banco.app.controllers;

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
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;
import spring.boot.webflu.ms.cuenta.banco.app.service.TypeProductBankService;

@RequestMapping("/api/TipoProducto")
@RestController
public class TypeProductBankControllers {

	@Autowired
	private TypeProductBankService tipoProductosService;

	@GetMapping
	public Mono<ResponseEntity<Flux<TypeProductBank>>> findAll() {
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(tipoProductosService.findAllTipoproducto()));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<TypeProductBank>> viewId(@PathVariable String id) {
		return tipoProductosService.findByIdTipoProducto(id)
				.map(p -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PutMapping
	public Mono<TypeProductBank> updateProducto(@RequestBody TypeProductBank tipoProducto) {
		// System.out.println(cliente.toString());
		return tipoProductosService.saveTipoProducto(tipoProducto);
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteTipoProducto(@PathVariable String id) {
		return tipoProductosService.findByIdTipoProducto(id).flatMap(t -> {

			return tipoProductosService.deleteTipo(t).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
	}

	@PostMapping
	public Mono<TypeProductBank> saveTipoProducto(@RequestBody TypeProductBank tipoProducto) {
		return tipoProductosService.saveTipoProducto(tipoProducto);
	}

}
