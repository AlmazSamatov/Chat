import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private String name;

    ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        // Ask user to enter it's name
        dos.writeUTF("Enter your name, please:");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            try {
                String msg = dis.readUTF();

                if (name == null)
                    name = msg;
                else
                    logger.info(String.format("Received message from user with name %s", name), msg);

                sendMessageToOthers(name + ": " + msg);

            } catch (IOException e) {
                logger.error("Can not read message from user", e);
            }
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
        dos.writeUTF(msg);
    }
}
