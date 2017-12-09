package server;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Server {

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

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String ulr = exchange.getRequestURI().toString();
            InputStream body = exchange.getRequestBody();


            // byte[] bytes = builder.toString().getBytes();
            // exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            // os.write(bytes);
            os.close();
        }
    }
}
