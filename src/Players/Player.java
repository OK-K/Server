package Players;

import java.awt.*;
import java.io.Reader;

public interface Player {


    String getLogin();// получение логина

    void nullMatr();// обнуление матрицы. Подготовление перед рандомным расставлением

    void iWasShot(Point p);// выстрел ПО данному игроку

    Point makeAShot(Player enemy);// выстрел по ПРОТИВНИКУ

    Point makeAShot(Point p, Player enemy);// выстрел по ПРОТИВНИКУ

    int[][] ships();// получить игровое поле игрока

    void loadShips(int[][] ships);// установить игровое поле

    void myShipIsDead();// уменьшает количеств кораблей, чтобы определить момент окончания игры

    int getCountOfShips();// получение количества кораблей

    void arrangeShipsRandom();// расставить случайно корабли

    int[][] getShipsForClient();// выдача Дмитрию модернизированной матрицы
}
