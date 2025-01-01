package com.example.globalspeak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


public class GetStartedActivity extends AppCompatActivity {

    private final String[] languageCode = {
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

    private Button gettingStarted;

    private int pendingDownload = languageCode.length;

    private ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_getting_started);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        gettingStarted = findViewById(R.id.get_started_button);

        gettingStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isGetStartedButton", true);
                editor.apply();

                downloadLangModel();
            }
        });
    }
    private void downloadLangModel() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading Language Models");
        progressDialog.setMessage("Please Wait....");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();


        for (String langCode : languageCode) {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(langCode)
                    .build();

            Translator translator = Translation.getClient(options);

            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkModelDownload();
                            progressDialog.incrementProgressBy(languageCode.length - pendingDownload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            checkModelDownload();
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.incrementProgressBy(languageCode.length - pendingDownload);
                        }
                    });
        }
    }

    private void checkModelDownload() {
        pendingDownload--;
        if(pendingDownload == 0) {
            progressDialog.dismiss();
            Intent intent = new Intent(GetStartedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
