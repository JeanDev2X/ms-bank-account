package spring.boot.webflu.ms.cuenta.banco.app.dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Client {

	private String numdoc;
	private String nombres;
	private String apellidos;
	private String sexo;
	private String telefono;
	private String edad;
	private String correo;
	private TipoBancoCliente tipoCliente;
	private String codigoBanco;
	
	
	
}










