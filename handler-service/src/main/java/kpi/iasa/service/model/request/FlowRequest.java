package kpi.iasa.service.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class FlowRequest {

  @JsonProperty("dpid-meter-switch")
  String meterSwitch;

  @JsonProperty("dpid-queue-switch")
  String queueSwitch;

  @JsonProperty("src-addr")
  String sourceIp;

  @JsonProperty("dst-addr")
  String destinationIp;

  @EqualsAndHashCode.Exclude
  Integer bandwidth;

}
