package com.haruhi.bismark439.indianpoker.AIs;

import com.haruhi.bismark439.indianpoker.AI.Player;

import java.util.Stack;

import static com.haruhi.bismark439.indianpoker.AI.Main.BASE_CHIP;
import static com.haruhi.bismark439.indianpoker.AI.Main.flag_turn1;
import static com.haruhi.bismark439.indianpoker.AI.Main.getOther;
import static com.haruhi.bismark439.indianpoker.AI.Main.pLog;
import static com.haruhi.bismark439.indianpoker.AI.Main.user;
import static com.haruhi.bismark439.indianpoker.RaiseExtra.cRaise;

//Check average card of me for his confidence rate
//Use my confidence to raise or fold
public class DangoAI extends Player {
    int weight = 5;
    public int pruneThreshold = 99;
    int totalSum = 0;
    double myGuessCard = 0;
    public int[][] frequency = new int[59][11]; //Enemy Bet, my Card
    public int[][] lastSeen = new int[59][weight];
    public int[][] bluff = new int[21][30];//My conf / my legal bet options
    int lastRaiseAmount;
    boolean flag_raised=false;
    @Override
    public void setBrainSize() {
        this.Single_brain = new double[59];
        this.lastSeen = new int[59][weight];
        this.name = "Eclair Bot";
    }

