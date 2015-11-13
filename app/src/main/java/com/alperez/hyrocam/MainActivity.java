package com.alperez.hyrocam;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tokaracamara.android.verticalslidevar.VerticalSeekBar;

public class MainActivity extends AppCompatActivity implements SensorsController.OnAnglesListener {

    private static final int MAXIMUM_SELF_ALT = 1000;
    private static final int MAXIMUM_TARGET_DIST = 1600;
    private static final int INITIAL_SELF_ALT = 500;
    private static final int INITIAL_TARGET_X = 150;
    private static final int INITIAL_TARGET_Y = 600;
    private static final int INITIAL_TARGET_ALT = 220;

    private TextView vTxtYaw;
    private TextView vTxtPitch;
    private TextView vTxtRoll;
    private TextView vTxtAzimuth;
    private TextView vTxtInclination;
    private TextView vTxtTestScalProduct;

    private TargetView vTarget;
    private VerticalSeekBar vSelfAltController;
    private VerticalSeekBar vTargetAltController;

    SensorsController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vTxtYaw = (TextView) findViewById(R.id.txt_yaw);
        vTxtPitch = (TextView) findViewById(R.id.txt_pitch);
        vTxtRoll = (TextView) findViewById(R.id.txt_roll);
        vTxtAzimuth = (TextView) findViewById(R.id.txt_azimuth);
        vTxtInclination = (TextView) findViewById(R.id.txt_inclination);
        vTxtTestScalProduct = (TextView) findViewById(R.id.txt_test);
        vTarget = (TargetView) findViewById(R.id.target_set_view);
        vTarget.setRealLifeMaxTargetRadius(MAXIMUM_TARGET_DIST);
        vTarget.setRealLifeTargetAltitude(INITIAL_TARGET_ALT);
        vTarget.setRealLifeSelfAltitude(INITIAL_SELF_ALT);
        vTarget.setOnPositionChecgeListener(new TargetView.OnPositionChecgeListener() {
            float[] self = new float[]{0, 0, 0};
            float[] targ = new float[3];

            @Override
            public void onPositionChanged(boolean isTouchFinished, float targetX, float targetY, float targetZ, float selfZ) {
                self[2] = selfZ;
                mController.setSelfLocation(self);
                targ[0] = targetX;
                targ[1] = targetY;
                targ[2] = targetZ;
                mController.setTargetLocation(targ);
            }
        });
        vSelfAltController = (VerticalSeekBar) findViewById(R.id.self_altitude);
        vSelfAltController.setMax(MAXIMUM_SELF_ALT);
        vSelfAltController.setProgress(INITIAL_SELF_ALT);
        vSelfAltController.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
                vTarget.setRealLifeSelfAltitude(progress);
                if (progress < vTargetAltController.getMax()) {
                    vTargetAltController.setMax(progress);
                } else if (progress < vTargetAltController.getMax()) {
                    vTargetAltController.setMax(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(VerticalSeekBar seekBar) {}
        });
        vTargetAltController = (VerticalSeekBar) findViewById(R.id.target_altitude);
        vTargetAltController.setMax(INITIAL_SELF_ALT);
        vTargetAltController.setProgress(INITIAL_TARGET_ALT);
        vTargetAltController.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
                vTarget.setRealLifeTargetAltitude(progress);
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(VerticalSeekBar seekBar) {}
        });

        mController = new SensorsController((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        mController.setSelfLocation(new float[]{0, 0, INITIAL_SELF_ALT});
        mController.setTargetLocation(new float[]{INITIAL_TARGET_X, INITIAL_TARGET_Y, INITIAL_TARGET_ALT});
    }


    @Override
    public void onStart() {
        super.onStart();
        mController.addOrientationListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mController.activate(SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mController.release();
    }

    @Override
    public void onStop() {
        super.onStop();
        mController.removeOrientationListener(this);
    }


    @Override
    public void onAnglesChanged(SensorsController.SensorSourceType srcType, float yaw, float pitch, float roll, float camRelAzimuth, float camRelInclination, double testScalProd) {
        vTxtYaw.setText(getDegree(yaw));
        vTxtPitch.setText(getDegree(pitch));
        vTxtRoll.setText(getDegree(roll));
        vTxtAzimuth.setText(getDegree(camRelAzimuth));
        vTxtInclination.setText(getDegree(camRelInclination));
        vTxtTestScalProduct.setText(String.format("%1$.6f", testScalProd));
    }

    private static final String DEG_FORMAT = "%1$.1f deg";
    private String getDegree(float angle) {
        double deg = 180f*angle / Math.PI;
        return String.format(DEG_FORMAT, deg);
    }
}
