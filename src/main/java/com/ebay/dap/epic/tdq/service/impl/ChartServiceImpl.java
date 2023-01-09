package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.dto.ChartConfig;
import com.ebay.dap.epic.tdq.data.dto.DatasetConfig;
import com.ebay.dap.epic.tdq.data.entity.ChartInfoEntity;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ChartMapper;
import com.ebay.dap.epic.tdq.data.vo.*;
import com.ebay.dap.epic.tdq.service.BatchMetricService;
import com.ebay.dap.epic.tdq.service.ChartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChartServiceImpl implements ChartService {

    @Autowired
    private ChartMapper chartMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BatchMetricService batchMetricService;

    private static Script parseExpression(String expression) {
        GroovyShell groovyShell = new GroovyShell();
        return groovyShell.parse(expression);
    }

    private static <T> T exec(Script script) {
        T t = (T) script.run();
        return t;
    }

    public Map<String, Map<String, Double>> build(ChartInfoEntity chartEntity, LocalDate date) {
        String metricKeys = chartEntity.getMetricKeys();
        //todo: pass metric raw data

        // todo: get metric raw data from elasticsearch by metric_key
        MetricQueryParamVO metricQueryParamVO = new MetricQueryParamVO();
        metricQueryParamVO.setDate(date);
        String[] metricKeyArray = metricKeys.strip().split(",");
        // todo: iterate metricArray
        Map<String, Map<String, Double>> map = new HashMap<>();
        for (String key : metricKeyArray) {
            String strip = key.strip();
            metricQueryParamVO.setMetricKey(strip);
            MetricChartVO metricChartVO = batchMetricService.retrieveBatchMetric(metricQueryParamVO);
            List<MetricValueItemVO> metricValueItemVOList = metricChartVO.getMetricValueItemVOList();
            for (MetricValueItemVO metricValueItemVO : metricValueItemVOList) {
                String timestamp = metricValueItemVO.getTimestamp();
                map.compute(timestamp, (t, m) -> {
                    if (m == null) {
                        m = new HashMap<>();
                    }
                    m.put(strip, metricValueItemVO.getValue());
                    return m;
                });
            }
        }
        return map;
    }

    @Override
    public List<ChartVO> listChartInfoEntities() {
        List<ChartInfoEntity> chartInfoEntities = chartMapper.selectList(null);
        List<ChartVO> chartVOList = new ArrayList<>();
        ChartVO chartVO;
        for (ChartInfoEntity chartInfoEntity : chartInfoEntities) {
            chartVO = new ChartVO();
            chartVOList.add(chartVO);
            chartVO.setId(chartInfoEntity.getId());
            chartVO.setTitle(chartInfoEntity.getName());
            chartVO.setDescription(chartInfoEntity.getDescription());
            // ...
        }
        return chartVOList;
    }

    @Override
    public ChartDataVO retrieveChartData(Long id, LocalDate date) throws Exception {
        ChartInfoEntity chartEntity = chartMapper.selectById(id);

        Map<String, Map<String, Double>> map = build(chartEntity, date);
        ChartDataVO chartDataVO = new ChartDataVO();
        String viewCfg = chartEntity.getViewCfg();

        ChartConfig chartConfig = objectMapper.readValue(viewCfg, ChartConfig.class);
        chartDataVO.setLabels(map.keySet().stream().sorted().toList());
        convert(map, chartConfig, chartDataVO);
        return chartDataVO;
    }

    public static void convert(Map<String, Map<String, Double>> map, ChartConfig chartConfig, ChartDataVO chartDataVO) {
        List<DatasetConfig> datasetConfigurations = chartConfig.getDatasetConfigurations();
        for (DatasetConfig datasetConfiguration : datasetConfigurations) {
            DataSetVO dataSetVO = new DataSetVO();
            chartDataVO.getDatasets().add(dataSetVO);
            dataSetVO.setType(datasetConfiguration.getType());
            dataSetVO.setLabel(datasetConfiguration.getLabel());
            dataSetVO.setBackgroundColor(datasetConfiguration.getBackgroundColor());
            dataSetVO.setBarThickness(datasetConfiguration.getBarThickness());
            String expression = datasetConfiguration.getExpression();
            Script script = parseExpression(expression);
            Binding sharedData = new Binding();
            script.setBinding(sharedData);
            map.forEach((t, m) -> {
                m.forEach(sharedData::setProperty);
                Double result = ChartServiceImpl.<Double>exec(script);
                dataSetVO.getData().add(result);
            });
        }
    }
}
