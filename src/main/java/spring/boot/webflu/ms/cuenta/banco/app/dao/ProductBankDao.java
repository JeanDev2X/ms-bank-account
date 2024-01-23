package spring.boot.webflu.ms.cuenta.banco.app.dao;

import java.util.Date;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.boot.webflu.ms.cuenta.banco.app.documents.ProductBank;

public interface ProductBankDao extends ReactiveMongoRepository<ProductBank, String> {

}
