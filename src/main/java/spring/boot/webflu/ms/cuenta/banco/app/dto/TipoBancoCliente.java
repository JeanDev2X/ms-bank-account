package spring.boot.webflu.ms.cuenta.banco.app.dto;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TipoBancoCliente {


	private String id;
	private String descripcion;
}
