package kpi.iasa.service.client;

import kpi.iasa.cloud.FloodlightProperties;
import kpi.iasa.service.model.request.FlowRequest;
import kpi.iasa.service.model.request.SwitchConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class FloodlightClient {

  private final RestClient floodlightRestClient;
  private final FloodlightProperties floodlightProperties;

  public String getTopology() {
    return floodlightRestClient.get()
        .uri(floodlightProperties.topologyUri())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
  }

  public void linkSwitches(SwitchConfigRequest request) {

  }

  public void registerFlow(FlowRequest request) {

  }

}
