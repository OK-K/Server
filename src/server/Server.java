package server;

import battleOnTheSea.Battle;
import battleOnTheSea.BattleManager;
import com.sun.net.httpserver.*;

import java.awt.*;
import java.io.*;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {

    //доступные расширения файлов, которые сервер поддерживает для отправки
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

    //полный путь к файлам для отправки на клиент
    public static String contentPath = "D://seaBattleServer";

    //динамический массив с именами авторизованных игроков
    private static ArrayList<String> usersName = new ArrayList<>();

    //динамический массив с текущими играми
    private static ArrayList<BattleManager> games = new ArrayList<>();

    //объект встроенного http сервера
    private static HttpServer server;

    //динамический массив игроков, которые ожидают игру с другими игроками
    private static  ArrayList<String> waitingPlayers = new ArrayList<>();

    //точка входа в приложение
    public static void main(String[] args) throws IOException {
        createMainMenu();
    }

    //статический метод создания консольного меню приложения
    private static void createMainMenu() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Welcome to the server management panel!");
        System.out.println("Available commands:");
        System.out.println("       - start");
        System.out.println("       - stop");
        System.out.println("       - users");
        System.out.println("       - games");
        System.out.print("enter command: ");
        String choose = in.next();

        try {
            switch (choose) {
                case "start": {
                    String ip = startServer();
                    System.out.println("Server successfully started");
                    System.out.println("server ip address: " + ip);
                    createMainMenu();
                }
                case "stop": {
                    server.stop(2);
                    System.out.println("Server successfully stopped");
                    createMainMenu();
                }
                case "users": {
                    System.out.println("Authorized Users: " + usersName.size());
                    createMainMenu();
                }
                case "games": {
                    System.out.println("Current games : " + games.size());
                    createMainMenu();
                }

            }
        } catch (NullPointerException e)
        {
            System.out.println("Server not started!");
            createMainMenu();
        } catch (BindException e)
        {
            System.out.println("Server already started!");
            createMainMenu();
        }
    }

    //статический метод запуска сервера с ip адресом в локальной сети
    private static String startServer() throws IOException {
        String ip_adress = Inet4Address.getLocalHost().getHostAddress();
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(ip_adress), 80), 10);
        HttpContext context = server.createContext("/", new EchoHandler());
        server.setExecutor(null);
        server.start();
        return ip_adress;
    }

    //статический метод обработки запросов с клиента
    private static void processingRequest(HttpExchange exchange) throws IOException, InterruptedException {

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
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/changeShips") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                //ищем нужную нам игру
                for (int i = 0; i < games.size(); i++)
                {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0)
                    {
                        String response = "1";
                        sendResponce(exchange,response.getBytes());
                        games.get(i).getPlayers()[0].arrangeShipsRandom();
                        int[][] shipPlayer = games.get(games.size() - 1).getPlayers()[0].getShipsForClient();
                        int[][] shipAI = games.get(games.size() - 1).getPlayers()[1].getShipsForClient();

                        //запись двух матриц с кораблями в json файл
                        Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(games.size() - 1).getPlayers()[1].getLogin());
                        response = "1";
                        sendResponce(exchange,response.getBytes());
                        break;
                    }
                }
            }
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


            //запрос о проверки файла сохраненной игры
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/checkSave") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                File myFile = new File(Server.contentPath + "//json","save_" + login +".json");

                int[][] newShips = new int[10][10];
                int[][] newShipsAI = new int[10][10];
                if(myFile.exists()) {
                    FileReader fr = new FileReader(myFile);
                    BufferedReader br = new BufferedReader(fr);

                    String ship = br.readLine();
                    ship = br.readLine();
                    ship = br.readLine();

                    for (int i = 0; i < 10; i++)
                    {
                        for (int j = 0; j < 10; j++)
                        {
                            index = ship.indexOf(i + "_" + j);
                            int deck = Integer.parseInt(Parse.getValue(ship, index));
                            newShips[i][j] = deck;
                        }
                    }

                    index = ship.indexOf("lvl");
                    int lvl = Integer.parseInt(Parse.getValue(ship, index));



                    for (int i = 0; i < 10; i++)
                    {
                        for (int j = 0; j < 10; j++)
                        {
                            index = ship.lastIndexOf(i + "_" + j);
                            int deck = Integer.parseInt(Parse.getValue(ship, index));
                            newShipsAI[i][j] = deck;
                        }
                    }



                    games.add(new BattleManager(login, lvl));
                    games.get(games.size() - 1).getPlayers()[0].loadShips(newShips);
                    games.get(games.size() - 1).getPlayers()[1].loadShips(newShipsAI);

                    String response = "1";
                    sendResponce(exchange,response.getBytes());
                } else
                {
                    String response = "2";
                    sendResponce(exchange,response.getBytes());
                }
            }
            //запрос о сохранении игры с ИИ
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/saveGame") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                //ищем нужную нам игру
                for (int i = 0; i < games.size(); i++)
                {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0) {
                        int[][] shipEnemy;
                        int[][] shipPlayer;
                        shipEnemy = games.get(i).getPlayers()[1].getShipsForClient();
                        shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                        int lvl = 0;
                        String nameAI = games.get(i).getPlayers()[1].getLogin();
                        if (nameAI.compareTo("Вице-адмирал Трайон") == 0)
                            lvl = 1;
                        if (nameAI.compareTo("Адмирал Микель де Рюйтер") == 0)
                            lvl = 2;
                        if (nameAI.compareTo("Адмирал Павел Нахимов") == 0)
                            lvl = 3;
                        Json.saveJsonFilePlayerWithAI(shipPlayer,shipEnemy,login,games.get(i).getPlayers()[1].getLogin(),lvl);
                        String response = "1";
                        sendResponce(exchange,response.getBytes());
                    }
                }
            }
            //запрос о начале игры с ИИ и отсылка, кто первый должен ходить
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/runGame") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                //ищем нужную нам игру
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

            //запрос о начале игры с другим игроком и отсылка, кто первый должен ходить
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/runGameWithPlayer") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                int count = 0;
                for (int i = 0; i < waitingPlayers.size(); i++)
                {
                    if (waitingPlayers.get(i).compareTo(login) == 0)
                        count++;
                }
                if (count > 0) {
                    count = 0;
                } else  waitingPlayers.add(login);
                //ищем нужную нам игру
                for (int i = 0; i < games.size(); i++)
                {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0)
                    {
                        for (int j = 0; j < waitingPlayers.size(); j++)
                        {
                            if (waitingPlayers.get(j).compareTo(games.get(i).getPlayers()[1].getLogin()) == 0)
                            {
                                //waitingPlayers.remove(waitingPlayers.get(waitingPlayers.size() - 1));
                                //waitingPlayers.remove(j);
                                String response = "1";
                                sendResponce(exchange,response.getBytes());
                            }
                        }

                        //games.get(i).playTheGame();

                    } else if (games.get(i).getPlayers()[1].getLogin().compareTo(login) == 0)
                    {
                        for (int j = 0; j < waitingPlayers.size(); j++)
                        {
                            if (waitingPlayers.get(j).compareTo(games.get(i).getPlayers()[0].getLogin()) == 0)
                            {
                                //waitingPlayers.remove(waitingPlayers.get(waitingPlayers.size() - 1));
                                //waitingPlayers.remove(j);
                                String response = "1";
                                sendResponce(exchange,response.getBytes());
                            }
                        }
                    }
                }


            }

            //получание запроса о текущем ходе - кто должен ходить
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/getTurn") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                for (int i = 0; i < games.size(); i++) {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0) {
                        int turn = games.get(i).getTurn();
                        if (turn == 0)
                        {
                            String response = "0";
                            sendResponce(exchange, response.getBytes());
                            return;
                        } else {String response = "1";
                            sendResponce(exchange, response.getBytes());
                            return;}
                    } else  if (games.get(i).getPlayers()[1].getLogin().compareTo(login) == 0) {
                        int turn = games.get(i).getTurn();
                        if (turn == 1)
                        {
                            String response = "0";
                            sendResponce(exchange, response.getBytes());
                            return;
                        } else {String response = "1";
                            sendResponce(exchange, response.getBytes());
                            return;}
                    }

                }
            }


            //получение запроса с выстрелом игрока при игре с другим игроком
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/sendShotPlayer") == 0) {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                //записываем координату выстрела в строку
                index = inputStreamString.lastIndexOf("shot");
                String shot = Parse.getValue(inputStreamString, index);

                for (int i = 0; i < games.size(); i++) {
                    try {
                        if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0) {
                            int[][] shipEnemy;
                            int[][] shipPlayer;
                            Point p;

                            //получаем количество убитых/ранненых кораблей противника до выстрела пользователя
                            int count = 0;
                            shipEnemy = games.get(i).getPlayers()[1].getShipsForClient();
                            count = checkTurn(shipEnemy);

                            //выстрел пользователя
                            p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());

                            //проверяем, убит ли корабль после выстрела
                            boolean isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());
                            if (isDead) {
                                Battle.shipIsDead(games.get(i).getCurrentEnemy());
                            }

                            //получаем количество убитых/ранненых кораблей противника после выстрела пользователя
                            int newCount = 0;
                            shipEnemy = games.get(i).getPlayers()[1].getShipsForClient();
                            newCount = checkTurn(shipEnemy);

                            //если пользователь попал, то он продолжает стрелять
                            if (newCount > count && games.get(i).getTurn() == 0) {
                                //запись кораблей в матрицы
                                shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                                shipEnemy = games.get(i).getPlayers()[1].getShipsForClient();

                                //запись двух матриц с кораблями в json файл
                                Json.createJsonFilePlayerWithAI(shipPlayer, shipEnemy, login, games.get(i).getPlayers()[1].getLogin());
                                Json.createJsonFilePlayerWithAI(shipEnemy, shipPlayer, games.get(i).getPlayers()[1].getLogin(), login);
                                String response = "1";

                                //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                                if (Battle.isFinished(games.get(i)) == 1) {
                                    response = "lose=!one!";
                                    games.remove(i);
                                    sendResponce(exchange, response.getBytes());
                                    return;
                                }

                                //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                                if (Battle.isFinished(games.get(i)) == 2) {
                                    response = "lose=!two!";
                                    games.remove(i);
                                    sendResponce(exchange, response.getBytes());
                                    return;
                                }
                                sendResponce(exchange, response.getBytes());
                                return;
                            }
                            //если пользователь промахнулся, то стреляет другой игрок
                            else {

                                //запись кораблей в матрицы
                                shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                                shipEnemy = games.get(i).getPlayers()[1].getShipsForClient();

                                //запись двух матриц с кораблями в json файл
                                Json.createJsonFilePlayerWithAI(shipPlayer, shipEnemy, login, games.get(i).getPlayers()[1].getLogin());
                                Json.createJsonFilePlayerWithAI(shipEnemy, shipPlayer, games.get(i).getPlayers()[1].getLogin(), login);

                                //меняем очередность хода
                                games.get(i).nextTurn();
                                String response = "2";
                                sendResponce(exchange, response.getBytes());
                                return;
                            }

                        }


                        if (games.get(i).getPlayers()[1].getLogin().compareTo(login) == 0) {
                            int[][] shipEnemy;
                            int[][] shipPlayer;
                            Point p;

                            //получаем количество убитых/ранненых кораблей противника до выстрела пользователя
                            int count = 0;
                            shipEnemy = games.get(i).getPlayers()[0].getShipsForClient();
                            count = checkTurn(shipEnemy);

                            //выстрел пользователя
                            p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());

                            //проверяем, убит ли корабль после выстрела
                            boolean isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());
                            if (isDead) {
                                Battle.shipIsDead(games.get(i).getCurrentEnemy());
                            }

                            //получаем количество убитых/ранненых кораблей противника после выстрела пользователя
                            int newCount = 0;
                            shipEnemy = games.get(i).getPlayers()[0].getShipsForClient();
                            newCount = checkTurn(shipEnemy);

                            //если пользователь попал, то он продолжает стрелять
                            if (newCount > count && games.get(i).getTurn() == 1) {
                                //запись кораблей в матрицы
                                shipPlayer = games.get(i).getPlayers()[1].getShipsForClient();
                                shipEnemy = games.get(i).getPlayers()[0].getShipsForClient();

                                //запись двух матриц с кораблями в json файл
                                Json.createJsonFilePlayerWithAI(shipPlayer, shipEnemy, login, games.get(i).getPlayers()[0].getLogin());
                                Json.createJsonFilePlayerWithAI(shipEnemy, shipPlayer, games.get(i).getPlayers()[0].getLogin(), login);
                                String response = "1";

                                //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                                if (Battle.isFinished(games.get(i)) == 1) {
                                    response = "lose=!one!";
                                    games.remove(i);
                                    sendResponce(exchange, response.getBytes());
                                    return;
                                }

                                //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                                if (Battle.isFinished(games.get(i)) == 2) {
                                    response = "lose=!two!";
                                    games.remove(i);
                                    sendResponce(exchange, response.getBytes());
                                    return;
                                }
                                sendResponce(exchange, response.getBytes());
                                return;
                            }
                            //если пользователь промахнулся, то стреляет ИИ
                            else {
                                //меняем очередность хода

                                //запись кораблей в матрицы
                                shipPlayer = games.get(i).getPlayers()[1].getShipsForClient();
                                shipEnemy = games.get(i).getPlayers()[0].getShipsForClient();

                                //запись двух матриц с кораблями в json файл
                                Json.createJsonFilePlayerWithAI(shipPlayer, shipEnemy, login, games.get(i).getPlayers()[0].getLogin());
                                Json.createJsonFilePlayerWithAI(shipEnemy, shipPlayer, games.get(i).getPlayers()[0].getLogin(), login);
                                games.get(i).nextTurn();
                                String response = "2";
                                sendResponce(exchange, response.getBytes());
                                return;
                            }

                        }


                    } catch (IllegalArgumentException e)
                    {
                        String response = "same";
                        sendResponce(exchange, response.getBytes());
                    }
                }
            }

            //получение запроса с выстрелом пользователя при игре с ИИ
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/sendShotAI") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                //записываем координату выстрела в строку
                index = inputStreamString.lastIndexOf("shot");
                String shot = Parse.getValue(inputStreamString,index);


                for (int i = 0; i < games.size(); i++) {
                    try {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0) {


                        int[][] shipAI;
                        int[][] shipPlayer;
                        Point p;

                        //получаем количество убитых/ранненых кораблей ИИ до выстрела пользователя
                        int count = 0;
                        shipAI = games.get(i).getPlayers()[1].getShipsForClient();
                        count = checkTurn(shipAI);

                        //выстрел пользователя
                        p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());

                        //проверяем, убит ли корабль после выстрела
                        boolean isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());
                        if (isDead) {
                            Battle.shipIsDead(games.get(i).getCurrentEnemy());
                        }
                        //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                        if (Battle.isFinished(games.get(i)) == 1) {
                            String response = "lose=!one!";
                            games.remove(i);
                            sendResponce(exchange, response.getBytes());
                            return;
                        }

                        //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                        if (Battle.isFinished(games.get(i)) == 2) {
                            String response = "lose=!two!";
                            games.remove(i);
                            sendResponce(exchange, response.getBytes());
                            return;
                        }
                        //получаем количество убитых/ранненых кораблей ИИ после выстрела пользователя
                        int newCount = 0;
                        shipAI = games.get(i).getPlayers()[1].getShipsForClient();
                        newCount = checkTurn(shipAI);


                        //если пользователь попал, то он продолжает стрелять
                        if (newCount > count && games.get(i).getTurn() == 0) {
                            //запись кораблей в матрицы
                            shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                            shipAI = games.get(i).getPlayers()[1].getShipsForClient();

                            //запись двух матриц с кораблями в json файл
                            Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(i).getPlayers()[1].getLogin());
                            String response = "1";
                            //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                            if (Battle.isFinished(games.get(i)) == 1) {
                                response = "lose=!one!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }

                            //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                            if (Battle.isFinished(games.get(i)) == 2) {
                                response = "lose=!two!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }
                            sendResponce(exchange, response.getBytes());
                            return;
                        }
                        //если пользователь промахнулся, то стреляет ИИ
                        else {
                            //меняем очередность хода
                            games.get(i).nextTurn();

                            //получаем количество убитых/ранненых кораблей пользователя до выстрела ИИ
                            shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                            count = checkTurn(shipPlayer);

                            //выстрел ИИ
                            p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());

                            //проверяем, убит ли корабль после выстрела
                            isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());
                            //если да, то он заполняется нулями
                            if (isDead) {
                                Battle.shipIsDead(games.get(i).getCurrentEnemy());
                            }
                            //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                            if (Battle.isFinished(games.get(i)) == 1) {
                                String response = "lose=!one!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }

                            //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                            if (Battle.isFinished(games.get(i)) == 2) {
                                String response = "lose=!two!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }
                            //получаем количество убитых/ранненых кораблей пользователя после выстрела ИИ
                            shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                            newCount = checkTurn(shipPlayer);
                        }

                        //если ИИ попал, то он продолжает стрелять
                        if (newCount > count && games.get(i).getTurn() == 1) {
                            //в эту строку будут записываться координаты выстрелов ИИ
                            String shots = "";
                            int countShots = 0;

                            //ИИ стреляет до момента, пока количество убитых/раненных кораблей до выстрела не совпадает с их количеством после выстрела
                            while (newCount != count) {
                                count = newCount;

                                //выстрел ИИ
                                p = games.get(i).getCurrentPlayer().makeAShot(Parse.getShotPoint(shot), games.get(i).getCurrentEnemy());

                                //добавляем в строку координаты выстрела
                                countShots++;
                                shots += "x" + countShots + "=!" + p.x + "!";
                                shots += "y" + countShots + "=!" + p.y + "!";

                                //проверяем, убит ли корабль после выстрела
                                isDead = Battle.checkTheField(p, games.get(i).getCurrentEnemy());
                                //если да, то он заполняется нулями
                                if (isDead) {
                                    Battle.shipIsDead(games.get(i).getCurrentEnemy());
                                }

                                //получаем количество убитых/ранненых кораблей пользователя после выстрела ИИ
                                shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                                newCount = checkTurn(shipPlayer);

                            }

                            //меняем очередность хода
                            games.get(i).nextTurn();

                            //получение матриц текущего хода
                            shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                            shipAI = games.get(i).getPlayers()[1].getShipsForClient();

                            //запись двух матриц с кораблями текущего хода в json файл
                            Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(i).getPlayers()[1].getLogin());
                            String response = "1";
                            //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                            if (Battle.isFinished(games.get(i)) == 1) {
                                response = "lose=!one!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }

                            //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                            if (Battle.isFinished(games.get(i)) == 2) {
                                response = "lose=!two!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }
                            sendResponce(exchange, response.getBytes());
                            return;
                        }

                        //если ИИ промахнулся
                        else {

                            //меняем очередность хода
                            games.get(i).nextTurn();

                            //получение матриц текущего хода
                            shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                            shipAI = games.get(i).getPlayers()[1].getShipsForClient();

                            //запись двух матриц с кораблями текущего хода в json файл
                            Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(i).getPlayers()[1].getLogin());
                            String response = "1";
                            //проверяем, закончилась ли игра и проиграл ли в таком случае первый игрок
                            if (Battle.isFinished(games.get(i)) == 1) {
                                response = "lose=!one!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }

                            //проверям, закончилась ли игра и проиграл ли в таком случае второй игрок
                            if (Battle.isFinished(games.get(i)) == 2) {
                                response = "lose=!two!";
                                games.remove(i);
                                sendResponce(exchange, response.getBytes());
                                return;
                            }
                            sendResponce(exchange, response.getBytes());
                        }
                    }
                } catch (IllegalArgumentException e)
                    {
                        String response = "same";
                        sendResponce(exchange, response.getBytes());
                    }
                    catch (IOException e)
                    {
                        String response = "errorJson";
                        sendResponce(exchange, response.getBytes());
                    }
                }
            }

            //запрос изменения расстановки кораблей вручную
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/changeShipByPlayer") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                int[][] newShips = new int[10][10];
                int[][] shipAI;
                int[][] shipPlayer;
                for (int i = 0; i < 10; i++)
                {
                    for (int j = 0; j < 10; j++)
                    {
                        index = inputStreamString.indexOf(i + "_" + j);
                        int deck = Integer.parseInt(Parse.getValue(inputStreamString, index));
                        newShips[i][j] = deck;
                    }
                }
                for (int i = 0; i < games.size(); i++) {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0) {
                        games.get(i).getPlayers()[0].loadShips(newShips);
                        //получение матриц текущего хода
                        shipPlayer = games.get(i).getPlayers()[0].getShipsForClient();
                        shipAI = games.get(i).getPlayers()[1].getShipsForClient();

                        //запись двух матриц с кораблями текущего хода в json файл
                        Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(i).getPlayers()[1].getLogin());
                        String response = "1";
                        sendResponce(exchange, response.getBytes());
                        break;
                    } else if (games.get(i).getPlayers()[1].getLogin().compareTo(login) == 0)
                    {
                        games.get(i).getPlayers()[1].loadShips(newShips);

                        //получение матриц текущего хода
                        shipPlayer = games.get(i).getPlayers()[1].getShipsForClient();
                        shipAI = games.get(i).getPlayers()[0].getShipsForClient();

                        //запись двух матриц с кораблями текущего хода в json файл
                        Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(i).getPlayers()[1].getLogin());
                        String response = "1";
                        sendResponce(exchange, response.getBytes());
                        break;
                    }
                }


            }

            //запрос от пользователя, который выбрал игру с пользователем и ожидает его
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/startWaitGame") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                for (int i = 0; i < waitingPlayers.size(); i++)
                    if (waitingPlayers.get(i).compareTo(login) == 0 ) {
                        String response = "1";
                        sendResponce(exchange, response.getBytes());
                        return;
                    }

                waitingPlayers.add(login);


                String response = "1";
                sendResponce(exchange, response.getBytes());

            }

            //запрос от пользователя о попытке начать игру с другим пользователем
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/startPlayerGame") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);
                for (int i = 0; i < games.size(); i++) {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0) {
                        //если другой пользователь уже создал с этим пользователем игру

                        int[][] shipPlayer = games.get(games.size() - 1).getPlayers()[0].getShipsForClient();
                        int[][] shipPlayer2 = games.get(games.size() - 1).getPlayers()[1].getShipsForClient();
                        Json.createJsonFilePlayerWithAI(shipPlayer, shipPlayer2, login, games.get(games.size() - 1).getPlayers()[0].getLogin());
                        String response = "1";
                        sendResponce(exchange, response.getBytes());
                        return;
                    } else if (games.get(i).getPlayers()[1].getLogin().compareTo(login) == 0)
                    {
                        int[][] shipPlayer = games.get(games.size() - 1).getPlayers()[1].getShipsForClient();
                        int[][] shipPlayer2 = games.get(games.size() - 1).getPlayers()[0].getShipsForClient();
                        Json.createJsonFilePlayerWithAI(shipPlayer, shipPlayer2, login, games.get(games.size() - 1).getPlayers()[1].getLogin());
                        String response = "1";
                        sendResponce(exchange, response.getBytes());
                        return;
                    }
                }

                if (waitingPlayers.size() > 1)
                {
                    int indexlogin = 0;
                    for (int i = 0; i < waitingPlayers.size(); i++)
                        if (waitingPlayers.get(i).compareTo(login) != 0 )
                            indexlogin = i;
                    games.add(new BattleManager(login,waitingPlayers.get(indexlogin))); //создаем новую игру с другим игроком
                    for (int i = 0; i < waitingPlayers.size(); i++)
                        if (waitingPlayers.get(i).compareTo(login) == 0)
                        {
                            if (i > indexlogin)
                            {
                                waitingPlayers.remove(i);
                                waitingPlayers.remove(indexlogin);
                            }
                            else
                            {
                                waitingPlayers.remove(indexlogin);
                                waitingPlayers.remove(i);
                            }
                        }
                    //запись кораблей в матрицы
                    int[][] shipPlayer1 = games.get(games.size() - 1).getPlayers()[0].getShipsForClient();
                    int[][] shipPlayer2 = games.get(games.size() - 1).getPlayers()[1].getShipsForClient();

                    //запись двух матриц с кораблями в json файл
                    Json.createJsonFilePlayerWithAI(shipPlayer1, shipPlayer2, login, games.get(games.size() - 1).getPlayers()[1].getLogin());
                    String response = "1";
                    sendResponce(exchange, response.getBytes());
                } else
                {
                    String response = "0";
                    sendResponce(exchange, response.getBytes());
                }
            }
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/clearWaiting") == 0)
            {
                //записываем в строку пришедший запрос
                InputStream inputStream = exchange.getRequestBody();
                String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                //узнаем логин игрока
                int index = inputStreamString.lastIndexOf("login");
                String login = Parse.getValue(inputStreamString, index);

                for (int i = 0; i < games.size(); i++)
                {
                    if (games.get(i).getPlayers()[0].getLogin().compareTo(login) == 0)
                    {
                        for (int j = 0; j < waitingPlayers.size(); j++)
                        {
                            if (waitingPlayers.get(j).compareTo(games.get(i).getPlayers()[1].getLogin()) == 0)
                            {
                                for (int q = 0; q < waitingPlayers.size(); q++) {
                                    if (waitingPlayers.get(q).compareTo(games.get(i).getPlayers()[0].getLogin()) == 0) {
                                        if (q > j) {
                                            waitingPlayers.remove(q);
                                            waitingPlayers.remove(j);
                                        } else {
                                            waitingPlayers.remove(j);
                                            waitingPlayers.remove(q);
                                        }
                                    }
                                }
                                String response = "1";
                                sendResponce(exchange,response.getBytes());
                            }
                        }

                        //games.get(i).playTheGame();

                    } else if (games.get(i).getPlayers()[1].getLogin().compareTo(login) == 0)
                    {
                        for (int j = 0; j < waitingPlayers.size(); j++)
                        {
                            if (waitingPlayers.get(j).compareTo(games.get(i).getPlayers()[0].getLogin()) == 0)
                            {
                                for (int q = 0; q < waitingPlayers.size(); q++) {
                                    if (waitingPlayers.get(q).compareTo(games.get(i).getPlayers()[1].getLogin()) == 0) {
                                        if (q > j) {
                                            waitingPlayers.remove(q);
                                            waitingPlayers.remove(j);
                                        } else {
                                            waitingPlayers.remove(j);
                                            waitingPlayers.remove(q);
                                        }
                                    }
                                }
                                String response = "1";
                                sendResponce(exchange,response.getBytes());
                            }
                        }
                    }
                }
            }

        }

    }

    //слушатель запросов с клиента
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

    //статический метод, отправляющий запрос на клиент
    private static void sendResponce(HttpExchange exchange, byte[] buffer) throws IOException {
        exchange.sendResponseHeaders(200, buffer.length);
        OutputStream os = exchange.getResponseBody();
        os.write(buffer);
        os.close();
    }

    //статический метод, подсчитывающий количество раннеых/убитых кораблей
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
