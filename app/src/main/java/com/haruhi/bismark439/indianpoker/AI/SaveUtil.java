package com.haruhi.bismark439.indianpoker.AI;

import android.content.Context;

import com.haruhi.bismark439.indianpoker.AIs.DangoAI;
import com.haruhi.bismark439.indianpoker.AIs.EclairAI;
import com.haruhi.bismark439.indianpoker.AIs.mathAI;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.haruhi.bismark439.indianpoker.AI.userBrain.overrideUserBrain;
import static com.haruhi.bismark439.indianpoker.AI.userBrain.userBrain;
import static com.haruhi.bismark439.indianpoker.AI.userBrain.userSingleBrain;
import static com.haruhi.bismark439.indianpoker.AILearns.bname;
import static com.haruhi.bismark439.indianpoker.AILearns.selectedAI_id;
import static com.haruhi.bismark439.indianpoker.MainActivity.AiDB;
import static com.haruhi.bismark439.indianpoker.MainActivity.brainFileName;
import static com.haruhi.bismark439.indianpoker.MainActivity.players;
import static com.haruhi.bismark439.indianpoker.MainActivity.sharedEditor;
import static com.haruhi.bismark439.indianpoker.MainActivity.sharedPref;

/**
 * Created by Bismark439 on 25/03/2018.
 */

public class SaveUtil {
    public static AiData getByID(int id) {
        for (int i = 0; i < AiDB.size(); i++) {
            if (AiDB.get(i).id == id) return AiDB.get(i);
        }
        System.out.println("Couldn't find "+id+" return null");
        return null;
    }
    public static void resetSave(int id){
        sharedEditor.putBoolean("brain" + id + "inited", false);
        sharedEditor.commit();
    }
    //Global LOAD HELPER
    public static void loadAI(Context context) {
        int AIid = selectedAI_id;
        System.out.println("ID: " + AIid);
        switch (AIid) {
            case 0:
                loadUserBrain();
                overrideUserBrain(AIid);
                break;
            case 1: //Math
                players[1] = new mathAI();
                players[1].AI_id = AIid;
                break;
            case 2:
                brainFileName = "ai70_m0.txt";
                readBrain(context, getByID(AIid));
                break;
            case 3: //Dango
                players[1] = new DangoAI();
                loadDango(context,getByID(AIid));
                break;
            case 4: //Eclair
                players[1] = new EclairAI();
                loadEclair(context, getByID(AIid));
                break;
            case 5: //Bold
                brainFileName = "ai_bold.txt";
                readBrain(context, getByID(AIid));
                break;
        }
        sharedEditor.putInt("defaultBrain", AIid);
        sharedEditor.commit();
       if(bname!=null) bname.setText(getByID(AIid).getName());
    }

    //Save current Player's brain into its id
    public static void saveAI(Player player) {
        int id = player.AI_id;
        System.out.println(id + "= Player Brain save called ");
        switch (id) {
            case 0:
                saveUserBrain();
                break;
            case 1:
                //Math. do nothing
                break;
            case 2:
                saveHDBrain(player);
                break;
            case 3:
                saveDango((DangoAI) player);
                break;
            case 4:
                saveEclair((EclairAI) player);
                break;

        }

        sharedEditor.putBoolean("brain" + id + "inited", true);
        sharedEditor.commit();
    }

