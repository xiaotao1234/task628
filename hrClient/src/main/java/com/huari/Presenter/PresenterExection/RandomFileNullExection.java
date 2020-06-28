package com.huari.Presenter.PresenterExection;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class RandomFileNullExection extends Exception{
    public RandomFileNullExection() {
    }

    public RandomFileNullExection(String message) {
        super(message);
    }

    public RandomFileNullExection(String message, Throwable cause) {
        super(message, cause);
    }

    public RandomFileNullExection(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public RandomFileNullExection(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
