package com.haruhi.bismark439.indianpoker.AI;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haruhi.bismark439.indianpoker.R;

import java.text.DecimalFormat;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.haruhi.bismark439.indianpoker.AI.SaveUtil.saveAI;
import static com.haruhi.bismark439.indianpoker.AI.SaveUtil.saveUserBrain;
import static com.haruhi.bismark439.indianpoker.MainActivity.devmode;
import static com.haruhi.bismark439.indianpoker.MainActivity.players;
import static com.haruhi.bismark439.indianpoker.MainActivity.saveWin;
import static com.haruhi.bismark439.indianpoker.MainActivity.toast;
import static com.haruhi.bismark439.indianpoker.MainActivity.total_win;
import static com.haruhi.bismark439.indianpoker.MainActivity.translateUpAnim;
import static com.haruhi.bismark439.indianpoker.RaiseExtra.REQCODE;
import static com.haruhi.bismark439.indianpoker.RaiseExtra.cRaise;

public class Main extends AppCompatActivity {
    static final int greenColor = Color.parseColor("#8800c800");
    static final int whiteColor = Color.parseColor("#88ffffff");
    static final int TURNTIME = 1000;
    public static final int BASE_CHIP = 24;
    public static boolean user = false;
    static Deck gameDeck = new Deck();
    static int fAction = -1;
    static int aiAction = -1;
    static int STATE = 0;
    static int p1win = 0;
    static int p0win = 0;
    static int FIRST = 0;
    static int SECOND = 1;
    public static boolean flag_turn1 = false;
    static boolean flag_draw = false;
    LinearLayout actionLin;
    TextView mybet;
    TextView aibet;
    TextView mychip;
    TextView aichip;
    TextView total;
    TextView mycard;
    TextView aicard;
    TextView myaction;
    TextView aiaction;
    TextView result;
    TextView phaseInst;
    TextView remainDeck;
    TextView brainExp;
    ProgressBar brainPrg;
    SeekBar raiseBar;
    static TextView aiLog;

