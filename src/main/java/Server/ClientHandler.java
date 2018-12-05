package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler extends Thread {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private Queue<String> messagesToSent = new ConcurrentLinkedQueue<>();
    private SendMessagesToClient sendMessagesToClient;
    private String name;
    private int number;

    ClientHandler(Socket socket, int number) throws IOException {
        this.number = number;
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        // Ask user to enter it's name
        dataOutputStream.writeUTF("Enter your name, please: ");

        // run thread, which sends messages to current user
        sendMessagesToClient = new SendMessagesToClient();
        sendMessagesToClient.start();
    }

    @Override
    public void run() {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                String msg = dataInputStream.readUTF();

                if (name == null) {
                    name = msg;
                    dataOutputStream.writeUTF("Now you can write messages here.\n");
                } else {
                    logger.info(String.format("Received message from user with name %s", name), msg);

                    // operation call
                    if (msg.charAt(0) == '/') {
                        RunOperation runOperation = new RunOperation(msg);
                        runOperation.start();
                    }

                    if (msg.equals("exit")) {
                        Thread.currentThread().interrupt();
                        sendMessagesToClient.interrupt();
                        Server.clientHandlers.remove(this);
                        break;
                    }

                    sendMessageToOthers(name + ": " + msg);
                }
            }
        } catch (IOException e) {
            logger.error("Can not read message from user", e);
        }

        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            logger.error("Error happened while closing resources", e);
        }

    }

    // get name of user or number of user if name is null
    public String getNameOrNumber() {
        if (name == null)
            return String.valueOf(number);
        else
            return name;
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
        messagesToSent.add(msg);
    }

    private class SendMessagesToClient extends Thread {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(500);

                    if (!messagesToSent.isEmpty())
                        dataOutputStream.writeUTF(messagesToSent.poll() + '\n');

                }
            } catch (Exception e) {
                logger.error("Error while sending message to clint with name: " + name, e);
            }
        }
    }


    private class RunOperation extends Thread {

        String msg;
        String operation;

        RunOperation(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {

            try {
                msg = msg.replace("/", "");
                String[] cmd  = msg.split(" ");

                operation = cmd[0];

                cmd[0] = Character.toUpperCase(cmd[0].charAt(0)) + cmd[0].substring(1);
                String currentPath = String.valueOf(Paths.get("").toAbsolutePath()) + "/jars/" + cmd[0] + ".jar";
                currentPath = String.valueOf(Paths.get(currentPath));
                URL[] classLoaderUrls = new URL[]{new File(currentPath).toURI().toURL()};
                URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

                Class c = urlClassLoader.loadClass(cmd[0]);

                String msgToUser = (String) c.getDeclaredMethods()[0].invoke(null, (Object) cmd);

                messagesToSent.add("Result of " + operation + " is: " + msgToUser);

                urlClassLoader.close();

            } catch (InvocationTargetException | IllegalAccessException e) {
                messagesToSent.add("Illegal arguments passed to operation: /" + operation);
                logger.error("Can not call operation with args from user: " + getNameOrNumber(), e);
            } catch (ClassNotFoundException | IOException e) {
                messagesToSent.add("Can not find operation : /" + operation);
                logger.error("Can not find operation from user: " + getNameOrNumber(), e);
            }

        }
    }
}
