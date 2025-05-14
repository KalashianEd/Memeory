package com.kalashianed.memeory.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Утилитарный класс для работы с размерами экрана и пропорциональным масштабированием
 */
public class DisplayUtils {

    /**
     * Получает ширину экрана в пикселях
     */
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    /**
     * Получает высоту экрана в пикселях
     */
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    /**
     * Конвертирует DP в пиксели
     */
    public static float dpToPx(float dp, Context context) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Конвертирует пиксели в DP
     */
    public static float pxToDp(float px, Context context) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * Вычисляет процент от ширины экрана
     */
    public static int getWidthPercent(int percent) {
        return (getScreenWidth() * percent) / 100;
    }

    /**
     * Вычисляет процент от высоты экрана
     */
    public static int getHeightPercent(int percent) {
        return (getScreenHeight() * percent) / 100;
    }

    /**
     * Устанавливает пропорциональную ширину View
     */
    public static void setViewWidthPercent(View view, int percent) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = getWidthPercent(percent);
        view.setLayoutParams(params);
    }

    /**
     * Устанавливает пропорциональную высоту View
     */
    public static void setViewHeightPercent(View view, int percent) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = getHeightPercent(percent);
        view.setLayoutParams(params);
    }

    /**
     * Устанавливает пропорциональный размер текста в зависимости от ширины экрана
     */
    public static void setProportionalTextSize(TextView textView, float baseTextSize) {
        float screenWidthDp = pxToDp(getScreenWidth(), textView.getContext());
        float scaleFactor = screenWidthDp / 360f; // Базовое значение для экрана 360dp
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseTextSize * scaleFactor);
    }

    /**
     * Вычисляет размер элемента в зависимости от размера экрана
     */
    public static int getProportionalDimension(Context context, int baseDimension) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenDensity = displayMetrics.density;
        float screenWidthDp = displayMetrics.widthPixels / screenDensity;
        float scaleFactor = screenWidthDp / 360f; // Базовое значение для экрана 360dp
        
        return Math.round(baseDimension * scaleFactor);
    }
} 