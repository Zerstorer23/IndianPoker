package com.haruhi.bismark439.indianpoker.AIs;

import com.haruhi.bismark439.indianpoker.AI.Player;

import java.util.Stack;

public class mathAI extends Player {

    @Override
    public void setBrainSize() {
        this.name = "Math";
    }

    @Override
    public int getAction() {
        return getHeuristicAction();
    }

    @Override
    public void evaluateAction(int value) {
//You don't evaluate
        this.recordedAction = new Stack<>();
    }

    @Override
    public void printBrainStruct() {

    }


    @Override
    public String readAImind() {
        //You dont read
        return null;
    }
}
