package com.alperez.hyrocam;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

/**
 * Created by stanislav.perchenko on 23-Oct-15.
 */
public class SensorsController implements SensorEventListener {

    public enum SensorSourceType {
        ROTATION_VECTOR, RAW;
    }

    public interface OnAnglesListener {
        void onAnglesChanged(SensorSourceType srcType, float yaw, float pitch, float roll, float camRelAzimuth, float camRelInclination, double testScalProd);
    }

    private SensorManager mSm;
    private boolean activated;
    private List<WeakReference<OnAnglesListener>> mListeners = new ArrayList<>();

    public SensorsController(SensorManager sm) {
        if (sm == null) {
            throw new IllegalArgumentException("Instance of the SensorManager must be provided");
        }
        mSm = sm;
    }


    public boolean activate(int periodUs) {
        if (!activated) {
            Sensor[] sensors = new Sensor[3];


            for (Sensor s : mSm.getSensorList(Sensor.TYPE_ROTATION_VECTOR)) {
                if (s.getVendor().contains("Google Inc.") && s.getVersion() >= 3) {
                    sensors[0] = s;
                    break;
                }
            }
            if (sensors[0] == null) {
                sensors[0] = mSm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            }
            sensors[1] = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensors[2] = mSm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            for (Sensor s : sensors) {
                if (s != null) {
                    mSm.registerListener(this, s, periodUs);
                }
            }
            activated = true;

        }
        return false;
    }

