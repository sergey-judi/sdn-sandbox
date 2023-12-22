package kpi.iasa.service.client;

import kpi.iasa.cloud.FloodlightProperties;
import kpi.iasa.service.model.request.FlowRequest;
import kpi.iasa.service.model.request.SwitchConfigRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloodlightClient {

  private final Set<SwitchConfigRequest> linkedSwitches = new HashSet<>();
  private final Set<FlowRequest> registeredFlows = new HashSet<>();

  private final RestClient floodlightRestClient;
  private final RestTemplate floodlightRestTemplate;
  private final FloodlightProperties floodlightProperties;

  public String getTopology() {
    return floodlightRestClient.get()
        .uri(floodlightProperties.topologyUri())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
  }

  public synchronized void linkSwitches(SwitchConfigRequest request) {
    if (linkedSwitches.contains(request)) {
      return;
    }

    log.info("Trying to delete switches link with request=[{}]", request);
    ResponseEntity<Void> responseEntity = floodlightRestTemplate.exchange(
        floodlightProperties.switchesUri(),
        HttpMethod.DELETE,
        new HttpEntity<>(request),
        Void.class
    );
    log.info("Successfully deleted switches link with response status code=[{}]", responseEntity.getStatusCode());

    log.info("Trying to link switches with request=[{}]", request);
    floodlightRestClient.post()
        .uri(floodlightProperties.switchesUri())
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve();
    log.info("Successfully linked switches with response status code=[{}]", responseEntity.getStatusCode());

    linkedSwitches.add(request);
  }

  public synchronized void registerFlow(FlowRequest request) {
    String destinationIp = floodlightProperties.prioritization()
        .flows()
        .get(request.getSourceIp());

    if (registeredFlows.contains(request)
        || !Objects.equals(request.getDestinationIp(), destinationIp)) {
      return;
    }

    log.info("Trying to register flow with request=[{}]", request);
    ResponseEntity<Void> responseEntity = floodlightRestClient.post()
        .uri(floodlightProperties.flowsUri())
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .toBodilessEntity();
    log.info("Successfully registered flow with response status code=[{}]", responseEntity.getStatusCode());

    registeredFlows.add(request);
  }

}
