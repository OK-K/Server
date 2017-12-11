package battleOnTheSea;

import Players.AI;
import Players.Human;
import Players.Player;
import Players.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class BattleManager {
    private Player[] players = new Player[2];

    // конструктор по умолчанию для игры двух ИИ
    public BattleManager(){
        players[0] = new AI(0);
        players[1] = new AI(3);
    }
    // конструктор для игры человека с ИИ
    public BattleManager(String login, int level){
        players[0] = new Human(login);
        players[1] = new AI(level);
    }
    // конструктор для игры человек-человек
    public BattleManager(String login1, String login2){
        players[0] = new Human(login1);
        players[1] = new Human(login2);
    }
    // универсальный конструктор
    public BattleManager(Player player1, Player player2){
        players[0] = player1;
        players[1] = player2;
    }
    // геттер для игроков
    public Player[] getPlayers() {
        return players;
    }
    // метод запускается, когда checkTheField возвращает true
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
    private boolean isFinished(){
        if (players[0].getCountOfShips()==0 || players[1].getCountOfShips()==0)
            return true;
        return false;
    }
    // процесс игры, реализованный на массиве интерфейсов
    public int playTheGame(){
        Random rand = new Random();
        int turn = rand.nextInt(2);
        int[] count= new int[2];
        Point p;
        do{

            do {

                p = players[turn].makeAShot(players[1 - turn]);
                count[turn]++;
                boolean isDead = checkTheField(p, players[1 - turn]);
                if (isDead) {
                    shipIsDead(players[1 - turn]);
                    //System.out.println(players[turn].getLogin()+" утопил корабль на "+p);
                }

            }while (players[1 - turn].ships()[p.x][p.y]!=-5 && !isFinished());

            turn = 1 - turn;
        }while (!isFinished());
        turn = 1 - turn;
        //System.out.println(players[turn].getLogin()+" выиграл на ходе "+count[turn]);
        return turn;


    }
    // вывод на экран текущей ситуации
    public String showSituation() {
        return players[0]+"\n"+players[1];
    }

}
