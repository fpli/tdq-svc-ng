package com.ebay.dap.epic.tdq.common.util;

import java.time.LocalDate;

public interface TDQDateUtil {

    static LocalDate getYesterday(){
        return LocalDate.now().minusDays(1);
    }
}
