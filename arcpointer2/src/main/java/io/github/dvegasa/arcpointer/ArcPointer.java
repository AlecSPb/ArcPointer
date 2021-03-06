package io.github.dvegasa.arcpointer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


/**
 * 01.06.2018
 */

public class ArcPointer extends View {

    private int radius;
    private int colorBackground;
    private int workAngle;
    private int colorLine;
    private int colorMarker;
    private float lineLengthRatio;
    private float markerLengthRatio;
    private float lineStrokeWidth;
    private float markerStrokeWidth;

    /**
     * @deprecated Since 1.0.2, replaced by {@link #notchesColors}
     */
    private int colorNotches;

    /**
     * @deprecated Since 1.0.2, replaced by {@link #notchesStrokeWidth}
     */
    @Deprecated
    private float notchStrokeWidth;

    /**
     * @deprecated Since 1.0.2, replaced by {@link #notchesLengthRatio}
     */
    @Deprecated
    private float notchLengthRatio;

    private float[] notches;
    private boolean isAnimated;
    private long animationVelocity;
    private float value;

    private float[] notchesLengthRatio; //added in v1.0.2
    private float[] notchesStrokeWidth; //added in v1.0.2
    private int[] notchesColors; //added in v1.0.2

    private ValueAnimator animation = null;
    private float finalValue;

    private int startAngle;
    private int sweepAngle;
    private int centerX, centerY;

    private Paint paint;
    private Canvas canvas;
    private RectF oval;

    public ArcPointer(Context context) {
        super(context);
        init();
    }

    public ArcPointer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ArcPointer, 0, 0);
        try {
            colorBackground = a.getColor(R.styleable.ArcPointer_colorBackground, 0xFFCCCCCC);
            radius = a.getDimensionPixelSize(R.styleable.ArcPointer_radius, 250);
            workAngle = a.getInt(R.styleable.ArcPointer_workAngle, 120);
            colorLine = a.getColor(R.styleable.ArcPointer_colorLine, Color.BLACK);
            colorMarker = a.getColor(R.styleable.ArcPointer_colorMarker, 0xFFEE5622);

            lineLengthRatio = a.getFloat(R.styleable.ArcPointer_lineLengthRatio, 0.8f);
            markerLengthRatio = a.getFloat(R.styleable.ArcPointer_markerLengthRatio, 0.4f);
            lineStrokeWidth = a.getFloat(R.styleable.ArcPointer_lineStrokeWidth, 2f);
            markerStrokeWidth = a.getFloat(R.styleable.ArcPointer_markerStrokeWidth, 3f);
            colorNotches = a.getColor(R.styleable.ArcPointer_colorNotches, 0xFF999999);

            notchLengthRatio = a.getFloat(R.styleable.ArcPointer_notchLengthRatio, 0.2f);
            notchStrokeWidth = a.getFloat(R.styleable.ArcPointer_notchStrokeWidth, 1.5f);
            animationVelocity = (long) a.getInt(R.styleable.ArcPointer_animationVelocity, 1500);
        } finally {
            a.recycle();
        }
        init();

        /////////////////////////////////////////////////////////////
        // Init default values
        notchesLengthRatio = new float[]{0.2f};
        notchesStrokeWidth = new float[]{3f};
        notchesColors = new int[]{0xFF999999};
        notches = new float[]{0.5f};
    }

    private void init() {
        initPaints();
        canvas = new Canvas();
        oval = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minw = getPaddingLeft() + getPaddingRight() + radius * 2;

        int minh = getPaddingBottom() + getPaddingTop() + radius * 2;

        setMeasuredDimension(minw, minh);
    }

    private void initPaints() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colorBackground);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /////////////////////////////////////////////////////////////
        // Arc
        startAngle = 270 - workAngle / 2;
        sweepAngle = workAngle;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorBackground);
        paint.setStrokeWidth(0f);

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        oval.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(oval, startAngle, sweepAngle, true, paint);

        /////////////////////////////////////////////////////////////
        // Notches
