package com.example.speechtotext;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_TO_TEXT = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;

    private TextView textView, updatedtextView;
    private ImageButton button;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.originalTextView);
        updatedtextView = findViewById(R.id.updatedTextView);
        button = findViewById(R.id.speechToTextButton);

        textView.setText("Hello i am mirza opu");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> speechResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (speechResults != null && !speechResults.isEmpty()) {
                        String spokenText = speechResults.get(0);
                        String referenceText = "Hello world";

                        if (spokenText.equals(referenceText)) {
                            updatedtextView.setText(spokenText);
                        } else {
                            highlightDifferences(spokenText, referenceText);
                        }
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

    private void highlightDifferences(String spokenText, String referenceText) {
        StringBuilder highlightedText = new StringBuilder();
        String[] spokenWords = spokenText.split(" ");
        String[] referenceWords = referenceText.split(" ");

        int minLength = Math.min(spokenWords.length, referenceWords.length);

        // Compare each word in the spoken text with the corresponding word in the reference text
        for (int i = 0; i < minLength; i++) {
            if (spokenWords[i].equals(referenceWords[i])) {
                highlightedText.append(spokenWords[i]).append(" ");
            } else {
                highlightedText.append("<font color='red'>").append(spokenWords[i]).append("</font> ");
            }
        }

        // Add any remaining words from the longer text
        if (spokenWords.length > referenceWords.length) {
            for (int i = minLength; i < spokenWords.length; i++) {
                highlightedText.append("<font color='red'>").append(spokenWords[i]).append("</font> ");
            }
        } else if (spokenWords.length < referenceWords.length) {
            for (int i = minLength; i < referenceWords.length; i++) {
                highlightedText.append(referenceWords[i]).append(" ");
            }
        }

        // Set the text of the TextView with HTML formatting to display the highlighted differences
        updatedtextView.setText(android.text.Html.fromHtml(highlightedText.toString()));
    }
}