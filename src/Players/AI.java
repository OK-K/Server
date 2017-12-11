package Players;

import Players.Player;
import random.AdmiralRandom;

import java.awt.*;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;

public class AI implements Player {
    private ArrayList<Point> luckyShot = new ArrayList<Point>();
    private String login;
    private int[][] myShips = new int[12][12];
    private Random randomCoordinate;
    private int countOfTheShips = 10;
    int level;
    private AdmiralRandom admiral = new AdmiralRandom();

    public AI() {
        randomCoordinate = new Random();
        level = 2;
        login = "Artificial Admiral";
        nullMatr();
        myShips = admiral.specialArrangement();
    }
    public AI(int level) {
        randomCoordinate = new Random();
        this.level = level;
        nullMatr();
        switch (level){
            case 0:{
                login = "Лалка затраленная";
                myShips = admiral.randomArrangement();
                break;
            }
            case 1:{
                login = "Вице-адмирал Трайон";
                myShips = admiral.randomArrangement();
                break;
            }
            case 2:{
                login = "Адмирал Микель де Рюйтер";
                myShips = admiral.specialArrangement();
                break;
            }
            case 3:{
                login = "Адмирал Павел Нахимов";
                myShips = admiral.specialArrangement();
                break;
            }
        }
    }

    @Override
    public int[][] ships() {
        return myShips;
    }

    @Override
    public void loadShips(int[][] ships) {
        myShips=ships;
    }

    @Override
    public void myShipIsDead() {
        countOfTheShips--;
    }

    @Override
    public int getCountOfShips() {
        return countOfTheShips;
    }

    @Override
    public void arrangeShipsRandom() {
        nullMatr();
        if (level > 1)
            myShips = admiral.specialArrangement();
        else
            myShips = admiral.randomArrangement();
    }

    @Override
    public int[][] getShipsForClient() {
        int[][] send = new int[10][10];
        for(int i=1; i<= 10; i++)
            for(int j=1; j<=10; j++)
                send[i-1][j-1] = myShips[i][j];
        return send;
    }

    private Point easyShot(Player enemy){
        int[][] enemyShips = enemy.ships();
        int x = 0;
        int y = 0;
        Point shot = null;
        boolean luck = false;
        do {
            x = 1+randomCoordinate.nextInt(10);
            y = 1+randomCoordinate.nextInt(10);
            if (enemyShips[x][y] > 0){
                shot = new Point(x,y);
                luck = true;
            }

        }while (!luck);
        return shot;
    }

    private Point middleShot(Player enemy){
        int[][] enemyShips = enemy.ships();
        int x = 0;
        int y = 0;
        Point shot = null;
        boolean luck = false;
        if(luckyShot.isEmpty()){
            shot = easyShot(enemy);
            if(enemyShips[shot.x][shot.y]>1 && enemyShips[shot.x][shot.y]<5)
                luckyShot.add(shot);
        }
        else{
            shot = new Point();
            if(luckyShot.size()==1){
                if(enemyShips[luckyShot.get(0).x+1][luckyShot.get(0).y] > 0) {
                    x = luckyShot.get(0).x+1;
                    y = luckyShot.get(0).y;
                }
                else if(enemyShips[luckyShot.get(0).x-1][luckyShot.get(0).y] > 0) {
                    x = luckyShot.get(0).x-1;
                    y = luckyShot.get(0).y;
                }
                else if(enemyShips[luckyShot.get(0).x][luckyShot.get(0).y+1] > 0) {
                    x = luckyShot.get(0).x;
                    y = luckyShot.get(0).y+1;
                }
                else if(enemyShips[luckyShot.get(0).x][luckyShot.get(0).y-1] > 0) {
                    x = luckyShot.get(0).x;
                    y = luckyShot.get(0).y-1;
                }

                shot.x = x;
                shot.y = y;
                if (enemyShips[shot.x][shot.y] != 5)
                    luckyShot.add(shot);
                if(enemyShips[shot.x][shot.y] == luckyShot.size())
                    luckyShot.clear();
                return shot;

            }
            if(luckyShot.size()==2){
                Point p1 = luckyShot.get(0);
                Point p2 = luckyShot.get(1);
                if(p1.x == p2.x){
                    if(p1.y > p2.y){
                        if(enemyShips[p1.x][p1.y+1] > 0){
                            x = p1.x;
                            y = p1.y+1;
                        }
                        else  if(enemyShips[p2.x][p2.y-1] > 0){
                            x = p2.x;
                            y = p2.y-1;
                        }
                    }
                    else{
                        if(enemyShips[p2.x][p2.y+1] > 0){
                            x = p2.x;
                            y = p2.y+1;
                        }
                        else  if(enemyShips[p1.x][p1.y-1] > 0){
                            x = p1.x;
                            y = p1.y-1;
                        }
                    }
                }
                else{
                    if(p1.x > p2.x){
                        if(enemyShips[p1.x+1][p1.y] > 0){
                            x = p1.x+1;
                            y = p1.y;
                        }
                        else  if(enemyShips[p2.x-1][p2.y] > 0){
                            x = p2.x-1;
                            y = p2.y;
                        }
                    }
                    else{
                        if(enemyShips[p2.x+1][p2.y] > 0){
                            x = p2.x+1;
                            y = p2.y;
                        }
                        else  if(enemyShips[p1.x-1][p1.y] > 0){
                            x = p1.x-1;
                            y = p1.y;
                        }
                    }
                }

                shot.x = x;
                shot.y = y;
                if (enemyShips[shot.x][shot.y] != 5)
                    luckyShot.add(shot);
                if(enemyShips[shot.x][shot.y] == luckyShot.size())
                    luckyShot.clear();
                return shot;

            }
            if(luckyShot.size()==3){
                Point maxP = luckyShot.get(0);
                Point minP = luckyShot.get(0);
                for(int i=1; i<3; i++){
                    if(luckyShot.get(i).x+luckyShot.get(i).y > maxP.y + maxP.x)
                        maxP = luckyShot.get(i);
                    if(luckyShot.get(i).x+luckyShot.get(i).y < minP.y + minP.x)
                        minP = luckyShot.get(i);
                }
                int dx = (maxP.x - minP.x)/2;
                int dy = (maxP.y - minP.y)/2;
                if(enemyShips[maxP.x+dx][maxP.y+dy] > 0) {
                    x = maxP.x + dx;
                    y = maxP.y + dy;
                }
                else  if(enemyShips[minP.x-dx][minP.y-dy] > 0){
                    x = minP.x-dx;
                    y = minP.y-dy;
                }
                shot.x = x;
                shot.y = y;
                if (enemyShips[shot.x][shot.y] != 5)
                    luckyShot.add(shot);
                if(enemyShips[shot.x][shot.y] == luckyShot.size())
                    luckyShot.clear();
                return shot;
            }
        }

        return shot;
    }

