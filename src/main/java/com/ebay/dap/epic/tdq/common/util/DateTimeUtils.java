package com.ebay.dap.epic.tdq.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    /**
     * get eBay server time zone, which is at UTC-7
     *
     * @return
     */
    public static ZoneId eBayServerZoneId() {
        return ZoneId.of("GMT-7");
    }


    /**
     * convert timestamp to LocalDateTime, in eBay server's time zone, which is UTC-7
     *
     * @param ts
     * @return
     */
    public static LocalDateTime tsToLocalDateTime(long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), eBayServerZoneId());
    }

    /**
     * convert Instant to LocalDateTime, in eBay server's time zone, which is UTC-7
     */
    public static LocalDateTime instantToLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, eBayServerZoneId());
    }

    /**
     * get LocalDateTime.now() in eBay server's time zone, which is UTC-7
     */
    public static LocalDateTime currentTime() {
        return LocalDateTime.now(eBayServerZoneId());
    }

}
