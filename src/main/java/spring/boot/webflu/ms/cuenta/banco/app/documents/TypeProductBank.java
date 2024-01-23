package spring.boot.webflu.ms.cuenta.banco.app.documents;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

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
