package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.ebay.dap.epic.tdq.data.entity.scorecard.CheckItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;



class CheckItemMapperTest extends AbstractMapperTest {

    @Autowired
    private CheckItemMapper checkItemMapper;

    @Test
    void testInsert() {
        CheckItemEntity entity = new CheckItemEntity();
        entity.setMetricKey("abc");
        entity.setCategory("ABC");
        entity.setExecuteOrder(1);

        checkItemMapper.insert(entity);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getMetricKey()).isEqualTo("abc");
        assertThat(entity.getCategory()).isEqualTo("ABC");
        assertThat(entity.getExecuteOrder()).isEqualTo(1);
    }
}