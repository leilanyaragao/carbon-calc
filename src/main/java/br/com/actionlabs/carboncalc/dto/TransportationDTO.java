package br.com.actionlabs.carboncalc.dto;

import br.com.actionlabs.carboncalc.enums.TransportationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransportationDTO {
  private TransportationType type;
  private int monthlyDistance;
}
