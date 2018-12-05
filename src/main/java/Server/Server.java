package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    static final Logger logger = LoggerFactory.getLogger(Server.class);
    static AtomicBoolean isRunning = new AtomicBoolean(true);

    public static void main(String[] args) throws IOException {
        // server will listen on port 8000
        ServerSocket serverSocket = new ServerSocket(Constants.serverPort);

        logger.info(String.format("Server listens to requests on port %s", Integer.toString(Constants.serverPort)));

        startWaitingForStopCommand();

        while (isRunning.get()) {

            Socket socket = serverSocket.accept();

            logger.info("New user request received: " + socket);

            ClientHandler clientHandler = new ClientHandler(socket, clientHandlers.size() + 1);

            clientHandlers.add(clientHandler);

            logger.info(String.format("Saved user %s to user pool", clientHandler.getNameOrNumber()));

            clientHandler.start();

            logger.info(String.format("Ready to work with user %s", clientHandler.getNameOrNumber()));
        }

        stopAllConnections();
    }

    private static void stopAllConnections() {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.interrupt();
        }
    }

    private static void startWaitingForStopCommand() {
        Scanner scanner = new Scanner(System.in);
        WaitForStopCommand waitForStopCommand = new WaitForStopCommand(scanner);
        waitForStopCommand.start();
    }

    public static class WaitForStopCommand extends Thread {

        private Scanner scanner;

        WaitForStopCommand(Scanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public void run() {

            if (scanner.next().equals("stop")) {
                isRunning.set(false);
            }

            scanner.close();
        }
    }
}
