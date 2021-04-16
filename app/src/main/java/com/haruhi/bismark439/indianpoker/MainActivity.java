package com.haruhi.bismark439.indianpoker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.haruhi.bismark439.indianpoker.AI.AiData;
import com.haruhi.bismark439.indianpoker.AI.Main;
import com.haruhi.bismark439.indianpoker.AI.Player;
import com.haruhi.bismark439.indianpoker.AIs.mathAI;

import java.util.ArrayList;

import static com.haruhi.bismark439.indianpoker.AI.SaveUtil.loadAI;
import static com.haruhi.bismark439.indianpoker.AI.SaveUtil.loadUserBrain;
import static com.haruhi.bismark439.indianpoker.AI.userBrain.overrideUserBrain;
import static com.haruhi.bismark439.indianpoker.AILearns.selectedAI_id;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor sharedEditor;
    public static boolean mute = true;
    static final String PREF_NAME = "iPoker_PREF";
    public static Player AIplayer = new mathAI();
    public static Player pPlayer = new mathAI();
    public static Player[] players = {pPlayer, AIplayer};

    public static ArrayList<AiData> AiDB = new ArrayList<>();
    public static Animation translateUpAnim;
    public static Animation translateDownAnim;
    public static Animation translateLeftAnim;
    public static Animation translateRightAnim;
    public static String brainFileName = "";
    public static int total_win = 0;
    public static boolean devmode=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            sharedPref = getApplicationContext().getSharedPreferences(PREF_NAME, getApplicationContext().MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
        //resetSave(3);

    loadUserBrain();//load user
    initDB();//load DB
    players[0].id = 0;
    players[1].id = 1;
    int AIid = sharedPref.getInt("defaultBrain", 0);
    System.out.println("Looked pref: " + AIid);
    if (AIid == 0) {
        overrideUserBrain(AIid);
    } else {
        //Load what? TODO
        selectedAI_id = AIid;
        loadAI(getApplicationContext());
    }
    loadWin(getApplicationContext());
}catch (Exception e){
    TextView tv = findViewById(R.id.debugging);
    tv.setText("");
   StackTraceElement[] tt =  e.getStackTrace();
   for(int i=0;i<tt.length;i++){
       tv.append(tt[i]+"\n");
   }
    toast(getApplicationContext(),e.toString());
}

        translateUpAnim = AnimationUtils.loadAnimation(this, R.anim.translate_up);
        translateDownAnim = AnimationUtils.loadAnimation(this, R.anim.translate_down);
        translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);
        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        translateUpAnim.setAnimationListener(animListener);
        translateDownAnim.setAnimationListener(animListener);
        translateLeftAnim.setAnimationListener(animListener);
        translateRightAnim.setAnimationListener(animListener);
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener {

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {

        }

        public void onAnimationStart(Animation animation) {

        }

    }




    public static void initDB() {
        AiDB = new ArrayList<>();
        AiData temp = new AiData("User Bot", true, "User",0);
        AiDB.add(temp);
        /*temp = new AiData("Melon Bot", true, "mathAI",1);
        AiDB.add(temp);*/
        temp = new AiData("Coffee Bot", true, "ClassicAI",2);
        temp.requiredWin = 3;
        AiDB.add(temp);
        temp = new AiData("Dango Bot", true, "DangoAI",3);
        temp.requiredWin = 5;
        AiDB.add(temp);
        temp = new AiData("Eclair Bot", true, "EclairAI",4);
        temp.requiredWin = 5;
        AiDB.add(temp);
      /*  temp = new AiData("Banana Bot", true, "boldAI",4);
        temp.requiredWin = 7;
        AiDB.add(temp);
        /*
        temp = new AiData("Nagato Bot", false, "ai72_m4.txt",5);
        temp.requiredWin = 9;
        AiDB.add(temp);
        */
    }

    public static void loadWin(Context context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        total_win = sharedPref.getInt("mywin", 0);
    }

    public static void saveWin(Context context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
        sharedEditor.putInt("mywin", total_win);
    }

    public static void toast(Context context, String a) {
        Toast.makeText(context, a, Toast.LENGTH_LONG).show();
    }

    public void onPlaygame(View v) {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        startActivity(intent);
    }

    public void onAIsetting(View v) {
        Intent intent = new Intent(getApplicationContext(), AILearns.class);
        startActivity(intent);
    }
}
