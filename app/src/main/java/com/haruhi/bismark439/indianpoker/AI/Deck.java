package com.haruhi.bismark439.indianpoker.AI;

import java.util.ArrayList;
import java.util.Collections;

import static com.haruhi.bismark439.indianpoker.MainActivity.players;

public class Deck {
    ArrayList<Integer> deck;
    int i = -1;
    public Deck(){
        this.deck = new ArrayList<>();
    }
    public void init(){
        this.deck = new ArrayList<>();
        this.i = -1;
        for(int i=0;i<2;i++){
            for(int n = 1; n<11;n++){//2 sets of 1~10
            push(n);
            }
        }
        shuffle(3);
    }

    public int pop(){
        int c = deck.remove(i);
        i--;
        if(i<0){init();
        players[0].initCardCount();
        players[1].initCardCount();
        }
        return c;
    }
    public void push(int c){
        deck.add(c);
        i++;
    }
    public void shuffle(int n){
        for(int i=0;i<n;i++){
            Collections.shuffle(this.deck);
        }
    }
}