    //Load Brain, either usermade or preset
    public static void readBrain(Context context, AiData aiData) {
        try {
            int[][][] brain = new int[21][59][3];
            int id = aiData.id;
            boolean init = sharedPref.getBoolean("brain" + id + "inited", false);
            if (init) {
                for (int c = 0; c < 21; c++) {
                    for (int b = 0; b < 59; b++) {
                        for (int a = 0; a < 3; a++) {
                            brain[c][b][a] = sharedPref.getInt("brain" + id + "c" + c + "b" + b + "a" + a, 0);
                        }
                    }
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("preset/" + brainFileName)));
                // do reading, usually loop until end of file reading
                String mLine = reader.readLine();
                int b = 0;
                while (mLine != null) {
                    String[] lineToken = mLine.split("/");
                    for (int i = 0; i < lineToken.length; i++) {
                        String[] actionToken = lineToken[i].split(",");
                        for (int z = 0; z < actionToken.length; z++) {
                            brain[b][i][z] = Integer.parseInt(actionToken[z]);
                        }
                    }
                    mLine = reader.readLine();
                    b++;
                }
                reader.close();
            }
            players[1].brain = brain;
            players[1].AI_id = id;
            players[1].drawBrainMap();
            players[1].name = aiData.getName();
            System.out.println("Successful load " + brainFileName + " ID " + id);
            sharedEditor.putInt("defaultBrain", id);
            sharedEditor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveHDBrain(Player player) {
        int id = player.AI_id;
        System.out.println("Saving "+id+" = "+player.name);
        for (int c = 0; c < 21; c++) {
            for (int b = 0; b < 59; b++) {
                for (int a = 0; a < 3; a++) {
                    sharedEditor.putInt("brain" + id + "c" + c + "b" + b + "a" + a, player.brain[c][b][a]);
                }
            }
        }
        sharedEditor.putBoolean("brain" + id + "inited", true);
        sharedEditor.commit();
    }

    //Load SingleBrain, Preset
    public static void readSingleBrain(Context context, int id) {
        try {
            double[] brain = new double[59];
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("preset/dango.txt")));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            int b = 0;
            while (mLine != null) {
                double mycard = Double.parseDouble(mLine);
                brain[b] = mycard;
                mLine = reader.readLine();
                b++;
            }
            reader.close();
            players[1].Single_brain = brain;
            players[1].AI_id = id;
            System.out.println("Successful load 경단 AI" + id);
            sharedEditor.putInt("defaultBrain", id);
            sharedEditor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Load Single Brain, determine preset or user
    public static void loadDango(Context context, AiData aiData) {
        System.out.println("Loading Dango...");
        try {
            int[][] freq = new int[59][11];
            int[][] stack = new int[59][5];
            int[][] bluf = new int[21][30];
            int id = aiData.id;
            boolean init = sharedPref.getBoolean("brain" + id + "inited", false);
            if (init) {
                for (int b = 0; b < 59; b++) {
                    for (int c = 1; c < 11; c++) {
                        freq[b][c] = sharedPref.getInt("brain" + id + "c" + c + "b" + b, 0);
                    }
                }
                for (int b = 0; b < 59; b++) {
                    for (int c = 0; c < 5; c++) {
                        stack[b][c] = sharedPref.getInt("brainStack" + id + "c" + c + "b" + b, 0);
                    }
                }
                for (int c = 0; c < 21; c++) {
                    for (int b = 0; b < 30; b++) {
                        bluf[c][b] = sharedPref.getInt("brainBluff" + id + "c" + c + "b" + b, 0);
                        System.out.println("Loading brainBluff+"+id+" "+c+" b"+b+": "+bluf[c][b]);
                    }
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("preset/dango.txt")));
                // do reading, usually loop until end of file reading
                String mLine = reader.readLine();
                int b = 0;
                boolean next= true;
                while (next) {
                    System.out.println(mLine);
                    String[] lineToken = mLine.split(",");
                    for (int c = 0; c < lineToken.length; c++) {
                        freq[b][c] = Integer.parseInt(lineToken[c]);
                    }
                    mLine = reader.readLine();
                    if(mLine.equals("Stack")){
                        next=false;
                    }
                    b++;
                }
                b = 0;
                next=true;
                mLine = reader.readLine();
                while (next) {
                    //  System.out.println(mLine);
                    String[] lineToken = mLine.split(",");
                    for (int c = 0; c < lineToken.length; c++) {
                        stack[b][c] = Integer.parseInt(lineToken[c]);
                    }
                    mLine = reader.readLine();
                    if(mLine.equals("Bluff")){
                        next=false;
                    }
                    b++;
                }

                b = 0;
                mLine = reader.readLine();
                while (mLine!=null) {
                    //  System.out.println(mLine);
                    String[] lineToken = mLine.split(",");
                    for (int c = 0; c < lineToken.length; c++) {
                        bluf[b][c] = Integer.parseInt(lineToken[c]);
                    }
                    mLine = reader.readLine();
                    b++;
                }
                reader.close();
            }
            ((DangoAI) players[1]).frequency = freq;
            ((DangoAI) players[1]).lastSeen = stack;
            ((DangoAI) players[1]).bluff = bluf;
            ((DangoAI) players[1]).constructBrain();
            players[1].AI_id = id;
            players[1].name = aiData.getName();
            System.out.println("Successful load 경단" + " ID " + id);
            sharedEditor.putInt("defaultBrain", id);
            sharedEditor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveDango(DangoAI player) {
        int id = player.AI_id;
        System.out.println(id + "= Player Brain save called ");
        for (int b = 0; b < 59; b++) {
            for (int c = 1; c < 11; c++) {
                sharedEditor.putInt("brain" + id + "c" + c + "b" + b, player.frequency[b][c]);
            }
        }

        for (int b = 0; b < 59; b++) {
            for (int c = 0; c < 5; c++) {
                sharedEditor.putInt("brainStack" + id + "c" + c + "b" + b, player.lastSeen[b][c]);
            }
        }
        for (int c = 0; c < 21; c++) {
             for (int b = 0; b < 30; b++) {
                 System.out.println("Saving brainBluff+"+id+" "+c+" b"+b+": "+player.bluff[c][b]);
                sharedEditor.putInt("brainBluff" + id + "c" + c + "b" + b, player.bluff[c][b]);
            }
        }
        sharedEditor.commit();
    }

    //Save Eclair
    public static void saveEclair(EclairAI player) {
        int id = player.AI_id;
        System.out.println(id + "= Player Brain save called ");
        for (int b = 0; b < 59; b++) {
            for (int c = 1; c < 11; c++) {
                sharedEditor.putInt("brain" + id + "c" + c + "b" + b, player.frequency[b][c]);
            }
        }

        for (int b = 0; b < 59; b++) {
            for (int c = 0; c < 5; c++) {
                sharedEditor.putInt("brainStack" + id + "c" + c + "b" + b, player.lastSeen[b][c]);
            }
        }
        sharedEditor.commit();
    }
    //Load Eclair
    public static void loadEclair(Context context, AiData aiData) {
        try {
            int[][] freq = new int[59][11];
            int[][] stack = new int[59][5];
            int id = aiData.id;
            boolean init = sharedPref.getBoolean("brain" + id + "inited", false);
            if (init) {
                for (int b = 0; b < 59; b++) {
                    for (int c = 1; c < 11; c++) {
                        freq[b][c] = sharedPref.getInt("brain" + id + "c" + c + "b" + b, 0);
                    }
                }
                for (int b = 0; b < 59; b++) {
                    for (int c = 0; c < 5; c++) {
                        stack[b][c] = sharedPref.getInt("brainStack" + id + "c" + c + "b" + b, 0);
                    }
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("preset/eclair.txt")));
                // do reading, usually loop until end of file reading
                String mLine = reader.readLine();
                int b = 0;
                boolean next= true;
                while (next) {
                    System.out.println(mLine);
                    String[] lineToken = mLine.split(",");
                    for (int c = 0; c < lineToken.length; c++) {
                        freq[b][c] = Integer.parseInt(lineToken[c]);
                    }
                    mLine = reader.readLine();
                    if(mLine.equals("Stack")){
                        next=false;
                    }
                    b++;
                }
                b = 0;
                mLine = reader.readLine();
                while (mLine!=null) {
                  //  System.out.println(mLine);
                    String[] lineToken = mLine.split(",");
                    for (int c = 0; c < lineToken.length; c++) {
                        stack[b][c] = Integer.parseInt(lineToken[c]);
                    }
                    mLine = reader.readLine();
                    b++;
                }
                reader.close();
            }
            ((EclairAI) players[1]).frequency = freq;
            ((EclairAI) players[1]).lastSeen = stack;
            ((EclairAI) players[1]).constructBrain();
            players[1].AI_id = id;
            players[1].name = aiData.getName();
            System.out.println("Successful load 이클레어" + " ID " + id);
            sharedEditor.putInt("defaultBrain", id);
            sharedEditor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    ///-----------USER BRAIN
    public static void loadUserBrain() {
        System.out.println("Load User Brain called");
        boolean inited = sharedPref.getBoolean("brain" + "User" + "inited", false);
        if (inited) {
            System.out.println("Loading 3D Brain map");
            for (int c = 0; c < 21; c++) {
                for (int b = 0; b < 59; b++) {
                    for (int a = 0; a < 3; a++) {
                        userBrain[c][b][a] = sharedPref.getInt("brain" + "User" + "c" + c + "b" + b + "a" + a, 0);
                        //  if( userBrain[c][b][a]!=0){System.out.println("Found non zeo entry");}
                    }
                }
            }
        }

        inited = sharedPref.getBoolean("sbrain" + 3 + "inited", false);
        if (inited) {
            System.out.println("Dango bot inited " + 3);
            for (int b = 0; b < 59; b++) {
                userSingleBrain[b] = ((double) sharedPref.getInt("sbrain" + 3 + "b" + b, 0)) / 1000;
                //  System.out.println(userSingleBrain[b]);
            }
        }
    }

    public static void saveUserBrain() {
        System.out.println("Save User Brain called");
        for (int c = 0; c < 21; c++) {
            for (int b = 0; b < 59; b++) {
                for (int a = 0; a < 3; a++) {
                    sharedEditor.putInt("brain" + "User" + "c" + c + "b" + b + "a" + a, userBrain[c][b][a]);
                }
            }
        }
       /* for (int b = 0; b < 59; b++) {
                int nagisa = (int)(userSingleBrain[b]*1000);
                sharedEditor.putInt("sbrain" +3 + "b" + b , nagisa);
        }
        sharedEditor.putBoolean("sbrain" + 3 + "inited", true);
*/
        sharedEditor.putBoolean("brain" + "User" + "inited", true);
        sharedEditor.commit();
    }
}
