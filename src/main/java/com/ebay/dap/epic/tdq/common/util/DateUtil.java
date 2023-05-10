package com.ebay.dap.epic.tdq.common.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;


public class DateUtil {

    public static LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }

    public static List<LocalDate> datesStepBackBetween(LocalDate startInclusive, LocalDate endInclusive, long amountToSubtract, ChronoUnit unit){
        long end = endInclusive.toEpochDay();
        long start = startInclusive.toEpochDay();
        if (end - start <= 0) {
            return List.of();
        }
        LocalDate temp = endInclusive;
        List<LocalDate> dates = switch (unit){
            case DAYS -> {
                // datesUntil maybe better for days
                List<LocalDate> list = new ArrayList<>();
                while (temp.isAfter(startInclusive)){
                    list.add(temp);
                    temp = temp.minusDays(amountToSubtract);
                }
                yield list;
            }
            case WEEKS -> {
                List<LocalDate> list = new ArrayList<>();
                while (temp.isAfter(startInclusive)){
                    list.add(temp);
                    temp = temp.minusWeeks(amountToSubtract);
                }
                yield list;
            }
            case MONTHS -> {
                List<LocalDate> list = new ArrayList<>();
                while (temp.isAfter(startInclusive)){
                    list.add(temp);
                    temp = temp.minusMonths(amountToSubtract);
                }
                yield list;
            }
            case YEARS -> {
                List<LocalDate> list = new ArrayList<>();
                while (!temp.isBefore(startInclusive)){
                    list.add(temp);
                    temp = temp.minusYears(amountToSubtract);
                }
                yield list;
            }
            default -> throw new IllegalArgumentException(unit + "is not supported!");
        };

        dates.sort(Comparator.naturalOrder());
        return dates;
    }

    public static Stream<LocalDate> datesStepBackBetween(LocalDate startInclusive, LocalDate endExclusive, Period step){
        if (step.isZero()) {
            throw new IllegalArgumentException("step is zero");
        }
        long end = endExclusive.toEpochDay();
        long start = startInclusive.toEpochDay();
        long until = end - start;
        long months = step.toTotalMonths();
        long days = step.getDays();
        if ((months < 0 && days > 0) || (months > 0 && days < 0)) {
            throw new IllegalArgumentException("period months and days are of opposite sign");
        }
        if (until == 0) {
            return Stream.empty();
        }
        int sign = months > 0 || days > 0 ? 1 : -1;
        if (sign < 0 ^ until < 0) {
            throw new IllegalArgumentException(endExclusive + (sign < 0 ? " > " : " < ") + startInclusive);
        }
        if (months == 0) {
            long steps = (until - sign) / days; // non-negative
            return LongStream.rangeClosed(0, steps).mapToObj(
                    n -> LocalDate.ofEpochDay(end - n * days)).sorted();
        }
        // 48699/1600 = 365.2425/12, no overflow, non-negative result
        long steps = until * 1600 / (months * 48699 + days * 1600) + 1;
        long addMonths = months * steps;
        long addDays = days * steps;
        //  (year * 12L + month - 1)
        long maxAddMonths = months > 0 ? (LocalDate.MAX.getYear() * 12L + LocalDate.MAX.getMonthValue() - 1) - (startInclusive.getYear() * 12L + startInclusive.getMonthValue() - 1)
                : (startInclusive.getYear() * 12L + startInclusive.getMonthValue() - 1) - (LocalDate.MIN.getYear() * 12L + LocalDate.MIN.getMonthValue() - 1);
        // adjust steps estimation
        if (addMonths * sign > maxAddMonths
                || (startInclusive.plusMonths(addMonths).toEpochDay() + addDays) * sign >= end * sign) {
            steps--;
            addMonths -= months;
            addDays -= days;
            if (addMonths * sign > maxAddMonths
                    || (startInclusive.plusMonths(addMonths).toEpochDay() + addDays) * sign >= end * sign) {
                steps--;
            }
        }
        return LongStream.rangeClosed(0, steps).mapToObj(n -> endExclusive.minusMonths(months * n).minusDays(days * n)).sorted();
    }
}
