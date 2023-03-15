package com.ebay.dap.epic.tdq.dsl;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GroovyEngineTest {

    @Test
    void evalAsInt() {
        var text = """
                if ($metric_key > 1) {
                    return 2;
                } else {
                    return 3;
                }
                """;

        Map<String, Object> kv = new HashMap<>();
        kv.put("metric_key", 1);

        Integer result = GroovyEngine.evalAsInt(text, kv);

        assertThat(result).isEqualTo(3);

    }
}