package com.example.globalspeak;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import android.Manifest;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner sourceLanguage, targetLanguage;
    EditText sourceText;
    TextView resultText;
    ImageButton mic, speech;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    Intent speechIntent;
    TextToSpeech textToSpeech;

    // Resource to Populate Spinner 1
    private final String[] fromLanguage = {"From", "English", "Hindi", "Tamil", "Telugu", "Japanese", "Chinese", "Russian", "Romanian", "Arabic", "French", "Spanish", "German", "Portuguese"};
    // Resource to Populate Spinner 2
    private final String[] toLanguage = {"To", "English", "Hindi", "Tamil", "Telugu", "Japanese", "Chinese", "Russian", "Romanian", "Arabic", "French", "Spanish", "German", "Portuguese"};
    // Used for determining language for the language model
    private final String[] languageCode = {
            "",
            TranslateLanguage.ENGLISH,
            TranslateLanguage.HINDI,
            TranslateLanguage.TAMIL,
            TranslateLanguage.TELUGU,
            TranslateLanguage.JAPANESE,
            TranslateLanguage.CHINESE,
            TranslateLanguage.RUSSIAN,
            TranslateLanguage.ROMANIAN,
            TranslateLanguage.ARABIC,
            TranslateLanguage.FRENCH,
            TranslateLanguage.SPANISH,
            TranslateLanguage.GERMAN,
            TranslateLanguage.PORTUGUESE
    };

    // Used for determining the STT language for Recognizer Intent
    String[] speechLangCodes = {
            "",
            "en-IN", // English
            "hi-IN", // Hindi
            "ta-IN", // Tamil
            "te-IN", // Telugu
            "ja-JP", // Japanese
            "zh-CN", // Chinese (Mandarin, Simplified)
            "ru-RU", // Russian
            "ro-RO", // Romanian
            "ar-SA", // Arabic
            "fr-FR", // French
            "es-ES", // Spanish
            "de-DE", // German
            "pt-PT"  // Portuguese
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sourceLanguage = findViewById(R.id.spinner1);
        targetLanguage = findViewById(R.id.spinner2);
        sourceText = findViewById(R.id.sourceText);
        resultText = findViewById(R.id.resultText);
        mic = findViewById(R.id.mic);
        speech = findViewById(R.id.text_to_speech);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fromLanguage);
        sourceLanguage.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, toLanguage);
        targetLanguage.setAdapter(adapter2);

        sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence c, int i, int i1, int i2) {

                String charSequence = c.toString();
                sourceLanguage.getSelectedItem();
                targetLanguage.getSelectedItem();

                if (!charSequence.isEmpty() && sourceLanguage.getSelectedItemPosition() != 0 && targetLanguage.getSelectedItemPosition() != 0) {
                    setupTranslator(charSequence);
                }
                else {
                    resultText.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.RECORD_AUDIO
            },1);
        }

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int langPosition = sourceLanguage.getSelectedItemPosition();
                String langCode = speechLangCodes[langPosition];

                startSpeechInput(langCode);

                startActivityForResult(speechIntent, REQ_CODE_SPEECH_INPUT);

            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = resultText.getText().toString();

                if (!result.isEmpty()) {
                    int langPosition = targetLanguage.getSelectedItemPosition();
                    Locale locale = getLocaleForSpeechLangCode(langPosition);

                    if (locale != null) {
                        textToSpeech.setLanguage(locale);
                        textToSpeech.speak(result, TextToSpeech.QUEUE_FLUSH, null, null);
                    }

                }
            }
        });
    }

    private void startSpeechInput(String languageCode) {
            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode);
            speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now...");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                sourceText.setText(spokenText); // Display the recognized text
            }
        }
    }

    private Locale getLocaleForSpeechLangCode(int langPosition) {
        switch (langPosition) {
            case 1: return Locale.ENGLISH; // en-IN
            case 2: return new Locale("hi", "IN"); // Hindi
            case 3: return new Locale("ta", "IN"); // Tamil
            case 4: return new Locale("te", "IN"); // Telugu
            case 5: return Locale.JAPANESE; // Japanese
            case 6: return Locale.SIMPLIFIED_CHINESE; // Chinese
            case 7: return new Locale("ru", "RU"); // Russian
            case 8: return new Locale("ro", "RO"); // Romanian
            case 9: return new Locale("ar", "SA"); // Arabic
            case 10: return Locale.FRENCH; // French
            case 11: return new Locale("es", "ES"); // Spanish
            case 12: return Locale.GERMAN; // German
            case 13: return new Locale("pt", "PT"); // Portuguese
            default: return null; // Language not supported
        }
    }


    private void setupTranslator(String charSequence) {

        String sourceLangCode = languageCode[sourceLanguage.getSelectedItemPosition()];
        String targetLangCode = languageCode[targetLanguage.getSelectedItemPosition()];

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLangCode)
                .setTargetLanguage(targetLangCode)
                .build();

        Translator translator = Translation.getClient(options);

        translator.translate(charSequence)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        resultText.setText(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to load language model", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}