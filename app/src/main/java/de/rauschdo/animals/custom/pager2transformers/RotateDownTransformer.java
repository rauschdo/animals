package de.rauschdo.animals.custom.pager2transformers;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class RotateDownTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        float width = (float) page.getWidth();
        float height = (float) page.getHeight();
        float rotation = -15.0F * position * -1.25F;
        page.setPivotX(width * 0.5F);
        page.setPivotY(height);
        page.setRotation(rotation);
    }
}
