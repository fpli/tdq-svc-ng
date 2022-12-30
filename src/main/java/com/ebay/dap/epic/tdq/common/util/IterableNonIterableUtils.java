package com.ebay.dap.epic.tdq.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class IterableNonIterableUtils {

    @JoinString
    public String joinString(Collection<String> in) {
        if (in != null && !in.isEmpty()) {
            return String.join(",", in);
        } else {
            return "";
        }
    }

    @SplitStringToSet
    public Set<String> splitStringToSet(String in) {
        if (in != null && in.length() > 0) {
            return Arrays.stream(in.split(","))
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    @JoinInteger
    public String joinInteger(Collection<Integer> in) {
        if (in != null && !in.isEmpty()) {
            return in.stream().map(String::valueOf).collect(Collectors.joining(","));
        } else {
            return "";
        }
    }

    @SplitStringToIntegerSet
    public Set<Integer> splitStringToIntegerSet(String in) {
        if (in != null && in.length() > 0) {
            return Arrays.stream(in.split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }
}
