package com.haruhi.bismark439.indianpoker.AI;

import static com.haruhi.bismark439.indianpoker.MainActivity.players;

/**
 * Created by Bismark439 on 06/02/2018.
 */

public class userBrain {
    public static int[][][] userBrain = new int[21][59][3];//Confidence /BetMoney / myCallRaiseFold
    public static double[] userSingleBrain = new double[59];//Confidence /BetMoney / myCallRaiseFold


    public static void overrideUserBrain(int AIid) {
        players[1].AI_id=AIid;
        players[1].brain = userBrain;
        players[1].Single_brain = userSingleBrain;
        players[1].drawBrainMap();
        players[1].printBrainMap();
    }

}
