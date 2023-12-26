package kpi.iasa.service;

import kpi.iasa.cloud.FloodlightProperties;
import kpi.iasa.service.client.FloodlightClient;
import kpi.iasa.service.model.request.FlowRequest;
import kpi.iasa.service.model.request.SwitchConfigRequest;
import kpi.iasa.web.model.request.PacketConfigurationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloodlightService {

  private final FloodlightClient floodlightClient;
  private final FloodlightProperties floodlightProperties;

  public String getTopology() {
    return floodlightClient.getTopology();
  }

  public void configure(PacketConfigurationRequest request) {
    if (floodlightProperties.isPrioritizationDisabled()) {
      return;
    }

    String meterSwitch = floodlightProperties.prioritization().meterSwitch();
    String queueSwitch = floodlightProperties.prioritization().queueSwitch();

    floodlightClient.linkSwitches(
        new SwitchConfigRequest()
            .setMeterSwitch(meterSwitch)
            .setQueueSwitch(queueSwitch)
    );

    floodlightClient.registerFlow(
        new FlowRequest()
            .setMeterSwitch(meterSwitch)
            .setQueueSwitch(queueSwitch)
            .setSourceIp(request.sourceIp())
            .setDestinationIp(request.destinationIp())
            .setBandwidth(ThreadLocalRandom.current().nextInt(1000, 10000))
    );
  }

}
