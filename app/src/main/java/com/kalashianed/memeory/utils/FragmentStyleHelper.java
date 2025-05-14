package com.kalashianed.memeory.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.kalashianed.memeory.MemeoryApplication;
import com.kalashianed.memeory.R;

/**
 * Класс для стилизации элементов интерфейса во фрагментах
 */
public class FragmentStyleHelper {
    
    /**
     * Применяет стили ко всем элементам фрагмента
     */
    public static void applyStylesToFragment(Fragment fragment, View view) {
        UIProportionsManager proportionsManager = MemeoryApplication.getUIProportionsManager();
        if (proportionsManager == null || view == null) return;
        
        // Рекурсивно ищем и стилизуем все элементы в иерархии View
        if (view instanceof ViewGroup) {
            applyStylesToViewGroup(proportionsManager, (ViewGroup) view);
        } else {
            applySingleViewStyle(proportionsManager, view);
        }
    }
    
    /**
     * Рекурсивно применяет стили ко всем дочерним элементам ViewGroup
     */
    private static void applyStylesToViewGroup(UIProportionsManager manager, ViewGroup viewGroup) {
        // Сначала стилизуем само ViewGroup
        applySingleViewStyle(manager, viewGroup);
        
        // Затем рекурсивно проходим по всем дочерним элементам
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof ViewGroup) {
                applyStylesToViewGroup(manager, (ViewGroup) child);
            } else {
                applySingleViewStyle(manager, child);
            }
        }
    }
    
    /**
     * Применяет стиль к отдельному View в зависимости от его типа
     */
    private static void applySingleViewStyle(UIProportionsManager manager, View view) {
        if (view instanceof TextView && !(view instanceof Button)) {
            TextView textView = (TextView) view;
            // Получаем текущий размер шрифта и применяем пропорциональный
            float currentSize = textView.getTextSize() / textView.getContext().getResources().getDisplayMetrics().scaledDensity;
            manager.applyProportionalTextSize(textView, currentSize);
        } 
        else if (view instanceof Button) {
            manager.styleButtonProportionally((Button) view);
        } 
        else if (view instanceof CardView) {
            CardView cardView = (CardView) view;
            manager.applyProportionalCornerRadius(cardView, cardView.getRadius());
            manager.applyProportionalElevation(cardView, cardView.getCardElevation());
        }
        else if (view instanceof ImageView) {
            // Для изображений с тегом "logo" применяем специальный стиль
            if (view.getTag() != null && view.getTag().equals("logo")) {
                manager.styleLogoProportionally((ImageView) view, 110);
            }
        }
        
        // Применяем пропорциональные отступы для всех элементов с ненулевыми отступами
        if (view.getPaddingLeft() > 0 || view.getPaddingTop() > 0 || 
            view.getPaddingRight() > 0 || view.getPaddingBottom() > 0) {
            manager.applyProportionalPadding(
                view, 
                view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                view.getPaddingBottom()
            );
        }
        
        // Применяем пропорциональную элевацию если она установлена
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && 
            view.getElevation() > 0) {
            manager.applyProportionalElevation(view, view.getElevation());
        }
    }
} 