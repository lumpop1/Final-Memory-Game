package com.jianjian.jc486415.memorygame;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import tyrantgit.explosionfield.ExplosionField;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private int numOfElements;
    private GameButton[] buttons;
    private int[] buttonGraphicLocation;
    private int[] buttonGraphics;

    private GameButton selectedButton1;
    private GameButton selectedButton2;

    private TextView textName;

    private boolean isBusy = false;

    private final Handler handler = new Handler();
    private long time;
    private CountDownTimer timer;
    private float score = 0;

    private String name;
    private int age;

    private GridLayout grid;
    private ExplosionField mExplosionField;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private SensorEventListener rotationEventListener;
    private boolean isSnapped = false;
    private float[] initialPosition = new float[3];
    private float scoreCoefficient;


    private Stack<GameButton> matchedPairs = new Stack<>();
    private int pairedNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        Bundle extrasBundle = this.getIntent().getExtras();
        grid = (GridLayout) findViewById(R.id.game_grid);

        int numCols = extrasBundle.getInt("columns");
        int numRows = extrasBundle.getInt("rows");
        time = (long) extrasBundle.getInt("time");
        name = extrasBundle.getString("name");
        age = extrasBundle.getInt("age");

        switch ((int) time) {
            case 20:
                scoreCoefficient = 0.25f;
                break;
            case 90:
                scoreCoefficient = 0.5f;
                break;
            case 120:
                scoreCoefficient = 0.75f;
                break;
        }


        textName = (TextView) findViewById(R.id.name_container_text);
        textName.setText(name);

        grid.setColumnCount(numCols);
        grid.setRowCount(numRows);

        numOfElements = numCols * numRows;

        buttons = new GameButton[numOfElements];
        buttonGraphics = new int[numOfElements / 2];

        loadGraphics();

        buttonGraphicLocation = new int[numOfElements];

        shuffleButtonGraphics();

        // fill grid with buttons
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                GameButton tempButton = new GameButton(this, row, col, buttonGraphics[buttonGraphicLocation[(row * numCols) + col]], numOfElements);
                tempButton.setId(View.generateViewId());
                tempButton.setOnClickListener(this);
                buttons[(row * numCols) + col] = tempButton;
                grid.addView(tempButton);
            }
        }

        mExplosionField = ExplosionField.attach2Window(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (rotationSensor == null) {
            Toast.makeText(this, getString(R.string.no_gyro_text), Toast.LENGTH_LONG).show();
        }

        rotationEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (isSnapped) {
                    if (IsTilted(sensorEvent) && pairedNum > 0) {
                        punishPlayer();
                    }
                } else {
                    for (int i = 0; i < 3; i++)
                        initialPosition[i] = sensorEvent.values[i];
                    isSnapped = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) { }
        };

        sensorManager.registerListener(rotationEventListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        //start timer
        countDownStart();

    }

    private void punishPlayer() {
        GameButton gb;
        isBusy = true;
        for (int i = 0; i < 2; i++) {
            gb = matchedPairs.pop();
            gb.setMatched(false);
            gb.setEnabled(true);
            gb.flip();
        }
        pairedNum--;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isBusy = false;
            }
        }, 500);
    }

    private boolean IsTilted(SensorEvent sensorEvent) {
        for (int i = 0; i < initialPosition.length; i++)
            if (sensorEvent.values[i] > initialPosition[i] + 0.5 || sensorEvent.values[i] < initialPosition[i] - 0.5) {
                return true;
            }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(rotationEventListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(rotationEventListener);
    }

    private void countDownStart() {
        timer = new CountDownTimer((long) time * 1000, 1000) {

            TextView timer = (TextView) findViewById(R.id.seconds_left_text);

            public void onTick(long millisUntilFinished) {
                if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) == 10)
                    timer.setTextColor(Color.RED);
                timer.setText(" " + TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
                score = ((TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) * scoreCoefficient) + (numOfElements * scoreCoefficient)) * 1000;
            }

            public void onFinish() {
                timer.setText("HALT!!!");
                Toast.makeText(GameActivity.this, getString(R.string.loser_text), Toast.LENGTH_LONG).show();

                mExplosionField.explode(textName);
                mExplosionField.explode((TextView) findViewById(R.id.seconds_left_text));

                TransitionManager.beginDelayedTransition(grid, makeExplodeTransition());
                toggleVisibility(buttons);

                score = 0;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnToMenu();
                    }
                }, 2000);
            }
        }.start();
    }

    private void returnToMenu() {
        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("age", age);
        intent.putExtra("score", score);
        startActivity(intent);
    }

    private void loadGraphics() {
        buttonGraphics[0] = R.drawable.button_1;
        buttonGraphics[1] = R.drawable.button_2;
        if (numOfElements > 4) {
            buttonGraphics[2] = R.drawable.button_3;
            buttonGraphics[3] = R.drawable.button_4;
            buttonGraphics[4] = R.drawable.button_5;
            buttonGraphics[5] = R.drawable.button_6;
            buttonGraphics[6] = R.drawable.button_7;
            buttonGraphics[7] = R.drawable.button_8;
        }
        if (numOfElements > 16) {
            buttonGraphics[8] = R.drawable.button_9;
            buttonGraphics[9] = R.drawable.button_10;
            buttonGraphics[10] = R.drawable.button_11;
            buttonGraphics[11] = R.drawable.button_12;
        }
    }


    protected void shuffleButtonGraphics() {
        Random rand = new Random();

        for (int i = 0; i < numOfElements; i++) {
            buttonGraphicLocation[i] = i % (numOfElements / 2);
        }

        for (int i = 0; i < numOfElements; i++) {
            int temp = buttonGraphicLocation[i];
            int swapIdx = rand.nextInt(numOfElements);
            buttonGraphicLocation[i] = buttonGraphicLocation[swapIdx];
            buttonGraphicLocation[swapIdx] = temp;
        }

    }


    @Override
    public void onClick(View view) {

        if (isBusy)
            return;

        GameButton button = (GameButton) view;

        if (button.isMatched())
            return;

        if (selectedButton1 == null) {
            selectedButton1 = button;
            selectedButton1.flip();
            return;
        }

        if (selectedButton1.getId() == button.getId())
            return;

        if (selectedButton1.getFrontImageDrawableId() == button.getFrontImageDrawableId()) {
            button.flip();
            selectedButton1.setMatched(true);
            selectedButton1.setEnabled(false);
            button.setEnabled(false);

            //save last pair flipped
            matchedPairs.push(selectedButton1);
            matchedPairs.push(button);

            selectedButton1 = null;
            pairedNum++;
            checkIfWon();
            return;
        } else {
            selectedButton2 = button;
            selectedButton2.flip();
            isBusy = true;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedButton2.flip();
                    selectedButton1.flip();
                    selectedButton1 = null;
                    selectedButton2 = null;
                    isBusy = false;
                }
            }, 500);
        }
    }

    private void checkIfWon() {
        if (pairedNum == buttonGraphics.length) {
            timer.cancel();
            Toast.makeText(this, getString(R.string.winner_text),
                    Toast.LENGTH_LONG).show();

            mExplosionField.explode(textName);
            mExplosionField.explode((TextView) findViewById(R.id.seconds_left_text));
            mExplosionField.explode((TextView) findViewById(R.id.time_text));

            TransitionManager.beginDelayedTransition(grid, makeFadeTransition());
            toggleVisibility(buttons);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    returnToMenu();
                }
            }, 2000);
        }
    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        returnToMenu();
    }


    private Explode makeExplodeTransition() {
        Explode explode = new Explode();
        explode.setDuration(2000);
        explode.setInterpolator(new AnticipateOvershootInterpolator());
        return explode;
    }

    private Fade makeFadeTransition() {
        Fade fade = new Fade();
        fade.setDuration(2000);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }

    // Custom method to toggle visibility of views
    private void toggleVisibility(View... views) {
        // Loop through the views
        for (View v : views) {
            if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
    }
}
