package kpi.iasa.cloud;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties("floodlight")
public record FloodlightProperties(
    @NotNull URI topologyUri,
    @NotNull URI switchesUri,
    @NotNull URI flowsUri
) {}
