package com.denyskostetskyi.multithreadingapp.model;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class Range {
    private final long from;
    private final long to;

    public Range(long from, long to) {
        this.from = from;
        this.to = to;
    }

    public Range(long to) {
        this(1, to);
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public BigInteger calculateSumOfSquares() {
        BigInteger sum = BigInteger.ZERO;
        for (long i = from; i <= to; i++) {
            BigInteger number = BigInteger.valueOf(i);
            BigInteger square = number.multiply(number);
            sum = sum.add(square);
        }
        return sum;
    }

    @NonNull
    @Override
    public String toString() {
        return "Range{" + "from=" + from + ", to=" + to + '}';
    }
}

