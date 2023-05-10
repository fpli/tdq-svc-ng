package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.ebay.dap.epic.tdq.data.entity.scorecard.GroovyRuleDefEntity;
import com.ebay.dap.epic.tdq.data.enums.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
class GroovyRuleDefMapperTest extends AbstractMapperTest {

    @Autowired
    private GroovyRuleDefMapper mapper;

    @Autowired
    private CategoryResultMapper categoryResultMapper;

    @Test
    public void t1(){
        LocalDate maxDt = categoryResultMapper.getMaxDt();
        System.out.println(maxDt);
        LocalDate minDt = categoryResultMapper.getMinDt();
        System.out.println(minDt);
    }

    @Test
    void testInsert() {
        GroovyRuleDefEntity entity = new GroovyRuleDefEntity();
        entity.setName("myRule");
        entity.setMetricKeys("my_key");
        entity.setCategory(Category.COMPLETENESS);
        entity.setDefaultWeight(new BigDecimal("1"));
        entity.setGroovyScript("bla");

        mapper.insert(entity);

        List<GroovyRuleDefEntity> ruleDefEntities = mapper.selectList(null);
        log.info("{}", ruleDefEntities);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getName()).isEqualTo("myRule");
        assertThat(entity.getMetricKeys()).isEqualTo("my_key");
        assertThat(entity.getCategory()).isEqualTo(Category.COMPLETENESS);
        assertThat(entity.getDefaultWeight()).isEqualTo(new BigDecimal("1"));
        assertThat(entity.getGroovyScript()).isEqualTo("bla");

    }
}