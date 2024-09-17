package com.denyskostetskyi.multithreadingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denyskostetskyi.multithreadingapp.databinding.ActivityMainBinding;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int numberOfTasks = 1;

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
        String text = getString(R.string.number_of_parallel_tasks, numberOfTasks);
        binding.textViewNumberOfTasks.setText(text);
    }

    private void setSeekBarChangeListener() {
        binding.seekBarNumberOfTasks.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numberOfTasks = progress + 1;
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
            } else {
                binding.textViewResult.setText("");
                updateLoadingState(true);
                calculate(input);
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

    private void calculate(long input) {
        calculateUsingThread(input);
    }

    private void calculateUsingThread(long n) {
        long startTime = System.currentTimeMillis();
        new Thread(() -> {
            BigInteger result = calculateSumOfSquares(n);
            long elapsedTime = getElapsedTime(startTime);
            runOnUiThread(() -> {
                appendResult(R.string.result_using_thread, n, result, elapsedTime);
                updateLoadingState(false);
            });
        }).start();
    }

    private BigInteger calculateSumOfSquares(long n) {
        BigInteger sum = BigInteger.ZERO;
        for (long i = 1; i <= n; i++) {
            BigInteger number = BigInteger.valueOf(i);
            BigInteger square = number.multiply(number);
            sum = sum.add(square);
        }
        return sum;
    }

    private long getElapsedTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private void appendResult(
            @StringRes int methodStringId,
            long n,
            BigInteger sum,
            long elapsedTime
    ) {
        String methodString = getString(methodStringId);
        StringBuilder result = new StringBuilder(methodString);
        result.append(getString(R.string.sum_of_squares_from_1_to_n, n));
        result.append(sum.toString());
        result.append("\n");
        result.append(getString(R.string.elapsed_time, elapsedTime));
        result.append("\n");
        binding.textViewResult.append(result);
    }
}
