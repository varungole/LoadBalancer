package loadbalancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {

    private final int PORT;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final List<AvailableServers> availableServers;
    private final ExecutorService threadPool;

    public LoadBalancer(int PORT, List<AvailableServers> availableServers) {
        this.PORT = PORT;
        this.availableServers = new CopyOnWriteArrayList<>(availableServers);
        threadPool = Executors.newFixedThreadPool(10);
    }

    public void start() {
        try(ServerSocket loadBalancer = new ServerSocket(PORT)) {
           System.out.println("Load Balancer is live on PORT " + PORT);
           while(true) {
            Socket clientSocket = loadBalancer.accept();
            System.out.println("Client connected to load balancer");
         
            threadPool.execute(() -> {
                try {
                    forwardRequest(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
           }
         
        } catch(IOException e) {
            e.getMessage();
        }
    }

    private void forwardRequest(Socket clientSocket) throws IOException {
        if(availableServers.isEmpty()) {
            System.out.println("No server left");
            clientSocket.close();
            return;
        }

        AvailableServers server = getNextServer();
        try(Socket serverSocket = new Socket("localhost", server.PORT)) {
           
            //Forward client request to server
           InputStream clientInput = clientSocket.getInputStream();
           OutputStream serverOutput = serverSocket.getOutputStream();
           transferData(clientInput, serverOutput, server.PORT);

           //Forward server response to client
           InputStream serverInput = serverSocket.getInputStream();
           OutputStream clientOutput = clientSocket.getOutputStream();
           transferData(serverInput, clientOutput, server.PORT);
        } catch(IOException e) {
            System.err.println("Error forwarding request: " + e.getMessage());
        }
    }

    private void transferData(InputStream input, OutputStream output, int PORT) throws IOException{
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            output.flush();
            System.out.println("Received response from server " + PORT);
        }
    }

    private AvailableServers getNextServer() {
        int index = currentIndex.getAndUpdate(i -> (i+1)%availableServers.size());
        return availableServers.get(index);
    }

    public static void main(String[] args) {

        List<AvailableServers> availableServers = new ArrayList<>();
        availableServers.add(new AvailableServers(7070));
        availableServers.add(new AvailableServers(8080));
        availableServers.add(new AvailableServers(9090));
        LoadBalancer loadBalancer = new LoadBalancer(1010, availableServers);
        System.out.println("STARTING LOAD BALANCER");
        loadBalancer.start();
    }
}
