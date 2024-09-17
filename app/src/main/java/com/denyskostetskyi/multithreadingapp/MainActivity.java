package com.denyskostetskyi.multithreadingapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.denyskostetskyi.multithreadingapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
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
        setButtonClickListener();
    }

    private void setButtonClickListener() {
        binding.buttonCalculate.setOnClickListener(v -> {
            binding.textViewResult.setText("");
            long input = getInputNumber();
            if (input == 0) {
                showWarning();
            } else {
                calculate(input);
            }
        });
    }

    private long getInputNumber() {
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

    private void calculate(long input) {
        binding.textViewResult.append("" + input);
    }
}