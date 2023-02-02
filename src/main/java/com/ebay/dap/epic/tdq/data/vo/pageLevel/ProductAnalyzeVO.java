package com.ebay.dap.epic.tdq.data.vo.pageLevel;

import com.ebay.dap.epic.tdq.data.dto.PageFamilyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductAnalyzeVO {
    List<PageFamilyDTO> pageFamilyDTOList;
}
