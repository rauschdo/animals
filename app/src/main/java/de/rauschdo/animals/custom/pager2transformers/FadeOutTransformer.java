package de.rauschdo.animals.custom.pager2transformers;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class FadeOutTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        page.setTranslationX(-position * page.getWidth());

        page.setAlpha(1 - Math.abs(position));

    }
}
