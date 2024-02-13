package spring.boot.webflu.ms.cuenta.banco.app.documents;

import java.util.Date;

import javax.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection ="ProductBank")
public class ProductBank {
	
	@Id
	@NotEmpty
	private String id;
	
	@NotEmpty
	private String dni;
	
	@NotEmpty
	private String numeroCuenta; //DEBE DE SER UNICO - numero_cuenta
	
	@NotEmpty
	private String numeroTarjeta;//DEBE DE SER UNICO
	
	@NotEmpty	
	private TypeProductBank tipoProducto;
	
	@NotEmpty
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String fecha_afiliacion;
	
	@NotEmpty
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String fecha_caducidad;
	@NotEmpty
	private double saldo;
	
	
	@NotEmpty
	private String codigoBanco;
	
	public ProductBank() {

	}

	public ProductBank(String dni,String numeroCuenta,String numeroTarjeta,
			TypeProductBank tipoProducto,double saldo,String codigoBanco) {
		this.dni = dni;
		this.numeroCuenta = numeroCuenta;
		this.numeroTarjeta = numeroTarjeta;
		this.tipoProducto = tipoProducto;
		this.saldo = saldo;
		this.codigoBanco = codigoBanco;
	}

}










