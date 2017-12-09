package random;

import java.util.Random;

public class AdmiralRandom {
    private static Random randomCoordinate;
    private int[][] myShips = new int[12][12];

    public AdmiralRandom(){
        randomCoordinate = new Random();
        for (int i=0; i<12; i++)
            for (int j=0; j<12; j++) {
                if (i == 0 || j==0 || i==11 || j==11)
                    myShips[i][j] = -5;
                else
                    myShips[i][j] = 0;
            }
    }

    public int[][] randomArrangement(){
        for (int i=0; i<12; i++)
            for (int j=0; j<12; j++) {
                if (i == 0 || j==0 || i==11 || j==11)
                    myShips[i][j] = -5;
                else
                    myShips[i][j] = 0;
            }

        arrange4();
        for (int i=0; i<3; i++) arrange2();
        for (int i=0; i<2; i++) arrange3();
        for (int i=0; i<4; i++) arrange1();

        for (int i=1; i<11; i++)
            for (int j=1; j<11; j++) {
                if (myShips[i][j] == 0)
                    myShips[i][j] = 5;
            }
        return myShips;
    }

    public int[][] specialArrangement(){
        int corner = 1 + randomCoordinate.nextInt(4);
        boolean v = randomCoordinate.nextBoolean();
        int dx = 0;
        int dy=0;
        int sdx = 0;
        int sdy = 0;
        switch (corner){
            case 1:{
                if(v){
                    dx=1;
                    sdy =1;
                }
                else {
                    dy = 1;
                    sdx = 1;
                }
                if(randomCoordinate.nextBoolean())
                    fillBigShips1(1,1,dx,dy,sdx,sdy);
                else
                    fillBigShips2(1,1,dx,dy,sdx,sdy);
                break;
            }
            case 2:{
                if(v){
                    dx=1;
                    sdy =-1;
                }
                else{
                    dy=-1;
                    sdx = 1;
                }
                if(randomCoordinate.nextBoolean())
                    fillBigShips1(1,10,dx,dy,sdx,sdy);
                else
                    fillBigShips2(1,10,dx,dy,sdx,sdy);
                break;
            }
            case 3:{
                if(v){
                    dx=-1;
                    sdy = -1;
                }
                else{
                    dy=-1;
                    sdx=-1;
                }
                if(randomCoordinate.nextBoolean())
                    fillBigShips1(10,10,dx,dy,sdx,sdy);
                else
                    fillBigShips2(10,10,dx,dy,sdx,sdy);
                break;
            }
            case 4:{
                if(v){
                    dx=-1;
                    sdy=1;
                }
                else{
                    dy=1;
                    sdx=-1;
                }
                if(randomCoordinate.nextBoolean())
                    fillBigShips1(10,1,dx,dy,sdx,sdy);
                else
                    fillBigShips2(10,1,dx,dy,sdx,sdy);
                break;
            }
        }
        for (int i=0; i<4; i++) arrange1();

        for (int i=1; i<11; i++)
            for (int j=1; j<11; j++) {
                if (myShips[i][j] == 0)
                    myShips[i][j] = 5;
            }

        return myShips;
    }

    private void fillBigShips1(int x, int y, int dx, int dy, int sdx, int sdy){
        for(int i=0; i<4; i++){
            for(int j=0;j<10; j++){
                if(i==0 && j<4) myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 4;
                else if((i==0 && j!=4 && j!=7) || (i==2 && j>7)) myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 2;
                else if(i==2 && j!=3 && j!=7) myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 3;
                else myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 5;

            }
        }
    }

    private void fillBigShips2(int x, int y, int dx, int dy, int sdx, int sdy){
        for(int i=0; i<4; i++){
            for(int j=0;j<10; j++){
                if(i==0 && j<4) myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 4;
                else if(i==0 && j!=4 && j!=7) myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 2;
                else if((i==2 && j>7)) myShips[x+9*sdx+j*dx][y+9*sdy+j*dy] = 2;
                else if(i==2 && j!=3 && j!=7) myShips[x+9*sdx+j*dx][y+9*sdy+j*dy] = 3;
                else if(i==2 && (j==3 || j==7)) myShips[x+9*sdx+j*dx][y+9*sdy+j*dy] = 5;
                else if(i==3) myShips[x+8*sdx+j*dx][y+8*sdy+j*dy] = 5;
                else myShips[x+i*sdx+j*dx][y+i*sdy+j*dy] = 5;

            }
        }

    }

