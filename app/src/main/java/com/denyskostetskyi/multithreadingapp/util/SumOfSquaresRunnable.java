package com.denyskostetskyi.multithreadingapp.util;

import android.util.Log;

import com.denyskostetskyi.multithreadingapp.model.Range;

import java.math.BigInteger;
import java.util.List;

public class SumOfSquaresRunnable implements Runnable {
    public static final String TAG = "SumOfSquaresRunnable";
    private final Range range;
    private final List<BigInteger> sums;

    public SumOfSquaresRunnable(Range range, List<BigInteger> sums) {
        this.range = range;
        this.sums = sums;
    }

    @Override
    public void run() {
        BigInteger result = range.calculateSumOfSquares();
        synchronized (sums) {
            sums.add(result);
        }
        Log.d(TAG, "Thread " + Thread.currentThread().getId() + " finished for " + range);
    }
}
