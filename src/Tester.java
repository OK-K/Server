import Players.AI;
import Players.Human;
import Players.Player;
import Players.Player;
import battleOnTheSea.BattleManager;
import random.AdmiralRandom;

import java.awt.*;

public class Tester {
    public static void main(String[] args){
        int count = 0;
        for(int i=0; i<10000; i++) {
            BattleManager.prepareTheGame(null);
            if (BattleManager.playTheGame() == 1) count++;
            //System.out.println(BattleManager.showSituation());
        }
        System.out.println(count);
        /*BattleManager.testGame();*/
    }
    private static void show(int[][] myShips){
        String res ="";
        for (int i=1; i<=10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (myShips[i][j] >= 0) res += " "+myShips[i][j] + " ";
                else res += myShips[i][j] + " ";
            }
            res+="\n";
        }
        System.out.println(res);
    }

    private static void strange(int[][] a, boolean firstDiag){
        boolean canMakeAShot = false;
        int j=0;
        if(firstDiag)
            j= -1;
        else
            j= 1;
            for (int i = 1; i <= 10; i++) {
                j++;
                if (j == 4) j = 0;
                int k = 1;
                do {
                    a[i][k * 4 - j % 4] = 4;
                    k++;

                } while (k * 4 - j % 4 <= 10);
            }

    }
}
