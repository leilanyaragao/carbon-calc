package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.dto.CarbonCalculationResultDTO;
import br.com.actionlabs.carboncalc.dto.StartCalcRequestDTO;
import br.com.actionlabs.carboncalc.dto.TransportationDTO;
import br.com.actionlabs.carboncalc.dto.UpdateCalcInfoRequestDTO;
import br.com.actionlabs.carboncalc.model.CarbonCalculationFactor;
import br.com.actionlabs.carboncalc.model.EnergyEmissionFactor;
import br.com.actionlabs.carboncalc.model.SolidWasteEmissionFactor;
import br.com.actionlabs.carboncalc.model.TransportationEmissionFactor;
import br.com.actionlabs.carboncalc.repository.CarbonCalculationRepository;
import br.com.actionlabs.carboncalc.repository.EnergyEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.SolidWasteEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.TransportationEmissionFactorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarbonCalculatorService {
    @Autowired
    private CarbonCalculationRepository carbonCalculationRepository;
    @Autowired
    private EnergyEmissionFactorRepository energyEmissionFactorRepository;
    @Autowired
    private SolidWasteEmissionFactorRepository solidWasteEmissionFactorRepository;
    @Autowired
    private TransportationEmissionFactorRepository transportationEmissionFactorRepository;

    public String startCalculation(StartCalcRequestDTO request) {
        CarbonCalculationFactor startCalculation = new CarbonCalculationFactor();
        startCalculation.setName(request.getName());
        startCalculation.setEmail(request.getEmail());
        startCalculation.setPhoneNumber(request.getPhoneNumber());
        startCalculation.setUf(request.getUf());
        return carbonCalculationRepository.save(startCalculation).getId();
    }

    public void updateCalculation(UpdateCalcInfoRequestDTO request) {
        CarbonCalculationFactor carbonCalculation = carbonCalculationRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Carbon Calculation not found for id: " + request.getId()));

        carbonCalculation.setEnergyConsumption(request.getEnergyConsumption());
        carbonCalculation.setTransportation(request.getTransportation());
        carbonCalculation.setSolidWasteTotal(request.getSolidWasteTotal());
        carbonCalculation.setRecyclePercentage(request.getRecyclePercentage());

        carbonCalculationRepository.save(carbonCalculation);
    }

    public CarbonCalculationResultDTO calculateCarbonFootprint(String id) {
        CarbonCalculationFactor carbonCalculation = carbonCalculationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carbon Calculation not found for id: " + id));

        double energyEmission = calculateEnergyEmission(carbonCalculation.getEnergyConsumption(), carbonCalculation.getUf());
        double transportationEmission = calculateTransportationEmission(carbonCalculation.getTransportation());
        double wasteEmission = calculateWasteEmission(carbonCalculation.getSolidWasteTotal(), carbonCalculation.getRecyclePercentage(), carbonCalculation.getUf());

        double totalEmission = energyEmission + transportationEmission + wasteEmission;

        CarbonCalculationResultDTO resultDTO = new CarbonCalculationResultDTO();
        resultDTO.setEnergy(energyEmission);
        resultDTO.setTransportation(transportationEmission);
        resultDTO.setSolidWaste(wasteEmission);
        resultDTO.setTotal(totalEmission);

        return resultDTO;
    }

    private double calculateEnergyEmission(int energyConsumption, String uf) {
        EnergyEmissionFactor energyEmissionFactor = energyEmissionFactorRepository.findByUf(uf)
                .orElseThrow(() -> new IllegalArgumentException("Energy emission factor not found"));
        return energyConsumption * energyEmissionFactor.getFactor();
    }

    private double calculateTransportationEmission(List<TransportationDTO> transportations) {
        double totalEmission = 0;
        for (TransportationDTO transportation : transportations) {
            TransportationEmissionFactor transportationEmissionFactor = transportationEmissionFactorRepository.findByType(transportation.getType().toString())
                    .orElseThrow(() -> new IllegalArgumentException("Transportation emission factor not found"));
            totalEmission += transportation.getMonthlyDistance() * transportationEmissionFactor.getFactor();
        }
        return totalEmission;
    }

    private double calculateWasteEmission(int solidWasteTotal, double recyclePercentage, String uf) {
        double carbonEmission = 0.0;
        SolidWasteEmissionFactor solidWasteEmissionFactor = solidWasteEmissionFactorRepository.findByUf(uf)
                .orElseThrow(() -> new IllegalArgumentException("Solid Waste emission factor not found"));
        carbonEmission += solidWasteTotal * solidWasteEmissionFactor.getRecyclableFactor();
        carbonEmission += solidWasteTotal * solidWasteEmissionFactor.getNonRecyclableFactor();
        return carbonEmission;
    }

}
