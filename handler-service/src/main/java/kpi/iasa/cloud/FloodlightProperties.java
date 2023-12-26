package kpi.iasa.cloud;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.Map;

@ConfigurationProperties("floodlight")
public record FloodlightProperties(
    @NotNull URI topologyUri,
    @NotNull URI switchesUri,
    @NotNull URI flowsUri,
    @Valid @NotNull PrioritizationProperties prioritization
) {

  public boolean isPrioritizationDisabled() {
    return !prioritization.enabled;
  }

  public record PrioritizationProperties(
      @NotNull Boolean enabled,
      @NotBlank String meterSwitch,
      @NotBlank String queueSwitch,
      @NotNull Map<String, String> flows
  ) {}

}
