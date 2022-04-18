package com.capstone.autism_training.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.capstone.autism_training.R;
import com.capstone.autism_training.card.DeckTableHelper;
import com.capstone.autism_training.card.DeckTableManager;
import com.capstone.autism_training.deck.DeckInfoTableHelper;
import com.capstone.autism_training.deck.DeckInfoTableManager;
import com.capstone.autism_training.utilities.ImageHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ImageIdentificationActivity extends AppCompatActivity {

    public static final String TAG = ImageIdentificationActivity.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    private DeckTableManager deckTableManager;
    private Cursor cursor;
    private ArrayList<Integer> cardPositions;
    private int currentAnswerIndex;
    private int correctOption;

    private MaterialButtonToggleGroup buttonToggleGroup;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_identification);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.cheer);
        sharedPreferences = this.getSharedPreferences("activities", MODE_PRIVATE);
        toolbar.getMenu().findItem(R.id.action_play_sound).setChecked(sharedPreferences.getBoolean("playSoundImageIdentification", true));
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_play_sound) {
                item.setChecked(!item.isChecked());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("playSoundImageIdentification", item.isChecked());
                editor.apply();
                return true;
            }
            return false;
        });

        DeckInfoTableManager deckInfoTableManager = new DeckInfoTableManager(getApplicationContext());
        deckInfoTableManager.open();
        cursor = deckInfoTableManager.fetch();
        ArrayList<String> decks = new ArrayList<>();
        while (!cursor.isAfterLast() || cursor.isFirst()) {
            int nameIndex = cursor.getColumnIndex(DeckInfoTableHelper.NAME);
            decks.add(cursor.getString(nameIndex));
            cursor.moveToNext();
        }
        cursor.close();

        deckTableManager = new DeckTableManager(this);
        cardPositions = new ArrayList<>();

        MaterialAutoCompleteTextView chooseDeckAutoCompleteTextView = findViewById(R.id.chooseDeckAutoCompleteTextView);
        buttonToggleGroup = findViewById(R.id.buttonToggleGroup);
        MaterialButton submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, decks);
        chooseDeckAutoCompleteTextView.setAdapter(adapter);
        chooseDeckAutoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            deckTableManager.open(adapterView.getItemAtPosition(i).toString().replace(" ", "_"));
            if (!cursor.isClosed()) {
                cursor.close();
            }
            cursor = deckTableManager.fetch();
            deckTableManager.close();

            if (cursor.getCount() >= 4) {
                cardPositions.clear();
                for (int j=0; j<cursor.getCount(); j++) {
                    cardPositions.add(j);
                }
                Collections.shuffle(cardPositions);
                currentAnswerIndex = 0;

                nextQuestion();

                LinearLayout activityLinearLayout = findViewById(R.id.activityLinearLayout);
                if (activityLinearLayout.getVisibility() == View.GONE) {
                    activityLinearLayout.setVisibility(View.VISIBLE);
                }
            }
            else {
                LinearLayout activityLinearLayout = findViewById(R.id.activityLinearLayout);
                if (activityLinearLayout.getVisibility() != View.GONE) {
                    activityLinearLayout.setVisibility(View.GONE);
                }
                Toast.makeText(getApplicationContext(), "At least 4 cards are needed in a deck to perform this activity", Toast.LENGTH_LONG).show();
            }
        });

        submitButton.setOnClickListener(view -> {
            int id = buttonToggleGroup.getCheckedButtonId();
            if (id == View.NO_ID) {
                Snackbar.make(view, "Please select an answer", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", view1 -> {}).show();
                return;
            }

            nextButton.setEnabled(true);

            boolean correctAnswer = false;
            switch (correctOption) {
                case 0:
                    if (id == R.id.option1) {
                        correctAnswer = true;
                    }
                    break;
                case 1:
                    if (id == R.id.option2) {
                        correctAnswer = true;
                    }
                    break;
                case 2:
                    if (id == R.id.option3) {
                        correctAnswer = true;
                    }
                    break;
                case 3:
                    if (id == R.id.option4) {
                        correctAnswer = true;
                    }
                    break;
            }

            if (correctAnswer) {
                Snackbar.make(view, "Correct answer", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", view1 -> {}).show();
                if (sharedPreferences.getBoolean("playSoundImageIdentification", true)) {
                    mediaPlayer.start();
                }
            }
            else {
                Snackbar.make(view, "Wrong answer", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", view1 -> {}).show();
            }
        });

        nextButton.setOnClickListener(view -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }
            currentAnswerIndex++;
            if (currentAnswerIndex == cardPositions.size()) {
                Snackbar.make(view, "You have come to the end of the deck", Snackbar.LENGTH_LONG).show();
                LinearLayout activityLinearLayout = findViewById(R.id.activityLinearLayout);
                activityLinearLayout.setVisibility(View.GONE);
            }
            else {
                nextQuestion();
            }
            NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
            nestedScrollView.fullScroll(View.FOCUS_UP);
        });
    }

    private void nextQuestion() {
        buttonToggleGroup.clearChecked();
        nextButton.setEnabled(false);

        Random random = new Random();
        int answerPosition = cardPositions.get(currentAnswerIndex);
        int imageColumnIndex = cursor.getColumnIndex(DeckTableHelper.IMAGE);
        int answerColumnIndex = cursor.getColumnIndex(DeckTableHelper.ANSWER);

        ArrayList<Integer> otherPositions = new ArrayList<>(cardPositions);
        otherPositions.remove(currentAnswerIndex);

        ArrayList<byte[]> images = new ArrayList<>();
        cursor.moveToPosition(otherPositions.remove(random.nextInt(otherPositions.size())));
        images.add(cursor.getBlob(imageColumnIndex));
        cursor.moveToPosition(otherPositions.remove(random.nextInt(otherPositions.size())));
        images.add(cursor.getBlob(imageColumnIndex));
        cursor.moveToPosition(otherPositions.remove(random.nextInt(otherPositions.size())));
        images.add(cursor.getBlob(imageColumnIndex));
        cursor.moveToPosition(answerPosition);
        correctOption = random.nextInt(4);
        images.add(correctOption, cursor.getBlob(imageColumnIndex));

        MaterialTextView textView = findViewById(R.id.questionTextView);
        textView.setText(String.format(getString(R.string.identify_question_text_view_text_activity_image_identification), cursor.getString(answerColumnIndex)));

        ShapeableImageView imageView1 = findViewById(R.id.imageView1);
        imageView1.setImageBitmap(ImageHelper.toCompressedBitmap(images.get(0)));

        ShapeableImageView imageView2 = findViewById(R.id.imageView2);
        imageView2.setImageBitmap(ImageHelper.toCompressedBitmap(images.get(1)));

        ShapeableImageView imageView3 = findViewById(R.id.imageView3);
        imageView3.setImageBitmap(ImageHelper.toCompressedBitmap(images.get(2)));

        ShapeableImageView imageView4 = findViewById(R.id.imageView4);
        imageView4.setImageBitmap(ImageHelper.toCompressedBitmap(images.get(3)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cursor.close();
        mediaPlayer.stop();
    }
}