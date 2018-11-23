package Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static Scanner scanner = new Scanner(System.in);
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {

        InetAddress ip = InetAddress.getByName("localhost");

        // connect to server
        Socket s = new Socket(ip, Constants.serverPort);

        dataInputStream = new DataInputStream(s.getInputStream());
        dataOutputStream = new DataOutputStream(s.getOutputStream());

        SendMessage sendMessage = new SendMessage();
        GetMessage getMessage = new GetMessage();

        sendMessage.start();
        getMessage.start();
    }

    public static class SendMessage extends Thread {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                String msg = scanner.nextLine();

                try {
                    dataOutputStream.writeUTF(msg);
                } catch (IOException e) {
                    logger.error("Can not send message", e);
                }
            }
        }
    }

    public static class GetMessage extends Thread {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    String msg = dataInputStream.readUTF();
                    System.out.println(msg);
                } catch (IOException e) {
                    logger.error("Can not read message", e);
                }
            }

        }
    }
}
