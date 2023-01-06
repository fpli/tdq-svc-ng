package com.ebay.dap.epic.tdq.service.mmd;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GlobalConfig {
    String clientAppDomainId;
    Boolean noiseRemoval;
    String modelType;
    String configType;
    CustomParams customParams;
    String checkPoint;
}