package Players;

import java.awt.*;
import java.io.Reader;

public interface Player {


    String getLogin();

    void nullMatr();

    void iWasShot(Point p);

    Point makeAShot(Player enemy);

    int[][] ships();

    void loadShips(int[][] ships);

    void myShipIsDead();

    int getCountOfShips();

    void arrangeShipsRandom();

    int[][] getShipsForClient();
}
