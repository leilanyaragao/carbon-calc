package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.dto.CarbonCalculationResultDTO;
import br.com.actionlabs.carboncalc.dto.StartCalcRequestDTO;
import br.com.actionlabs.carboncalc.dto.TransportationDTO;
import br.com.actionlabs.carboncalc.dto.UpdateCalcInfoRequestDTO;
import br.com.actionlabs.carboncalc.enums.TransportationType;
import br.com.actionlabs.carboncalc.model.CarbonCalculationFactor;
import br.com.actionlabs.carboncalc.model.EnergyEmissionFactor;
import br.com.actionlabs.carboncalc.model.SolidWasteEmissionFactor;
import br.com.actionlabs.carboncalc.model.TransportationEmissionFactor;
import br.com.actionlabs.carboncalc.repository.CarbonCalculationRepository;
import br.com.actionlabs.carboncalc.repository.EnergyEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.SolidWasteEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.TransportationEmissionFactorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class CarbonCalculatorServiceTest {

    @InjectMocks
    private CarbonCalculatorService carbonCalculatorService;

    @Mock
    private CarbonCalculationRepository carbonCalculationRepository;

    @Mock
    private EnergyEmissionFactorRepository energyEmissionFactorRepository;

    @Mock
    private TransportationEmissionFactorRepository transportationEmissionFactorRepository;

    @Mock
    private SolidWasteEmissionFactorRepository solidWasteEmissionFactorRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startCalculation() {
        StartCalcRequestDTO requestDTO = new StartCalcRequestDTO();
        requestDTO.setName("Leilany Ulisses");
        requestDTO.setEmail("leilany@example.com");
        requestDTO.setPhoneNumber("123456");
        requestDTO.setUf("PE");

        CarbonCalculationFactor carbonCalculation = new CarbonCalculationFactor();
        carbonCalculation.setId("123");
        when(carbonCalculationRepository.save(any(CarbonCalculationFactor.class))).thenReturn(carbonCalculation);

        String result = carbonCalculatorService.startCalculation(requestDTO);

        assertNotNull(result);
        assertEquals("123", result);
    }

    @Test
    void updateCalculation() {
        UpdateCalcInfoRequestDTO updateCalc = new UpdateCalcInfoRequestDTO();
        updateCalc.setId("123");
        updateCalc.setEnergyConsumption(100);
        updateCalc.setTransportation(List.of(new TransportationDTO(TransportationType.CAR, 2)));
        updateCalc.setSolidWasteTotal(50);
        updateCalc.setRecyclePercentage(0.2);

        CarbonCalculationFactor carbonCalculation = new CarbonCalculationFactor();
        carbonCalculation.setId("123");
        carbonCalculation.setName("Leilany Ulisses");
        carbonCalculation.setEmail("leilany@example.com");
        carbonCalculation.setPhoneNumber("123456");
        carbonCalculation.setUf("PE");
        when(carbonCalculationRepository.findById("123")).thenReturn(Optional.of(carbonCalculation));

        when(carbonCalculationRepository.save(any(CarbonCalculationFactor.class))).thenReturn(carbonCalculation);

        carbonCalculatorService.updateCalculation(updateCalc);

        assertEquals("123", carbonCalculation.getId());
        assertEquals("Leilany Ulisses", carbonCalculation.getName());
        assertEquals("leilany@example.com", carbonCalculation.getEmail());
        assertEquals("123456", carbonCalculation.getPhoneNumber());
        assertEquals("PE", carbonCalculation.getUf());
        assertEquals(100, carbonCalculation.getEnergyConsumption());
        assertNotNull(carbonCalculation.getTransportation());
        assertEquals(1, carbonCalculation.getTransportation().size());
        assertEquals(TransportationType.CAR, carbonCalculation.getTransportation().get(0).getType());
        assertEquals(2, carbonCalculation.getTransportation().get(0).getMonthlyDistance());
        assertEquals(50, carbonCalculation.getSolidWasteTotal());
        assertEquals(0.2, carbonCalculation.getRecyclePercentage());

    }

    @Test
    void calculateCarbonFootprint() {
        CarbonCalculationFactor carbonCalculation = new CarbonCalculationFactor();
        carbonCalculation.setId("123");
        carbonCalculation.setUf("PE");
        carbonCalculation.setEnergyConsumption(100);
        carbonCalculation.setTransportation(List.of(new TransportationDTO(TransportationType.CAR, 2)));
        carbonCalculation.setSolidWasteTotal(50);
        carbonCalculation.setRecyclePercentage(0.2);

        when(carbonCalculationRepository.findById("123")).thenReturn(Optional.of(carbonCalculation));

        EnergyEmissionFactor energyEmissionFactor = new EnergyEmissionFactor();
        energyEmissionFactor.setFactor(0.3);
        when(energyEmissionFactorRepository.findByUf("PE")).thenReturn(Optional.of(energyEmissionFactor));

        TransportationEmissionFactor transportationEmissionFactor = new TransportationEmissionFactor();
        transportationEmissionFactor.setFactor(0.5);
        when(transportationEmissionFactorRepository.findByType(TransportationType.CAR.toString())).thenReturn(Optional.of(transportationEmissionFactor));

        SolidWasteEmissionFactor solidWasteEmissionFactor = new SolidWasteEmissionFactor();
        solidWasteEmissionFactor.setRecyclableFactor(0.1);
        solidWasteEmissionFactor.setNonRecyclableFactor(0.2);
        when(solidWasteEmissionFactorRepository.findByUf("PE")).thenReturn(Optional.of(solidWasteEmissionFactor));

        CarbonCalculationResultDTO result = carbonCalculatorService.calculateCarbonFootprint("123");

        assertNotNull(result);
        assertEquals(30.0, result.getEnergy());
        assertEquals(1.0, result.getTransportation());
        assertEquals(15.0, result.getSolidWaste());
        assertEquals(46.0, result.getTotal());
    }
}