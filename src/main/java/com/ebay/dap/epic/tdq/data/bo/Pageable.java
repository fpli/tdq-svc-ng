package com.ebay.dap.epic.tdq.data.bo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder()
public class Pageable<T> {

  private long current;
  private long totalPages;
  private long totalRecords;
  private long pageSize;
  private boolean hasPrevious;
  private boolean hasNext;
  private List<T> records;

}
