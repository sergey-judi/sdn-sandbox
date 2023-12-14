package net.floodlightcontroller.wide.service;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.staticentry.IStaticEntryPusherService;
import net.floodlightcontroller.util.InstructionUtils;
import net.floodlightcontroller.util.MatchUtils;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketInService {

  private boolean wasSet = true;

  private final OFFactory ofFactory;
  private final IStaticEntryPusherService staticEntryPusherService;

  public PacketInService(OFFactory ofFactory, IStaticEntryPusherService staticEntryPusherService) {
    this.ofFactory = ofFactory;
    this.staticEntryPusherService = staticEntryPusherService;
  }

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
      } else if (protocol == IpProtocol.TCP) {
        log("TCP: %s", ipv4.getPayload());

        handleTcpPacket(ipv4);
      } else if (protocol == IpProtocol.UDP) {
        log("UDP: %s", ipv4.getPayload());
      } else {
        log("Unknown protocol: %s", protocol);
      }

      TransportPort srcPort = null;

      if (!wasSet) {
      log("Composing custom match");
      List<OFAction> actions = new ArrayList<>();

      OFActionSetField setDataLayerDestination = ofFactory.actions()
          .buildSetField()
          .setField(
              ofFactory.oxms()
                  .buildEthDst()
                  .setValue(MacAddress.of("7e:65:97:89:60:52"))
                  .build()
          )
          .build();

      OFActionSetField setNetworkLayerDestination = ofFactory.actions()
          .buildSetField()
          .setField(
              ofFactory.oxms()
                  .buildIpv4Dst()
                  .setValue(IPv4Address.of("10.0.0.3"))
                  .build()
          )
          .build();

      OFActionOutput output = ofFactory.actions()
          .buildOutput()
          .setMaxLen(0xFFffFFff)
          .setPort(OFPort.of(3))
          .build();

      actions.add(setDataLayerDestination);
      actions.add(setNetworkLayerDestination);
      actions.add(ofFactory.actions().popVlan());
      actions.add(output);

      OFInstructionApplyActions instructions = ofFactory.instructions().buildApplyActions()
          .setActions(actions)
          .build();

      Match customMatch = ofFactory.buildMatch()
          .setExact(MatchField.IN_PORT, OFPort.of(2))
//          .setExact(MatchField.IN_PORT, match.get(MatchField.IN_PORT))
//            .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//            .setExact(MatchField.IP_PROTO, IpProtocol.TCP)
//          .setExact(MatchField.IPV4_SRC, ipv4.getSourceAddress())
//          .setExact(MatchField.IPV4_DST, ipv4.getDestinationAddress())
//            .setExact(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.1"))
//            .setExact(MatchField.IPV4_DST, IPv4Address.of("10.0.0.2"))
          .build();

      OFFlowAdd flowAdd = ofFactory.buildFlowAdd()
//          .setBufferId(message.getBufferId())
//          .setBufferId(OFBufferId.NO_BUFFER)
          .setHardTimeout(10000)
          .setIdleTimeout(10000)
          .setPriority(12345)
          .setTableId(TableId.of(0))
          .setOutPort(OFPort.of(3))
          .setMatch(customMatch)
          .setInstructions(Collections.singletonList(instructions))
          .build();

      log("Finished composing custom match");
//        iofSwitch.write(flowAdd);

      log(staticEntryPusherService.getEntries(iofSwitch.getId()));

      staticEntryPusherService.addFlow("custom-flow-1", flowAdd, iofSwitch.getId());

      log("Wrote custom match to switch");
      log(staticEntryPusherService.getEntries(iofSwitch.getId()));

      try {
        log(InstructionUtils.applyActionsToString(instructions));
      } catch (Exception exception) {

      }

      wasSet = true;
      }

//      if (ipv4.getProtocol().equals(IpProtocol.TCP)) {
//        TCP tcp = (TCP) ipv4.getPayload();
//        srcPort = tcp.getSourcePort();
//      } else if (ipv4.getProtocol().equals(IpProtocol.UDP)) {
//        UDP udp = (UDP) ipv4.getPayload();
//        srcPort = udp.getSourcePort();
//      }

//      if (srcPort != null) {
//        System.out.println("Traffic Type: " + identifyTrafficType(srcPort.getPort()));
//      }
    } else if (etherType == EthType.ARP) {
      ARP arp = (ARP) ethernet.getPayload();

      handleArpPacket(arp);
    }
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

}
