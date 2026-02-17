import com.sun.net.httpserver.*;
import java.io.*;

public class CustomerHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (exchange.getRequestMethod().equals("GET")) {
            if (path.equals("/"))
                sendFile(exchange, "web/index.html");
            else if (path.contains("register"))
                sendFile(exchange, "web/register.html");
            else
                sendFile(exchange, "web/login.html");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        String[] data = body.split("&");

        if (path.contains("register")) {

            String name = data[0].split("=")[1];
            int age = Integer.parseInt(data[1].split("=")[1]);
            String address = data[2].split("=")[1];
            String email = data[3].split("=")[1];
            String password = data[4].split("=")[1];

            if (age < 16) {
                send(exchange, "You must be 16 or older to register.");
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader("data/customers.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(email)) {
                    send(exchange, "Email already exists. <a href='/login'>Login here</a>");
                    br.close();
                    return;
                }
            }
            br.close();

            FileWriter fw = new FileWriter("data/customers.csv", true);
            fw.append("\n" + System.currentTimeMillis() + "," +
                    name + "," + age + "," + address + "," + email + "," + password);
            fw.close();

            send(exchange, "Registration successful! <a href='/login'>Login</a>");
        }

        else if (path.contains("login")) {

            String email = data[0].split("=")[1];
            String password = data[1].split("=")[1];

            BufferedReader br = new BufferedReader(new FileReader("data/customers.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(email) && line.contains(password)) {
                    SessionManager.loggedInUser = email;
                    SessionManager.userAddress.put(email, line.split(",")[3]);
                    br.close();
                    sendFile(exchange, "web/login-success.html");
                    return;
                }
            }
            br.close();

            send(exchange, "Invalid credentials.");
        }
    }

    private void sendFile(HttpExchange ex, String path) throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(new File(path).toPath());
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    private void send(HttpExchange ex, String msg) throws IOException {
        ex.sendResponseHeaders(200, msg.length());
        ex.getResponseBody().write(msg.getBytes());
        ex.close();
    }
}
