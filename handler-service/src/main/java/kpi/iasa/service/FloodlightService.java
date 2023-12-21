package kpi.iasa.service;

import kpi.iasa.service.client.FloodlightClient;
import kpi.iasa.web.model.request.PacketConfigurationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloodlightService {

  private final FloodlightClient floodlightClient;

  public String getTopology() {
    return floodlightClient.getTopology();
  }

  public void configure(PacketConfigurationRequest request) {
    log.info("{}", request);
  }

}
