package com.haruhi.bismark439.indianpoker.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import static com.haruhi.bismark439.indianpoker.AI.Main.BASE_CHIP;
import static com.haruhi.bismark439.indianpoker.AI.Main.FIRST;
import static com.haruhi.bismark439.indianpoker.AI.Main.flag_draw;
import static com.haruhi.bismark439.indianpoker.AI.Main.flag_turn1;
import static com.haruhi.bismark439.indianpoker.AI.Main.getOther;
import static com.haruhi.bismark439.indianpoker.AI.Main.user;
import static com.haruhi.bismark439.indianpoker.AI.userBrain.userBrain;
import static com.haruhi.bismark439.indianpoker.MainActivity.players;
import static com.haruhi.bismark439.indianpoker.RaiseExtra.cRaise;


public abstract class Player {
    public int[][][] brain = new int[21][59][3];//Confidence /BetMoney / myCallRaiseFold
    public boolean[][][] brainMap = new boolean[21][59][3];
    public double[] Single_brain = new double[21];

    public final double BASE_FOLD = 15; //30 def
    public final double BASE_RAISE = 60; //18+15 def
    public final int adjWeight = 1000;
    public int cautiousness = 5;
    public int memory = 100; // 1~100%
    public int chips = BASE_CHIP;
    public int bet = 0;
    public int id = 0;
    public int currentCard;
    public int enemyCard;
    public double confidence;
    public double foldThreshold = BASE_FOLD;
    public double raiseThreshold = BASE_RAISE;
    public Stack<int[]> recordedAction = new Stack(); //Int[] length 3 / 0: my Condifence / 1: money /2: myaction
    public ArrayList<Integer> knownCards = new ArrayList<Integer>();
    public String name;
    public int AI_id = 0;
    public boolean learning = true;

    //Actions
    public int doAction() {
        int myaction = getAction();
        if (myaction == 2) {
            raise();
        } else if (myaction == 1) {
            call();
        } else {
            fold();
        }
        recordMyAction(myaction);
        return myaction;
    }

    public void invokeAction(int action) { //Player did action. Input into system
        if (action == 2) {
            raise();
        } else if (action == 1) {
            call();
        } else {
            fold();
            //Fold
        }
        recordMyAction(action);
    }


    //AI Methods
    public Player() {
        setBrainSize();
    }

    //ABSTRACT METHODS
    public abstract void setBrainSize();

    public abstract int getAction();

    public abstract void evaluateAction(int value);

    public abstract void printBrainStruct();

    //Brain organisation
    public void reAdjustMap(int c, int b) {
        int total = 0;
        if (b < 0) b = 0;
        for (int i = 0; i < 3; i++) {
            total = total + Math.abs(brain[c][b][i]);
        }
        if (total > adjWeight) {
            for (int i = 0; i < 3; i++) {
                brain[c][b][i] = brain[c][b][i] / 2;
            }
        }
//You dont re-adjust
    }


    //Print functions
    public int printBrainMap() {
        double total = 20 * 32 * 3;
        double opened = 0;
        for (int conf = 0; conf < 21; conf++) {
            for (int bet = 0; bet < 59; bet++) {
                boolean allopen = true;
                for (int a = 0; a < 3; a++) {
                    if (this.brainMap[conf][bet][a]) {
                        opened++;
                    } else {
                        allopen = false;
                    }
                }
                if (allopen) {
                    //     System.out.print("1");
                } else {
                    //   System.out.print("0");
                }
                if (bet == 58) {
                    //System.out.println();
                } else {
                    //     System.out.print(",");
                }
            }
        }
        double ratio = opened / total * 100;
        return (int) ratio;
    }

    public void drawBrainMap() {
        for (int c = 0; c < 21; c++) {
            for (int b = 0; b < 59; b++) {
                for (int a = 0; a < 3; a++) {
                    if (this.brain[c][b][a] == 0) {
                        this.brainMap[c][b][a] = false;
                    } else {
                        this.brainMap[c][b][a] = true;
                    }
                }
            }
        }
    }

    public abstract String readAImind();

