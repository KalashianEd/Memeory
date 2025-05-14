package com.kalashianed.memeory.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kalashianed.memeory.R;

/**
 * Утилитный класс для удобной работы с анимациями в приложении
 */
public class CustomAnimationUtils {

    /**
     * Применяет анимацию появления с масштабированием к указанному элементу
     * @param view элемент для анимации
     * @param delay задержка перед началом (мс)
     * @param duration продолжительность (мс)
     */
    public static void applyScaleInAnimation(View view, int delay, int duration) {
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.setAlpha(0f);
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setInterpolator(new OvershootInterpolator())
            .setDuration(duration)
            .setStartDelay(delay)
            .start();
    }
    
    /**
     * Применяет анимацию появления снизу с затуханием
     * @param view элемент для анимации
     * @param delay задержка перед началом (мс)
     * @param duration продолжительность (мс)
     */
    public static void applySlideUpWithFade(View view, int delay, int duration) {
        view.setTranslationY(100);
        view.setAlpha(0f);
        
        view.animate()
            .translationY(0)
            .alpha(1f)
            .setInterpolator(new DecelerateInterpolator(1.2f))
            .setDuration(duration)
            .setStartDelay(delay)
            .start();
    }
    
    /**
     * Применяет анимацию появления справа
     * @param view элемент для анимации
     * @param delay задержка перед началом (мс)
     * @param duration продолжительность (мс)
     */
    public static void applySlideInFromRight(View view, int delay, int duration) {
        view.setTranslationX(view.getContext().getResources().getDisplayMetrics().widthPixels);
        view.setAlpha(0f);
        
        view.animate()
            .translationX(0)
            .alpha(1f)
            .setInterpolator(new DecelerateInterpolator(1.5f))
            .setDuration(duration)
            .setStartDelay(delay)
            .start();
    }
    
    /**
     * Применяет анимацию появления слева
     * @param view элемент для анимации
     * @param delay задержка перед началом (мс)
     * @param duration продолжительность (мс)
     */
    public static void applySlideInFromLeft(View view, int delay, int duration) {
        view.setTranslationX(-view.getContext().getResources().getDisplayMetrics().widthPixels);
        view.setAlpha(0f);
        
        view.animate()
            .translationX(0)
            .alpha(1f)
            .setInterpolator(new DecelerateInterpolator(1.5f))
            .setDuration(duration)
            .setStartDelay(delay)
            .start();
    }
    
    /**
     * Применяет пульсирующую анимацию к элементу
     * @param view элемент для анимации
     * @param repeat повторять ли анимацию постоянно
     */
    public static void applyPulseAnimation(View view, boolean repeat) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f);
        scaleDownX.setDuration(700);
        scaleDownY.setDuration(700);
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleUpX.setDuration(700);
        scaleUpY.setDuration(700);
        
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleDownX).with(scaleDownY);
        animSet.play(scaleUpX).with(scaleUpY).after(scaleDownX);
        
        if (repeat) {
            animSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    animSet.start();
                }
            });
        }
        
        animSet.start();
    }
    
    /**
     * Анимация появления элементов с затуханием и задержкой
     * @param view элемент для анимации
     * @param duration продолжительность анимации
     * @param delay задержка начала анимации
     */
    public static void fadeIn(View view, int duration, int delay) {
        view.setAlpha(0f);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }
    
    /**
     * Применяет пульсирующую анимацию к элементу
     * @param view элемент для анимации
     * @param minScale минимальный масштаб
     * @param maxScale максимальный масштаб
     * @param duration продолжительность одного цикла
     * @param repeat повторять ли анимацию бесконечно
     */
    public static void pulseAnimation(View view, float minScale, float maxScale, long duration, boolean repeat) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", minScale);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", minScale);
        scaleDownX.setDuration(duration / 2);
        scaleDownY.setDuration(duration / 2);
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", maxScale);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", maxScale);
        scaleUpX.setDuration(duration / 2);
        scaleUpY.setDuration(duration / 2);
        
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleDownX).with(scaleDownY);
        animSet.play(scaleUpX).with(scaleUpY).after(scaleDownX);
        
        if (repeat) {
            animSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    animSet.start();
                }
            });
        }
        
        animSet.start();
    }
    
    /**
     * Загружает анимацию из ресурсов и применяет к View
     * @param context контекст
     * @param view элемент для анимации
     * @param animationResId идентификатор ресурса анимации
     */
    public static void loadAndApplyAnimation(Context context, View view, int animationResId) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animationResId);
        view.startAnimation(animation);
    }
    
    /**
     * Создает и запускает анимацию затухания
     * @param view элемент для анимации
     * @param fromAlpha начальная прозрачность (0-1)
     * @param toAlpha конечная прозрачность (0-1)
     * @param duration продолжительность анимации в мс
     */
    public static void fadeAnimation(View view, float fromAlpha, float toAlpha, long duration) {
        AlphaAnimation fadeAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        fadeAnimation.setDuration(duration);
        fadeAnimation.setFillAfter(true);
        view.startAnimation(fadeAnimation);
    }
    
    /**
     * Создает каскадную анимацию для группы элементов (с задержкой между элементами)
     * @param context контекст
     * @param views массив элементов для анимации
     * @param animationResId идентификатор ресурса анимации
     * @param delayBetweenItems задержка между анимациями элементов (мс)
     */
    public static void applyCascadeAnimation(Context context, View[] views, int animationResId, int delayBetweenItems) {
        for (int i = 0; i < views.length; i++) {
            Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, animationResId);
            animation.setStartOffset(i * delayBetweenItems);
            views[i].startAnimation(animation);
        }
    }
} 