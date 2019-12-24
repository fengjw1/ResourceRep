/*
 * Copyright 2018 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.securelockview.sample;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.Log;
import android.view.animation.CycleInterpolator;

import com.kevalpatel.passcodeview.internal.BasePasscodeView;
import com.kevalpatel.passcodeview.keys.Key;


public class MyRoundKey extends Key {
    /**
     * Maximum alpha value in the ripple animation.
     */
    private static final int MAX_RIPPLE_ALPHA = 255;
    /**
     * Ripple animation duration in milli seconds.
     */
    private static final int RIPPLE_DURATION = 350;
    /**
     * {@link Paint} of the key.
     */
    @NonNull
    private final Paint mKeyPaint;
    /**
     * {@link TextPaint} of the key title text.
     */
    @NonNull
    private final TextPaint mKeyTextPaint;
    /**
     * {@link Paint} of the ripple animations.
     */
    @NonNull
    private final Paint mRipplePaint;
    /**
     * Radius of the round key. This radius is decided by the key bound width and key padding.
     *
     * @see #calculateKeyRadius(Rect, float)
     */
    private final float mKeyRadius;

    /**
     * {@link ValueAnimator} for the key ripple animations.
     */
    private ValueAnimator mRippleValueAnimator;
    /**
     * {@link ValueAnimator} for the authentication error. This animator will shake the view left-right
     * for two times.
     */
    private ValueAnimator mErrorAnimator;               //Left-Right animator

    /**
     * Boolean to set <code>true</code> if the ripple animation is running else <code>false</code>.
     */
    private boolean isRippleEffectRunning = false;

    /**
     * Current ripple radius.
     */
    private int mCurrentRippleRadius = 0;
    /**
     * Value of the alpha in the ripple color.
     */
    private int mCurrentAlpha = 178;

    /**
     * Public constructor.
     *
     * @param builder {@link MyRoundKey.Builder}.
     */
    private MyRoundKey(@NonNull final MyRoundKey.Builder builder,
                       @NonNull final String keyTitle,
                       @NonNull final Rect bound) {
        super(builder, keyTitle, bound);

        //Set the keyboard paint
        mKeyPaint = new Paint();
        mKeyPaint.setStyle(Paint.Style.FILL);
        mKeyPaint.setColor(builder.mKeyStrokeColor);
        mKeyPaint.setTextSize(builder.mKeyTextSize);
        mKeyPaint.setStrokeWidth(builder.mKeyStrokeWidth);
        mKeyPaint.setAlpha(builder.mKeyPaintAlpha);//设置按键透明度

        //Set the keyboard text paint
        mKeyTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mKeyTextPaint.setColor(builder.mKeyTextColor);
        mKeyTextPaint.setTextSize(builder.mKeyTextSize);
        mKeyTextPaint.setFakeBoldText(true);
        mKeyTextPaint.setTextAlign(Paint.Align.CENTER);

        //Prepare ripple paint
        mRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRipplePaint.setColor(builder.mKeyStrokeColor);
        mRipplePaint.setStyle(Paint.Style.FILL);
        mRipplePaint.setAlpha(builder.mRipplePaintAlpha);//设置按键按下的透明度

        mKeyRadius = calculateKeyRadius(getBounds(), builder.mKeyPadding);

        setUpAnimator();
    }

    /**
     * Initialise the filed and listener for ripple effect mRippleValueAnimator
     */
    private void setUpAnimator() {
//        final int circleAlphaOffset = (int) (MAX_RIPPLE_ALPHA / mKeyRadius);
        mRippleValueAnimator = ValueAnimator.ofFloat(0, mKeyRadius);
        mRippleValueAnimator.setDuration(RIPPLE_DURATION);
        mRippleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isRippleEffectRunning) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    Log.d("fengjw", "animatedValue : " + animatedValue);
                    mCurrentRippleRadius = (int) animatedValue;
