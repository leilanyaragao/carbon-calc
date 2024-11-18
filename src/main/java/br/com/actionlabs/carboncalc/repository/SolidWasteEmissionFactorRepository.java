package br.com.actionlabs.carboncalc.repository;

import br.com.actionlabs.carboncalc.model.SolidWasteEmissionFactor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolidWasteEmissionFactorRepository
        extends MongoRepository<SolidWasteEmissionFactor, String> {
    Optional<SolidWasteEmissionFactor> findByUf(String uf);
}
