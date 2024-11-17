package mycode.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;

import mycode.dto.SonarMetricDTO;

@Service
public class ChartService {

  @Autowired
  GridStore store;

  public List<SonarMetricDTO> getVcsEvents() throws Exception {

    Container<?, Row> container = store.getContainer("sonarMetrics");
    if (container == null) {
      throw new Exception("Container not found.");
    }
    List<SonarMetricDTO> eventList = new ArrayList<>();


    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    Date now = new Date();

    String nowString = dateFormat.format(now);
    String startTime = "1971-12-23T18:18:52.000Z";

    String queryString = "select * where Timestamp >= TIMESTAMP('" + startTime
        + "') and Timestamp <= TIMESTAMP('" + nowString + "')";
    Query<Row> query = container.query(queryString);
    RowSet<Row> rs = query.fetch();

    while (rs.hasNext()) {
      Row row = rs.next();
      SonarMetricDTO event = new SonarMetricDTO();
      event.setTimestamp(row.getTimestamp(0));
      event.setMetricName(row.getString(1));
      event.setMetricValue(row.getString(2));
      event.setComponent(row.getString(3));

      eventList.add(event); 

    }
    return eventList;
  }

}
