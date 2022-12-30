package com.ebay.dap.epic.tdq.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagDetailFilterQueryVO implements Serializable {

    private static final long serialVersionUID = -8591136579977647037L;

    String tagName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    Map<String, Set<String>> dimensions;
}
