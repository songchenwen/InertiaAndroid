/*
 * Copyright 2014 songchenwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.songchenwen.inertiaandroid.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.songchenwen.inertiaandroid.Inertia;

/**
 * Created by songchenwen on 14-6-10.
 */
public class PieView extends View implements Inertia.InertiaObject {
    public static final float DefaultStartAngle = 90;
    public static final int[] Colors = new int[]{Color.rgb(254, 35, 93), Color.rgb(36, 182, 212),
            Color.rgb(38, 230, 91), Color.rgb(194, 253, 70), Color.rgb(237, 193, 155),
            Color.rgb(100, 114, 253), Color.rgb(117, 140, 129), Color.rgb(200, 47, 217),
            Color.rgb(239, 204, 41), Color.rgb(253, 38, 38), Color.rgb(253, 160, 38),
            Color.rgb(144, 183, 177)};
    private static final float TotalAngle = 360;

    private static final Paint paint = new Paint();

    static {
        paint.setAntiAlias(true);
    }

    private float startAngle = DefaultStartAngle;
    private Inertia inertia = new Inertia(this);

    public PieView(Context context) {
        super(context);
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStartAngle(float angle) {
        this.startAngle = angle;
        causeDraw();
    }

    public void setStartAngleRound360(float angle) {
        if (angle < 0) {
            angle = angle + (float) Math.ceil(angle / -360.0) * 360.0f;
        }
        if (angle >= 360) {
            angle = angle % 360;
        }
        setStartAngle(angle);
    }

    public float getStartAngle() {
        return startAngle;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawARGB(0, 0, 0, 0);
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        float angle = this.startAngle;
        float deltaAngle = TotalAngle / (float) Colors.length;
        for (int i = 0;
             i < Colors.length;
             i++) {
            paint.setColor(Colors[i]);
            canvas.drawArc(rect, angle, deltaAngle, true, paint);
            angle += deltaAngle;
        }
        super.onDraw(canvas);
    }

    public void causeDraw() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }


    // rotation start
    double fingerRotation;
    double newFingerRotation;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float xc = getWidth() / 2;
        final float yc = getHeight() / 2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setStartAngleRound360(getStartAngle());
                fingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                inertia.beginCollectSpeed();
                break;
            case MotionEvent.ACTION_MOVE:
                newFingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                double rotationDelta = newFingerRotation - fingerRotation;
                if (Math.abs(rotationDelta) > 360.0 / 6) {
                    break;
                }
                setStartAngle((float) (getStartAngle() + rotationDelta));
                fingerRotation = newFingerRotation;
                break;
            case MotionEvent.ACTION_UP:
                fingerRotation = newFingerRotation = 0.0f;
                inertia.endCollectSpeed();
                break;
        }
        return true;
    }

    @Override
    public void setDistance(float distance) {
        setStartAngle(distance);
    }

    @Override
    public float getDistance() {
        return getStartAngle();
    }
}
