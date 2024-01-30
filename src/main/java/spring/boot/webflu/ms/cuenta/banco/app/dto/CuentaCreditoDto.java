package spring.boot.webflu.ms.cuenta.banco.app.dto;


import javax.validation.constraints.NotEmpty;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
public class CuentaCreditoDto {
	

	@Id
	@NotEmpty
	private String id;
	@NotEmpty
	private String numeroCuenta;
	@NotEmpty
	private String dni;
	@NotEmpty
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String fecha_caducidad;
	@NotEmpty
	private Double credito;
	@NotEmpty
	private Double saldo;
	@NotEmpty
	private Double consumo;
	@NotEmpty
	private String usuario;
	@NotEmpty
	private String clave;
	@NotEmpty
	private String codigoBanco;

	
	//private tipoProducto tipoCliente;
}










