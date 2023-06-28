package com.ebay.dap.epic.tdq.service.impl;

import com.ebay.dap.epic.tdq.data.dto.ChartConfig;
import com.ebay.dap.epic.tdq.data.dto.ChartInfoDTO;
import com.ebay.dap.epic.tdq.data.dto.DatasetConfig;
import com.ebay.dap.epic.tdq.data.entity.ChartInfoEntity;
import com.ebay.dap.epic.tdq.data.enums.ChartMode;
import com.ebay.dap.epic.tdq.data.mapper.mybatis.ChartInfoMapper;
import com.ebay.dap.epic.tdq.data.pronto.MetricDoc;
import com.ebay.dap.epic.tdq.data.vo.ChartDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartPreviewDataVO;
import com.ebay.dap.epic.tdq.data.vo.ChartVO;
import com.ebay.dap.epic.tdq.data.vo.ChartValueVO;
import com.ebay.dap.epic.tdq.data.vo.DataSetVO;
import com.ebay.dap.epic.tdq.data.vo.MetricChartVO;
import com.ebay.dap.epic.tdq.data.vo.MetricQueryParamVO;
import com.ebay.dap.epic.tdq.data.vo.MetricValueItemVO;
import com.ebay.dap.epic.tdq.service.BatchMetricService;
import com.ebay.dap.epic.tdq.service.ChartService;
import com.ebay.dap.epic.tdq.service.MetricService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class ChartServiceImpl implements ChartService {

    @Autowired
    private ChartInfoMapper chartInfoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BatchMetricService batchMetricService;

    @Autowired
    private MetricService metricService;

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
        List<ChartInfoEntity> chartInfoEntities = chartInfoMapper.selectList(null);
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
        ChartInfoEntity chartEntity = chartInfoMapper.selectById(id);

        Map<String, Map<String, Double>> map = build(chartEntity, date);
        ChartDataVO chartDataVO = new ChartDataVO();
        String viewCfg = chartEntity.getViewCfg();

        ChartConfig chartConfig = objectMapper.readValue(viewCfg, ChartConfig.class);
        chartDataVO.setLabels(map.keySet().stream().sorted().toList());
        convert(map, chartConfig, chartDataVO);
        return chartDataVO;
    }


    @Override
    public ChartPreviewDataVO getChartData(Long id) throws Exception {
        Preconditions.checkNotNull(id);

        ChartInfoEntity entity = chartInfoMapper.selectById(id);

        if (entity == null) {
            throw new RuntimeException("Chart does not exist with id: " + id);
        }

        ChartPreviewDataVO vo = new ChartPreviewDataVO();
        vo.setChartId(entity.getId());
        vo.setTitle(entity.getName());
        vo.setMode(entity.getMode());
        vo.setViewCfg(entity.getViewCfg());
        vo.setDescription(entity.getDescription());

        // FIXME: remove hard-coded labels
        LocalDate end = LocalDateTime.now(ZoneId.of("GMT-7")).minusHours(17 + 24).toLocalDate();
        LocalDate start = end.minusDays(30);
        List<String> labels = new ArrayList<>();
        vo.setLabels(labels);

        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            labels.add(date.plusDays(1).toString());
        }

        List<String> metricKeys = Arrays.stream(entity.getMetricKeys().split(",")).map(String::trim).toList();
        Map<String, List<ChartValueVO>> datasets = getDatasets(entity.getMode(), metricKeys);

        vo.setDatasets(datasets);

        return vo;
    }

    private Map<String, List<ChartValueVO>> getDatasets(ChartMode mode, List<String> metricKeys) {
        Map<String, List<ChartValueVO>> datasets = new HashMap<>();
        return switch (mode) {
            case SINGLE, MULTIPLE -> getMetric(metricKeys);
            case BY_DIMENSION -> getMetricWithDimension(metricKeys);
            default -> throw new RuntimeException("Not Support");
        };
    }

    private Map<String, List<ChartValueVO>> getMetricWithDimension(List<String> metricKeys) {
        Preconditions.checkNotNull(metricKeys);
        Preconditions.checkArgument(metricKeys.size() == 1, "Wrong metric_key count for chart");

        // FIXME: remove hard-coded endDt
        LocalDate endDt = LocalDateTime.now(ZoneId.of("GMT-7")).minusHours(17 + 24).toLocalDate();
        Map<String, List<ChartValueVO>> datasets = new HashMap<>();
        final String metricKey = metricKeys.get(0);
        List<MetricDoc> dailyMetricDimensionSeries = metricService.getDailyMetricDimensionSeries(metricKey, endDt, 30);
        Map<Object, List<MetricDoc>> collect = dailyMetricDimensionSeries.stream().collect(groupingBy(e -> e.getDimension().entrySet().stream().toList().get(0).getValue()));
        for (Map.Entry<Object, List<MetricDoc>> entry : collect.entrySet()) {
            String d = entry.getKey().toString();
            List<ChartValueVO> list = entry.getValue().stream().map(e -> {
                ChartValueVO valueVO = new ChartValueVO();
                valueVO.setLabel(e.getDt().toString());
                valueVO.setValue(e.getValue().toPlainString());
                return valueVO;
            }).toList();
            datasets.put(d, list);
        }

        return datasets;
    }

    private Map<String, List<ChartValueVO>> getMetric(List<String> metricKeys) {
        Map<String, List<ChartValueVO>> datasets = new HashMap<>();
        // FIXME: get metric series data concurrently
        // FIXME: remove hard-coded endDt
        LocalDate endDt = LocalDateTime.now(ZoneId.of("GMT-7")).minusHours(17 + 24).toLocalDate();
        for (String metricKey : metricKeys) {
            List<MetricDoc> metricSeries = metricService.getDailyMetricSeries(metricKey, endDt, 30);
            List<ChartValueVO> list = metricSeries.stream().map(e -> {
                ChartValueVO valueVO = new ChartValueVO();
                valueVO.setLabel(e.getDt().toString());
                valueVO.setValue(e.getValue().toString());
                return valueVO;
            }).toList();
            datasets.put(metricKey, list);
        }
        return datasets;
    }


    @Override
    public List<ChartInfoDTO> listAllChartInfo() {
        List<ChartInfoEntity> chartInfoEntityList = chartInfoMapper.findAll();
        List<ChartInfoDTO> chartInfoDTOList = new ArrayList<>();
        for (ChartInfoEntity entity : chartInfoEntityList) {
            ChartInfoDTO dto = new ChartInfoDTO();
            dto.setChartId(entity.getId());
            dto.setTitle(entity.getName());
            dto.setDescription(entity.getDescription());
            dto.setMetricKeys(Arrays.stream(entity.getMetricKeys().split(",")).map(String::trim).toList());
            dto.setMode(entity.getMode());
            dto.setExp(entity.getExp());
            dto.setViewCfg(entity.getViewCfg());
            chartInfoDTOList.add(dto);
        }
        return chartInfoDTOList;
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
