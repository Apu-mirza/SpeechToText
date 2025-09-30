package com.example.speechtotext;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_TO_TEXT = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;

    private TextView listeningView, titleView, updatedtextView;
    private ImageButton button;
    private SpeechRecognizer speechRecognizer;
    private ConstraintLayout micLayout;
    long startTime, elapsedTime = 3000, stopTime;
    private Handler backgroundHandler;
    private Runnable backgroundRunnable;
    private ImageView talkingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = findViewById(R.id.titleText);
        listeningView = findViewById(R.id.listeningText);
        updatedtextView = findViewById(R.id.originalTextView);
        button = findViewById(R.id.speechToTextButton);
        micLayout = findViewById(R.id.micLayout);
        talkingView = findViewById(R.id.talking);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedtextView.setText("");
                startTogglingBackground();
                startSpeechToText();
            }
        });

        // Check if the RECORD_AUDIO permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            // Permission already granted, initialize speech recognizer
            initializeSpeechRecognizer();
        }
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onError(int error) {
                    stopTogglingBackground();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> speechResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    stopTogglingBackground();
                    if (speechResults != null && !speechResults.isEmpty()) {
                        String spokenText = speechResults.get(0);
                        String referenceText = "Hello world";

                        updatedtextView.setText(spokenText);
//                        if (spokenText.equals(referenceText)) {
//                            updatedtextView.setText(spokenText);
//                        } else {
//                            highlightDifferences(spokenText, referenceText);
//                        }
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        } else {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSpeechToText() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

            speechRecognizer.startListening(intent);
        }
    }

    private void startTogglingBackground(){
        backgroundHandler = new Handler();
        final long visibleTime = 800;       // Duration to keep the background visible
        final long goneTime = 300;      // Duration to keep the background gone
        listeningView.setVisibility(View.VISIBLE);
        titleView.setVisibility(View.INVISIBLE);
        talkingView.setVisibility(View.VISIBLE);


        backgroundRunnable = new Runnable() {
            boolean isVisible = true;

            @Override
            public void run() {
                if (isVisible) {
                    micLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_round));
                    backgroundHandler.postDelayed(this, visibleTime);
                } else {
                    micLayout.setBackground(null);
                    backgroundHandler.postDelayed(this, goneTime);
                }
                isVisible = !isVisible;
            }
        };

        // Start the animation immediately
        backgroundHandler.post(backgroundRunnable);
    }

    private void stopTogglingBackground() {
        listeningView.setVisibility(View.INVISIBLE);
        titleView.setVisibility(View.VISIBLE);
        talkingView.setVisibility(View.INVISIBLE);
        if (backgroundHandler != null && backgroundRunnable != null) {
            // Remove any pending runnables to stop the animation loop
            backgroundHandler.removeCallbacks(backgroundRunnable);
        }
        // Ensure the background is cleared when recognition stops
        if (micLayout != null) {
            micLayout.setBackground(null);
        }
    }
}