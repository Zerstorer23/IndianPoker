package com.haruhi.bismark439.indianpoker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.haruhi.bismark439.indianpoker.AI.Deck;
import com.haruhi.bismark439.indianpoker.AIs.DangoAI;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import static com.haruhi.bismark439.indianpoker.AI.SaveUtil.loadAI;
import static com.haruhi.bismark439.indianpoker.MainActivity.players;
import static com.haruhi.bismark439.indianpoker.MainActivity.total_win;

public class AILearns extends AppCompatActivity {
    /* Player AIplayer = new Player(1);
     Player pPlayer = new Player(0);
    Player[] players = {pPlayer,AIplayer};*/
    static Deck gameDeck = new Deck();
    int FIRST = 0;
    int SECOND = 1;
    int fAction;
    int aiAction;
    int p1win = 0;
    int p0win = 0;
    boolean mute = true;
    final int BASE_RAISE_UNIT = 3;
    int Max_Raise = BASE_RAISE_UNIT;
    boolean skipTo = false;
    static boolean infinite = false;
    static TextView tvvv;
    public static TextView bname;
    static GraphView graph;
    static TextView totWin;
    public static int selectedAI_id;

    static ListView listViewAi;
    static AdapterAI AdapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ailearns);
        //  tv= findViewById(R.id.learndone);
        bname = findViewById(R.id.brainname);
        totWin = findViewById(R.id.totalwin);
        listViewAi = (ListView) findViewById(R.id.ai_listview);
        AdapterList = new AdapterAI(getApplicationContext());
        listViewAi.setAdapter(AdapterList);
        AdapterList.notifyDataSetChanged();
        graph = (GraphView) findViewById(R.id.brainMapGraph);
        setTotalWin();
    }

    public void runInfinite() {
        int i = 0;
        while (infinite) {
            gameDeck.init();
            players[1].resetState();
            players[0].resetState();
            AImode();
            i++;
            final int x = i;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvvv.setText(x + " iteration!");
                }
            });

            // save(players[1],1);
        }
    }

    public static void onLoad(Context context) {
        int id = selectedAI_id;
        loadAI(context);
        switch (id) {
            case 0:
                drawBrainInfo();
                break;
            case 1: //Math
                graph.removeAllSeries();
                break;
            case 2:
                drawBrainInfo();
                break;
            case 3: //Dango
                drawDangoBrainInfo();
                break;
            case 4: //Bold
                drawSingleBrainInfo();
                break;
        }
    }

    public void setTotalWin() {
        totWin.setText("My total win: " + total_win);
    }

    public void onLearning(View v) {
        new Thread() {
            public void run() {
                for (int x = 0; x < 10; x++) {
                    for (int i = 0; i < 10; i++) {
                        gameDeck.init();
                        players[1].resetState();
                        players[0].resetState();
                        AImode();
                        System.out.println("Iterated " + i);
                    }
                    final int y = x;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvvv.setText(y + "x 10 iteration!");
                        }
                    });
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvvv.setText("Finished!");
                    }
                });

            }
        }.start();

    }

    public static boolean isOpen(int x, int y) {
        boolean open = true;
        for (int i = 0; i < 3; i++) {
            if (!players[1].brainMap[y][x][i]) open = false;
        }
        return open;
    }

    public static int howActive(int x, int y) {
        int sum = 0;
        int profit = -10;
        for (int i = 0; i < 3; i++) {
            int val = players[1].brain[y][x][i];
            if (val > profit) profit = val;
            if (val < 0) val = -val;
            sum = sum + val;
        }
        int active;
        if (sum < 10) {
            active = 1;
        } else if (sum < 1000) {
            active = 2;
        } else {
            active = 3;
        }
        return active;
    }
    public static int whichAction(int x,int y){
        int maxIndex = 0;
        for (int i = 1; i < 3; i++) {
            int val = players[1].brain[y][x][i];
            if (val > players[1].brain[y][x][maxIndex]) maxIndex = i;
        }
        return maxIndex;
    }
    public static void drawBrainInfo() {
        //  players[1].drawBrainMap();
        graph.removeAllSeries();
        PointsGraphSeries<DataPoint> degree1 = new PointsGraphSeries<>();
        PointsGraphSeries<DataPoint> degree2 = new PointsGraphSeries<>();
        PointsGraphSeries<DataPoint> degree3 = new PointsGraphSeries<>();
        PointsGraphSeries<DataPoint> degree4 = new PointsGraphSeries<>();
        PointsGraphSeries<DataPoint> degree5 = new PointsGraphSeries<>();
        PointsGraphSeries<DataPoint> degree6 = new PointsGraphSeries<>();
        int max = 59 * 21;
        for (int x = 0; x < 59; x++) {
            for (int y = 0; y < 21; y++) {
                if (isOpen(x, y)) {
                    DataPoint temp = new DataPoint(x, y);
                    int degree = howActive(x, y);
                    int profit = whichAction(x,y);
                    boolean high = (profit!=0);
                    switch (degree) {
                        case 1:
                            if (high) {
                                degree1.appendData(temp, true, max);
                            } else {
                                degree4.appendData(temp, true, max);
                            }
                            break;
                        case 2:
                            if (high) {
                                degree2.appendData(temp, true, max);
                            } else {
                                degree5.appendData(temp, true, max);
                            }
                            break;
                        case 3:
                            if (high) {
                                degree3.appendData(temp, true, max);
                            } else {
                                degree6.appendData(temp, true, max);
                            }
                            break;
                    }
                }
            }//debug

        }
        degree1.setSize(degree1.getSize() / 4);
        degree2.setSize(degree2.getSize() / 3);
        degree3.setSize(degree3.getSize() / 2);
        degree4.setSize(degree4.getSize() / 4);
        degree5.setSize(degree5.getSize() / 3);
        degree6.setSize(degree6.getSize() / 2);


        degree1.setShape(PointsGraphSeries.Shape.RECTANGLE);
        degree2.setShape(PointsGraphSeries.Shape.RECTANGLE);
        degree3.setShape(PointsGraphSeries.Shape.RECTANGLE);
        degree4.setShape(PointsGraphSeries.Shape.RECTANGLE);
        degree5.setShape(PointsGraphSeries.Shape.RECTANGLE);
        degree6.setShape(PointsGraphSeries.Shape.RECTANGLE);


        degree1.setColor(Color.parseColor("#4400c800"));
        degree2.setColor(Color.parseColor("#8800c800"));
        degree3.setColor(Color.parseColor("#00FF00"));
        degree4.setColor(Color.parseColor("#44c80000"));
        degree5.setColor(Color.parseColor("#88c80000"));
        degree6.setColor(Color.parseColor("#FF0000"));

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(35);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(21);
        // enable scaling and scrolling
        //   graph.getViewport().setScrollable(true); // enables horizontal scrolling
        //    graph.getViewport().setScrollableY(true); // enables vertical scrolling
        //     graph.getViewport().setScalable(true);
        //    graph.getViewport().setScalableY(true);
        graph.addSeries(degree1);
        graph.addSeries(degree2);
        graph.addSeries(degree3);
        graph.addSeries(degree4);
        graph.addSeries(degree5);
        graph.addSeries(degree6);
        graph.setBackgroundColor(Color.parseColor("#000000"));
        //  graph.getGridLabelRenderer().setHorizontalAxisTitle("Matches played");
        //   graph.getGridLabelRenderer().setVerticalAxisTitle("Number of vehicles");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.parseColor("#00c800"));
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.parseColor("#00c800"));
    }

    public static void drawSingleBrainInfo() {
        //  players[1].drawBrainMap();
        graph.removeAllSeries();
        DataPoint[] nagisa = new DataPoint[30];
        for (int x = 0; x < nagisa.length; x++) {
            DataPoint temp = new DataPoint(x, players[1].Single_brain[x]);
            nagisa[x] = temp;
        }
        LineGraphSeries<DataPoint> degree1 = new LineGraphSeries<>(nagisa);

        degree1.setColor(Color.parseColor("#00C800"));
// styling series
        degree1.setTitle("Card Guessing");
        degree1.setDrawDataPoints(false);
        degree1.setDataPointsRadius(8);
        degree1.setThickness(8);

// custom paint to make a dotted line
     /*   Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
        degree1.setCustomPaint(paint);*/
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(30); //COnfidence
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(11); //Card
        // enable scaling and scrolling
        //   graph.getViewport().setScrollable(true); // enables horizontal scrolling
        //    graph.getViewport().setScrollableY(true); // enables vertical scrolling
        //     graph.getViewport().setScalable(true);
        //    graph.getViewport().setScalableY(true);
        graph.addSeries(degree1);
        graph.setBackgroundColor(Color.parseColor("#FFFFFF"));
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Boldness");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Expected Card");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.parseColor("#00c800"));
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.parseColor("#00c800"));
    }
    public static void drawDangoBrainInfo() {
        //  players[1].drawBrainMap();
        graph.removeAllSeries();
        DataPoint[] nagisa = new DataPoint[30];
        DangoAI player = (DangoAI)players[1];
        for (int x = 0; x < nagisa.length; x++) {
            DataPoint temp = new DataPoint(x,player.Single_brain[x]);
            nagisa[x] = temp;
        }
        LineGraphSeries<DataPoint> degree1 = new LineGraphSeries<>(nagisa);

        degree1.setColor(Color.parseColor("#00C800"));
// styling series
        degree1.setTitle("Card Guessing");
        degree1.setDrawDataPoints(false);
        degree1.setDataPointsRadius(8);
        degree1.setThickness(8);

        //BLUFF
        DataPoint[] tomoyo = new DataPoint[21];
        for (int x = 0; x < tomoyo.length; x++) {
            double modx= (double)x/21 * 30;
            double mody = 0;
            for(int i=1;i<player.bluff[x].length;i++){
                if(player.bluff[x][i]>player.bluff[x][(int)mody])mody=i;
            }
            mody = mody / 30 * 10;

            DataPoint temp = new DataPoint(modx, mody);
            tomoyo[x] = temp;
        }
        LineGraphSeries<DataPoint> degree2 = new LineGraphSeries<>(tomoyo);
        degree2.setColor(Color.parseColor("#0088C8"));
// styling series
        degree2.setTitle("Bluffing");
        degree2.setDrawDataPoints(false);
        degree2.setDataPointsRadius(4);
        degree2.setThickness(4);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(30); //COnfidence
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(11); //Card

        graph.addSeries(degree1);
        graph.addSeries(degree2);
        graph.setBackgroundColor(Color.parseColor("#FFFFFF"));
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Boldness");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Expected Card");
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.parseColor("#00c800"));
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.parseColor("#00c800"));
    }
    public void AImode() {
        boolean stat = true;
        boolean changeOrder = true;
        while (stat) {
            players[1].setCard(gameDeck.pop());
            players[0].setCard(gameDeck.pop());
            players[1].showOpponentCard(players[0].currentCard);
            players[0].showOpponentCard(players[1].currentCard);
            players[0].bet++;
            players[1].bet++;
            players[0].chips--;
            players[1].chips--;
            if (changeOrder) {
                int temp = FIRST;
                FIRST = SECOND;
                SECOND = temp;
            }
            fAction = players[FIRST].doAction();
            aiAction = players[SECOND].doAction();
            do {
                switch (aiAction) {
                    case 0:
                        skipTo = true;
                        //fold. do nothing
                        break;
                    case 1:
                        skipTo = true;
                        //called. do nothing
                        break;
                    case 2:
                        fAction = players[FIRST].doAction();
                        if (fAction == 2) {
                            aiAction = players[SECOND].doAction();
                        } else {
                            skipTo = true;
                        }
                        //raise -> fold or call or raise
                        break;
                }
            } while (aiAction == 2 && !skipTo);

            changeOrder = doMatch();
            if (players[1].chips <= 0 || players[0].chips <= 0) {
                stat = false;
                if (players[1].chips <= 0) {
                    p1win++;
                } else {
                    p0win++;
                }
            }
        }
    }

    public boolean doMatch() {
        boolean firstWin = false;
        boolean draw = false;
        int change = 0;
        if (fAction == 0) {
            firstWin = false;
            if (players[FIRST].currentCard == 10) {
                players[FIRST].changeChip(-10);
                players[SECOND].changeChip(10);
                change = change + 10;
            }
            //AI win. player fold
        } else if (aiAction == 0) {
            firstWin = true;
            if (players[SECOND].currentCard == 10) {
                players[FIRST].changeChip(10);
                players[SECOND].changeChip(-10);
                change = change + 10;
                if (!mute) System.out.println("10 Fold penalty!");
            }
            //AI win. player fold
        } else if (players[SECOND].currentCard == players[FIRST].currentCard) {
            draw = true;
        } else {
            firstWin = (players[FIRST].currentCard > players[SECOND].currentCard);
        }
        if (!mute) {
            System.out.println(players[FIRST].bet + "- Player " + players[FIRST].currentCard + " VS " + players[SECOND].currentCard + " AI-" + players[SECOND].bet);
        }

        players[FIRST].showOpponentCard(players[FIRST].currentCard);
        players[SECOND].showOpponentCard(players[SECOND].currentCard);
        if (draw) {
            Max_Raise = BASE_RAISE_UNIT;
            return true;
        } else if (firstWin) {

            players[FIRST].changeChip(players[FIRST].bet + players[SECOND].bet);
            players[FIRST].evaluateAction(players[SECOND].bet + change);
            players[SECOND].evaluateAction(-players[SECOND].bet - change);
            resetStage();
            return true;
        } else {

            players[SECOND].changeChip((players[FIRST].bet + players[SECOND].bet));
            players[FIRST].evaluateAction(-players[SECOND].bet - change);
            players[SECOND].evaluateAction(players[SECOND].bet + change);
            resetStage();
            return false;
        }
    }

    public void resetStage() {
        players[FIRST].bet = 0;
        players[SECOND].bet = 0;
        Max_Raise = BASE_RAISE_UNIT;
    }
}
