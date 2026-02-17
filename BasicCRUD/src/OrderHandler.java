import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
// Add PDFBox imports if using Apache PDFBox:
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.pdmodel.PDPage;
// import org.apache.pdfbox.pdmodel.PDPageContentStream;
// import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class OrderHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        if (path.contains("payment")) {
            servePaymentPage(exchange, query);
        } else if (path.contains("confirm")) {
            handleOrderConfirmation(exchange, query);
        } else if (path.contains("bill.txt")) {
            serveBillPDF(exchange);
        }
    }

    private void servePaymentPage(HttpExchange exchange, String query) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("web/payment.html"));
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void handleOrderConfirmation(HttpExchange exchange, String query) throws IOException {
        if (query == null || !query.contains("total=")) {
            String msg = "<h2>Payment error</h2><p>Missing total amount. Please go back to the cart.</p>";
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(400, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
            return;
        }
    
        String totalValue = null;
        for (String pair : query.split("&")) {
            if (pair.startsWith("total=")) {
                totalValue = pair.split("=", 2)[1];
                break;
            }
        }
    
        double total = Double.parseDouble(totalValue);
        String email = SessionManager.loggedInUser;
        String rawAddress = SessionManager.userAddress.get(email);

        String name = getCustomerNameByEmail(email); 
        String address = java.net.URLDecoder.decode(rawAddress, java.nio.charset.StandardCharsets.UTF_8);

        reduceStock();
        saveOrder(email, total, address);

        // Generate PDF bill
        BillGenerator.generateBillPDF(email, total, address, CartHandler.cart);

        CartHandler.cart.clear();

        // Serve success page
        serveSuccessPage(exchange, name, address, total);
    }

    private void serveSuccessPage(HttpExchange exchange, String name, String address, double total) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("web/success.html"));
        String html = new String(bytes, StandardCharsets.UTF_8);
        
        // Replace placeholders
        html = html.replace("{{name}}", name);
        html = html.replace("{{address}}", address);
        html = html.replace("{{total}}", String.format("%.2f", total));
        
        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private void serveBillPDF(HttpExchange exchange) throws IOException {
        File pdfFile = new File("bill.txt");
        if (!pdfFile.exists()) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        
        byte[] bytes = Files.readAllBytes(pdfFile.toPath());
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=bill.txt");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void reduceStock() throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("data/products.csv"));
        String line;

        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            if (!p[0].equals("id") && CartHandler.cart.containsKey(p[0])) {
                int currentStock = Integer.parseInt(p[3]);
                int purchased = CartHandler.cart.get(p[0]);
                int newStock = currentStock - purchased;
                lines.add(p[0] + "," + p[1] + "," + p[2] + "," + newStock);
            } else {
                lines.add(line);
            }
        }
        br.close();

        FileWriter fw = new FileWriter("data/products.csv");
        for (String l : lines)
            fw.write(l + "\n");
        fw.close();
    }

    private void saveOrder(String email, double total, String address) throws IOException {
        FileWriter fw = new FileWriter("data/orders.csv", true);
        fw.append("\n" + System.currentTimeMillis() +
                "," + email + "," + total + "," + address);
        fw.close();
    }

    private String getCustomerNameByEmail(String email) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("data/customers.csv"));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5 && p[4].equals(email)) {
                    return p[1]; // name
                }
            }
        } finally {
            br.close();
        }
        // Fallback: if not found, just return email
        return email;
    }
}