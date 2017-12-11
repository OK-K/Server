package Players;

import java.awt.*;
import java.io.Reader;

public interface Player {


    String getLogin();// получение логина

    void nullMatr();// обнуление матрицы. Подготовление перед рандомным расставлением

    void iWasShot(Point p);// выстрел ПО данному игроку

    Point makeAShot(Player enemy);// выстрел по ПРОТИВНИКУ

    int[][] ships();

    void loadShips(int[][] ships);

    void myShipIsDead();

    int getCountOfShips();

    void arrangeShipsRandom();

    int[][] getShipsForClient();
}