    private Point hardShot(Player enemy){
        int[][] enemyShips = enemy.ships();
        Point shot = null;
        if(!luckyShot.isEmpty()){
            shot = middleShot(enemy);
        }
        else{
            boolean ready = false;
            shot = tryDiagonal(enemy, true);
            if(shot!=null){
                if(enemyShips[shot.x][shot.y]>1 && enemyShips[shot.x][shot.y]<5)
                    luckyShot.add(shot);
                return shot;
            }
            else{
                shot = tryDiagonal(enemy, false);
                if(shot!=null){
                    if(enemyShips[shot.x][shot.y]>1 && enemyShips[shot.x][shot.y]<5)
                        luckyShot.add(shot);
                    return shot;
                }
                else{
                    return middleShot(enemy);
                }
            }

        }

        return shot;
    }

    private Point tryDiagonal(Player enemy, boolean firstDiag){
        boolean canMakeAShot = false;
        int j=0;
        if(firstDiag)
            j= -1;
        else
            j= 1;
        do {
            for (int i = 1; i <= 10; i++) {
                j++;
                if (j == 4) j = 0;
                int k = 1;
                do {
                    if (enemy.ships()[i][k * 4 - j % 4] > 0) {
                        canMakeAShot = true;
                        if (randomCoordinate.nextBoolean() && canMakeAShot) {
                            return new Point(i, k * 4 - j % 4);
                        }
                    }
                    k++;

                } while (k * 4 - j % 4 <= 10);
            }
        }while (canMakeAShot);

        return null;
    }

    private Point absoluteDestruction(Player enemy){
        int[][] enemyShips = enemy.ships();
        int x;
        int y;
        Point shot = null;
        //System.out.println(enemy);
        int cheat = randomCoordinate.nextInt(10);
        if (cheat == 1 && luckyShot.isEmpty()){
            boolean luck = false;
            do {
                x = 1+randomCoordinate.nextInt(10);
                y = 1+randomCoordinate.nextInt(10);
                if (enemyShips[x][y] > 0 && enemyShips[x][y] != 5){
                    shot = new Point(x,y);
                    if (enemyShips[x][y] != 1)
                        luckyShot.add(shot);
                    luck = true;
                }
            }while (!luck);
        }
        else{
            shot = middleShot(enemy);
        }

        return shot;
    }

    public void getShipsFromFile(Reader rd) throws NoSuchMethodException {
        throw new NoSuchMethodException("Данный метод пока не реализован");

    }

    public void iWasShot(Point p){
        if (myShips[p.x][p.y]<1) throw new IllegalArgumentException("Почему вы стреляете по уже стреляной клетке?");
        else myShips[p.x][p.y] = -myShips[p.x][p.y];
    }

    @Override
    public Point makeAShot(Player enemy) {
        Point p;
        switch (level){
            case 0:{
                p=easyShot(enemy);
                break;
            }
            case 1:{
                p=middleShot(enemy);
                break;
            }
            case 2:{
                p=hardShot(enemy);
                break;
            }
            case 3:{
                p=absoluteDestruction(enemy);
                break;
            }
            default: p=middleShot(enemy);
        }
        return p;
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
