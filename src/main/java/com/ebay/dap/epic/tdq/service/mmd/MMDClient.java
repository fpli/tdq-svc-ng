package com.ebay.dap.epic.tdq.service.mmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class MMDClient {

    @Autowired
    @Qualifier("mmdRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public MMDResult findAnomaly(MMDRequest mmdRequest) throws MMDRestException {
        try {
            log.info("mmdRequest: {}",  objectMapper.writeValueAsString(mmdRequest));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

        if (result == null) {
            throw new MMDRestException("Error when calling MMD");
        }

        log.info("MMD result: {}", result);
        return result;
    }
}
