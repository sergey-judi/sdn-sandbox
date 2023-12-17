package kpi.iasa.service;

import kpi.iasa.service.client.FloodlightClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FloodlightService {

  private final FloodlightClient floodlightClient;

  public String getTopology() {
    return floodlightClient.getTopology();
  }

}
