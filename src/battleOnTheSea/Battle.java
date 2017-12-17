package battleOnTheSea;

import Players.Player;

import java.awt.*;

public class Battle {
    private static void shipIsDead(Player player){
        player.myShipIsDead();
        int[][] ships = player.ships();
        for (int i=1; i<11; i++)
            for(int j=1; j<11;j++) {
                if (ships[i][j - 1] == 0 ||
                        ships[i - 1][j] == 0 ||
                        ships[i][j + 1] == 0 ||
                        ships[i + 1][j] == 0 ||
                        ships[i + 1][j + 1] == 0 ||
                        ships[i + 1][j - 1] == 0 ||
                        ships[i - 1][j - 1] == 0 ||
                        ships[i - 1][j + 1] == 0) {
                    if(ships[i][j] >0)
                        ships[i][j] = -1*ships[i][j];
                }
            }
    }
    // метод проверки сделанного выстрела на предмет убийства
    private static boolean checkTheField(Point shot, Player player){
        player.iWasShot(shot);
        int[][] ships = player.ships();
        switch (ships[shot.x][shot.y]){
            case -1: {
                ships[shot.x][shot.y] = 0;
                return true;
            }
            case -2:{
                if(ships[shot.x+1][shot.y] == -2){
                    ships[shot.x+1][shot.y] = 0;
                    ships[shot.x][shot.y] = 0;
                    return true;
                }
                if(ships[shot.x-1][shot.y] == -2){
                    ships[shot.x-1][shot.y] = 0;
                    ships[shot.x][shot.y] = 0;
                    return true;
                }
                if(ships[shot.x][shot.y+1] == -2){
                    ships[shot.x][shot.y+1] = 0;
                    ships[shot.x][shot.y] = 0;
                    return true;
                }
                if(ships[shot.x][shot.y-1] == -2){
                    ships[shot.x][shot.y-1] = 0;
                    ships[shot.x][shot.y] = 0;
                    return true;
                }
                return false;
                /*for (int i=-1; i<2; i++)
                    for(int j=-1; j<2; j++)
                    {
                            if(ships[shot.x+i][shot.y+j] == -2 && i+j!=0){
                                ships[shot.x+i][shot.y+j] = 0;
                                ships[shot.x][shot.y] = 0;
                                player.myShipIsDead();
                                return true;
                            }
                    }
                return false;*/

            }
            case -3:{

                Point p1 = new Point();
                int count=1;

                if(ships[shot.x][shot.y-1] == -3 || ships[shot.x-1][shot.y] == -3 || ships[shot.x][shot.y+1] == -3 || ships[shot.x+1][shot.y] == -3){
                    if(ships[shot.x][shot.y-1] == -3){
                        p1.x = shot.x;
                        p1.y = shot.y-1;
                        count++;
                    }
                    if(ships[shot.x-1][shot.y] == -3){
                        count++;
                        if (count==3) {
                            ships[shot.x][shot.y] = 0;
                            ships[shot.x-1][shot.y] = 0;
                            ships[p1.x][p1.y] = 0;
                            return true;
                        }
                        else{
                            p1.x = shot.x-1;
                            p1.y = shot.y;
                        }

                    }
                    if(ships[shot.x][shot.y+1] == -3){
                        count++;
                        if (count==3) {
                            ships[shot.x][shot.y] = 0;
                            ships[shot.x][shot.y+1] = 0;
                            ships[p1.x][p1.y] = 0;
                            return true;
                        }
                        else{
                            p1.x = shot.x;
                            p1.y = shot.y+1;
                        }
                    }
                    if(ships[shot.x+1][shot.y] == -3){
                        count++;
                        if (count==3) {
                            ships[shot.x][shot.y] = 0;
                            ships[shot.x+1][shot.y] = 0;
                            ships[p1.x][p1.y] = 0;
                            return true;
                        }
                        else{
                            p1.x = shot.x+1;
                            p1.y = shot.y;
                        }
                    }
                }
                else return false;

                if(ships[p1.x + (p1.x-shot.x)][p1.y + (p1.y-shot.y)] == -3){
                    ships[shot.x][shot.y] = 0;
                    ships[p1.x + (p1.x-shot.x)][p1.y + (p1.y-shot.y)] = 0;
                    ships[p1.x][p1.y] = 0;
                    return true;
                }
                return false;

            }
            case -4:{
                int count = 0;
                for(int i=1; i<=10; i++)
                    for(int j=1; j<=10; j++)
                        if(ships[i][j]==-4) count++;
                if(count==4){
                    for(int i=1; i<=10; i++)
                        for(int j=1; j<=10; j++)
                            if(ships[i][j]==-4) ships[i][j]=0;
                    return true;
                }
                return false;
            }
            case -5:{
                return false;
            }
        }

        return false;
    }
    // признак конца игры
    private static boolean isFinished(Player[] players){
        if (players[0].getCountOfShips()==0 || players[1].getCountOfShips()==0)
            return true;
        return false;
    }
}
