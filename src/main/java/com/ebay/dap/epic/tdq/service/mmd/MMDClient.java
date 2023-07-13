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

    public MMDResult findAnomaly(MMDRequest mmdRequest) throws MMDException {
        try {
            log.info("mmdRequest: {}",  objectMapper.writeValueAsString(mmdRequest));
        } catch (JsonProcessingException e) {
            throw new MMDException(e);
        }

        MMDResult result = null;

        // retry 5 times for http api call
        int retryAttempt = 5;
        while (retryAttempt > 0) {
            try {
                // sleep 5s between every retry
                if (retryAttempt < 5) {
                    Thread.sleep(5000L);
                }
                result = restTemplate.postForObject("/", mmdRequest, MMDResult.class);
                if (result != null && result.getCode() == 200) {
                    // success get results, exit loop
                    break;
                }
                retryAttempt--;
            } catch (Exception e) {
                log.error("Failed to call MMD find-anomaly api", e);
                retryAttempt--;
            }
        }

        // throw exception if there is still no success response after 5 attempts
        if (result == null) {
            throw new MMDException("Error when calling MMD");
        }

        log.info("MMD result: {}", result);
        return result;
    }
}
