package server;

import battleOnTheSea.Battle;
import battleOnTheSea.BattleManager;
import com.sun.net.httpserver.*;

import java.awt.*;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {

    private static final Map<String, String> extensions = new HashMap<String, String>(){
        {
            put("html", "text/html");
            put("css", "text/css");
            put("png", "image/png");
            put("jpg", "image/jpg");
            put("gif", "image/gif");
            put("js", "text/javascript");
            put("ico", "image/x-icon");
            put("json", "application/json");
            put("ttf", "application/x-font-ttf");

        }
    };
    public static String contentPath = "D://seaBattleServer";
    private static ArrayList<String> usersName = new ArrayList<>();
    public static ArrayList<BattleManager> games = new ArrayList<>();
    public static HttpServer server;
    public static boolean test = false;

    public static void main(String[] args) throws IOException {
        createMainMenu();
    }

    public static void createMainMenu() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Welcome to the server management panel!");
        System.out.println("Available commands:");
        System.out.println("       - start");
        System.out.println("       - stop");
        System.out.print("enter command: ");
        String choose = in.next();
        switch (choose)
        {
            case "start":
            {
                String ip = startServer();
                System.out.println("Server successfully started");
                System.out.println("server ip adress: " + ip);
                createMainMenu();
            }
            case "stop":
            {
                server.stop(3);
                createMainMenu();
            }
        }
    }

    public static String startServer() throws IOException {
        String ip_adress = Inet4Address.getLocalHost().getHostAddress();
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(ip_adress), 80), 10);
        HttpContext context = server.createContext("/", new EchoHandler());
        server.setExecutor(null);
        server.start();
        return ip_adress;
    }

    public static void processingRequest(HttpExchange exchange) throws IOException, InterruptedException {

        int start = 0;
        String url = exchange.getRequestURI().toString();
        String httpMethod = exchange.getRequestMethod();

        if (httpMethod.compareTo("GET") == 0 && url.compareTo("/") == 0)
        {
            FileInputStream fis = new FileInputStream(contentPath + "//pages//login.html");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer, 0, fis.available());
            sendResponce(exchange,buffer);
            return;
        }

        start = url.lastIndexOf('.');
        if (start > 0 && httpMethod.compareTo(("GET")) == 0)
        {
            String newUrl = url.replaceAll("/","//");
            start = newUrl.lastIndexOf('.');
            String extension = newUrl.substring(start + 1);
            if (extensions.containsKey(extension))
            {
                File fileToSend = new File(contentPath + newUrl);
                if(fileToSend.exists())
                {
                    FileInputStream fis = new FileInputStream(contentPath + newUrl);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer, 0, fis.available());
                    sendResponce(exchange,buffer);
                    return;
                }
            }
        } else
        {
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/sendLogin") == 0)
            {
                String response = "1";
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();

                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString,index);

                for(int i = 0; i < usersName.size(); i++)
                {
                    if (login.compareTo(usersName.get(i)) == 0)
                    {
                        response = "wrong";
                        sendResponce(exchange,response.getBytes());
                        return;
                    }
                }
                usersName.add(login);
                sendResponce(exchange,response.getBytes());
                return;
            }

            //принимаем запрос, где пользователь выбирает среднюю сложность
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/sendComplexity") == 0)
            {
                try {
                    InputStream inputStream = exchange.getRequestBody();
                    String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                    int index = inputStreamString.lastIndexOf("level");
                    int lvl = Integer.parseInt(Parse.getValue(inputStreamString, index));

                    index = inputStreamString.lastIndexOf("login");
                    String login = Parse.getValue(inputStreamString, index);

                    games.add(new BattleManager(login, lvl)); //создаем новую игру с компьютером с соответсвующим уровнем сложности

                    //запись кораблей в матрицы
                    int[][] shipPlayer = games.get(games.size() - 1).getPlayers()[0].getShipsForClient();
                    int[][] shipAI = games.get(games.size() - 1).getPlayers()[1].getShipsForClient();

                    //запись двух матриц с кораблями в json файл
                    Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(games.size() - 1).getPlayers()[1].getLogin());
                    String response = "1";
                    sendResponce(exchange,response.getBytes());

                }
                //ловим исключение, если произошла ошибка записи в json
                catch (IOException e)
                {
                    String response = "wrongWriteFile";
                    sendResponce(exchange,response.getBytes());
                }
                return;
            }

            //запрос о начале игры с ИИ
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/runGame") == 0)
            {
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                for (int i = 0; i < games.size(); i++)
                {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0)
                    {
                        String response = "1";
                        sendResponce(exchange,response.getBytes());
                        //games.get(i).playTheGame();
                        break;
                    }
                }


            }

            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/sendShotAI") == 0)
            {
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                index = inputStreamString.lastIndexOf("shot");
                String shot = Parse.getValue(inputStreamString,index);
                int ind = 0;

                for (int i = 0; i < games.size(); i++)
                {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0)
                    {
                        if(Battle.isFinished(games.get(i)) == 1)
                        {
                            String response = "lose=!one!";
                            games.remove(i);
                            sendResponce(exchange, response.getBytes());
                            return;
                        }

                        if(Battle.isFinished(games.get(i)) == 2)
                        {
                            String response = "lose=!two!";
                            games.remove(i);
                            sendResponce(exchange, response.getBytes());
                            return;
                        }
                        int[][] shipAI;
                        int[][] shipPlayer;
                        int count = 0;

                        shipAI = games.get(i).getPlayers()[1].getShipsForClient();
                        count = checkTurn(shipAI);

                        Point p;
                        ind = i;
                        p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot),games.get(i).getCurrentEnemy());

                        boolean isDead = Battle.checkTheField(p,games.get(i).getCurrentEnemy());

                        if (isDead) {
                            Battle.shipIsDead(games.get(i).getCurrentEnemy());
                        }

                        int newCount = 0;
                        shipAI = games.get(i).getPlayers()[1].getShipsForClient();
                        newCount = checkTurn(shipAI);


                        //если пользователь попал, то он продолжает стрелять
                        if (newCount > count && games.get(i).getTurn() == 0)
                        {
                            //запись кораблей в матрицы
                            shipPlayer = games.get(ind).getPlayers()[0].getShipsForClient();
                            shipAI = games.get(ind).getPlayers()[1].getShipsForClient();

                            //запись двух матриц с кораблями в json файл
                            Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(ind).getPlayers()[1].getLogin());
                            String response = "1";
                            sendResponce(exchange, response.getBytes());
                            return;
                        } else
                        {
                            games.get(i).nextTurn();
                            shipPlayer = games.get(ind).getPlayers()[0].getShipsForClient();
                            count = checkTurn(shipPlayer);
                            p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());

                            isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());

                            if (isDead) {
                                Battle.shipIsDead(games.get(i).getCurrentEnemy());
                            }

                            shipPlayer = games.get(ind).getPlayers()[0].getShipsForClient();
                            newCount = checkTurn(shipPlayer);
                        }
                        if (newCount > count && games.get(i).getTurn() == 1)
                        {
                            String shots = "";
                            int countShots = 0;

                            while (newCount != count)
                            {
                                count = newCount;

                                p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());
                                countShots++;
                                shots += "x" + countShots + "=!" + p.x + "!";
                                shots += "y" + countShots + "=!" + p.y + "!";
                                isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());

                                if (isDead) {
                                    Battle.shipIsDead(games.get(i).getCurrentEnemy());
                                }

                                shipPlayer = games.get(ind).getPlayers()[0].getShipsForClient();
                                newCount = checkTurn(shipPlayer);

                            }
                            games.get(i).nextTurn();
                            shipPlayer = games.get(ind).getPlayers()[0].getShipsForClient();
                            shipAI = games.get(ind).getPlayers()[1].getShipsForClient();

                            //запись двух матриц с кораблями в json файл
                            Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(ind).getPlayers()[1].getLogin());
                            String response = "1";
                            sendResponce(exchange, response.getBytes());
                            return;
                        }
                        else {
                            games.get(i).nextTurn();
                            //запись кораблей в матрицы
                            shipPlayer = games.get(ind).getPlayers()[0].getShipsForClient();
                            shipAI = games.get(ind).getPlayers()[1].getShipsForClient();

                            //запись двух матриц с кораблями в json файл
                            Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(ind).getPlayers()[1].getLogin());
                            String response = "1";
                            sendResponce(exchange, response.getBytes());
                        }
                    }
                }
            }

        }

    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                processingRequest(exchange);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendResponce(HttpExchange exchange, byte[] buffer) throws IOException {
        exchange.sendResponseHeaders(200, buffer.length);
        OutputStream os = exchange.getResponseBody();
        os.write(buffer);
        os.close();
    }

    private static int checkTurn(int[][] ships)
    {
        int count = 0;

        for (int q = 0; q < ships.length; q++)
        {
            for (int j = 0; j < ships[q].length; j++)
            {
                if (ships[q][j] == -1 || ships[q][j] == -2 || ships[q][j] == -3 || ships[q][j] == -4 || ships[q][j] == 0)
                    count++;
            }
        }
        return count;
    }


}
