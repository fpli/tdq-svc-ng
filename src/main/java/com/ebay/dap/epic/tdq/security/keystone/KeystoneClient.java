package com.ebay.dap.epic.tdq.security.keystone;

import com.ebay.dap.epic.tdq.security.keystone.response.KeystoneAuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class KeystoneClient {

  private final RestTemplate restTemplate;

  public KeystoneClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public KeystoneAuthResponse auth(KeystoneRequest request) {
    ResponseEntity<KeystoneAuthResponse> responseEntity;
    try {
      responseEntity = restTemplate
          .postForEntity("/v2.0/tokens", request, KeystoneAuthResponse.class);

      if (responseEntity.getStatusCode().is2xxSuccessful()) {

        KeystoneAuthResponse body = responseEntity.getBody();
        if (body != null && body.getAccess() != null &&
            body.getAccess().getUser() != null) {
          log.info("Keystone auth success: {}", body);
          return body;
        }
      }
    } catch (Unauthorized e) {
      log.error("Keystone auth failed, 401 Unauthorized", e);
      throw new BadCredentialsException("Keystone auth failed, 401 Unauthorized");
    }
    throw new BadCredentialsException("Keystone auth failed: " + responseEntity.toString());
  }
}
