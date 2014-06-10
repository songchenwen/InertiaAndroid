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

package com.songchenwen.inertiaandroid;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

/**
 * Created by songchenwen on 14-6-10.
 */
public class Inertia {
    public static interface InertiaObject {
        public void setDistance(float distance);

        public float getDistance();
    }

    public static final int DefaultSpeedCollectionDuration = 150;
    public static final float DefaultInertiaResistance = 0.0005f;
    public static final int DefaultInertiaDrawInterval = 20;

    private int speedCollectionDuration = DefaultSpeedCollectionDuration;
    private float inertiaResistance = DefaultInertiaResistance;
    private int inertiaDrawInterval = DefaultInertiaDrawInterval;
    private Handler handler;
    private InertiaObject inertiaObject;

    public Inertia(InertiaObject object, int speedCollectionDuration, float inertiaResistance,
                   int inertiaDrawInterval, Handler handler) {
        if (object == null) {
            throw (new NullPointerException());
        }
        this.speedCollectionDuration = speedCollectionDuration;
        this.inertiaResistance = inertiaResistance;
        this.inertiaDrawInterval = inertiaDrawInterval;
        this.inertiaObject = object;
        if (handler != null) {
            this.handler = handler;
        } else {
            this.handler = new Handler(Looper.getMainLooper());
        }
    }

    public Inertia(InertiaObject object, int speedCollectionDuration, float inertiaResistance,
                   int inertiaDrawInterval) {
        this(object, speedCollectionDuration, inertiaResistance, inertiaDrawInterval, null);
    }

    public Inertia(InertiaObject object) {
        this(object, DefaultSpeedCollectionDuration, DefaultInertiaResistance,
                DefaultInertiaDrawInterval);
    }

    private ArrayList<TimeAndDistance> distances = new ArrayList<TimeAndDistance>();

    public void beginCollectSpeed() {
        reset();
        collectDistanceRunnable.run();
    }

    public void endCollectSpeed() {
        handler.removeCallbacks(collectDistanceRunnable);
        handler.removeCallbacks(inertiaDrawRunnable);
        TimeAndDistance currentRotation = new TimeAndDistance();
        if (distances.size() > 0) {
            TimeAndDistance lastRotation = null;
            for (int i = distances.size() - 1;
                 i >= 0;
                 i--) {
                TimeAndDistance r = distances.get(i);
                if (currentRotation.timeStamp - r.timeStamp >= speedCollectionDuration) {
                    lastRotation = r;
                    break;
                }
            }
            distances.clear();
            if (lastRotation != null) {
                handleInertia(currentRotation, lastRotation);
            }
        }
    }

    public void reset() {
        distances.clear();
        handler.removeCallbacks(collectDistanceRunnable);
        handler.removeCallbacks(inertiaDrawRunnable);
        inertiaSpeed = 0;
        lastInertialDrawTime = System.currentTimeMillis();
    }

    private Runnable collectDistanceRunnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(collectDistanceRunnable);
            TimeAndDistance cr = new TimeAndDistance();
            for (int i = distances.size() - 1;
                 i >= 0;
                 i--) {
                TimeAndDistance r = distances.get(i);
                if (cr.timeStamp - r.timeStamp > speedCollectionDuration * 1.5f) {
                    distances.remove(r);
                }
            }
            distances.add(cr);
            handler.postDelayed(collectDistanceRunnable, speedCollectionDuration);
        }
    };

    private class TimeAndDistance {
        public long timeStamp;
        public float distance;

        public TimeAndDistance() {
            timeStamp = System.currentTimeMillis();
            distance = inertiaObject.getDistance();
        }
    }

    // handle inertial draw begin
    private float inertiaSpeed;
    private long lastInertialDrawTime;

    private void handleInertia(TimeAndDistance currentDistance, TimeAndDistance lastDistance) {
        handler.removeCallbacks(inertiaDrawRunnable);
        inertiaSpeed = (currentDistance.distance - lastDistance.distance) / (float)
                (currentDistance.timeStamp - lastDistance.timeStamp);
        lastInertialDrawTime = System.currentTimeMillis();
        handler.postDelayed(inertiaDrawRunnable, inertiaDrawInterval);
    }

    private Runnable inertiaDrawRunnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(inertiaDrawRunnable);
            if (Math.abs(inertiaSpeed) < inertiaResistance * (float) inertiaDrawInterval) {
                return;
            }
            long drawTime = System.currentTimeMillis();
            long duration = drawTime - lastInertialDrawTime;
            float delta = inertiaSpeed * duration;
            inertiaObject.setDistance(inertiaObject.getDistance() + delta);
            float speedDelta = inertiaResistance * (duration);
            if (inertiaSpeed > 0) {
                if (inertiaSpeed > speedDelta) {
                    inertiaSpeed -= speedDelta;
                    lastInertialDrawTime = drawTime;
                    handler.postDelayed(inertiaDrawRunnable, inertiaDrawInterval);
                }
            } else {
                if (inertiaSpeed < speedDelta) {
                    inertiaSpeed += speedDelta;
                    lastInertialDrawTime = drawTime;
                    handler.postDelayed(inertiaDrawRunnable, inertiaDrawInterval);
                }
            }
        }
    };
}
