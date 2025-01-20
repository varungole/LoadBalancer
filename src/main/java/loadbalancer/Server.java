package loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private int PORT;

    public Server(int PORT) {
        this.PORT = PORT;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening at port " + PORT);
            while (true) {
                try (Socket plug = serverSocket.accept()) {
                    System.out.println("Client connected!");
                    handleClient(plug);
                }
            }
        } catch (IOException ioException) {
            System.out.println("Error starting the server: " + ioException.getMessage());
        }
    }

    private void handleClient(Socket plug) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(plug.getInputStream()));
        OutputStream out = plug.getOutputStream();

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            System.out.println(line); 
        }

        String response = "HTTP/1.1 200 OK\r\n" +
                          "Content-Type: text/plain\r\n" +
                          "Content-Length: 35\r\n" +
                          "\r\n" +
                          "Response from server: HTTP/1.1 200 OK";

        out.write(response.getBytes());
        out.flush();
    }

    public static void main(String[] args) throws IOException {
        Server server1 = new Server(8080);
        Server server2 = new Server(9090);
        Server server3 = new Server(7070);
        new Thread(() -> {
            try {
                server1.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();;

        new Thread(() -> {
            try {
                server2.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                server3.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        
    }
}
