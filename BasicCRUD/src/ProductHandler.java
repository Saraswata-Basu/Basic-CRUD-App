import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ProductHandler implements HttpHandler {
    private void serveProductsJSON(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("{\"products\":[");
        
        BufferedReader br = new BufferedReader(new FileReader("data/products.csv"));
        String line;
        boolean first = true;
        
        while ((line = br.readLine()) != null) {
            if (line.startsWith("id")) continue;
            String[] p = line.split(",");
            if (!first) json.append(",");
            json.append("{\"id\":\"").append(p[0])
                .append("\",\"name\":\"").append(p[1])
                .append("\",\"price\":").append(p[2])
                .append(",\"stock\":").append(p[3])
                .append("}");
            first = false;
        }
        br.close();
        
        json.append("]}");
        
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    public void handle(HttpExchange exchange) throws IOException {
        if (SessionManager.loggedInUser == null) {
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        
        if (path.equals("/products/api")) {
            serveProductsJSON(exchange);
        } else {
            byte[] bytes = Files.readAllBytes(Paths.get("web/products.html"));
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }

}