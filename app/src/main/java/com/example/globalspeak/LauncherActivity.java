package com.example.globalspeak;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.splashscreen.SplashScreen;

public class LauncherActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    MainViewModel viewModel;
    SplashScreen splashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        splashScreen = SplashScreen.installSplashScreen(this);

        splashScreen.setKeepOnScreenCondition(() -> {
            Boolean isReady = viewModel.getIsReady().getValue();
            return isReady == null || !isReady;
        });


        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            ObjectAnimator zoomX = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.getIconView(),
                    View.SCALE_X,
                    0.4f,
                    0.0f
            );

            zoomX.setInterpolator(new OvershootInterpolator());
            zoomX.setDuration(500);
            zoomX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                    super.onAnimationEnd(animation, isReverse);
                    splashScreenViewProvider.remove();
                }
            });

            ObjectAnimator zoomY = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.getIconView(),
                    View.SCALE_Y,
                    0.4f,
                    0.0f
            );

            zoomY.setInterpolator(new OvershootInterpolator());
            zoomY.setDuration(500);
            zoomY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                    super.onAnimationEnd(animation, isReverse);
                    splashScreenViewProvider.remove();
                }
            });

            zoomX.start();
            zoomY.start();
        });
        
        viewModel.getIsReady().observe(this, isReady -> {
            if (isReady != null && isReady) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleActivityTransition();
                    }
                }, 500);

            }
        });

        EdgeToEdge.enable(this);
    }

    private void handleActivityTransition() {
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);
        boolean isGetStartedButton = sharedPreferences.getBoolean("isGetStartedButton", false);

        if (isFirstLaunch && !isGetStartedButton) {
            startActivity(new Intent(this, GetStartedActivity.class));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();
        }
        else if (!isGetStartedButton) {
            startActivity(new Intent(this, GetStartedActivity.class));
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
