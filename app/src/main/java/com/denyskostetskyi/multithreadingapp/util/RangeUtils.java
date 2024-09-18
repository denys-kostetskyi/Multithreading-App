package com.denyskostetskyi.multithreadingapp.util;

import com.denyskostetskyi.multithreadingapp.model.Range;

import java.util.ArrayList;
import java.util.List;

public class RangeUtils {

    public static List<Range> divideIntoRanges(long lastNumber, int numberOfRanges) {
        List<Range> ranges = new ArrayList<>();
        if (lastNumber <= 0 || numberOfRanges <= 0) {
            return ranges;
        }
        long rangeSize = lastNumber / numberOfRanges;
        long remainder = lastNumber % numberOfRanges;
        long from = 1;
        for (int i = 0; i < numberOfRanges; i++) {
            long to = from + rangeSize - 1;
            if (remainder > 0) {
                to++;
                remainder--;
            }
            ranges.add(new Range(from, to));
            from = to + 1;
        }
        return ranges;
    }
}
