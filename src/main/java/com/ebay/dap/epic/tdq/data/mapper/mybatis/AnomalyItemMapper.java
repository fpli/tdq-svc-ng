package com.ebay.dap.epic.tdq.data.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebay.dap.epic.tdq.data.entity.AnomalyItemEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface AnomalyItemMapper extends BaseMapper<AnomalyItemEntity> {
    @Delete("delete from anomaly_item where type = #{type} and dt = #{dt} and ref_id = #{tag}")
    long deleteInBatch(@Param("type") String type, @Param("dt") LocalDate localDate, @Param("tag") String tag);

    default long save(AnomalyItemEntity anomalyItemEntity) {
        insert(anomalyItemEntity);
        return anomalyItemEntity.getId();
    }

    @Select("select * from anomaly_item where type = #{type} and ref_id = #{tag} and dt between #{begin} and #{end}")
    List<AnomalyItemEntity> findAllByTypeAndRefIdAndDtBetween(@Param("type") String type, @Param("tag") String tagName, @Param("begin") LocalDate begin, @Param("end") LocalDate dt);

    default void saveAll(List<AnomalyItemEntity> anomalyItems) {
        anomalyItems.forEach(this::insert);
    }

    @Select({
            "<script>",
            "select * from anomaly_item where type = #{type} and ref_id in ",
            "<foreach item='item' index='index' collection='refIds'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            " and dt = #{localDate}",
            "</script>"
    })
    List<AnomalyItemEntity> findAllByTypeAndRefIdInAndDt(@Param("type") String type, @Param("refIds") List<String> refIds, @Param("localDate") LocalDate localDate);
}
