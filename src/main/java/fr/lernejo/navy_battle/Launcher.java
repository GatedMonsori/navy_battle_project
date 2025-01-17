package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Launcher {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("First argument is the port.");
        }

        int port = Integer.parseInt(args[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(1));

        server.createContext("/ping", exchange -> {
            String body = "OK";
            exchange.sendResponseHeaders(200, body.length());
            try (var os = exchange.getResponseBody()) {
                os.write(body.getBytes());
            }
        });

        server.start();
        System.out.println("Server started on port " + port);
    }
}
