package com.denyskostetskyi.multithreadingapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denyskostetskyi.multithreadingapp.databinding.ActivityMainBinding;
import com.denyskostetskyi.multithreadingapp.model.Range;
import com.denyskostetskyi.multithreadingapp.util.RangeUtils;
import com.denyskostetskyi.multithreadingapp.util.Stopwatch;
import com.denyskostetskyi.multithreadingapp.util.SumOfSquaresRunnable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final int TOTAL_METHODS_COUNT = 3; //Threads, Handler and CompletableFuture methods

    private AtomicInteger completedMethodsCounter;
    private int parallelTasksCount = 1; // number of parallel tasks for each method
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
    }

    private void initViews() {
        updateSeekBarLabel();
        setSeekBarChangeListener();
        setButtonClickListener();
    }

    private void updateSeekBarLabel() {
        String text = getString(R.string.number_of_parallel_tasks, parallelTasksCount);
        binding.textViewNumberOfTasks.setText(text);
    }

    private void setSeekBarChangeListener() {
        binding.seekBarNumberOfTasks.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                parallelTasksCount = progress + 1;
                updateSeekBarLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setButtonClickListener() {
        binding.buttonCalculate.setOnClickListener(v -> {
            long input = parseInputNumber();
            if (input == 0) {
                showWarning();
                return;
            }
            try {
                List<Range> ranges = RangeUtils.divideIntoRanges(input, parallelTasksCount);
                binding.textViewResult.setText("");
                updateLoadingState(true);
                calculate(input, ranges);
            } catch (IllegalArgumentException e) {
                showWarning();
            }
        });
    }

    private long parseInputNumber() {
        String inputStr = binding.editTextInput.getText().toString();
        try {
            return Long.parseLong(inputStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showWarning() {
        binding.textViewResult.setText(R.string.please_enter_a_valid_number);
    }

    private void updateLoadingState(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.seekBarNumberOfTasks.setEnabled(!isLoading);
        binding.buttonCalculate.setEnabled(!isLoading);
    }

    private void calculate(long input, List<Range> ranges) {
        completedMethodsCounter = new AtomicInteger();
        calculateUsingThreads(input, ranges);
        calculateUsingHandler(input, ranges);
        calculateUsingCompletableFuture(input, ranges);
    }

    private void calculateUsingThreads(long n, List<Range> ranges) {
        Stopwatch stopwatch = new Stopwatch();
        new Thread(() -> {
            List<BigInteger> sums = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(parallelTasksCount);
            String logTag = "calculateUsingThreads";
            for (Range range : ranges) {
                new Thread(new SumOfSquaresRunnable(range, sums, latch, logTag)).start();
            }
            awaitResultAndUpdateUi(latch, R.string.result_using_threads, n, sums, stopwatch);
        }).start();
    }

    private void calculateUsingHandler(long n, List<Range> ranges) {
        Stopwatch stopwatch = new Stopwatch();
        List<BigInteger> sums = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(parallelTasksCount);
        String logTag = "calculateUsingHandler";
        for (Range range : ranges) {
            HandlerThread taskThread = startNewHandlerThread("TaskThread");
            getHandler(taskThread).post(() -> {
                new SumOfSquaresRunnable(range, sums, latch, logTag).run();
                taskThread.quitSafely();
            });
        }
        HandlerThread handlerThread = startNewHandlerThread(logTag);
        getHandler(handlerThread).post(() -> {
            awaitResultAndUpdateUi(latch, R.string.result_using_handler, n, sums, stopwatch);
            handlerThread.quitSafely();

        });
    }

    private void calculateUsingCompletableFuture(long n, List<Range> ranges) {
        Stopwatch stopwatch = new Stopwatch();
        List<CompletableFuture<BigInteger>> futures = new ArrayList<>();
        for (Range range : ranges) {
            futures.add(CompletableFuture.supplyAsync(range::calculateSumOfSquares));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            BigInteger totalSum = futures.stream()
                    .map(CompletableFuture::join)
                    .reduce(BigInteger.ZERO, BigInteger::add);
            long elapsedTime = stopwatch.getElapsedTime();
            runOnUiThread(() -> updateUi(
                    R.string.result_using_completable_future,
                    n,
                    totalSum,
                    elapsedTime
            ));
        });
    }

    private HandlerThread startNewHandlerThread(String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return handlerThread;
    }

    private Handler getHandler(HandlerThread handlerThread) {
        return new Handler(handlerThread.getLooper());
    }

    private void awaitResultAndUpdateUi(
            CountDownLatch latch,
            @StringRes int methodResId,
            long n,
            List<BigInteger> sums,
            Stopwatch stopwatch
    ) {
        try {
            latch.await();
            final BigInteger totalSum = getTotalSum(sums);
            long elapsedTime = stopwatch.getElapsedTime();
            runOnUiThread(() -> updateUi(
                    methodResId,
                    n,
                    totalSum,
                    elapsedTime
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private BigInteger getTotalSum(List<BigInteger> sums) {
        BigInteger totalSum = BigInteger.ZERO;
        for (BigInteger sum : sums) {
            totalSum = totalSum.add(sum);
        }
        return totalSum;
    }

    private void updateUi(
            @StringRes int methodStringId,
            long n,
            BigInteger sum,
            long elapsedTime
    ) {
        appendResult(methodStringId, n, sum, elapsedTime);
        if (completedMethodsCounter.incrementAndGet() == TOTAL_METHODS_COUNT) {
            updateLoadingState(false);
        }
    }

    private void appendResult(
            @StringRes int methodStringId,
            long n,
            BigInteger sum,
            long elapsedTime
    ) {
        StringBuilder result = new StringBuilder(getString(methodStringId))
                .append(getString(R.string.sum_of_squares_from_1_to_n, n))
                .append(sum.toString())
                .append("\n")
                .append(getString(R.string.elapsed_time, elapsedTime))
                .append("\n");
        binding.textViewResult.append(result);
    }
}