    public void release() {
        if (activated) {
            activated = false;
            mSm.unregisterListener(this);
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public void addOrientationListener(OnAnglesListener l) {
        for (WeakReference<OnAnglesListener> wl : mListeners) {
            if ((wl.get() != null) && (wl.get() == l)) {
                return;
            }
        }
        mListeners.add(new WeakReference<OnAnglesListener>(l));
    }

    public void removeOrientationListener(OnAnglesListener l) {
        int index = -1;
        int i=0;
        for (WeakReference<OnAnglesListener> wl : mListeners) {
            if ((wl.get() != null) && (wl.get() == l)) {
                index = i;
                break;
            }
            i++;
        }
        if (index >= 0) {
            mListeners.remove(index);
        }
    }

    /**
     * Set target coordinates (x, y, z)
     * @param target
     */
    public void setTargetLocation(float[] target) {
        if (target != null && target.length >= 3) {
            System.arraycopy(target, 0, mTarget, 0, 3);
        }
    }

    /**
     * Set device location (x, y, z)
     * @param location
     */
    public void setSelfLocation(float[] location) {
        if (location != null && location.length >= 3) {
            System.arraycopy(location, 0, mDeviceLocation, 0, 3);
        }
    }

    private float[] mTarget = new float[3];
    private float[] mDeviceLocation = new float[3];

    /**********************************************************************************************/
    /********************************   Receiving sensor events   *********************************/
    /**********************************************************************************************/
    // Sensors' data
    private float[] mAccelRawData = new float[3];
    private boolean accelDataUpdated;
    private float[] mMagnetRawData = new float[3];
    private boolean magnetDataUpdated;
    private float[] mRawRotationVector = new float[3];

    private float[] mOrigRotationMatrixAccelMag = new float[9];
    private float[] mOrigRotationMatrixRotVect = new float[9];

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccelRawData, 0, 3);
                accelDataUpdated = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagnetRawData, 0, 3);
                magnetDataUpdated = true;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, mRawRotationVector, 0, 3);
                SensorManager.getRotationMatrixFromVector(mOrigRotationMatrixRotVect, mRawRotationVector);
                calculate(SensorSourceType.ROTATION_VECTOR, mOrigRotationMatrixRotVect);
                break;
        }

        if (accelDataUpdated && magnetDataUpdated) {
            accelDataUpdated = false;
            magnetDataUpdated = false;
            SensorManager.getRotationMatrix(mOrigRotationMatrixAccelMag, null, mAccelRawData, mMagnetRawData);
            calculate(SensorSourceType.RAW, mOrigRotationMatrixAccelMag);
        }
    }


    /**********************************************************************************************/
    /************************   Implementation of all calculations   ******************************/
    /**********************************************************************************************/
    private float[] mYawPitchRollAngles = new float[3];




    private void calculate(SensorSourceType srcType, float[] rotMatrix) {
        SensorManager.getOrientation(rotMatrix, mYawPitchRollAngles);


        /*
         * 3x3 (length=9) case:
         *   /  R[ 0]   R[ 1]   R[ 2]  \
         *   |  R[ 3]   R[ 4]   R[ 5]  |
         *   \  R[ 6]   R[ 7]   R[ 8]  /
         */
        final float[] Xort = new float[3];
        final float[] Yort = new float[3];
        Xort[0] = rotMatrix[0];
        Xort[1] = rotMatrix[3];
        Xort[2] = rotMatrix[6];
        Yort[0] = rotMatrix[1];
        Yort[1] = rotMatrix[4];
        Yort[2] = rotMatrix[7];


        //----  Constants for plane surface  ----
        // A*x + B*y + C*z = 0
        float A = Xort[1]*Yort[2] - Yort[1]*Xort[2];
        float B = Yort[0]*Xort[2] - Xort[0]*Yort[2];
        float C = Xort[0]*Yort[1] - Yort[0]*Xort[1];


        //----  Update target vector  ----
        float[] T = new float[3];
        T[0] = mTarget[0] - mDeviceLocation[0];
        T[1] = mTarget[1] - mDeviceLocation[1];
        T[2] = mTarget[2] - mDeviceLocation[2];

        //----  Projection of the target vector on the device plane  ----
        final Matrix M = new Matrix(3, 3);
        M.set(0, 0, B);
        M.set(0, 1, -A);
        M.set(0, 2, 0);
        M.set(1, 0, C);
        M.set(1, 1, 0);
        M.set(1, 2, -A);
        M.set(2, 0, A);
        M.set(2, 1, B);
        M.set(2, 2, C);

        double det = M.det();

        final Matrix V = new Matrix(3, 1);
        V.set(0, 0, T[0]*B - T[1]*A);
        V.set(1, 0, T[1]*C - T[2]*A);
        V.set(2, 0, 0);

        final Matrix Pmatr = M.solve(V); // Result matrix - projection point
        final float[] P = new float[3];
        P[0] = (float)Pmatr.get(0, 0);
        P[1] = (float)Pmatr.get(1, 0);
        P[2] = (float)Pmatr.get(2, 0);

        //----  Check projection result  ----
        // The angle between the vectors PO and TP must be 90degres
        // So find scalar product
        final double testScalProd = (0-(double)P[0])*((double)T[0]-(double)P[0]) + (0-(double)P[1])*((double)T[1]-(double)P[1]) + (0-(double)P[2])*((double)T[2]-(double)P[2]);


        //----  Find local, plane-relative inclination  ----
        final float[] TP = new float[]{P[0]-T[0], P[1]-T[1], P[2]-T[2]};
        final float inclination = (float)Math.asin(Math.sqrt(TP[0]*TP[0] + TP[1]*TP[1] + TP[2]*TP[2]) / Math.sqrt(T[0]*T[0] + T[1]*T[1] + T[2]*T[2]));

        //----  Find local, plane-relative azimuth  ----
        //----  Use
        final double lenXort = Math.sqrt(Xort[0]*Xort[0] + Xort[1]*Xort[1] + Xort[2]*Xort[2]);
        final double lenOP = Math.sqrt(P[0]*P[0] + P[1]*P[1] + P[2]*P[2]);

        final double cosTheta = (Xort[0]*P[0] + Xort[1]*P[1] + Xort[2]*P[2]) / (lenXort * lenOP);
        final double sinTheta = ((Xort[1]*P[2] - Xort[2]*P[1]) + (Xort[2]*P[0]-Xort[0]*P[2]) + (Xort[0]*P[1] - Xort[1]*P[0])) / (lenXort * lenOP);


        double azSin1 = Math.asin(sinTheta);
        double azSin2 = (sinTheta >= 0) ? (Math.PI - azSin1) : (-Math.PI - azSin1);
        double azCos = Math.acos(cosTheta);

        double epsAz1 = Math.min(Math.abs(azSin1 - azCos), Math.abs(azSin1 + azCos));
        double epsAz2 = Math.min(Math.abs(azSin2 - azCos), Math.abs(azSin2 + azCos));

        final float azFinal = (float)((epsAz1 < epsAz2) ? azSin1 : azSin2);


        //----  Report results  ----
        for (WeakReference<OnAnglesListener> wl : mListeners) {
            if (wl.get() != null) {
                wl.get().onAnglesChanged(srcType, mYawPitchRollAngles[0], mYawPitchRollAngles[1], mYawPitchRollAngles[2], azFinal, inclination, testScalProd);
            }
        }
    }


}
