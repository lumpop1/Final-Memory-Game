package com.jianjian.jc486415.memorygame;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class TableFragment extends Fragment {

    private ArrayList<TextView> tvs = new ArrayList<>();
    private ArrayList<ScoreEntity> scores;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_table,container,false);

        tvs.add((TextView)(view.findViewById(R.id.top1)));
        tvs.add((TextView)(view.findViewById(R.id.top2)));
        tvs.add((TextView)(view.findViewById(R.id.top3)));
        tvs.add((TextView)(view.findViewById(R.id.top4)));
        tvs.add((TextView)(view.findViewById(R.id.top5)));
        tvs.add((TextView)(view.findViewById(R.id.top6)));
        tvs.add((TextView)(view.findViewById(R.id.top7)));
        tvs.add((TextView)(view.findViewById(R.id.top8)));
        tvs.add((TextView)(view.findViewById(R.id.top9)));
        tvs.add((TextView)(view.findViewById(R.id.top10)));

        for(int i = 0; i < scores.size(); i++) {
            tvs.get(i).setText(i + ". Name: " + scores.get(i).getName() + ", Score: " + scores.get(i).getScore());
        }


        return view;
    }

    public void setScoreEntities(ArrayList<ScoreEntity> scores){
        this.scores = scores;
    }


}




