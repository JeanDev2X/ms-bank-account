package spring.boot.webflu.ms.cuenta.banco.app.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;

@Getter
@Setter
@Data
public class TipoCuentaBancoDto {

	private String id;
	private String descripcion;
}