    @Override
    public int getAction() {
        int otherbet = getOther(this.id).bet - 1;
        double rand = Math.random();
        double crit = confidence / 100;
        int myAction = 2;
        boolean[] moves = possibleMoves();
        myGuessCard = Single_brain[otherbet];
        if (otherbet == 0) {
            myGuessCard = expectimax();
        } // First move is expectimax

        if (myGuessCard == 0) {
            return getRandomAction();
        } else {
            //Overfit
            int count = 1;
            if (otherbet > 1) {
                myGuessCard += Single_brain[otherbet - 1];
                count++;
            }
            if (otherbet < Single_brain.length - 1 && otherbet != 0) {
                myGuessCard += Single_brain[otherbet + 1];
                count++;
            }
            myGuessCard = myGuessCard / count;
            if (enemyCard == 10) {
                myAction=bluff10();
            } else if (enemyCard < myGuessCard) {
                //I probably win
                // Use confidence to raise
                if ((rand < crit) && moves[2]) {
                    if (user) System.out.println("Decided to Raise");
                    //If confidence 0.8, high chance to raise
                    myAction = 2;
                } else {
                    if (user) System.out.println("I may win, but try annealing CALL");
                    myAction = 1;
                }
            } else {
                //I probably lose
                // Use confidence to c
                if ((rand > crit) && moves[0]) {
                    if (user) System.out.println("Decided to Fold");
                    //Confidence 0.2
                    //High chance to fold.
                    myAction = 0;
                } else {
                    if (user) System.out.println("I may lose, but try annealing CALL");
                    myAction = 1;
                }
            }
        }
        try {
            if (user) Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (myAction == 2) {
            cRaise = bluff();
            if(cRaise<0){
                if(moves[1]){
                    myAction=1;
                }else{
                    myAction=0;
                }
            }
        }
        return myAction;
    }

    private int bluff10() {
        //1. Guess what my card would be.
        //That's expectimax
        double tomoyo = expectimax();
        //2.Calculate his expected confidence
        double total = knownCards.size();
        double wincase = 0;
        for (int i = 0; i < total; i++) {
            if (knownCards.get(i) >= tomoyo) {//Can Win+draw
                wincase++;
            }
        }
        double confidence = wincase / total;
        if (confidence < 0.721) {
            //If confidence is 0.2
            double temperature = Math.random();
            if (temperature > confidence) {
                //80% of bluffing
                return 2;
            } else {
                if (flag_turn1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } else {
            //Very low chance of annealing
            //Confidence is like 0.80
            double crit = (1 - confidence) / 2;
            //crit is 0.1
            double temperature = Math.random();
            if (temperature < crit) {
                //   System.out.println("Let's bluff");
                //10% of bluffing
                return 2;
            } else {
                //       System.out.println("Dont bluff");
                if (flag_turn1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
    private int bluff(){
        //Returns raise amount when you raise.
        int conf = (int) this.confidence / 5;
        int raiseBound = Math.min(this.chips-getOther(this.id).bet, getOther(this.id).chips);
        if(raiseBound<1)return 1;
        int maxIndex = 1;
        boolean isOpen = true;
        for(int i=1;i<raiseBound;i++){
            if(bluff[conf][i]==0){
                maxIndex=i;
                isOpen=false;
                break;
            }else{
                if(bluff[conf][i]>bluff[conf][maxIndex]){
                    maxIndex=i;
                }
            }
        }
        //  System.out.println("Found Max raise at "+maxIndex);
        if(isOpen&&maxIndex>1&&maxIndex<BASE_CHIP-2){
            maxIndex--;
            maxIndex+=(int)(Math.random() *3);//+ 0~2
        }else{

        }
        //Prune
        int sum = 0;
        int prune = 99;
        for(int i=1;i<raiseBound;i++){
            sum+=Math.abs(bluff[conf][i]);
        }
        if(sum>prune){
            for(int i=1;i<raiseBound;i++){
                bluff[conf][i]=bluff[conf][i]/2;
            }
        }
        if(bluff[conf][maxIndex]<0){
            //System.out.println("Negative raise detected");
            flag_raised = false;
            return -1;
        }else{
            lastRaiseAmount = maxIndex;
            flag_raised = true;
            return maxIndex;
        }
        //Found maxIndex
    }
    private double expectimax() {
        double sum = 0;
        for (int i = 0; i < knownCards.size(); i++) {
            sum += knownCards.get(i);
            //    System.out.println("EXP SUM: +"+knownCards.get(i)+" = "+sum);
        }
        // System.out.println("EXP VAL " + sum / knownCards.size());
        return sum / knownCards.size();
    }

    @Override
    public void evaluateAction(int value) {
        //5 from 5Weight of most seen
        //5 from last 5Weight cards seen
        int enemyBet = getOther(this.id).bet;
        int myCard = this.currentCard;
        adjustBrain(enemyBet);
        this.frequency[enemyBet][myCard]++;
        updateStack(enemyBet, myCard);
        //  System.out.println("Sizes "+lastSeen.size()+" val "+ frequency[enemyBet][myCard]);
        int[] maxList = getHighFrequency(frequency[enemyBet].clone());
        int[] LRUstack = this.lastSeen[enemyBet];
        if (Single_brain[enemyBet] == 0) {
            Single_brain[enemyBet] = this.currentCard;
        } else {
            double cons = 0;
            for (int i = 0; i < maxList.length; i++) {
                if (maxList[i] > 0) {
                    cons += maxList[i];
                }
            }
            cons = cons / totalSum;

            double lib = 0;
            for (int i = 0; i < LRUstack.length; i++) {
                lib += LRUstack[i];
            }
            lib = lib / LRUstack.length;
            double total = LRUstack.length + totalSum;
            if (totalSum == 0) {
                cons = 2.5;
            }
            if (LRUstack.length == 0) {
                lib = 2.5;
            }
            if (total != 0) {
                Single_brain[enemyBet] = cons * (totalSum / total) + lib * ((double) LRUstack.length / total);
            }
        }
        if(flag_raised){
            flag_raised=!flag_raised;
            System.out.println("New record; "+value);
            //You raised somehow
            int conf = (int)this.confidence/5;
            bluff[conf][lastRaiseAmount] += value;
            System.out.println("EVAL Last raise = "+lastRaiseAmount);
        }
        if (!recordedAction.empty()) evaluateForUser(value, recordedAction.pop());
        this.recordedAction = new Stack<>();
    }

    private void updateStack(int bet, int card) {
        boolean needshift = true;
        for (int i = 0; i < weight; i++) {
            if (this.lastSeen[bet][i] == 0) {
                this.lastSeen[bet][i] = card;
                needshift = false;
            }
        }
        if (needshift) {
            for (int i = 0; i < weight - 1; i++) {
                this.lastSeen[bet][i] = this.lastSeen[bet][i + 1];
            }
            this.lastSeen[bet][weight - 1] = card;
        }
    }

    private int[] getHighFrequency(int[] huko) {
        totalSum = 0;
        int[] maxList = new int[weight]; //Max 5 cards
        //get top 5
        for (int x = 0; x < weight; x++) {
            int maxIndex = 1;
            for (int i = 2; i < huko.length; i++) {
                if (huko[i] > huko[maxIndex]) {
                    maxIndex = i;
                }
            }//found local max
            maxList[x] = maxIndex;
            huko[maxIndex] = -1 * huko[maxIndex];//reset Frequency
        }//Found 5 max

        for (int i = 0; i < maxList.length; i++) {
            int card = maxList[i];
            huko[card] = -1 * huko[card];//restore Frequency
            boolean found = false;
            for (int x = 0; x < knownCards.size(); x++) {
                if (knownCards.get(x) == card) {
                    found = true;
                    break;
                }
            }
            if (found) {
                maxList[i] = card * huko[card]; // Card * Frequency
                totalSum += huko[card];
            } else {
                maxList[i] = -1;
            }
        }

        //Check possibility
        return maxList;
    }

    @Override
    public void printBrainStruct() {
        System.out.println("=====================FREQUENCY====================");
        for (int b = 0; b < this.frequency.length; b++) {
            System.out.print(frequency[b][0]);
            for (int c = 1; c < this.frequency[b].length; c++) {
                System.out.print("," + frequency[b][c]);
            }
            System.out.println("");
        }
        System.out.println("Stack");
        for (int b = 0; b < this.lastSeen.length; b++) {
            System.out.print(lastSeen[b][0]);
            for (int c = 1; c < this.lastSeen[b].length; c++) {
                System.out.print("," + lastSeen[b][c]);
            }
            System.out.println("");
        }
        System.out.println("Bluff");
        for (int b = 0; b < this.bluff.length; b++) {
            System.out.print((b*5)+": "+bluff[b][0]);
            for (int c = 1; c < this.bluff[b].length; c++) {
                System.out.print("," + bluff[b][c]);
            }
            System.out.println("");
        }
    }

    private void adjustBrain(int otherbet) {
        int sum = 0;
        for (int i = 1; i < 11; i++) {
            sum += frequency[otherbet][i];
        }
        if (sum >= pruneThreshold) {
            for (int i = 1; i < 11; i++) {
                frequency[otherbet][i] = frequency[otherbet][i] / 2;
            }
        }
    }

    public void constructBrain() {
        for (int b = 0; b < 59; b++) {
            int enemyBet = b;
            int[] maxList = getHighFrequency(frequency[enemyBet].clone());
            int[] LRUstack = this.lastSeen[enemyBet];

            double cons = 0;
            for (int i = 0; i < maxList.length; i++) {
                if (maxList[i] > 0) {
                    cons += maxList[i];
                }
            }
            cons = cons / totalSum;

            double lib = 0;
            for (int i = 0; i < LRUstack.length; i++) {
                lib += LRUstack[i];
            }
            lib = lib / LRUstack.length;
            double total = LRUstack.length + totalSum;
            if (totalSum == 0) {
                cons = 2.5;
            }
            if (LRUstack.length == 0) {
                lib = 2.5;
            }
            if (total != 0) {
                Single_brain[enemyBet] = cons * (totalSum / total) + lib * ((double) LRUstack.length / total);
            }
            System.out.println("Evaluation " + b + " : " + Single_brain[b]);
        }
    }

    @Override
    public String readAImind() {
        int otherbet = getOther(this.id).bet - 1;
        if (otherbet < 0) otherbet = 0;
        String out = "";// ((int)this.confidence)+"% confidence \n ";
        String mycard = "My card might be : " + String.format("%.2f", myGuessCard);
        if (this.id == 1) pLog(out + mycard);
        return out + mycard;
    }

}
