import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

    static List<ClientHandler> clientHandlers = new ArrayList<>();
    static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        // server will listen on port 8000
        ServerSocket serverSocket = new ServerSocket(8000);

        logger.info("Server listens to requests on port 8000");

        Scanner sc = new Scanner(System.in);

        while (!sc.next().equals("stop")) {
            Socket socket = serverSocket.accept();

            logger.info("New user request received: ", socket);

            ClientHandler clientHandler = new ClientHandler(socket);

            clientHandlers.add(clientHandler);

            logger.info(String.format("Saved user №%s to user pool", clientHandlers.size()));

            clientHandler.run();

            logger.info(String.format("Ready to work with user №%s", clientHandlers.size()));
        }

        stopAllConnections();
    }

    public static void stopAllConnections() {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.interrupt();
        }
    }
}