//        paint.setColor(colorNotches);

        if (notches != null) {
            for (int i = 0; i < notches.length; i++) {

                paint.setColor(colorNotches);

                if (i <= notchesStrokeWidth.length - 1) {
                    paint.setStrokeWidth(notchesStrokeWidth[i]);
                } else {
                    paint.setStrokeWidth(notchesStrokeWidth[notchesStrokeWidth.length - 1]);
                }

                if (i <= notchesColors.length - 1) {
                    paint.setColor(notchesColors[i]);
                } else {
                    paint.setColor(notchesColors[notchesColors.length - 1]);
                }

                float startAngle = 90 - (workAngle / 2);
                float additionalAngle = workAngle * notches[i];
                float totalAngle = startAngle + additionalAngle - 90;

//                float markLength = radius * notchLengthRatio;
                float markLength;
                if (i <= notchesLengthRatio.length - 1) {
                    markLength = radius * notchesLengthRatio[i];
                } else {
                    markLength = radius * notchesLengthRatio[notchesLengthRatio.length - 1];
                }

                float offsetTopX = (float) (radius * Math.sin(Math.toRadians(totalAngle)));
                float offsetTopY = (float) (radius * Math.cos(Math.toRadians(totalAngle)));
                float offsetBottomX = (float) ((radius - markLength) * Math.sin(Math.toRadians(totalAngle)));
                float offsetBottomY = (float) ((radius - markLength) * Math.cos(Math.toRadians(totalAngle)));

                float topX = centerX + offsetTopX;
                float topY = centerY - offsetTopY;
                float bottomX = centerX + offsetBottomX;
                float bottomY = centerY - offsetBottomY;
                canvas.drawLine(topX, topY, bottomX, bottomY, paint);
            }
        }

        /////////////////////////////////////////////////////////////
        // Line
        // angle {1}
        final float additional = workAngle * value;
        final float starting = 90 - (workAngle / 2);

        final float angle = additional + starting - 90;
        // end {1}

        final float c = radius * lineLengthRatio; // line's length
        final float a = (float) (c * Math.sin(Math.toRadians(angle))); // x offset
        final float b = (float) (c * Math.cos(Math.toRadians(angle))); // y offset

        final float lineStartX = centerX;
        final float lineStartY = centerY;
        float lineStopX = lineStartX + a;
        float lineStopY = lineStartY - b;

        paint.setColor(colorLine);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineStrokeWidth);

        if (lineStrokeWidth <= 0) {
            paint.setColor(0x00000000);
        }

        canvas.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, paint);

        /////////////////////////////////////////////////////////////
        // Marker
        final float orC = c * (1f - markerLengthRatio);
        final float orA = (float) (orC * Math.sin(Math.toRadians(angle)));
        final float orB = (float) (orC * Math.cos(Math.toRadians(angle)));

        final float orLineStartX = centerX + orA;
        final float orLineStartY = centerY - orB;

        paint.setColor(colorMarker);
        paint.setStrokeWidth(markerStrokeWidth);

        if (markerStrokeWidth <= 0) {
            paint.setColor(0x00000000);
        }

        canvas.drawLine(orLineStartX, orLineStartY, lineStopX, lineStopY, paint);

    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());

        bundle.putInt("radius", this.radius);
        bundle.putInt("colorBackground", this.colorBackground);
        bundle.putInt("workAngle", this.workAngle);
        bundle.putInt("colorLine", this.colorLine);
        bundle.putInt("colorMarker", this.colorMarker);

        bundle.putFloat("lineLengthRatio", this.lineLengthRatio);
        bundle.putFloat("markerLengthRatio", this.markerLengthRatio);
        bundle.putFloat("lineStrokeWidth", this.lineStrokeWidth);
        bundle.putFloat("markerStrokeWidth", this.markerStrokeWidth);
        bundle.putInt("colorNotches", this.colorNotches);

        bundle.putFloat("notchStrokeWidth", this.notchStrokeWidth);
        bundle.putFloat("notchLengthRatio", this.notchLengthRatio);
        bundle.putFloatArray("notches", this.notches);
        bundle.putBoolean("isAnimated", this.isAnimated);
        bundle.putLong("animationVelocity", this.animationVelocity);

        bundle.putFloat("value", this.value);
        bundle.putFloat("finalValue", this.finalValue);

        // v 1.0.2 {
        bundle.putFloatArray("notchesLengthRatio", this.notchesLengthRatio);
        bundle.putFloatArray("notchesStrokeWidth", this.notchesStrokeWidth);
        // v 1.0.2 }

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            this.radius = bundle.getInt("radius");
            this.colorBackground = bundle.getInt("colorBackground");
            this.workAngle = bundle.getInt("workAngle");
            this.colorLine = bundle.getInt("colorLine");
            this.colorMarker = bundle.getInt("colorMarker");

            this.lineLengthRatio = bundle.getFloat("lineLengthRatio");
            this.markerLengthRatio = bundle.getFloat("markerLengthRatio");
            this.lineStrokeWidth = bundle.getFloat("lineStrokeWidth");
            this.markerStrokeWidth = bundle.getFloat("markerStrokeWidth");
            this.colorNotches = bundle.getInt("colorNotches");

            this.notchStrokeWidth = bundle.getFloat("notchStrokeWidth");
            this.notchLengthRatio = bundle.getFloat("notchLengthRatio");
            this.notches = bundle.getFloatArray("notches");
            this.isAnimated = bundle.getBoolean("isAnimated");
            this.animationVelocity = bundle.getLong("animationVelocity");

            this.value = bundle.getFloat("value");
            this.finalValue = bundle.getFloat("finalValue");

            // v 1.0.2 {
            this.notchesLengthRatio = bundle.getFloatArray("notchesLengthRatio");
            this.notchesStrokeWidth = bundle.getFloatArray("notchesStrokeWidth");
            // v 1.0.2 }
        }
        this.setValue(finalValue); /* continue animation */
        invalidate();
        super.onRestoreInstanceState(state);
    }

    /////////////////////////////////////////////////////////////
    // Getters and setters

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        requestLayout();
        invalidate();
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public void setColorBackground(int colorBackground) {
        this.colorBackground = colorBackground;
        invalidate();
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (value < 0 || value > 1) {
            throw new RuntimeException("Param 'value' must be float between 0 and 1");
        }
        finalValue = value;

        float previousValue = this.value;

        if (animation != null) {
            animation.cancel();
        }

        if (isAnimated) {
            float delta = Math.abs(previousValue - value);
            long animationDuration = (long) (animationVelocity * (delta / 1.0f));

            animation = ValueAnimator.ofFloat(previousValue, value);
            animation.setDuration(animationDuration);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ArcPointer.this.value = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animation.start();
        } else {
            this.value = value;
        }

        invalidate();
    }

    public int getWorkAngle() {
        return workAngle;
    }

    public void setWorkAngle(int workAngle) {
        if (workAngle < 0) {
            throw new RuntimeException("Param 'workAngle' must be int greater than 0");
        }
        this.workAngle = workAngle;
        invalidate();
    }

    public int getColorLine() {
        return colorLine;
    }

    public void setColorLine(int colorLine) {
        this.colorLine = colorLine;
        invalidate();
    }

    public int getColorMarker() {
        return colorMarker;
    }

    public void setColorMarker(int colorMarker) {
        this.colorMarker = colorMarker;
        invalidate();
    }

    public float getLineLengthRatio() {
        return lineLengthRatio;
    }

    public void setLineLengthRatio(float lineLengthRatio) {
        this.lineLengthRatio = lineLengthRatio;
        invalidate();
    }

    public float getMarkerLengthRatio() {
        return markerLengthRatio;
    }

    public void setMarkerLengthRatio(float markerLengthRatio) {
        this.markerLengthRatio = markerLengthRatio;
        invalidate();
    }

    public float getLineStrokeWidth() {
        return lineStrokeWidth;
    }

    public void setLineStrokeWidth(float lineStrokeWidth) {
        this.lineStrokeWidth = lineStrokeWidth;
        invalidate();
    }

    public float getMarkerStrokeWidth() {
        return markerStrokeWidth;
    }

    public void setMarkerStrokeWidth(float markerStrokeWidth) {
        this.markerStrokeWidth = markerStrokeWidth;
        invalidate();
    }

    public float[] getNotches() {
        return notches;
    }

    public void setNotches(int n) {
        if (n < 0) n = 0;

        float[] m = new float[n];
        float step = (float) 1 / (n + 1);

        for (int i = 1; i <= n; i++) {
            m[i - 1] = step * i;
        }
        this.notches = m;

        invalidate();
    }

    public void setNotches(float[] notches) {
        this.notches = notches;
        invalidate();
    }

    /**
     * @deprecated since 1.0.2, use {@link #getNotchesLengthRatio()} instead
     */
    @Deprecated
    public float getNotchLengthRatio() {
        return notchesLengthRatio[0];
    }

    /**
     * @deprecated since 1.0.2, use {@link #setNotchesLengthRatio(float[] ratios)} and its overloads instead
     */
    @Deprecated
    public void setNotchLengthRatio(float notchLengthRatio) {
        setNotchesLengthRatio(notchLengthRatio);
    }

    public float[] getNotchesLengthRatio() {
        return notchesLengthRatio;
    }

    public void setNotchesLengthRatio(float ratio) {
        this.notchesLengthRatio = new float[]{ratio};
        invalidate();
    }

    public void setNotchesLengthRatio(float[] ratios) {
        this.notchesLengthRatio = ratios;
        invalidate();
    }

    /**
     * @deprecated since 1.0.2, use {@link #getNotchesColors()} instead
     */
    @Deprecated
    public int getColorNotches() {
        return colorNotches;
    }

    /**
     * @deprecated since 1.0.2, use {@link #setNotchesColors(int[])} and its overload instead
     */
    @Deprecated
    public void setColorNotches(int colorNotches) {
        this.colorNotches = colorNotches;
        invalidate();
    }

    public void setNotchesColors(int[] colors) {
        this.notchesColors = colors;
        invalidate();
    }

    public int[] getNotchesColors() {
        return this.notchesColors;
    }

    public void setNotchesColors(int color) {
        this.notchesColors = new int[]{color};
        invalidate();
    }

    /**
     * @deprecated since 1.0.2, use {@link #getNotchesStrokeWidth()} instead
     */
    @Deprecated
    public float getNotchStrokeWidth() {
        return this.notchesStrokeWidth[0];
    }

    /**
     * @deprecated since 1.0.2, use {@link #setNotchesStrokeWidth(float[])} and its overload instead
     */
    @Deprecated
    public void setNotchStrokeWidth(float notchStrokeWidth) {
        setNotchesStrokeWidth(notchStrokeWidth);
        invalidate();
    }

    public void setNotchesStrokeWidth(float notchesStrokeWidth) {
        this.notchesStrokeWidth = new float[]{notchesStrokeWidth};
        invalidate();
    }

    public float[] getNotchesStrokeWidth() {
        return this.notchesStrokeWidth;
    }

    public void setNotchesStrokeWidth(float[] notchesStrokeWidth) {
        this.notchesStrokeWidth = notchesStrokeWidth;
        invalidate();
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public long getAnimationVelocity() {
        return animationVelocity;
    }

    public void setAnimationVelocity(long animationVelocity) {
        this.animationVelocity = animationVelocity;
    }
}
