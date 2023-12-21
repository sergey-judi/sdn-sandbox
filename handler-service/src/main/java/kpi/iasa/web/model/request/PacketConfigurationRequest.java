package kpi.iasa.web.model.request;

public record PacketConfigurationRequest(
    String switchId,
    Integer switchPort,
    String type,
    String protocol,
    Integer sourcePort,
    Integer destinationPort,
    String sourceIp,
    String destinationIp,
    String sourceMac,
    String destinationMac,
    Integer totalLength,
    String data,
    String bufferId,
    Short checksum
) {}
