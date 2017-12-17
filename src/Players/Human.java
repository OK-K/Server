package Players;

import Players.Player;
import random.AdmiralRandom;
import java.awt.*;

public class Human implements Player {
    int[][] myShips = new int[12][12];
    String login;
    private int countOfShips = 10;
    private AdmiralRandom admiral = new AdmiralRandom();


    public Human(String login){
        this.login = login;
        nullMatr();
        myShips=admiral.randomArrangement();
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public void nullMatr() {
        for (int i=0; i<12; i++)
            for (int j=0; j<12; j++) {
                if (i == 0 || j==0 || i==11 || j==11)
                    myShips[i][j] = -5;
                else
                    myShips[i][j] = 0;
            }
    }

    public void iWasShot(Point p){
        if (myShips[p.x][p.y]<1) throw new IllegalArgumentException("Почему вы стреляете по уже стреляной клетке?");
        else myShips[p.x][p.y] = -myShips[p.x][p.y];
    }
//-------------------------------------------------------------------------------------
    @Override//предполагается наличие кода, который берёт сделанную
    public Point makeAShot(Player enemy) {
        return null;
    }

    @Override
    public Point makeAShot(Point p, Player enemy) {
        enemy.iWasShot(p);
        return p;
    }

    @Override
    public int[][] ships() {
        return myShips;
    }

    @Override
    public void loadShips(int[][] ships) {
        if (ships[0].length == 10){
            for (int i=1; i<=10; i++)
                for (int j=1; j<=10; j++) {
                    myShips[i][j] = ships[i-1][j-1];
                }
        }
        else if (ships[0].length == 12){
            myShips = ships;
        }
        else throw new IllegalArgumentException("Размеры матрицы не соответствуют размерам поля");
    }

    @Override
    public void myShipIsDead() {
        countOfShips--;
    }

    @Override
    public int getCountOfShips() {
        return countOfShips;
    }

    @Override
    public void arrangeShipsRandom() {
        nullMatr();
        myShips=admiral.randomArrangement();
    }

    @Override
    public int[][] getShipsForClient() {
        int[][] send = new int[10][10];
        for(int i=1; i<= 10; i++)
            for(int j=1; j<=10; j++)
                send[i-1][j-1] = myShips[i][j];
        return send;
    }

    @Override
    public String toString() {
        String res = getLogin()+"\n";
        for (int i=1; i<=10; i++) {
            for (int j = 1; j <= 10; j++) {
                if (myShips[i][j] >= 0) res += " "+myShips[i][j] + " ";
                else res += myShips[i][j] + " ";
            }
            res+="\n";
        }
        return res;
    }
}
