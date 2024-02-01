package spring.boot.webflu.ms.cuenta.banco.app.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;

@Getter
@Setter
public class CuentaBancoDto {

	private String dni;
	private String numero_cuenta;
	private TipoCuentaBancoDto tipoProducto;
	private double saldo;
}
