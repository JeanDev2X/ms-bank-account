package spring.boot.webflu.ms.cuenta.banco.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import spring.boot.webflu.ms.cuenta.banco.app.documents.TypeProductBank;

public interface TypeProductBankDao extends ReactiveMongoRepository<TypeProductBank, String> {

}
