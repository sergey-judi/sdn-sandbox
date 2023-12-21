package net.floodlightcontroller.wide.service.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.floodlightcontroller.wide.service.data.PacketData;
import net.floodlightcontroller.wide.service.properties.PacketHandlerProperties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import sun.misc.IOUtils;

import java.io.InputStream;

@Slf4j
public class PacketHandlerClient {

  private static final CloseableHttpClient client = HttpClients.createDefault();

  private static final ObjectMapper objectMapper = new ObjectMapper()
      .findAndRegisterModules()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  public static void process(PacketData packetData) {
    try (
        CloseableHttpResponse httpResponse = client.execute(buildRequest(packetData))
    ) {

      try (InputStream responseBody = httpResponse.getEntity().getContent()) {
        String content = new String(IOUtils.readAllBytes(responseBody));
        log.info("Response content: {}", content);
      }

    } catch (Exception ex) {
      log.error("Exception occurred: {}", ex.getMessage());
    }

  }

  @SneakyThrows
  private static HttpPost buildRequest(PacketData packetData) {
    HttpPost request = new HttpPost(PacketHandlerProperties.CONFIGURATION_URL);

    String serializedRequestBody = objectMapper.writeValueAsString(packetData);

    request.setEntity(new StringEntity(serializedRequestBody));
    request.setHeader("Content-Type", "application/json");

    return request;
  }

}
