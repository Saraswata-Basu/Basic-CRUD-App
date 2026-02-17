import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CartHandler implements HttpHandler {

    // productId -> quantity
    static Map<String, Integer> cart = new HashMap<>();

    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        // Handle cart updates via GET/POST
        if (query != null) {
            if (query.contains("add")) {
                String[] params = query.split("&");
                String id = params[0].split("=")[1];
                int qty = Integer.parseInt(params[1].split("=")[1]);
                cart.put(id, cart.getOrDefault(id, 0) + qty);
            } else if (query.contains("update")) {
                String[] params = query.split("&");
                String id = params[0].split("=")[1];
                int qty = Integer.parseInt(params[1].split("=")[1]);
                if (qty > 0) {
                    cart.put(id, qty);
                } else {
                    cart.remove(id);
                }
            } else if (query.contains("remove")) {
                String id = query.split("=")[1];
                cart.remove(id);
            }
        }

        // Serve cart page
        if (path.equals("/cart")) {
            serveCartPage(exchange);
        } else if (path.equals("/cart/api")) {
            // API endpoint for cart data (JSON)
            serveCartJSON(exchange);
        }
    }

    private void serveCartPage(HttpExchange exchange) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("web/cart.html"));
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void serveCartJSON(HttpExchange exchange) throws IOException {
        // Return cart as JSON for JavaScript
        StringBuilder json = new StringBuilder("{\"items\":[");
        
        BufferedReader br = new BufferedReader(new FileReader("data/products.csv"));
        String line;
        boolean first = true;
        
        while ((line = br.readLine()) != null) {
            if (line.startsWith("id")) continue;
            String[] p = line.split(",");
            String id = p[0];
            if (cart.containsKey(id)) {
                if (!first) json.append(",");
                json.append("{\"id\":\"").append(id)
                    .append("\",\"name\":\"").append(p[1])
                    .append("\",\"price\":").append(p[2])
                    .append(",\"quantity\":").append(cart.get(id))
                    .append("}");
                first = false;
            }
        }
        br.close();
        
        json.append("]}");
        
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}