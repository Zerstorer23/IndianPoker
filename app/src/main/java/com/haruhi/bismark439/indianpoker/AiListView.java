package com.haruhi.bismark439.indianpoker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.haruhi.bismark439.indianpoker.AI.AiData;

import static com.haruhi.bismark439.indianpoker.AILearns.listViewAi;
import static com.haruhi.bismark439.indianpoker.AILearns.onLoad;
import static com.haruhi.bismark439.indianpoker.AILearns.selectedAI_id;
import static com.haruhi.bismark439.indianpoker.MainActivity.AiDB;
import static com.haruhi.bismark439.indianpoker.MainActivity.brainFileName;

/**
 * TODO: document your custom view class.
 */
public class AiListView extends LinearLayout {
    TextView name;
    RadioButton selected;
    TextView condition;
    AiData data;
    int index;

    public AiListView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.res_ai_list_view, this);
        name = findViewById(R.id.ai_name);
        selected = findViewById(R.id.ai_selected);
        condition=findViewById(R.id.condition);

        selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    System.out.println(index+" is clicked.");
                    selectedAI_id=AiDB.get(index).id;
                    for(int i=0;i<listViewAi.getChildCount();i++){
                        AiListView temp = (AiListView) listViewAi.getChildAt(i);
                       if(temp.index!=index)temp.selected.setChecked(false);
                    }
                    onLoad(getContext());
                }
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
              if(data.isUnlocked())selected.setChecked(true);
            }
        });
    }

    public boolean setData(AiData data,int position){
        this.data = data;
        name.setText(data.getName());
        System.out.println(data.getName()+": required win "+data.requiredWin+" unlocked? "+data.isUnlocked());
       // if(total_win>=data.requiredWin){
            this.data.setUnlocked(true);
       // }

        //dont put radiobutton in checkavle
        if(this.data.isUnlocked()){
            selected.setVisibility(VISIBLE);
            condition.setVisibility(INVISIBLE);
        }else{
            selected.setVisibility(INVISIBLE);
            condition.setVisibility(VISIBLE);
            condition.setText("You need to win "+data.requiredWin+" games to unlock this AI.");
        }
        index=position;
        if(this.data.getName().equals(brainFileName))this.selected.setChecked(true);
        return true;
    }
}
