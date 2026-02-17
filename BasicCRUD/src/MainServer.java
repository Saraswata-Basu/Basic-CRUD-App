import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new CustomerHandler());
        server.createContext("/register", new CustomerHandler());
        server.createContext("/login", new CustomerHandler());
        server.createContext("/products", new ProductHandler());
        server.createContext("/cart", new CartHandler());
        server.createContext("/payment", new OrderHandler());
        server.createContext("/confirm", new OrderHandler());
        server.createContext("/bill.txt", new OrderHandler());
        
        // Serve static files (CSS, JS)
        server.createContext("/css/", new StaticFileHandler("web/css/"));
        server.createContext("/js/", new StaticFileHandler("web/js/"));
        server.createContext("/style/", new StaticFileHandler("web/style/"));

        server.setExecutor(null);
        server.start();

        System.out.println("Server started at http://localhost:8080");
    }
}

// Add this handler class
class StaticFileHandler implements HttpHandler {
    private String basePath;
    
    public StaticFileHandler(String basePath) {
        this.basePath = basePath;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        String path = exchange.getRequestURI().getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        String filePath = basePath + fileName;
        
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String contentType = "text/plain";
            if (fileName.endsWith(".css")) contentType = "text/css";
            else if (fileName.endsWith(".js")) contentType = "application/javascript";
            
            exchange.getResponseHeaders().add("Content-Type", contentType + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException e) {
            exchange.sendResponseHeaders(404, -1);
        }
        exchange.close();
    }
}