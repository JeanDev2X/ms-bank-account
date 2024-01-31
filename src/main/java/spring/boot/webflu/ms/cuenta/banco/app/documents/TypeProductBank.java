package spring.boot.webflu.ms.cuenta.banco.app.documents;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Document(collection ="TypeProductBank")

public class TypeProductBank {

	
	@NotEmpty
	private String id;
	@NotEmpty
	private String descripcion;
		
	public TypeProductBank() {
		
	}

	public TypeProductBank(@NotEmpty String id, @NotEmpty String descripcion) {		
		
		this.id = id;
		this.descripcion = descripcion;
		
	}
	
	
	
}
