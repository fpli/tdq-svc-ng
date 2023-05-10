package com.ebay.dap.epic.tdq.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

class DateUtilTest {

    @Test
    void getYesterday() {
        LocalDate actual = DateUtil.getYesterday();
        var expected = LocalDate.now().minusDays(1);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void datesStepBackBetween() {
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        LocalDate sixMonthsAgo = endDate.minusMonths(6);
        LocalDate threeMonthsAgo = endDate.minusMonths(3);
        List<LocalDate> localDates = DateUtil.datesStepBackBetween(startDate, endDate, 1, ChronoUnit.MONTHS);
        Assertions.assertThat(localDates).isNotEmpty();
        Assertions.assertThat(localDates).size().isEqualTo(12);
        Assertions.assertThat(localDates).contains(endDate.minusMonths(1));
    }

}