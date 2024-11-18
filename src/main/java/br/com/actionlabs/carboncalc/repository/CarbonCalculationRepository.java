package br.com.actionlabs.carboncalc.repository;

import br.com.actionlabs.carboncalc.model.CarbonCalculationFactor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarbonCalculationRepository extends MongoRepository<CarbonCalculationFactor, String> {
}