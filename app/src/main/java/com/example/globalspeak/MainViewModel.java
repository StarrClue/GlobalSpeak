package com.example.globalspeak;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.Handler;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isReady = new MutableLiveData<>(false);

    public MainViewModel() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isReady.setValue(true);
        }, 3000);
    }

    public LiveData<Boolean> getIsReady() {
        return isReady;
    }
}
