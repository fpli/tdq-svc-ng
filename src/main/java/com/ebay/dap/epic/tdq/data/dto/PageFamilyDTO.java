package com.ebay.dap.epic.tdq.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageFamilyDTO {
    String pageFamilyName;
    List<Integer> pageIds = new ArrayList<>();
}