//                    mCurrentAlpha = (int) (MAX_RIPPLE_ALPHA - (animatedValue * circleAlphaOffset));
                    getPasscodeView().invalidate();
                }
            }
        });
        mRippleValueAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRippleEffectRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isRippleEffectRunning = false;
                mCurrentRippleRadius = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //Do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //Do nothing
            }
        });

        //Error animator
        mErrorAnimator = ValueAnimator.ofInt(0, 10);
        mErrorAnimator.setInterpolator(new CycleInterpolator(2));
        mErrorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getBounds().left += (int) animation.getAnimatedValue();
                getBounds().right += (int) animation.getAnimatedValue();
                getPasscodeView().invalidate();
            }
        });
    }

    /**
     * Calculate the circle radius.
     *
     * @param bounds  rectangle bound.
     * @param padding surround padding to the key.
     * @return radius.
     */
    private float calculateKeyRadius(Rect bounds, float padding) {
        return Math.min(bounds.height(), bounds.width()) / 2 - padding;       //radius = height or width - padding for single key
    }

    /**
     * Start Playing ripple animation and notify listener accordingly
     * <p>
     * notified
     */
    @Override
    public void playClickAnimation() {
        mRippleValueAnimator.start();
    }

    /**
     * Show animation indicated invalid pin code
     */
    @Override
    public void onAuthFail() {
        mErrorAnimator.start();
    }

    @Override
    public void onAuthSuccess() {
        //Do noting
    }

    /**
     * Draw the key of canvas.
     * Don't change until you know what you are doing. :-)
     *
     * @param canvas canvas of the view o which key will be drawn
     */
    @SuppressWarnings({"deprecation", "ConstantConditions"})
    @Override
    public void drawText(@NonNull Canvas canvas) {//画文字
        //Draw key text
        canvas.drawText(getDigit() + "",                //Text to display on key
                getBounds().exactCenterX(),             //Set start point at center width of key
                getBounds().exactCenterY() - (mKeyTextPaint.descent() + mKeyTextPaint.ascent()) / 2,    //center height of key - text height/2
                mKeyTextPaint);
    }

    @Override
    public void drawShape(@NonNull Canvas canvas) {//画圆
        //Draw circle background
        canvas.drawCircle(getBounds().exactCenterX(),   //Set center width of key
                getBounds().exactCenterY(),             //Set center height of key
                mKeyRadius,
                mKeyPaint);
        Log.d("fengjw", "drawShape : radius->" + mKeyRadius);

        //Play ripple effect if the key has ripple effect enabled.
        if (isRippleEffectRunning) {
            canvas.drawCircle(getBounds().exactCenterX(),
                    getBounds().exactCenterY(),
                    mCurrentRippleRadius,
                    mRipplePaint);
        }
    }

    @Override
    public void drawBackSpace(@NonNull Canvas canvas, @NonNull Drawable backSpaceIcon) {
        Log.d("fengjw", "drawBackSpace");
        backSpaceIcon.setColorFilter(new PorterDuffColorFilter(mKeyTextPaint.getColor(), PorterDuff.Mode.SRC_ATOP));
        backSpaceIcon.setBounds((int) (getBounds().exactCenterX() - mKeyRadius / 2),
                (int) (getBounds().exactCenterY() - mKeyRadius / 2),
                (int) (getBounds().exactCenterX() + mKeyRadius / 2),
                (int) (getBounds().exactCenterY() + mKeyRadius / 2));
        backSpaceIcon.draw(canvas);
    }

    /**
     * Check if the key is pressed or not for given touch coordinates?
     *
     * @param touchX touch X coordinate
     * @param touchY touch Y coordinate
     * @return true if the key is pressed else false.
     */
    @Override
    public boolean isKeyPressed(float touchX, float touchY) {
        if (getDigit().isEmpty()) return false;  //Empty key

        //Check if the click is between the width bounds
        //noinspection SimplifiableIfStatement
        if (touchX > getBounds().exactCenterX() - mKeyRadius
                && touchX < getBounds().exactCenterX() + mKeyRadius) {

            //Check if the click is between the height bounds
            return touchY > getBounds().exactCenterY() - mKeyRadius
                    && touchY < getBounds().exactCenterY() + mKeyRadius;
        }
        return false;
    }

    public static class Builder extends Key.Builder {
        /**
         * Surround padding to each single key.
         */
        @Dimension
        private float mKeyPadding;

        /**
         * Size of the key title text in pixels.
         */
        @Dimension
        private float mKeyTextSize;

        /**
         * Size of the width of the key border in pixels.
         */
        @Dimension
        private float mKeyStrokeWidth;

        /**
         * Key background stroke color.
         */
        @ColorInt
        private int mKeyStrokeColor;

        /**
         * Key title text color.
         */
        @ColorInt
        private int mKeyTextColor;

        /**
         * key paint alpha
         */
        @ColorInt
        private int mKeyPaintAlpha;

        /**
         * ripple paint alpha
         */
        @ColorInt
        private int mRipplePaintAlpha;

        public Builder(@NonNull final BasePasscodeView passcodeView) {
            super(passcodeView);
            setDefaults(getContext());
        }

        @NonNull
        public MyRoundKey.Builder setKeyPadding(@Dimension final float keyPadding) {
            mKeyPadding = keyPadding;
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyPadding(@DimenRes final int keyPaddingRes) {
            mKeyPadding = getContext().getResources().getDimension(keyPaddingRes);
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyTextSize(final float keyTextSize) {
            mKeyTextSize = keyTextSize;
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyTextSize(@DimenRes final int keyTextSize) {
            mKeyTextSize = getContext().getResources().getDimension(keyTextSize);
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyStrokeWidth(final float keyStrokeWidth) {
            mKeyStrokeWidth = keyStrokeWidth;
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyStrokeWidth(@DimenRes final int keyStrokeWidth) {
            mKeyStrokeWidth = getContext().getResources().getDimension(keyStrokeWidth);
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyStrokeColor(@ColorInt final int keyStrokeColor) {
            mKeyStrokeColor = keyStrokeColor;
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyStrokeColorResource(@ColorRes final int keyStrokeColor) {
            mKeyStrokeColor = getContext().getResources().getColor(keyStrokeColor);
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyTextColor(@ColorInt final int keyTextColor) {
            mKeyTextColor = keyTextColor;
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyTextColorResource(@ColorRes final int keyTextColor) {
            mKeyTextColor = getContext().getResources().getColor(keyTextColor);
            return this;
        }

        @NonNull
        public MyRoundKey.Builder setKeyPaintAlpha(final int alpha){
            mKeyPaintAlpha = alpha;
            return this;
        }

        public MyRoundKey.Builder setRipplePaintAlpha(final int alpha){
            mRipplePaintAlpha = alpha;
            return this;
        }

        @Override
        public Key buildInternal(@NonNull final String keyTitle,
                                 @NonNull final Rect bound) {
            return new MyRoundKey(this, keyTitle, bound);
        }

        private void setDefaults(@NonNull Context context) {
            mKeyTextColor = context.getResources().getColor(R.color.lib_key_default_color);
            mKeyStrokeColor = context.getResources().getColor(R.color.white);//默认透明
            mKeyTextSize = context.getResources().getDimension(R.dimen.lib_key_text_size);//默认text大小
            mKeyStrokeWidth = context.getResources().getDimension(R.dimen.lib_key_stroke_width);
            mKeyPadding = getContext().getResources().getDimension(R.dimen.lib_key_padding);
        }
    }
}
