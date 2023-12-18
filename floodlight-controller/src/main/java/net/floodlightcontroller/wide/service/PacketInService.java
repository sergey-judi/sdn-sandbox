package net.floodlightcontroller.wide.service;

import lombok.Getter;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.wide.service.data.PacketData;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PacketInService {

  public static final List<PacketData> packets = new ArrayList<>();

  public void handlePacketIn(IOFSwitch iofSwitch, OFPacketIn message, FloodlightContext context) {
    Match match = message.getMatch();

    log("In Port: %s", match.get(MatchField.IN_PORT));

    Ethernet ethernet = IFloodlightProviderService.bcStore.get(context, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
    EthType etherType = ethernet.getEtherType();

    log("Payload type: " + ethernet.getPayload().getClass().getSimpleName());

    if (etherType == EthType.IPv4) {
      IPv4 ipv4 = (IPv4) ethernet.getPayload();
      IpProtocol protocol = ipv4.getProtocol();

      if (protocol == IpProtocol.ICMP) {
        log("ICMP: %s", ipv4.getPayload());

        handleIcmpPacket(ipv4);
      } else if (protocol == IpProtocol.TCP) {
        log("TCP: %s", ipv4.getPayload());

        handleTcpPacket(ipv4);
      } else if (protocol == IpProtocol.UDP) {
        log("UDP: %s", ipv4.getPayload());

        handleUdpPacket(ipv4);
      } else {
        log("Unknown protocol: %s", protocol);
      }

    } else if (etherType == EthType.ARP) {
      ARP arp = (ARP) ethernet.getPayload();

      handleArpPacket(arp);
    } else {
      log("Unknown ethernet type: %s", etherType);
    }

    savePacketData(iofSwitch, message, ethernet);
  }

  public void savePacketData(IOFSwitch iofSwitch, OFPacketIn message, Ethernet ethernet) {
    EthType etherType = ethernet.getEtherType();
    PacketData packetData = new PacketData()
        .setSwitchId(iofSwitch.getId().toString())
        .setSwitchPort(message.getMatch().get(MatchField.IN_PORT).getPortNumber())
        .setType("IPv4");

    if (etherType == EthType.IPv4) {
      IPv4 ipv4 = (IPv4) ethernet.getPayload();
      IpProtocol protocol = ipv4.getProtocol();

      if (protocol == IpProtocol.ICMP) {
        ICMP packet = (ICMP) ipv4.getPayload();

        packetData.setProtocol("ICMP")
            .setSourcePort(null)
            .setDestinationPort(null)
            .setSourceIp(ipv4.getSourceAddress().toString())
            .setDestinationIp(ipv4.getDestinationAddress().toString())
            .setSourceMac(ethernet.getSourceMACAddress().toString())
            .setDestinationMac(ethernet.getDestinationMACAddress().toString())
            .setTotalLength(message.getTotalLen())
            .setData(message.getData())
            .setBufferId(message.getBufferId().toString())
            .setChecksum(packet.getChecksum());
      } else if (protocol == IpProtocol.TCP) {
        TCP packet = (TCP) ipv4.getPayload();

        packetData.setProtocol("TCP")
            .setSourcePort(packet.getSourcePort().getPort())
            .setDestinationPort(packet.getDestinationPort().getPort())
            .setSourceIp(ipv4.getSourceAddress().toString())
            .setDestinationIp(ipv4.getDestinationAddress().toString())
            .setSourceMac(ethernet.getSourceMACAddress().toString())
            .setDestinationMac(ethernet.getDestinationMACAddress().toString())
            .setTotalLength(message.getTotalLen())
            .setData(message.getData())
            .setBufferId(message.getBufferId().toString())
            .setChecksum(packet.getChecksum());
      } else if (protocol == IpProtocol.UDP) {
        UDP packet = (UDP) ipv4.getPayload();

        packetData.setProtocol("UDP")
            .setSourcePort(packet.getSourcePort().getPort())
            .setDestinationPort(packet.getDestinationPort().getPort())
            .setSourceIp(ipv4.getSourceAddress().toString())
            .setDestinationIp(ipv4.getDestinationAddress().toString())
            .setSourceMac(ethernet.getSourceMACAddress().toString())
            .setDestinationMac(ethernet.getDestinationMACAddress().toString())
            .setTotalLength(message.getTotalLen())
            .setData(message.getData())
            .setBufferId(message.getBufferId().toString())
            .setChecksum(packet.getChecksum());
      } else {
        log("Unknown protocol: %s", protocol);
      }

    } else if (etherType == EthType.ARP) {
      ARP arp = (ARP) ethernet.getPayload();

      packetData.setProtocol("ARP")
          .setSourcePort(null)
          .setDestinationPort(null)
          .setSourceIp(arp.getSenderProtocolAddress().toString())
          .setDestinationIp(arp.getTargetProtocolAddress().toString())
          .setSourceMac(ethernet.getSourceMACAddress().toString())
          .setDestinationMac(ethernet.getDestinationMACAddress().toString())
          .setTotalLength(message.getTotalLen())
          .setData(message.getData())
          .setBufferId(message.getBufferId().toString())
          .setChecksum(null);
    } else {
      log("Unknown ethernet type: %s", etherType);
    }

    packets.add(packetData);
  }

  public void handleIcmpPacket(IPv4 ipv4) {
    log(
        "Protocol: %s, Source: %s, Destination: %s, Payload: %s, Code: %s, Type: %s, Total length: %s",
        ipv4.getProtocol(),
        ipv4.getSourceAddress(),
        ipv4.getDestinationAddress(),
        ipv4.getPayload(),
        ((ICMP) ipv4.getPayload()).getIcmpType(),
        ((ICMP) ipv4.getPayload()).getIcmpCode(),
        ipv4.getTotalLength()
    );
  }

  public void handleTcpPacket(IPv4 ipv4) {
    log(
        "Protocol: %s, Source: %s, Destination: %s, Payload: %s, Source port: %s, Destination port: %s, Total length: %s",
        ipv4.getProtocol(),
        ipv4.getSourceAddress(),
        ipv4.getDestinationAddress(),
        ipv4.getPayload(),
        ((TCP) ipv4.getPayload()).getSourcePort(),
        ((TCP) ipv4.getPayload()).getDestinationPort(),
        ipv4.getTotalLength()
    );
  }

  public void handleUdpPacket(IPv4 ipv4) {
    log(
        "Protocol: %s, Source: %s, Destination: %s, Payload: %s, Source port: %s, Destination port: %s, Total length: %s",
        ipv4.getProtocol(),
        ipv4.getSourceAddress(),
        ipv4.getDestinationAddress(),
        ipv4.getPayload(),
        ((UDP) ipv4.getPayload()).getSourcePort(),
        ((UDP) ipv4.getPayload()).getDestinationPort(),
        ipv4.getTotalLength()
    );
  }

  public void handleArpPacket(ARP arp) {
    log(
        "Protocol: %s, Source: %s, Destination: %s, Payload: %s, Op code: %s",
        arp.getProtocolType(),
        arp.getSenderProtocolAddress(),
        arp.getTargetProtocolAddress(),
        arp.getPayload(),
        arp.getOpCode()
    );
  }

  public void log(String template, Object... params) {
    log(
        String.format(template, params)
    );
  }

  public void log(Object obj) {
    log("" + obj);
  }

  public void log(String message) {
    System.out.println(message);
  }

//  public void createMatch(IOFSwitch iofSwitch) {
//    log("Composing custom match");
//    List<OFAction> actions = new ArrayList<>();
//
//    OFActionSetField setDataLayerDestination = ofFactory.actions()
//        .buildSetField()
//        .setField(
//            ofFactory.oxms()
//                .buildEthDst()
//                .setValue(MacAddress.of("7e:65:97:89:60:52"))
//                .build()
//        )
//        .build();
//
//    OFActionSetField setNetworkLayerDestination = ofFactory.actions()
//        .buildSetField()
//        .setField(
//            ofFactory.oxms()
//                .buildIpv4Dst()
//                .setValue(IPv4Address.of("10.0.0.3"))
//                .build()
//        )
//        .build();
//
//    OFActionOutput output = ofFactory.actions()
//        .buildOutput()
//        .setMaxLen(0xFFffFFff)
//        .setPort(OFPort.of(3))
//        .build();
//
//    actions.add(setDataLayerDestination);
//    actions.add(setNetworkLayerDestination);
//    actions.add(ofFactory.actions().popVlan());
//    actions.add(output);
//
//    OFInstructionApplyActions instructions = ofFactory.instructions().buildApplyActions()
//        .setActions(actions)
//        .build();
//
//    Match customMatch = ofFactory.buildMatch()
//        .setExact(MatchField.IN_PORT, OFPort.of(2))
////          .setExact(MatchField.IN_PORT, match.get(MatchField.IN_PORT))
////            .setExact(MatchField.ETH_TYPE, EthType.IPv4)
////            .setExact(MatchField.IP_PROTO, IpProtocol.TCP)
////          .setExact(MatchField.IPV4_SRC, ipv4.getSourceAddress())
////          .setExact(MatchField.IPV4_DST, ipv4.getDestinationAddress())
////            .setExact(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.1"))
////            .setExact(MatchField.IPV4_DST, IPv4Address.of("10.0.0.2"))
//        .build();
//
//    OFFlowAdd flowAdd = ofFactory.buildFlowAdd()
////          .setBufferId(message.getBufferId())
////          .setBufferId(OFBufferId.NO_BUFFER)
//        .setHardTimeout(10000)
//        .setIdleTimeout(10000)
//        .setPriority(12345)
//        .setTableId(TableId.of(0))
//        .setOutPort(OFPort.of(3))
//        .setMatch(customMatch)
//        .setInstructions(Collections.singletonList(instructions))
//        .build();
//
//    log("Finished composing custom match");
////        iofSwitch.write(flowAdd);
//
//    log(staticEntryPusherService.getEntries(iofSwitch.getId()));
//
//    staticEntryPusherService.addFlow("custom-flow-1", flowAdd, iofSwitch.getId());
//
//    log("Wrote custom match to switch");
//    log(staticEntryPusherService.getEntries(iofSwitch.getId()));
//
//    try {
//      log(InstructionUtils.applyActionsToString(instructions));
//    } catch (Exception exception) {
//
//    }
//
////      if (ipv4.getProtocol().equals(IpProtocol.TCP)) {
////        TCP tcp = (TCP) ipv4.getPayload();
////        srcPort = tcp.getSourcePort();
////      } else if (ipv4.getProtocol().equals(IpProtocol.UDP)) {
////        UDP udp = (UDP) ipv4.getPayload();
////        srcPort = udp.getSourcePort();
////      }
//
////      if (srcPort != null) {
////        System.out.println("Traffic Type: " + identifyTrafficType(srcPort.getPort()));
////      }
//  }

}
