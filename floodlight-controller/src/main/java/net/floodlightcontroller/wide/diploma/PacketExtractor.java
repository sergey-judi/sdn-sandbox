package net.floodlightcontroller.wide.diploma;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.BasePacket;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PacketExtractor implements IFloodlightModule, IOFMessageListener {

  private IFloodlightProviderService floodlightProvider;

  @Override
  public Collection<Class<? extends IFloodlightService>> getModuleServices() {
    return null;
  }

  @Override
  public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
    return null;
  }

  @Override
  public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
    List<Class<? extends IFloodlightService>> services = new ArrayList<>();

    services.add(IFloodlightProviderService.class);

    return services;
  }

  @Override
  public void init(FloodlightModuleContext context) throws FloodlightModuleException {
    floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
  }

  @Override
  public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
    floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
  }

  @Override
  public String getName() {
    return "packet-extractor-listener";
  }

  @Override
  public boolean isCallbackOrderingPrereq(OFType type, String name) {
    return false;
  }

  @Override
  public boolean isCallbackOrderingPostreq(OFType type, String name) {
    return false;
  }

  @Override
  public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
    BasePacket pkt = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

    OFPacketIn pin = (OFPacketIn) msg;
    Match match = pin.getMatch();

    System.out.println("\n=======");
    System.out.println(sw);
    System.out.println("---");
    System.out.println(msg);
    System.out.println("---");
    System.out.println(cntx);
    System.out.println("---");
    System.out.println(pkt);
    System.out.println("---");
    System.out.println(pin);
    System.out.println("---");
    System.out.println(match);
    System.out.println("---");
    System.out.println("=======\n");

    return Command.CONTINUE;
  }

}
