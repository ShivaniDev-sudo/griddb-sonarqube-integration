package mycode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

import com.toshiba.mwcloud.gs.RowKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SonarMetricDTO {
  @RowKey
  public Date timestamp; // Time of the activity
  private String metricName; // e.g., coverage, bugs
  private String metricValue; // e.g., 80.5, 2
  private String component; // Project key (optional, for identification)
}
