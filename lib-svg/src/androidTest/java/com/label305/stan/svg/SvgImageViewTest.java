/*
 * |   _            _          _ ____   ___  _____   |
 * |  | |          | |        | |___ \ / _ \| ____|  |
 * |  | |      __ _| |__   ___| | __) | |_| | |__    |
 * |  | |     / _` | '_ \ / _ \ ||__ <|     |___ \   |
 * |  | |____| (_| | |_) |  __/ |___) |     |___) |  |
 * |  |______|\__,_|_.__/ \___|_|____/ \___/|____/   |
 *
 * @author Nick van den Berg <nick@label305.com>
 *
 * Copyright (c) 2013 Label305. All Right Reserved.
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 */

package com.label305.stan.svg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.graphics.drawable.StateListDrawable;
import android.test.AndroidTestCase;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsNot.*;

public class SvgImageViewTest extends AndroidTestCase {

    private SvgImageView mSvgImageView;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LinearLayout layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.svg_simple_layout, null);
        mSvgImageView = (SvgImageView) layout.findViewById(R.id.svg);
        mSvgImageView.layout(0, 0, 2, 2);
    }

    //Convert PictureDrawable to Bitmap
    private Bitmap pictureDrawable2Bitmap(final PictureDrawable pictureDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(mSvgImageView.getWidth(), mSvgImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pictureDrawable.getPicture());
        return bitmap;
    }

    private Bitmap getBitmapFromDrawable(final Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            return pictureDrawable2Bitmap((PictureDrawable) drawable);
        }
    }

    private Bitmap getBitmapFromImageView() {
        return getBitmapFromDrawable(mSvgImageView.getDrawable());
    }

    public void testSimpleSVGImageView() {
        Bitmap bmp = getBitmapFromImageView();
        int pixel = bmp.getPixel(0, 0);

        assertThat(pixel, equalTo(Color.RED));
        assertThat(pixel, not(equalTo(Color.BLACK)));
    }

    public void testBlueSVGImageView() {

        mSvgImageView.setSvgColor(Color.BLUE);

        Bitmap bmp = getBitmapFromImageView();

        int pixel = bmp.getPixel(0, 0);
        int transparentPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(pixel, equalTo(Color.BLUE));
        assertThat(transparentPixel, equalTo(Color.TRANSPARENT));
        assertThat(pixel, not(equalTo(Color.RED)));
        assertThat(pixel, not(equalTo(Color.BLACK)));
    }

    public void testInvertSVGImageView() {
        mSvgImageView.doInvertSvg();
        Bitmap bmp = getBitmapFromImageView();

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.TRANSPARENT));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.BLACK)));

        assertThat(rightPixel, equalTo(Color.BLACK));
        assertThat(rightPixel, not(equalTo(Color.TRANSPARENT)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
    }

    public void testInvertBlueSVGImageView() {
        mSvgImageView.doInvertSvg();
        mSvgImageView.setSvgColor(Color.BLUE);
        Bitmap bmp = getBitmapFromImageView();

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.TRANSPARENT));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.BLACK)));

        assertThat(rightPixel, equalTo(Color.BLUE));
        assertThat(rightPixel, not(equalTo(Color.TRANSPARENT)));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
    }

    public void testPressableSVGImageView() {

        mSvgImageView.setIsPressable();

        StateListDrawable stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        Bitmap bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.BLACK));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.TRANSPARENT)));

        assertThat(rightPixel, equalTo(Color.TRANSPARENT));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
        assertThat(rightPixel, not(equalTo(Color.RED)));

        mSvgImageView.setPressed(true);

        stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        leftPixel = bmp.getPixel(0, 0);
        rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.WHITE));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.TRANSPARENT)));

        assertThat(rightPixel, equalTo(Color.TRANSPARENT));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
    }

    public void testPressableColorSVGImageView() {

        mSvgImageView.setSvgColor(Color.BLUE);
        mSvgImageView.setIsPressable();

        StateListDrawable stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        Bitmap bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.BLUE));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.TRANSPARENT)));

        assertThat(rightPixel, equalTo(Color.TRANSPARENT));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
        assertThat(rightPixel, not(equalTo(Color.RED)));

        mSvgImageView.setPressed(true);

        stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        leftPixel = bmp.getPixel(0, 0);
        rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.WHITE));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.TRANSPARENT)));

        assertThat(rightPixel, equalTo(Color.TRANSPARENT));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
    }

    public void testPressableColorsSVGImageView() {

        mSvgImageView.setSvgColor(Color.BLUE);
        mSvgImageView.setPressedSvgColor(Color.GREEN);
        mSvgImageView.setIsPressable();

        StateListDrawable stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        Bitmap bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.BLUE));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.TRANSPARENT)));

        assertThat(rightPixel, equalTo(Color.TRANSPARENT));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
        assertThat(rightPixel, not(equalTo(Color.RED)));

        mSvgImageView.setPressed(true);

        stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        leftPixel = bmp.getPixel(0, 0);
        rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.GREEN));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.TRANSPARENT)));

        assertThat(rightPixel, equalTo(Color.TRANSPARENT));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
    }

    public void testPressableInvertedColorsSVGImageView() {

        mSvgImageView.doInvertSvg();
        mSvgImageView.setSvgColor(Color.BLUE);
        mSvgImageView.setPressedSvgColor(Color.GREEN);
        mSvgImageView.setIsPressable();

        StateListDrawable stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        Bitmap bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.TRANSPARENT));
        assertThat(leftPixel, not(equalTo(Color.BLACK)));
        assertThat(leftPixel, not(equalTo(Color.RED)));

        assertThat(rightPixel, equalTo(Color.BLUE));
        assertThat(rightPixel, not(equalTo(Color.TRANSPARENT)));
        assertThat(rightPixel, not(equalTo(Color.RED)));

        mSvgImageView.setPressed(true);

        stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        leftPixel = bmp.getPixel(0, 0);
        rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.TRANSPARENT));
        assertThat(leftPixel, not(equalTo(Color.BLACK)));
        assertThat(leftPixel, not(equalTo(Color.RED)));

        assertThat(rightPixel, equalTo(Color.GREEN));
        assertThat(rightPixel, not(equalTo(Color.TRANSPARENT)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
    }

    public void testPressableInvertedColorsFromLayoutSVGImageView() {

        LinearLayout layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.svg_test_all_layout, null);
        mSvgImageView = (SvgImageView) layout.findViewById(R.id.svg);
        mSvgImageView.layout(0, 0, 2, 2);

        StateListDrawable stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        Bitmap bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        int leftPixel = bmp.getPixel(0, 0);
        int rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.TRANSPARENT));
        assertThat(leftPixel, not(equalTo(Color.BLACK)));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.WHITE)));

        assertThat(rightPixel, equalTo(Color.WHITE));
        assertThat(rightPixel, not(equalTo(Color.TRANSPARENT)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
        assertThat(rightPixel, not(equalTo(Color.BLACK)));

        mSvgImageView.setPressed(true);

        stateListDrawable = (StateListDrawable) mSvgImageView.getDrawable();

        bmp = getBitmapFromDrawable(stateListDrawable.getCurrent());

        leftPixel = bmp.getPixel(0, 0);
        rightPixel = bmp.getPixel(mSvgImageView.getDrawable().getIntrinsicWidth() - 1, 0);

        assertThat(leftPixel, equalTo(Color.TRANSPARENT));
        assertThat(leftPixel, not(equalTo(Color.BLACK)));
        assertThat(leftPixel, not(equalTo(Color.RED)));
        assertThat(leftPixel, not(equalTo(Color.WHITE)));

        assertThat(rightPixel, equalTo(Color.BLACK));
        assertThat(rightPixel, not(equalTo(Color.TRANSPARENT)));
        assertThat(rightPixel, not(equalTo(Color.RED)));
        assertThat(rightPixel, not(equalTo(Color.WHITE)));
    }
}
