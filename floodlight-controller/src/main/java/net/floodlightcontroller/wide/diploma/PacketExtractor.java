package net.floodlightcontroller.wide.diploma;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.BasePacket;
import net.floodlightcontroller.staticentry.IStaticEntryPusherService;
import net.floodlightcontroller.wide.service.PacketInService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import sun.misc.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PacketExtractor implements IFloodlightModule, IOFMessageListener {

  private final OFFactory ofFactory = OFFactories.getFactory(OFVersion.OF_13);

  private IFloodlightProviderService floodlightProvider;
  private IStaticEntryPusherService staticEntryPusherService;
  private IOFSwitchService switchService;
  private PacketInService packetInService;

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
    staticEntryPusherService = context.getServiceImpl(IStaticEntryPusherService.class);
    switchService = context.getServiceImpl(IOFSwitchService.class);
    packetInService = new PacketInService(ofFactory, staticEntryPusherService);
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
  public Command receive(IOFSwitch ofSwitch, OFMessage message, FloodlightContext context) {
    BasePacket packet = IFloodlightProviderService.bcStore.get(context, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

    OFPacketIn packetIn = (OFPacketIn) message;
    Match match = packetIn.getMatch();

    packetInService.log(message);

    System.out.println(MatchField.IN_PORT.arePrerequisitesOK(match));
    System.out.println(MatchField.IPV4_SRC.arePrerequisitesOK(match));

    System.out.println("\n=======");
    System.out.println(ofSwitch.getActions());
    System.out.println(ofSwitch.getOFFactory().getVersion());
    System.out.println(staticEntryPusherService.getEntries(ofSwitch.getId()));
    System.out.println(ofSwitch.getAttributes());
    System.out.println(ofSwitch.getEnabledPorts());
    System.out.println(ofSwitch.getEnabledPortNumbers());
    System.out.println(ofSwitch.getTables());
    System.out.println("---");
    System.out.println(match);
    System.out.println("---");
    System.out.println("=======\n");

    packetInService.log("%s", packet);
    packetInService.log("%s", packetIn);

    packetInService.handlePacketIn(ofSwitch, packetIn, context);

    CompletableFuture.runAsync(this::testHttp);

    return Command.CONTINUE;
  }

  private void testHttp() {
    CloseableHttpClient httpClient = HttpClients.createDefault();

    try (CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet("https://official-joke-api.appspot.com/random_joke"))) {
      try (InputStream responseBody = httpResponse.getEntity().getContent()) {
        String content = new String(IOUtils.readAllBytes(responseBody));
        System.out.println("Response Content: " + content);
      }
    } catch (Exception ex) {
      packetInService.log("Exception occurred: " + ex.getMessage());
    }
  }

}
