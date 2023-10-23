package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.ebay.dap.epic.tdq.data.entity.ChartInfoEntity;
import com.ebay.dap.epic.tdq.data.enums.ChartMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ChartInfoMapperTest extends AbstractMapperTest {

    @Autowired
    private ChartInfoMapper mapper;

    @Test
    void testInsert() {
        ChartInfoEntity entity = new ChartInfoEntity();
        entity.setName("My Chart");
        entity.setMetricKeys("my_key");
        //TODO: remove this line, ut will fail, check root cause
        entity.setDispOrder(1);
        entity.setMode(ChartMode.BY_DIMENSION);

        mapper.insert(entity);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getName()).isEqualTo("My Chart");
        assertThat(entity.getDispOrder()).isEqualTo(1);
        assertThat(entity.getMetricKeys()).isEqualTo("my_key");

    }
}