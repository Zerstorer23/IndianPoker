package com.haruhi.bismark439.indianpoker.AIs;

import com.haruhi.bismark439.indianpoker.AI.Player;

import java.util.Stack;

public class randomAI extends Player{
    @Override
    public void setBrainSize() {

    }

    @Override
    public int getAction() {
        return getRandomAction();
    }

    @Override
    public void evaluateAction(int value) {
        this.recordedAction = new Stack<>();
    }

    @Override
    public void printBrainStruct() {

    }
    @Override
    public String readAImind() {
        return null;
    }
}
