package mycode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import mycode.service.ChartService;
import mycode.dto.SonarMetricDTO;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChartController {

  @Autowired
  ChartService chartService;

  @Autowired
  private ObjectMapper objectMapper;

  @GetMapping("/charts")
  public String showCharts(Model model) {
    try {
      List<SonarMetricDTO> events = chartService.getVcsEvents();

      Map<String, Integer> data1 = prepareClassesMetrics(events);
      Map<String, Integer> data2 = prepareVulnerabilitiesMetrics(events);

      // Convert Maps to JSON Strings for use in JavaScript in the Thymeleaf template
      String classesDataJson = objectMapper.writeValueAsString(data1);
      String vulnerabilityDataJson = objectMapper.writeValueAsString(data2);

      model.addAttribute("classesDataJson", classesDataJson);
      model.addAttribute("vulnerabilityDataJson", vulnerabilityDataJson);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return "charts";
  }

  private Map<String, Integer> prepareClassesMetrics(List<SonarMetricDTO> events) {
    Map<String, Integer> commitMap = new HashMap<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    for (SonarMetricDTO event : events) {
      if ("classes".equals(event.getMetricName())) {
        String timestamp = dateFormat.format(event.getTimestamp());
        commitMap.put(timestamp, Integer.valueOf(event.getComponent()));
      }
    }
    return commitMap;
  }

  private Map<String, Integer> prepareVulnerabilitiesMetrics(List<SonarMetricDTO> events) {
    Map<String, Integer> prMap = new HashMap<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    for (SonarMetricDTO event : events) {
      if ("vulnerabilities".equals(event.getMetricName())) {
        String timestamp = dateFormat.format(event.getTimestamp());
        prMap.put(timestamp, Integer.valueOf(event.getComponent()));
      }
    }
    return prMap;
  }

}