    //SHARED HEURISTIC
    public int getHeuristicAction() {
        int myaction = 1;
        boolean moves[] = possibleMoves();
        if (confidence >= raiseThreshold && moves[2]) {//You can only raise to call when call was first turn
            myaction = 2;
        } else if (confidence <= foldThreshold && moves[0]) {
            myaction = 0;
        } else {
            myaction = 1;
        }
        //call -> raise or call
        if (myaction == 2) {
            double rand = (int) (Math.random() * 10) + (2.4 * this.cautiousness);//Add Risk Factor here
            raiseThreshold = raiseThreshold + rand;//risk_EnemyConfidence*(1-(1/this.Stability));
            foldThreshold = foldThreshold + rand;//risk_EnemyConfidence;
            cRaise =getHeuristicRaise();
        }
        return myaction;
    }
    public int getHeuristicRaise(){
        int raise = 3;
        double chipto = Math.min(this.chips, getOther(this.id).chips);
        if(chipto<1)chipto = 1;
        if(chipto>10)chipto= chipto * 0.8;
        chipto = chipto * (Math.random());
        raise =(int) chipto;
        while(raise>this.chips||raise>getOther(this.id).chips){
            raise--;
        }
        return raise;
    }
    public int getRandomAction() {
        boolean[] m = possibleMoves();
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = 0; i < m.length; i++) {
            if (m[i]) al.add(i);
        }
        Collections.shuffle(al);
        int myaction = al.get(0);
        if(myaction==2){
            cRaise=getHeuristicRaise();
        }
        return al.get(0);
    }

    //COMMON METHODS
    public void resetState() {
        this.chips = BASE_CHIP;
        this.bet = 0;
        this.foldThreshold = BASE_FOLD;
        this.raiseThreshold = BASE_RAISE;
        initCardCount();
    }

    public boolean[] possibleMoves() {
        boolean[] moves = new boolean[3];
        //When can't you fold?
        if (flag_turn1 && FIRST == this.id) {//dont fold on first turn
            moves[0] = false;
        } else if (this.bet == getOther(this.id).bet) {//means same thing but in case of bug..
            moves[0] = false;
        } else {
            moves[0] = true;
        }

        //When can't you raise?
        int minimumPossible = Math.min(this.chips, getOther(this.id).chips);
        if (minimumPossible <= 0) {//cannot raise if remaining chip is <=0
            moves[2] = false;
            //   System.out.println("raise false");
        } else {
            moves[2] = true;
            //  System.out.println("raise true");
        }

        moves[1] = true;
        if (this.chips <= 1) {
            moves[2] = false;
        }

        //Draw case is just different
        if (flag_draw && (players[0].chips < 1 || players[1].chips < 1)) {
            moves[0] = false;
            moves[1] = true;
            moves[2] = false;
        }

        return moves;
    }

    public void initCardCount() {
        knownCards = new ArrayList<Integer>();
        for (int i = 0; i < 2; i++) {
            for (int n = 1; n < 11; n++) {//2 sets of 1~10
                knownCards.add(n);
            }
        }
    }

    public void setCard(int c) {
        this.currentCard = c;
    }

    public void showOpponentCard(int c) {
        this.enemyCard = c;
        int rand = (int) (Math.random() * 100);
        if (rand < this.memory) {
            count(c);
        }
        calculateConf();
        readAImind();
    }

    public void changeChip(int i) {
        this.chips = this.chips + i;
    }

    public void count(int c) {
        for (int i = 0; i < knownCards.size(); i++) {
            if (knownCards.get(i) == c) {
                knownCards.remove(i);
                i = 999;
            }
        }
    }

    public void calculateConf() {
        double total = knownCards.size();
        double wincase = 0;
        for (int i = 0; i < total; i++) {
            if (knownCards.get(i) >= enemyCard) {//Can Win+draw
                wincase++;
            }
        }
        this.confidence = wincase / total * 100;
        foldThreshold = BASE_FOLD + this.cautiousness * 3;
        raiseThreshold = BASE_RAISE + this.cautiousness * 3;
    }

    public void raise() {
        int myRaise = cRaise;
        int offset = Math.abs(getOther(this.id).bet - this.bet);
        offset = offset + myRaise;
        changeChip(-offset);
        this.bet = this.bet + offset;
    }

    public void call() {
        int offset =this.bet - getOther(this.id).bet;
        if (offset!=0) {//it means you called to Raise
            if (user) System.out.println("Called to Raise");
            offset= Math.abs(offset);
            this.bet = this.bet + offset;
            changeChip(-offset);
        }
    }

    public void fold() {
        if (user && this.id == 1) System.out.println("AI Fold.");
    }


    //AI Methods
    public void recordMyAction(int myaction) {
        int[] record = {(int) confidence / 5, getOther(this.id).bet - 1, myaction};
        this.recordedAction.push(record);
    }


    static int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }

    }

    public void evaluateForUser(int value, int[] temp) {
        System.out.println("Evaluate For User");
        boolean lost = (this.currentCard < this.enemyCard);
        boolean matchLost = (value < 0);
        if (matchLost) {//Lost match = Value = -10
            int fold = 0;
            if (lost) {// You actually fucking lost
                fold = (-value) * 2;
                userBrain[temp[0]][temp[1]][0] = userBrain[temp[0]][temp[1]][0] + fold;
                //Call
                userBrain[temp[0]][temp[1]][1] = userBrain[temp[0]][temp[1]][1] + value;
                //Raise
                userBrain[temp[0]][temp[1]][2] = userBrain[temp[0]][temp[1]][2] + value;
            } else {//You made a retarded fold
                //Fold penalty
                fold = value * 2;
                if (this.currentCard == 10) {
                    fold = fold - 20;
                }
                userBrain[temp[0]][temp[1]][0] = userBrain[temp[0]][temp[1]][0] + fold;
                //Call
                userBrain[temp[0]][temp[1]][1] = userBrain[temp[0]][temp[1]][1] - value;
                //Raise
                userBrain[temp[0]][temp[1]][2] = userBrain[temp[0]][temp[1]][2] - (value * 2);
            }
        } else {//Won match value = 8
            int fold = 0;
            if (lost) { //You won by bluff (Raise)
                userBrain[temp[0]][temp[1]][temp[2]] = userBrain[temp[0]][temp[1]][2] + (value * 3);
            } else {//you were gonna win anyway.
                fold = (-value) * 2;
                if (this.currentCard == 10) {
                    fold = fold - 20;
                }
                userBrain[temp[0]][temp[1]][0] = userBrain[temp[0]][temp[1]][0] + fold;
                //Raise *2
                userBrain[temp[0]][temp[1]][temp[2]] = userBrain[temp[0]][temp[1]][2] + (value * 3);

                //Call
                userBrain[temp[0]][temp[1]][1] = userBrain[temp[0]][temp[1]][1] + value;
            }
        }
    }

}
