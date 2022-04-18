package com.capstone.autism_training.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.autism_training.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class ActivitiesActivity extends AppCompatActivity {

    public static final String TAG = ActivitiesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        MaterialCardView imageIdentificationCardView = findViewById(R.id.imageIdentificationActivityCardView);
        imageIdentificationCardView.setOnClickListener(view -> startActivity(new Intent(this, ImageIdentificationActivity.class)) );

        MaterialCardView wordIdentificationCardView = findViewById(R.id.wordIdentificationActivityCardView);
        wordIdentificationCardView.setOnClickListener(view -> startActivity(new Intent(this, WordIdentificationActivity.class)) );
    }
}