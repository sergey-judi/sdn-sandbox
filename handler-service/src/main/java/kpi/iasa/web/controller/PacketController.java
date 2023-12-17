package kpi.iasa.web.controller;

import kpi.iasa.service.FloodlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/packets")
public class PacketController {

  private final FloodlightService floodlightService;

  @GetMapping
  public String get() {
    String topology = floodlightService.getTopology();

    System.out.println(topology);

    return "success";
  }

  @PostMapping("/configure")
  public void configure() {

  }

}
