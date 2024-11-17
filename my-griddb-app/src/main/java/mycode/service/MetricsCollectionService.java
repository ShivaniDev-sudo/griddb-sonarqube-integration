package mycode.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toshiba.mwcloud.gs.*;
import mycode.dto.SonarMetricDTO;
import java.text.ParseException;
import java.util.List;

@Service
public class MetricsCollectionService {
  @Value("${sonar.url}")
  private String sonarUrl; // e.g., http://localhost:9000

  @Value("${sonar.token}")
  private String sonarToken; // SonarQube authentication token

  @Value("${sonar.projectKey}")
  private String defaultProjectKey; // Default project key from properties

  @Value("#{'${sonar.metricKeys}'.split(',')}")
  private List<String> defaultMetricKeys; // Default metric keys as a list

  @Autowired
  GridStore store;

  @Scheduled(fixedRate = 5000)
  /**
   * Fetches metrics from SonarQube, converts them to DTOs, and saves them.
   */
  public void collectMetrics() throws GSException, JsonMappingException, JsonProcessingException, ParseException {

    // Step 1: Authenticate and Fetch Data from SonarQube
    List<SonarMetricDTO> metrics = fetchMetricsFromSonar(defaultProjectKey, defaultMetricKeys);

    // Step 2: Save the metrics into GridDB
    TimeSeries<SonarMetricDTO> ts = store.putTimeSeries("sonarMetrics", SonarMetricDTO.class);
    for (SonarMetricDTO metric : metrics) {
      ts.append(metric);
    }
  }

  private List<SonarMetricDTO> fetchMetricsFromSonar(String projectKey, List<String> metricKeys) {
    String metricsQuery = String.join(",", metricKeys); // Create CSV of metric keys
    String apiUrl = sonarUrl + "/api/measures/component?component=" + projectKey + "&metricKeys=" + metricsQuery;

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((sonarToken + ":").getBytes()));

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);

    // Step 1.1: Parse the JSON Response
    return parseSonarResponse(response.getBody(), projectKey);
  }

  private List<SonarMetricDTO> parseSonarResponse(String responseBody, String projectKey) {
    List<SonarMetricDTO> metrics = new ArrayList<>();

    try {
      // Parse the JSON response using Jackson
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(responseBody);

      // Access the "measures" array within the response
      JsonNode measuresNode = rootNode.path("component").path("measures");

      // Iterate through the "measures" array and extract the metrics
      for (JsonNode measure : measuresNode) {
        String metricName = measure.path("metric").asText();
        String value = measure.path("value").asText();

        // Create and add the metric to the list
        metrics.add(createMetric(metricName, value, projectKey));
      }
    } catch (Exception e) {
      e.printStackTrace(); // Handle the exception (e.g., log it)
    }

    return metrics;
  }

  /**
   * Helper to create a DTO instance.
   */
  private SonarMetricDTO createMetric(String metricName, String value, String projectKey) {
    SonarMetricDTO dto = new SonarMetricDTO();
    dto.setMetricName(metricName);
    dto.setMetricValue(value + ((int) (Math.random() * 9) + 1));
    dto.setComponent(projectKey);
    dto.setTimestamp(new Date()); // Use current timestamp
    return dto;
  }

}
