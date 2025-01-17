package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
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

        server.createContext("/api/game/start", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(404, 0); // Return 404 for unsupported methods
                exchange.close();
                return;
            }

            try {
                // Read the raw JSON body as a string
                String requestBody = new String(exchange.getRequestBody().readAllBytes());

                // Extract fields manually (basic parsing, assumes well-formed JSON)
                String id = requestBody.split("\"id\":\"")[1].split("\"")[0];
                String url = requestBody.split("\"url\":\"")[1].split("\"")[0];
                String message = requestBody.split("\"message\":\"")[1].split("\"")[0];

                // Create a response JSON string manually
                String responseBody = String.format(
                    "{\"id\":\"%s\",\"url\":\"http://localhost:%d\",\"message\":\"May the best code win\"}",
                    UUID.randomUUID().toString(),
                    port
                );

                // Send the response
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(202, responseBody.length());
                try (var os = exchange.getResponseBody()) {
                    os.write(responseBody.getBytes());
                }
            } catch (Exception e) {
                // Send a 400 response for bad input
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
            }
        });

        server.createContext("/api/game/fire", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            String query = exchange.getRequestURI().getQuery(); // e.g., "cell=A1"
            if (query == null || !query.startsWith("cell=")) {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
                return;
            }

            String cell = query.substring(5);
            if (!cell.matches("^[A-J](10|[1-9])$")) {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
                return;
            }

            String consequence = "miss";
            boolean shipLeft = true;

            // Build the JSON response
            String responseBody = String.format(
                "{\"consequence\":\"%s\",\"shipLeft\":%b}",
                consequence,
                shipLeft
            );

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length());
            try (var os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
            }
        });


        server.start();
        System.out.println("Server started on port " + port);

        if (args.length > 1) {
            String targetUrl = args[1];
            sendPostRequestToServer(port, targetUrl);
        }
    }

    private static void sendPostRequestToServer(int myPort, String targetUrl) {
        try {
            // Build the JSON body
            String body = String.format(
                "{\"id\":\"%s\",\"url\":\"http://localhost:%d\",\"message\":\"Hello from client!\"}",
                UUID.randomUUID().toString(),
                myPort
            );

            // Create the HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Create the POST request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl + "/api/game/start"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            // Send the request and handle the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        } catch (Exception e) {
            System.err.println("Error sending POST request: " + e.getMessage());
        }
    }
}
