package br.com.actionlabs.carboncalc.rest;

import br.com.actionlabs.carboncalc.dto.*;
import br.com.actionlabs.carboncalc.service.CarbonCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
@Slf4j
public class OpenRestController {

    @Autowired
    private CarbonCalculatorService carbonCalculatorService;

    @PostMapping("start-calc")
  public ResponseEntity<StartCalcResponseDTO> startCalculation(
      @RequestBody StartCalcRequestDTO request) {
        String id = carbonCalculatorService.startCalculation(request);
        StartCalcResponseDTO responseDTO = new StartCalcResponseDTO();
        responseDTO.setId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
  }

  @PutMapping("info")
  public ResponseEntity<UpdateCalcInfoResponseDTO> updateInfo(
      @RequestBody UpdateCalcInfoRequestDTO request) {
      carbonCalculatorService.updateCalculation(request);
      UpdateCalcInfoResponseDTO responseDTO = new UpdateCalcInfoResponseDTO();
      responseDTO.setSuccess(true);
      return ResponseEntity.ok(responseDTO);
  }

  @GetMapping("result/{id}")
  public ResponseEntity<CarbonCalculationResultDTO> getResult(@PathVariable String id) {
      CarbonCalculationResultDTO resultDTO = carbonCalculatorService.calculateCarbonFootprint(id);
      return ResponseEntity.ok(resultDTO);
  }
}
