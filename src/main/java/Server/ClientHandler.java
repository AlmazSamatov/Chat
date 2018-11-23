package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private String name;

    ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        // Ask user to enter it's name
        dataOutputStream.writeUTF("Enter your name, please:");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            try {
                String msg = dataInputStream.readUTF();

                if (name == null) {
                    name = msg;
                    dataOutputStream.writeUTF("Now you can write messages here.\n");
                } else {
                    logger.info(String.format("Received message from user with name %s", name), msg);

                    if (msg.equals("exit")) {
                        Thread.currentThread().interrupt();
                        Server.clientHandlers.remove(this);
                        break;
                    }

                    sendMessageToOthers(name + ": " + msg);
                }

            } catch (IOException e) {
                logger.error("Can not read message from user", e);
            }
        }

        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            logger.error("Error happened while closing resources", e);
        }

    }

    private void sendMessageToOthers(String msg) {
        for (ClientHandler clientHandler : Server.clientHandlers) {
            if (clientHandler != this) {
                try {
                    clientHandler.writeMessage(msg);
                } catch (IOException e) {
                    logger.error("Can not send message to other user", e);
                }
            }
        }
    }

    private void writeMessage(String msg) throws IOException {
        dataOutputStream.writeUTF(msg);
    }
}
