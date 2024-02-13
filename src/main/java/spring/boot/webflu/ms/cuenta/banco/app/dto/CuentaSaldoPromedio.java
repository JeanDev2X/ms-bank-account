package spring.boot.webflu.ms.cuenta.banco.app.dto;



import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CuentaSaldoPromedio {
	
	private String dni;
	private String numero_cuenta;
	private TipoCuentaBancoDto tipoProducto;
	private double saldoPromedio;
	private LocalDate fechaSaldo;

}