    private void arrange4(){
        int x= 1+randomCoordinate.nextInt(10);
        int y= 1+randomCoordinate.nextInt(10);
        boolean v= randomCoordinate.nextBoolean();
        myShips[x][y]=4;
        if (v){
            if(10-x > x-1){
                myShips[x+1][y]=4;
                myShips[x+2][y]=4;
                myShips[x+3][y]=4;
            }
            else{
                myShips[x-1][y]=4;
                myShips[x-2][y]=4;
                myShips[x-3][y]=4;
            }
        }
        else{
            if(10-y > y-1){
                myShips[x][y+1]=4;
                myShips[x][y+2]=4;
                myShips[x][y+3]=4;
            }
            else{
                myShips[x][y-1]=4;
                myShips[x][y-2]=4;
                myShips[x][y-3]=4;
            }
        }


        fill(4);
    }

    private void arrange1(){
        int x;
        int y;
        do{
            x= 1+randomCoordinate.nextInt(10);
            y= 1+randomCoordinate.nextInt(10);
        }while (myShips[x][y]!=0);
        myShips[x][y] = 1;

        fill(1);
    }

    private void arrange3(){
        int x;
        int y;
        boolean v=false;
        boolean h=false;
        boolean accept=false;
        do{
            x= 1+randomCoordinate.nextInt(10);
            y= 1+randomCoordinate.nextInt(10);
            if(myShips[x-1][y] == 0 && myShips[x+1][y] == 0){
                v=true;
                accept=true;
            }
            if(myShips[x][y-1] == 0 && myShips[x][y+1] == 0){
                h=true;
                accept = true;
            }
            if(v&&h) v=randomCoordinate.nextBoolean();

        }while (!accept);

        if(v){
            myShips[x][y] = 3;
            myShips[x-1][y] = 3;
            myShips[x+1][y] = 3;
        }
        else{
            myShips[x][y] = 3;
            myShips[x][y-1] = 3;
            myShips[x][y+1] = 3;
        }


        fill(3);

    }

    private void arrange2(){
        int x;
        int y;
        boolean v = true;
        boolean accept = false;
        do{
            x= 1+randomCoordinate.nextInt(9);
            y= 1+randomCoordinate.nextInt(9);
            v=randomCoordinate.nextBoolean();
            if(v){
                if(myShips[x][y] == 0 && myShips[x+1][y] == 0) accept = true;
            }
            else
                if(myShips[x][y] == 0 && myShips[x][y+1] == 0) accept = true;
        }while (!accept);
        myShips[x][y]=2;
        if(v)
            myShips[x+1][y] = 2;
        else
            myShips[x][y+1] = 2;
        fill(2);
    }

    private void fill(int num){
        for (int i=1; i<=10; i++)
            for(int j=1; j<=10;j++) {
                if (myShips[i][j - 1] == num || myShips[i - 1][j] == num || myShips[i][j + 1] == num || myShips[i + 1][j] == num ||
                    myShips[i + 1][j + 1] == num||myShips[i + 1][j - 1] == num || myShips[i - 1][j - 1] == num||myShips[i - 1][j + 1] == num) {

                    if(myShips[i][j] == 0) myShips[i][j] = 5;
                }
            }

    }

    @Override
    public String toString() {
        String res = "";
        for (int i=0; i<=11; i++) {
            for (int j = 0; j <= 11; j++) {
                if (myShips[i][j] >= 0) res += " "+myShips[i][j] + " ";
                else res += myShips[i][j] + " ";
            }
            res+="\n";
        }
        return res;
    }

}
