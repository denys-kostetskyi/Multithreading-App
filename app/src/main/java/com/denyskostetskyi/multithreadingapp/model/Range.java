package com.denyskostetskyi.multithreadingapp.model;

import androidx.annotation.NonNull;

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

    @NonNull
    @Override
    public String toString() {
        return "Range{" + "from=" + from + ", to=" + to + '}';
    }
}

