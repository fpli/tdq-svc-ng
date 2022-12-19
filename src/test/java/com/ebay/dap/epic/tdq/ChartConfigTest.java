package com.ebay.dap.epic.tdq;

import com.ebay.dap.epic.tdq.data.dto.ChartConfig;
import com.ebay.dap.epic.tdq.data.dto.DatasetConfig;
import com.ebay.dap.epic.tdq.data.entity.ChartInfoEntity;
import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.data.vo.DataSetVO;
import com.ebay.dap.epic.tdq.service.impl.BatchMetricServiceImpl;
import com.ebay.dap.epic.tdq.service.impl.ChartServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartConfigTest {

    @org.junit.Test
    public void f5(){
        System.out.println(Long.valueOf(BigDecimal.valueOf(7.369707384E9).toPlainString()));
    }

    @Test
    public void f4(){
        LocalDate localDateTime = LocalDate.parse("2022-12-14", BatchMetricServiceImpl.dateTimeFormatter);
        System.out.println(localDateTime.atTime(LocalTime.of(0, 1)));
        System.out.println(localDateTime);
    }

    @Test
    public void f3() throws IOException {
        ChartServiceImpl chartService = new ChartServiceImpl();
        BatchMetricServiceImpl batchMetricService = new BatchMetricServiceImpl();
        batchMetricService.setRestHighLevelClient(Tes.f3());
        //chartService.setBatchMetricService(batchMetricService);
        LocalDate yesterday = chartService.getYesterday();
        System.out.println(yesterday);
        ChartInfoEntity chartEntity = new ChartInfoEntity();
        chartEntity.setMetricKeys("total_ubi_session_cnt, nonbot_ubi_and_bot_clav_cnt");
        String config = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("config/batch.json"), StandardCharsets.UTF_8);
        chartEntity.setViewCfg(config);
        Map<String, Map<String, Double>> map = chartService.build(chartEntity, yesterday);
        LocalDate localDate = yesterday;
        for (int i = 1; i < 10; i++) {
            localDate = localDate.minusDays(i);
            map.putAll(chartService.build(chartEntity, localDate));
        }
        ChartDataVO chartDataVO = new ChartDataVO();
        String viewCfg = chartEntity.getViewCfg();
        ObjectMapper objectMapper = new ObjectMapper();
        ChartConfig chartConfig = objectMapper.readValue(viewCfg, ChartConfig.class);
        System.out.println(map);
        ChartServiceImpl.convert(map, chartConfig, chartDataVO);
        for (DataSetVO dataset : chartDataVO.getDatasets()) {
            System.out.println(dataset);
            System.out.println();
        }
    }

    @Test
    public void f1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ChartConfig chartConfig = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("config/simple.json"), ChartConfig.class);
        System.out.println(chartConfig.getName());
        List<DatasetConfig> datasetConfigurations = chartConfig.getDatasetConfigurations();
        for (DatasetConfig datasetConfiguration : datasetConfigurations) {
            System.out.println(objectMapper.writeValueAsString(datasetConfiguration));
        }
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (long i = 0; i < 10; i++) {
            HashMap<String, Double> doubleHashMap = new HashMap<>();
            doubleHashMap.put("k1", 30.0 + i);
            map.put(i+"", doubleHashMap);
        }
        ChartDataVO chartDataVO = new ChartDataVO();
        ChartServiceImpl.convert(map, chartConfig, chartDataVO);
        System.out.println(chartDataVO);
    }

    @Test
    public void f2() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ChartConfig chartConfig = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("config/complex.json"), ChartConfig.class);
        System.out.println(chartConfig.getName());
        List<DatasetConfig> datasetConfigurations = chartConfig.getDatasetConfigurations();
        for (DatasetConfig datasetConfiguration : datasetConfigurations) {
            System.out.println(objectMapper.writeValueAsString(datasetConfiguration));
        }
        System.out.println("---------------------------------");
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (long i = 0; i < 10; i++) {
            HashMap<String, Double> doubleHashMap = new HashMap<>();
            doubleHashMap.put("k1", 30.0 + i);
            doubleHashMap.put("k2", 10.0 + i);
            map.put(i+"", doubleHashMap);
        }
        ChartDataVO chartDataVO = new ChartDataVO();
        ChartServiceImpl.convert(map, chartConfig, chartDataVO);
        for (DataSetVO dataset : chartDataVO.getDatasets()) {
            System.out.println(dataset);
            System.out.println();
        }
    }
}
