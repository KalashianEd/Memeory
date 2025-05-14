package com.kalashianed.memeory.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

/**
 * Класс для управления пропорциями интерфейса
 */
public class UIProportionsManager {
    private final Context context;

    public UIProportionsManager(Context context) {
        this.context = context;
    }

    /**
     * Устанавливает пропорциональные отступы для View
     */
    public void applyProportionalMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.leftMargin = DisplayUtils.getProportionalDimension(context, left);
            params.topMargin = DisplayUtils.getProportionalDimension(context, top);
            params.rightMargin = DisplayUtils.getProportionalDimension(context, right);
            params.bottomMargin = DisplayUtils.getProportionalDimension(context, bottom);
            view.setLayoutParams(params);
        }
    }

    /**
     * Устанавливает пропорциональный размер для View
     */
    public void applyProportionalSize(View view, int width, int height) {
        if (width > 0 && height > 0) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = DisplayUtils.getProportionalDimension(context, width);
            params.height = DisplayUtils.getProportionalDimension(context, height);
            view.setLayoutParams(params);
        }
    }

    /**
     * Устанавливает пропорциональные отступы padding для View
     */
    public void applyProportionalPadding(View view, int left, int top, int right, int bottom) {
        view.setPadding(
                DisplayUtils.getProportionalDimension(context, left),
                DisplayUtils.getProportionalDimension(context, top),
                DisplayUtils.getProportionalDimension(context, right),
                DisplayUtils.getProportionalDimension(context, bottom)
        );
    }

    /**
     * Устанавливает пропорциональный размер текста для TextView
     */
    public void applyProportionalTextSize(TextView textView, float baseTextSize) {
        DisplayUtils.setProportionalTextSize(textView, baseTextSize);
    }

    /**
     * Устанавливает пропорциональный радиус скругления для CardView
     */
    public void applyProportionalCornerRadius(CardView cardView, float baseRadius) {
        cardView.setRadius(DisplayUtils.getProportionalDimension(context, (int) baseRadius));
    }

    /**
     * Устанавливает пропорциональную элевацию для View
     */
    public void applyProportionalElevation(View view, float baseElevation) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(DisplayUtils.getProportionalDimension(context, (int) baseElevation));
        }
    }

    /**
     * Стилизует кнопку пропорционально размерам экрана
     */
    public void styleButtonProportionally(Button button) {
        applyProportionalPadding(button, 24, 12, 24, 12);
        applyProportionalTextSize(button, 16);
        applyProportionalElevation(button, 4);
    }

    /**
     * Стилизует логотип пропорционально размерам экрана
     */
    public void styleLogoProportionally(ImageView logoView, int baseSize) {
        int proportionalSize = DisplayUtils.getProportionalDimension(context, baseSize);
        applyProportionalSize(logoView, proportionalSize, proportionalSize);
        applyProportionalPadding(logoView, 8, 8, 8, 8);
    }
} 