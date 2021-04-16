package com.haruhi.bismark439.indianpoker.AIs;

import com.haruhi.bismark439.indianpoker.AI.Player;

import java.util.Stack;

import static com.haruhi.bismark439.indianpoker.AI.Main.getOther;
import static com.haruhi.bismark439.indianpoker.AI.Main.pLog;
import static com.haruhi.bismark439.indianpoker.AI.Main.user;

public class ClassicAI extends Player {


    @Override
    public void setBrainSize() {
        this.brain = new int[21][59][3];
        this.brainMap = new boolean[21][59][3];
    }

    @Override
    public int getAction() {
        int otherbet = getOther(this.id).bet - 1;
        int conf = (int) this.confidence / 5;
        int myaction = -1;
        int max = 0;
        //Open map check
        boolean[] moves = possibleMoves();
        int[] tempArr = {0, 0, 0};
        boolean newRun = false;
        for (int i = 0; i < 3; i++) {
            //   System.out.println("COnf "+conf+"Otherbet "+otherbet+" i "+i);
            tempArr[i] = brain[conf][otherbet][i];
            if (moves[i] && !brainMap[conf][otherbet][i]) {
                newRun = true;
                break;
            }
        }
        //get action
        if (!newRun) {
            for (int i = 0; i < 3; i++) {
                if (moves[i]) {
                    if (myaction == -1) {//initial state
                        myaction = i;
                        max = tempArr[i];
                    } else {
                        if (tempArr[i] > max) {//max can be -999
                            max = tempArr[i];
                            myaction = i;
                        }
                    }
                }
            }
        } else {
            myaction = getHeuristicAction();
        }
        //Anneal
        double crit = this.confidence / 100;
        if (this.confidence > 10 &&moves[1]) {
            double rand = Math.random();
            //Anneal when enemy card is not 10.
            if (myaction == 2) {
                //I was gonna raise
                // 0.80 confidence, low chance to call
                if (rand > crit) {
                    if (user) System.out.println("I may win, but try annealing CALL"+crit);
                    myaction = 1;
                }
            } else if(myaction == 0){
                //I probably lose
                // 0.20 confidence. Low chance to raise
                if (rand < crit) {
                    if (user) System.out.println("I may lose, but try annealing CALL");
                    myaction = 1;
                }
            }
        }
        return myaction;
    }

    @Override
    public void evaluateAction(int value) {
        if (!learning) {
            return;
        }
        boolean lost = (this.currentCard < this.enemyCard);
        boolean matchLost = (value < 0);
        //evaluate single series
        if (!this.recordedAction.isEmpty()) {
            int[] temp = recordedAction.pop();
            reAdjustMap(temp[0], temp[1]);
            if (matchLost) {//Lost match = Value = -10
                int fold = 0;
                if (lost) {// You actually fucking lost
                    fold = (-value) * 2;
                    brain[temp[0]][temp[1]][0] = brain[temp[0]][temp[1]][0] + fold;
                    //Call
                    brain[temp[0]][temp[1]][1] = brain[temp[0]][temp[1]][1] + value;
                    //Raise
                    brain[temp[0]][temp[1]][2] = brain[temp[0]][temp[1]][2] + value * 2;

                    if (user) System.out.println("Fold " + fold + " / call,raise penalty " + value);
                } else {//You made a retarded fold
                    //Fold penalty
                    fold = value * 2;
                    if (this.currentCard == 10) {
                        fold = fold - 20;
                    }
                    brain[temp[0]][temp[1]][0] = brain[temp[0]][temp[1]][0] + fold;
                    //Call
                    brain[temp[0]][temp[1]][1] = brain[temp[0]][temp[1]][1] - value;
                    //Raise
                    brain[temp[0]][temp[1]][2] = brain[temp[0]][temp[1]][2] - (value * 2);

                    if (user)
                        System.out.println("Bad Decision: Fold " + fold + " / call " + (-value) + ",raise change " + (-(value * 2)));
                }
                if (!brainMap[temp[0]][temp[1]][0]) brainMap[temp[0]][temp[1]][0] = true;
                if (!brainMap[temp[0]][temp[1]][1]) brainMap[temp[0]][temp[1]][1] = true;
                if (!brainMap[temp[0]][temp[1]][2]) brainMap[temp[0]][temp[1]][2] = true;
            } else {//Won match value = 8
                int fold = 0;
                if (lost) { //You won by bluff (Raise)
                    brain[temp[0]][temp[1]][2] = brain[temp[0]][temp[1]][2] + (value * 2);
                    if (!brainMap[temp[0]][temp[1]][2]) brainMap[temp[0]][temp[1]][2] = true;
                    if (user) System.out.println("Raise bluff" + (value * 2));
                } else {//you were gonna win anyway.
                    if (user) System.out.println("Could have been better");
                    fold = (-value) * 2;
                    if (this.currentCard == 10) {
                        fold = fold - 20;
                    }
                    brain[temp[0]][temp[1]][0] = brain[temp[0]][temp[1]][0] + fold;
                    //Raise *2
                    brain[temp[0]][temp[1]][2] = brain[temp[0]][temp[1]][2] + (value * 2);
                    //Call
                    brain[temp[0]][temp[1]][1] = brain[temp[0]][temp[1]][1] + value;

                    if (!brainMap[temp[0]][temp[1]][0]) brainMap[temp[0]][temp[1]][0] = true;
                    if (!brainMap[temp[0]][temp[1]][1]) brainMap[temp[0]][temp[1]][1] = true;
                    if (!brainMap[temp[0]][temp[1]][2]) brainMap[temp[0]][temp[1]][2] = true;
                    if (user)
                        System.out.println("Fold penalty " + fold + " / call " + (value) + "/raise regret " + (value * 2));
                }
            }
            evaluateForUser(value,temp);
        }
        this.recordedAction = new Stack<>();
    }

    @Override
    public String readAImind() {
        int otherbet = getOther(this.id).bet - 1;
        if (otherbet < 0) otherbet = 0;
        int conf = (int) this.confidence / 5;
        String out = ((int) this.confidence) + "% confidence \n ";
        String fold = "Fold: " + this.brain[conf][otherbet][0];
        String call = "/ Call: " + this.brain[conf][otherbet][1];
        String raise = "/ Raise: " + this.brain[conf][otherbet][2];
        if (this.id == 1) pLog(out + fold + call + raise);
        return out + fold + call + raise;
    }

    @Override
    public void printBrainStruct() {
        //[10][59][3];
        for (int c = 0; c < 21; c++) {
            for (int b = 0; b < 59; b++) {
                System.out.print(this.brain[c][b][0] + "," + this.brain[c][b][1] + "," + this.brain[c][b][2]);
                if (b != 58) System.out.print("/");
            }
            System.out.println("");
        }
    }
}
