package kpi.iasa.service.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SwitchConfigRequest {

  @JsonProperty("dpid-meter-switch")
  String meterSwitch;

  @JsonProperty("dpid-queue-switch")
  String queueSwitch;

}
