package com.denyskostetskyi.multithreadingapp.util;

import android.util.Log;

import com.denyskostetskyi.multithreadingapp.model.Range;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SumOfSquaresRunnable implements Runnable {
    private final Range range;
    private final List<BigInteger> sums;
    private final CountDownLatch latch;
    private final String logTag;

    public SumOfSquaresRunnable(
            Range range,
            List<BigInteger> sums,
            CountDownLatch latch,
            String logTag
    ) {
        this.range = range;
        this.sums = sums;
        this.latch = latch;
        this.logTag = logTag;
    }

    @Override
    public void run() {
        BigInteger result = range.calculateSumOfSquares();
        synchronized (sums) {
            sums.add(result);
        }
        latch.countDown();
        Log.d(logTag, "Thread " + Thread.currentThread().getId() + " finished for " + range);
    }
}
