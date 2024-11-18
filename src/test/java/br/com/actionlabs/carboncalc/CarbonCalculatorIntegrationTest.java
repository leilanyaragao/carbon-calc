package br.com.actionlabs.carboncalc;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class CarbonCalculatorIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarbonCalculationRepository carbonCalculationRepository;

    @Autowired
    private EnergyEmissionFactorRepository energyEmissionFactorRepository;

    @Autowired
    private TransportationEmissionFactorRepository transportationEmissionFactorRepository;

    @Autowired
    private SolidWasteEmissionFactorRepository solidWasteEmissionFactorRepository;

    private static CarbonCalculationFactor getCarbonCalculationFactor() {
        CarbonCalculationFactor carbonCalculation = new CarbonCalculationFactor();
        carbonCalculation.setId("123");
        carbonCalculation.setName("Leilany Ulisses");
        carbonCalculation.setEmail("leilany@example.com");
        carbonCalculation.setPhoneNumber("123456");
        carbonCalculation.setUf("PE");
        return carbonCalculation;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        carbonCalculationRepository.deleteAll();
        energyEmissionFactorRepository.deleteAll();
        transportationEmissionFactorRepository.deleteAll();
        solidWasteEmissionFactorRepository.deleteAll();

    }

    @Test
    void testStartCalculationIntegration() throws Exception {
        StartCalcRequestDTO requestDTO = new StartCalcRequestDTO();
        requestDTO.setName("Leilany Ulisses");
        requestDTO.setEmail("leilany@example.com");
        requestDTO.setPhoneNumber("123456");
        requestDTO.setUf("PE");

        mockMvc.perform(post("/open/start-calc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void testUpdateCalculationInfo() throws Exception {
        carbonCalculationRepository.save(getCarbonCalculationFactor());

        UpdateCalcInfoRequestDTO updateCalc = new UpdateCalcInfoRequestDTO();
        updateCalc.setId("123");
        updateCalc.setEnergyConsumption(100);
        updateCalc.setSolidWasteTotal(50);
        updateCalc.setRecyclePercentage(0.2);
        updateCalc.setTransportation(List.of(new TransportationDTO(TransportationType.CAR, 2)));

        mockMvc.perform(put("/open/info")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateCalc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetCalculationResult() throws Exception {
        CarbonCalculationFactor carbonCalculation = getCarbonCalculationFactor();
        carbonCalculation.setEnergyConsumption(100);
        carbonCalculation.setTransportation(List.of(new TransportationDTO(TransportationType.CAR, 2)));
        carbonCalculation.setSolidWasteTotal(50);
        carbonCalculation.setRecyclePercentage(0.2);
        carbonCalculationRepository.save(carbonCalculation);

        EnergyEmissionFactor energyEmissionFactor = new EnergyEmissionFactor();
        energyEmissionFactor.setUf("PE");
        energyEmissionFactor.setFactor(0.3);
        energyEmissionFactorRepository.save(energyEmissionFactor);

        TransportationEmissionFactor transportationEmissionFactor = new TransportationEmissionFactor();
        transportationEmissionFactor.setType(TransportationType.CAR);
        transportationEmissionFactor.setFactor(0.5);
        transportationEmissionFactorRepository.save(transportationEmissionFactor);

        SolidWasteEmissionFactor solidWasteEmissionFactor = new SolidWasteEmissionFactor();
        solidWasteEmissionFactor.setUf("PE");
        solidWasteEmissionFactor.setRecyclableFactor(0.1);
        solidWasteEmissionFactor.setNonRecyclableFactor(0.2);
        solidWasteEmissionFactorRepository.save(solidWasteEmissionFactor);

        mockMvc.perform(get("/open/result/{id}", carbonCalculation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energy").value(30.0))
                .andExpect(jsonPath("$.solidWaste").value(15.0))
                .andExpect(jsonPath("$.transportation").value(1.0))
                .andExpect(jsonPath("$.total").value(46.0));
    }

}
