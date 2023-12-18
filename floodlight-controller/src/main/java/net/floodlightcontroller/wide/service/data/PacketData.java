package net.floodlightcontroller.wide.service.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PacketData {

  String switchId;
  Integer switchPort;
  String type;
  String protocol;
  Integer sourcePort;
  Integer destinationPort;
  String sourceIp;
  String destinationIp;
  String sourceMac;
  String destinationMac;
  Integer totalLength;
  byte[] data;
  String bufferId;
  Short checksum;

}
