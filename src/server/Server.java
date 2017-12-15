package server;

import battleOnTheSea.BattleManager;
import com.sun.net.httpserver.*;

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
    private static ArrayList<BattleManager> games = new ArrayList<>();

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
            }
            case "stop":
            {

            }
        }
    }

    public static String startServer() throws IOException {
        String ip_adress = Inet4Address.getLocalHost().getHostAddress();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(ip_adress), 80), 10);
        HttpContext context = server.createContext("/", new EchoHandler());
        server.setExecutor(null);
        server.start();
        return ip_adress;
    }

    public static void processingRequest(HttpExchange exchange) throws IOException {

        int start = 0;
        String url = exchange.getRequestURI().toString();
        //Headers head = exchange.getRequestHeaders();
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
            if (httpMethod.compareTo("POST") == 0 && url.compareTo("/sendComplexity") == 0)
            {
                try {
                    InputStream inputStream = exchange.getRequestBody();
                    String inputStreamString = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

                    int index = inputStreamString.lastIndexOf("level");
                    int lvl = Integer.parseInt(Parse.getValue(inputStreamString, index));

                    index = inputStreamString.lastIndexOf("login");
                    String login = Parse.getValue(inputStreamString, index);

                    games.add(new BattleManager(login, lvl));

                    int[][] shipPlayer = games.get(games.size() - 1).getPlayers()[0].getShipsForClient();
                    int[][] shipAI = games.get(games.size() - 1).getPlayers()[1].getShipsForClient();

                    Json.createJsonFilePlayerWithAI(shipPlayer, shipAI, login, games.get(games.size() - 1).getPlayers()[1].getLogin());
                    String response = "1";
                    sendResponce(exchange,response.getBytes());

                }
                catch (IOException e)
                {
                    String response = "wrongWriteFile";
                    sendResponce(exchange,response.getBytes());
                }

                //sendResponce(exchange,response.getBytes());
                return;
            }
        }

    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            processingRequest(exchange);
        }
    }

    private static void sendResponce(HttpExchange exchange, byte[] buffer) throws IOException {
        exchange.sendResponseHeaders(200, buffer.length);
        OutputStream os = exchange.getResponseBody();
        os.write(buffer);
        os.close();
    }


}
