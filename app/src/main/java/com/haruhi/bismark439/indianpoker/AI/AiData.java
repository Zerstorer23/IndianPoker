package com.haruhi.bismark439.indianpoker.AI;

/**
 * Created by Bismark439 on 05/02/2018.
 */

public class AiData {
    String name;
    boolean unlocked;
    String AImode;
    public int id;
    public int requiredWin;
    public int requiredNeuron;

    public AiData(String name, boolean unlocked, String AImode, int id) {
        this.name = name;
        this.unlocked = unlocked;
        this.AImode = AImode;
        this.requiredWin = 0;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public String getAImode() {
        return AImode;
    }

    public void setAImode(String AImode) {
        this.AImode = AImode;
    }
}