    boolean[] madeMove = new boolean[2];
    boolean stat = true;
    boolean changeOrder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_main);
        actionLin = findViewById(R.id.action_container);
        mybet = findViewById(R.id.mybet);
        aibet = findViewById(R.id.aibet);
        aichip = findViewById(R.id.aichip);
        mychip = findViewById(R.id.mychip);
        total = findViewById(R.id.totalstake);
        mycard = findViewById(R.id.mycard);
        aicard = findViewById(R.id.aicard);
        myaction = findViewById(R.id.myaction);
        aiaction = findViewById(R.id.aiaction);
        result = findViewById(R.id.result);
        phaseInst = findViewById(R.id.phaseInstruction);
        remainDeck = findViewById(R.id.remainingCard);
        brainExp = findViewById(R.id.brainExplored);
        brainPrg = findViewById(R.id.progressBar);
        aiLog = findViewById(R.id.ai_log);
        raiseBar = findViewById(R.id.raiseBar2);
        raiseBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Button bt = actionLin.findViewWithTag(2 + "");
                bt.setText(getString(R.string.raise) + i);//+Max_Raise);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        players[0].id = 0;
        players[1].id = 1;
        initAndReset();
        updateBoard();
        doTurn();
    }

    public void doTurn() {
        // players[1].readAImind();
        int prevState = STATE;
        if(players[0].bet>58)players[0].bet=58;
        if(players[1].bet>58)players[1].bet=58;
        System.out.println("Curr STATE " + STATE);
        switch (STATE) {
            case 0: //Init state
                actionLin.setVisibility(INVISIBLE);
                result.setText(" ");
                madeMove[0] = false;
                madeMove[1] = false;
                System.out.println("Each player bet 1 chip. Dispense Card");
                hideMyCard();
                showCardandCheck();
                flag_turn1 = true;
                updateBoard();
                STATE++;
                break;
            case 1: //first player
                System.out.println("Player " + FIRST + "'s turn.");
                if (FIRST == 1) { //1 is ala
                    System.out.println("Which is AI.");
                    if (flag_turn1) {//but it is first turn
                        aiAction = players[1].doAction();
                        System.out.println(" AI opening action " + aiAction);
                        updateBoard();
                        STATE++;
                    } else {
                        aiAction = players[1].doAction();
                        System.out.println(" AI Respond with " + aiAction);
                        updateBoard();
                        switch (aiAction) {
                            case 0:
                                STATE = 3; //FOLD = GO TO MATCH
                                break;
                            case 1://CALLED = GO TO MATCH
                                STATE = 3;
                                break;
                            case 2://AI raised
                                System.out.println("AI RAISE.");
                                STATE++;
                                break;
                        }
                    }
                    madeMove[1] = true;
                    setAction(1);
                } else {
                    actionLin.setVisibility(VISIBLE);
                    ((Button) actionLin.findViewWithTag(2 + "")).setText(getString(R.string.raise) + raiseBar.getProgress());
                    System.out.println("Which is Player. Waiting ...");
//Wait Player's input
                }
                break;
            case 2: //second player
                System.out.println("Player " + SECOND + "'s turn.");
                flag_turn1 = false;
                if (SECOND == 1) {//Make AI respond to player
                    System.out.println("Which is AI.");
                    aiAction = players[1].doAction();
                    updateBoard();
                    switch (aiAction) {
                        case 0:
                            STATE++; //FOLD = GO TO MATCH
                            break;
                        case 1://CALLED = GO TO MATCH
                            STATE++;
                            break;
                        case 2://AI raised
                            madeMove[0] = false;
                            STATE--;
                            break;
                    }
                    madeMove[1] = true;
                    setAction(1);
                } else {//Player is responding to AI
                    actionLin.setVisibility(VISIBLE);
                    ((Button) actionLin.findViewWithTag(2 + "")).setText(getString(R.string.raise) + raiseBar.getProgress());
                    System.out.println("WHICH IS PLAYER.");
                }
                break;
            case 3: //do match
                actionLin.setVisibility(INVISIBLE);
                System.out.println("Match Phase.");
                showMyCard();
                changeOrder = doMatchUser();
                updateBoard();
                if (players[1].chips <= 0 || players[0].chips <= 0) {
                    stat = false;
                    if (players[1].chips <= 0) {
                        p1win++;
                    } else {
                        p0win++;
                    }
                }
                break;
        }
        if (prevState == 3 && STATE == 3) {
            actionLin.setVisibility(INVISIBLE);
            new CountDownTimer(TURNTIME, 1000) {//1000
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    if ((players[0].chips > 0 && players[1].chips > 0) || flag_draw) {
                        STATE = 0;
                        updateBoard();
                        doTurn();
                    } else {
                        if (players[0].chips > 0) {
                            total_win++;
                            saveWin(getApplicationContext());
                            toast(getApplicationContext(), "You won!");
                        } else {
                            toast(getApplicationContext(), "You lost...");
                        }
                        saveUserBrain();
                        saveAI(players[1]);
                        finish();
                    }
                }
            }.start();
        } else if (prevState != STATE) {
            new CountDownTimer(TURNTIME, 1000) {//1000
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    updateBoard();
                    doTurn();
                }
            }.start();
        }
    }

    public void initAndReset() {
        players[0].memory = 100;
        players[0].cautiousness = 5;//User
        players[1].memory = 100;
        players[1].cautiousness = 5;

        gameDeck.init();
        players[1].resetState();
        players[0].resetState();
        fAction = -1;
        aiAction = -1;
    }

    public void showCardandCheck() {
        players[0].setCard(gameDeck.pop());
        players[1].setCard(gameDeck.pop());
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
            //System.out.println("Change order called. First is "+FIRST);
        }

    }

    public void onMyAction(View v) {
        int action = Integer.parseInt((String) v.getTag());
        fAction = action;
        players[0].invokeAction(fAction);
        if (STATE == 1) {//PLAYER IS FIRST P
            if (flag_turn1) {//Opening
                switch (fAction) {
                    case 0:
                        STATE++;
                        break;
                    case 1:
                        STATE++;
                        break;
                    case 2:
                        STATE++;
                        break;
                }
            } else {//AI mofo Raised
                switch (fAction) {
                    case 0:
                        STATE = 3;
                        break;
                    case 1:
                        STATE = 3;
                        break;
                    case 2:
                        STATE++;
                        break;
                }
            }
        } else { //PLAYER IS SECOND P
            switch (fAction) {
                case 0:
                    STATE++;
                    break;
                case 1:
                    STATE++;
                    break;
                case 2:
                    madeMove[1] = false;
                    STATE--;
                    break;
            }
        }
        updateScoreOnly();
        madeMove[0] = true;
        setAction(0);
        actionLin.setVisibility(INVISIBLE);
        new CountDownTimer(TURNTIME, 1000) {//1000
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                System.out.println("Countdown finished");
                updateBoard();
                doTurn();
            }
        }.start();
    }

    public void updateBoard() {
        int toplam = players[0].bet + players[1].bet;
        total.setText(getString(R.string.totalstake) + toplam);
        aibet.setText(players[1].bet + "");
        aichip.setText(players[1].chips + "");
        aicard.setText(players[1].currentCard + "");
        mybet.setText(players[0].bet + "");
        mychip.setText(players[0].chips + "");
        players[1].readAImind();
        int rCard = gameDeck.deck.size();
        if (rCard == 20) {
            rCard = 0;
        }
        remainDeck.setText(getString(R.string.remainingcard) + rCard);
        //Brain Panel
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        double a = players[1].printBrainMap();
        String explored = df.format(a);
        brainPrg.setProgress((int) a);
        switch (players[1].AI_id) {
            case 0:
                brainExp.setText("User bot: " + explored + getString(R.string.neurons));
                break;
            case 1:
                brainExp.setText("Math AI: " + players[1].foldThreshold + " ~ " + players[1].raiseThreshold);
                break;
            case 2:
                brainExp.setText("HIgh Dimension: " + explored + getString(R.string.neurons));
                break;
            case 3:
                String nagisa = String.format("%.2f", (players[1].Single_brain[0] + players[1].Single_brain[10] + players[1].Single_brain[1]));
                brainExp.setText("Dango bot: " + nagisa);
                brainPrg.setVisibility(GONE);
                break;
            case 4:
                String huko = String.format("%.2f", (players[1].Single_brain[0] + players[1].Single_brain[10] + players[1].Single_brain[1]));
                brainExp.setText("Eclair bot: " + huko);
                brainPrg.setVisibility(GONE);
                break;
        }
        //Action Panel
        if (!madeMove[1]) {
            aiaction.setBackgroundColor(whiteColor);
            aiaction.setText(" ");
        }
        if (!madeMove[0]) {
            flipVisibility(true);
        }
        //Button panel
        boolean[] possible = players[0].possibleMoves();
        for (int i = 0; i < 3; i++) {
            Button bt = actionLin.findViewWithTag(i + "");
            if (possible[i]) {
                bt.setVisibility(VISIBLE);
            } else {
                bt.setVisibility(INVISIBLE);
            }
            if (i == 2) {
                bt.setText(getString(R.string.raise) + cRaise);//+Max_Raise);
            }
        }


        //State panel
        switch (STATE) {
            case 0:
                phaseInst.setText(R.string.newround);
                break;
            case 1:
                if (FIRST == 0) {
                    phaseInst.setText(R.string.yourturn);
                } else {
                    phaseInst.setText(R.string.aithnks);
                }
                break;
            case 2:
                if (FIRST == 1) {
                    phaseInst.setText(R.string.yourturn);
                } else {
                    phaseInst.setText(R.string.aithnks);
                }
                break;
            case 3:
                aiaction.setText(R.string.cardopen);
                break;
        }


    }

    public void updateScoreOnly() {
        aibet.setText(players[1].bet + "");
        aichip.setText(players[1].chips + "");
        aicard.setText(players[1].currentCard + "");
        mybet.setText(players[0].bet + "");
        mychip.setText(players[0].chips + "");
    }

    public void setAction(int id) {
        if (id == 1) {
            if (madeMove[1]) {
                switch (aiAction) {
                    case 0:
                        aiaction.setText(R.string.fold);
                        break;
                    case 1:
                        aiaction.setText(R.string.call);
                        break;
                    case 2:
                        aiaction.setText(getString(R.string.raise) + cRaise);//+Max_Raise);
                        System.out.println(aiAction + "AI 레이즈" + cRaise);//(Max_Raise-1));
                        break;
                }
                aiaction.startAnimation(translateUpAnim);
                aiaction.setBackgroundColor(greenColor);
            } else {
                aiaction.setBackgroundColor(whiteColor);
                aiaction.setText(" ");
            }
            flipVisibility(true);
        } else {
            if (madeMove[0]) {
                switch (fAction) {
                    case 0:
                        myaction.setText(R.string.fold);
                        break;
                    case 1:
                        myaction.setText(R.string.call);
                        break;
                    case 2:
                        myaction.setText(getString(R.string.raise) + cRaise);//+Max_Raise);
                        break;
                }
                System.out.println("Set action called");
                flipVisibility(false);
                myaction.startAnimation(translateUpAnim);
                myaction.setBackgroundColor(greenColor);
            } else {
                flipVisibility(true);
            }
        }


    }

    public void showMyCard() {
        mycard.setVisibility(VISIBLE);
        mycard.setText(players[0].currentCard + "");
    }

    public void hideMyCard() {
        mycard.setVisibility(View.INVISIBLE);
        mycard.setText("");
    }

    public void flipVisibility(boolean rVisibile) {
        System.out.println("Flip visibility called " + rVisibile);
        if (rVisibile) {//Raise visible
            int maxRaise = players[0].chips;
            if (players[1].chips < players[0].chips) {
                maxRaise = players[1].chips;
            }
            raiseBar.setMax(maxRaise);
            if (maxRaise > 2) {
                raiseBar.setProgress(3);
            } else {
                raiseBar.setProgress(2);
            }
            ((Button) actionLin.findViewWithTag(2 + "")).setText(getString(R.string.raise) + raiseBar.getProgress());
            System.out.println("Make it visible can I raise?" + players[0].possibleMoves()[2]);
            myaction.setVisibility(GONE);
            if (players[0].possibleMoves()[2]) raiseBar.setVisibility(VISIBLE);
        } else {
            myaction.setVisibility(VISIBLE);
            raiseBar.setVisibility(GONE);
        }

    }

    public boolean doMatchUser() {
        boolean pWin = false;
        int change = 0;
        System.out.println("Do match: AI did " + aiAction + " vs " + fAction + " fAction");
        if (fAction == 0) {
            pWin = false;
            flag_draw = false;
            if (players[0].currentCard == 10) {
                players[0].changeChip(-10);
                players[1].changeChip(10);
                change = change + 10;
                System.out.println(getString(R.string.fold10penalty));
            }
            //AI win. player fold
        } else if (aiAction == 0) {
            pWin = true;
            flag_draw = false;
            if (players[1].currentCard == 10) {
                players[0].changeChip(10);
                players[1].changeChip(-10);
                change = change + 10;
                System.out.println(getString(R.string.bluff10bonus));
            }
            //AI win. player fold
        } else if (players[1].currentCard == players[0].currentCard) {
            flag_draw = true;
        } else {
            flag_draw = false;
            pWin = (players[0].currentCard > players[1].currentCard);
        }
        System.out.println(players[0].bet + "- 플레이어 " + players[0].currentCard + " VS " + players[1].currentCard + " AI-" + players[1].bet);

        players[0].showOpponentCard(players[1].currentCard);
        players[1].showOpponentCard(players[0].currentCard);
        if (flag_draw) {
            result.setText(R.string.draw);
            return true;
        } else if (pWin) {
            result.setText(R.string.playerwin);
            //      pLog(getString(R.string.playerearn)+(players[0].bet+players[1].bet));
            players[0].changeChip(players[0].bet + players[1].bet);
            //      players[0].evaluateAction(players[1].bet+change);
            players[1].evaluateAction(-players[1].bet - change);
            resetStage();
            return true;
        } else {
            result.setText(R.string.playerlose);
            // pLog(getString(R.string.playerloss)+(players[0].bet));
            players[1].changeChip((players[0].bet + players[1].bet));
            //   players[0].evaluateAction(-players[1].bet-change);
            players[1].evaluateAction(players[1].bet + change);
            resetStage();
            return false;
        }
    }

    public void onActionRaise(View v) {
        cRaise = raiseBar.getProgress();
        if (cRaise < 1) {
            toast(getApplicationContext(), "Cannot raise by " + cRaise);
            cRaise = 1;
            raiseBar.setProgress(1);
        }
        onMyAction(v);
//      Intent intent = new Intent(getApplicationContext(), RaiseExtra.class);
        //       startActivityForResult(intent,REQCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQCODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                View v = new View(this);
                v.setTag(2);
                onMyAction(v);
                // Do something with the contact here (bigger example below)
            }
        }
    }

    void resetStage() {
        if (players[0].bet > 0 && players[1].bet > 0) {
            players[FIRST].bet = 0;
            players[SECOND].bet = 0;
        }
    }

    public static Player getOther(int d) {
        if (players[0].id == d) {
            return players[1];
        } else {
            return players[0];
        }
    }

    public static void pLog(String a) {
        if (devmode) {
            if (aiLog != null) aiLog.setText(a + "\n");
        }
    }

    public void onLogClick(View v) {
        if (devmode) {
            TextView tv = findViewById(R.id.log_alt);
            TextView theview = findViewById(R.id.ai_log);
            if (theview.getVisibility() == VISIBLE) {
                theview.setVisibility(INVISIBLE);
                tv.setVisibility(VISIBLE);
            } else {
                theview.setVisibility(VISIBLE);
                tv.setVisibility(INVISIBLE);
            }
        }
    }
}
