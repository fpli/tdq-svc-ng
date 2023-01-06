package com.ebay.dap.epic.tdq.service.mmd;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Component
public class MMDClient {

    @Autowired
    @Qualifier("mmdRestTemplate")
    private RestTemplate restTemplate;

    public MMDResult findAnomaly(MMDRequest mmdRequest) {
        int retryAttempt = 5;
        MMDResult result = null;

        while (retryAttempt > 0) {
            try {
                // sleep 5s between every retry
                if (retryAttempt < 5) {
                    Thread.sleep(5000);
                }
                result = restTemplate.postForObject("/", mmdRequest, MMDResult.class);
                if (result != null && result.getCode() == 200) {
                    // success get result
                    break;
                }
                retryAttempt--;
            } catch (Exception e) {
                log.error("Failed to call MMD find-anomaly api", e);
                retryAttempt--;
            }
        }

        return result;
    }
}
