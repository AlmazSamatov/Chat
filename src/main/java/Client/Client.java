package Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {

        InetAddress ip = InetAddress.getByName("localhost");

        // connect to server
        Socket s = new Socket(ip, Constants.serverPort);

        Scanner scanner = new Scanner(System.in);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);
        DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());

        SendMessage sendMessage = new SendMessage(scanner, dataOutputStream, outputStreamWriter);
        GetMessage getMessage = new GetMessage(dataInputStream, outputStreamWriter);

        sendMessage.start();
        getMessage.start();
    }

    public static class SendMessage extends Thread {

        private Scanner scanner;
        private DataOutputStream dataOutputStream;
        private Writer outputWriter;

        SendMessage(Scanner scanner, DataOutputStream dataOutputStream, Writer outputWriter) {
            this.scanner = scanner;
            this.dataOutputStream = dataOutputStream;
            this.outputWriter = outputWriter;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                String msg = scanner.nextLine();

                try {
                    dataOutputStream.writeUTF(msg);
                } catch (IOException e) {
                    String errorMessage = "Can not send message to server. Most probably, this is network error.";
                    logger.error(errorMessage, e);
                    try {
                        outputWriter.write(errorMessage);
                    } catch (IOException e1) {
                        logger.error("Error happened while closing output writer", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }

            // close resources
            try {
                dataOutputStream.close();
                scanner.close();
                outputWriter.close();
            } catch (IOException e) {
                logger.error("Error happened while we try to close resources", e);
            }
        }
    }

    public static class GetMessage extends Thread {

        private DataInputStream dataInputStream;
        private Writer outputWriter;

        GetMessage(DataInputStream dataInputStream, Writer outputWriter) {
            this.dataInputStream = dataInputStream;
            this.outputWriter = outputWriter;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    String msg = dataInputStream.readUTF();
                    outputWriter.write(msg);
                } catch (IOException e) {
                    String errorMessage = "Can not read messages from server. Most probably, this is network error.";
                    logger.error(errorMessage, e);
                    try {
                        outputWriter.write(errorMessage);
                    } catch (IOException e1) {
                        logger.error("Error happened while closing output writer", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }

            // close resources
            try {
                dataInputStream.close();
                outputWriter.close();
            } catch (IOException e) {
                logger.error("Error happened while we try to close resources", e);
            }

        }
    }
}
