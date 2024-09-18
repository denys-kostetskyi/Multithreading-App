package com.denyskostetskyi.multithreadingapp.util;

public class Stopwatch {
    private final long startTime;

    public Stopwatch() {
        startTime = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
