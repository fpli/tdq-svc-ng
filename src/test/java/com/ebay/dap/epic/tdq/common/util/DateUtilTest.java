package com.ebay.dap.epic.tdq.common.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
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
        System.out.println(sixMonthsAgo + "---sixMonthsAgo---");
        System.out.println(DateUtil.datesStepBackBetween(sixMonthsAgo, endDate, 2, ChronoUnit.WEEKS));
        System.out.println(threeMonthsAgo + "--threeMonthsAgo---");
        System.out.println(DateUtil.datesStepBackBetween(threeMonthsAgo, endDate, 1, ChronoUnit.WEEKS));
        System.out.println(DateUtil.datesStepBackBetween(endDate.minusMonths(1), endDate, 1, ChronoUnit.DAYS));

        System.out.println();
        System.out.println();
        Stream<LocalDate> localDateStream = DateUtil.datesStepBackBetween(lastYear, endDate, Period.ofMonths(1));
        System.out.println(localDateStream.toList());
        System.out.println(DateUtil.datesStepBackBetween(sixMonthsAgo, endDate, Period.ofWeeks(2)).toList());
        System.out.println(DateUtil.datesStepBackBetween(threeMonthsAgo, endDate, Period.ofWeeks(1)).toList());
        System.out.println("***************");
        List<LocalDate> localDateList = DateUtil.datesStepBackBetween(endDate.minusMonths(1), endDate, Period.ofDays(1)).toList();

        ArrayList<LocalDate> localDates1 = new ArrayList<>(localDateList);
        Iterator<LocalDate> listIterator = localDates1.iterator();
        while(listIterator.hasNext()){
            System.out.println(listIterator.next());
            listIterator.remove();
        }
        System.out.println(localDates1);
    }

}