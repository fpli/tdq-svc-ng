package com.ebay.dap.epic.tdq.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

class DateUtilTest {

    @Test
    void getYesterday() {
        LocalDate actual = DateUtil.getYesterday();
        var expected = LocalDate.now().minusDays(1);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void datesStepBackBetween() {
        LocalDate endDate = LocalDate.now();
        LocalDate lastYear = endDate.minusYears(1);
        LocalDate sixMonthsAgo = endDate.minusMonths(6);
        LocalDate threeMonthsAgo = endDate.minusMonths(3);
        long amountToSubtract = 1;
        List<LocalDate> localDates = DateUtil.datesStepBackBetween(lastYear, endDate, 1, ChronoUnit.MONTHS);
        System.out.println(localDates);
        Assertions.assertThat(localDates).isNotEmpty();
        Assertions.assertThat(localDates).contains(endDate.minusMonths(1));

        System.out.println(sixMonthsAgo + "---sixMonthsAgo---");
        Assertions.assertThat(DateUtil.datesStepBackBetween(sixMonthsAgo, endDate, 2, ChronoUnit.WEEKS)).contains(endDate.minusWeeks(2));
        System.out.println(threeMonthsAgo + "--threeMonthsAgo---");
        Assertions.assertThat(DateUtil.datesStepBackBetween(threeMonthsAgo, endDate, 1, ChronoUnit.WEEKS)).contains(endDate.minusWeeks(3));
        Assertions.assertThat(DateUtil.datesStepBackBetween(endDate.minusMonths(1), endDate, 1, ChronoUnit.DAYS)).contains(endDate.minusDays(10));

        System.out.println();
        System.out.println();
        Stream<LocalDate> localDateStream = DateUtil.datesStepBackBetween(lastYear, endDate, Period.ofMonths(1));
        System.out.println(localDateStream.toList());
        Assertions.assertThat(DateUtil.datesStepBackBetween(sixMonthsAgo, endDate, Period.ofWeeks(2)).toList()).contains(endDate.minusWeeks(2));

        Assertions.assertThat(DateUtil.datesStepBackBetween(threeMonthsAgo, endDate, Period.ofWeeks(1)).toList()).contains(endDate.minusWeeks(1));
        System.out.println("***************");
        List<LocalDate> localDateList = DateUtil.datesStepBackBetween(endDate.minusMonths(1), endDate, Period.ofDays(1)).toList();

        Assertions.assertThat(localDateList).isNotEmpty();
        Assertions.assertThat(localDateList).size().isGreaterThanOrEqualTo(30);
    }

}