package com.jianjian.jc486415.memorygame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;


public class MenuActivity extends FragmentActivity {

    private final static String FILE_NAME = "ScoreEntities";
    private Button btn_2x2;
    private Button btn_4x4;
    private Button btn_5x5;
    private Button btn_table;
    private TextView textNameAge;
    private String name;
    private int age;
    private float score;

    private FragmentManager mFragmentManager;
    private TableFragment tableFragment;

    private ArrayList<ScoreEntity> scores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        // TODO : make new file for high scores, if one doesn't exist


        Bundle extrasBundle = this.getIntent().getExtras();
        name = extrasBundle.getString("name");
        age = extrasBundle.getInt("age");
        score = extrasBundle.getFloat("score");


        mFragmentManager = getSupportFragmentManager();

        loadScores();

        // check if it's a high score
        if(score != 0) {
            ScoreEntity tempScore;
            if(scores.isEmpty()) {
                tempScore = new ScoreEntity(name, score);
                scores.add(tempScore);
                writeToFile(scores);
            } else if(score > scores.get(scores.size()-1).getScore()) {
                tempScore = new ScoreEntity(name, score);
                if (scores.size() == 10)
                    scores.remove(scores.size() - 1);
                scores.add(tempScore);
                writeToFile(scores);
                loadScores();
            }
        }

        textNameAge = (TextView) findViewById(R.id.text_name_age);
        textNameAge.setText("Hello, " + name);


        btn_2x2 = findViewById(R.id.button_2X2);
        btn_2x2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterGame(2, 2, 20);
            }
        });

        btn_4x4 = findViewById(R.id.button_4X4);
        btn_4x4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterGame(4, 4, 90);
            }
        });

        btn_5x5 = findViewById(R.id.button_5X5);
        btn_5x5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterGame(5, 5, 120);
            }
        });

        btn_table = findViewById(R.id.btn_table);
        btn_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterTable();
            }
        });

    }

    private void loadScores() {
        scores = new ArrayList<>();
        scores.addAll(readFile());
        if(scores != null)
            Collections.sort(scores);
    }

    private ArrayList<ScoreEntity> readFile() {
        FileInputStream fis;
        Object obj = null;
        boolean cont = true;
        ArrayList<ScoreEntity> objects = new ArrayList<>();
        try {
            fis = this.openFileInput(FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            while(cont){
                obj = is.readObject();
                if(obj != null) {
                    ScoreEntity temp = (ScoreEntity)obj;
                    objects.add(temp);
                } else {
                    cont = false;
                    is.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return objects;
    }

    //for mock data
    public void writeToFile(ScoreEntity[] objects) {
        FileOutputStream fos;
        File file;
        try {
            if(!new File(FILE_NAME).exists())
             file = new File(this.getFilesDir(), FILE_NAME);
            fos = this.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            for(int i = 0; i < objects.length; i++)
                os.writeObject(objects[i]);
            os.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(ArrayList<ScoreEntity> objects) {
        FileOutputStream fos;
        File file;
        try {
            if(!new File(FILE_NAME).exists())
                file = new File(this.getFilesDir(), FILE_NAME);
            fos = this.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            for(int i = 0; i < objects.size(); i++)
                os.writeObject(objects.get(i));
            os.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void EnterTable() {
        // TODO : open table fragment
        if (mFragmentManager.getBackStackEntryCount() > 0) { // don't let it open two fragments simultaneously
            mFragmentManager.popBackStack();
        }
        showFragment();
        tableFragment = new TableFragment();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(R.id.fragment_frame, tableFragment).addToBackStack("my_fragment");
        ft.commit();
        tableFragment.setScoreEntities(scores);
    }


    private void EnterGame(int rows, int cols, int time) {
        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
        intent.putExtra("rows", rows);
        intent.putExtra("columns", cols);
        intent.putExtra("time", time);
        intent.putExtra("name", name);
        intent.putExtra("age", age);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            hideFragment();
            mFragmentManager.popBackStack();
        } else {
            moveTaskToBack(true);
        }
    }

    private void hideFragment() {
        findViewById(R.id.fragment_frame).setVisibility(View.GONE);
        findViewById(R.id.menu_header).setVisibility(View.VISIBLE);
        btn_2x2.setVisibility(View.VISIBLE);
        btn_4x4.setVisibility(View.VISIBLE);
        btn_5x5.setVisibility(View.VISIBLE);
    }

    private void showFragment() {
        findViewById(R.id.fragment_frame).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_header).setVisibility(View.GONE);
        btn_2x2.setVisibility(View.GONE);
        btn_4x4.setVisibility(View.GONE);
        btn_5x5.setVisibility(View.GONE);
    }

}
