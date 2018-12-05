import Client.Client;
import Client.Client.SendMessage;
import Client.Client.GetMessage;
import Server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import util.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class TestChat {

    private static DataInputStream dataInputStream1;
    private static DataOutputStream dataOutputStream1;
    private static DataInputStream dataInputStream2;
    private static DataOutputStream dataOutputStream2;

    private static Scanner scanner1;
    private static Scanner scanner2;

    private static OutputStreamWriter outputStreamWriter1;
    private static OutputStreamWriter outputStreamWriter2;

    private static SendMessage sendMessage1;
    private static SendMessage sendMessage2;

    private static GetMessage getMessage1;
    private static GetMessage getMessage2;

    private static Thread server;

    @BeforeClass
    public static void setUpResources() throws IOException {
        // run server
        server = new Thread(() -> {
            try {
                Server.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();

        // run 1-st user
        InetAddress ip = InetAddress.getByName("localhost");

        // connect to server
        Socket s1 = new Socket(ip, Constants.serverPort);

        scanner1 = new Scanner(new File(Paths.get("").toAbsolutePath().toString() + "/src/test/resources/in1.txt"));
        outputStreamWriter1 = new OutputStreamWriter(new FileOutputStream(new File(
                Paths.get("").toAbsolutePath().toString() + "/src/test/resources/out1.txt")));
        dataInputStream1 = new DataInputStream(s1.getInputStream());
        dataOutputStream1 = new DataOutputStream(s1.getOutputStream());

        sendMessage1 = new SendMessage(scanner1, dataOutputStream1, outputStreamWriter1);
        getMessage1 = new GetMessage(dataInputStream1, outputStreamWriter1);

        // run 2-nd client
        Socket s2 = new Socket(ip, Constants.serverPort);

        scanner2 = new Scanner(new File(Paths.get("").toAbsolutePath().toString() + "/src/test/resources/in2.txt"));
        outputStreamWriter2 = new OutputStreamWriter(new FileOutputStream(new File(Paths.get("").toAbsolutePath().toString() + "/src/test/resources/out2.txt")));
        dataInputStream2 = new DataInputStream(s2.getInputStream());
        dataOutputStream2 = new DataOutputStream(s2.getOutputStream());

        sendMessage2 = new SendMessage(scanner2, dataOutputStream2, outputStreamWriter2);
        getMessage2 = new GetMessage(dataInputStream2, outputStreamWriter2);
    }

    @Test
    public void testMessageSendingAndRetrieving() throws IOException, InterruptedException {
        getMessage1.start();
        getMessage2.start();

        sendMessage1.start();
        sendMessage2.start();

        Thread.sleep(3000);

        sendMessage1.interrupt();
        sendMessage2.interrupt();

        getMessage1.interrupt();
        getMessage2.interrupt();

        server.interrupt();

        scanner1.close();
        scanner2.close();

        outputStreamWriter1.close();
        outputStreamWriter2.close();

        dataInputStream1.close();
        dataInputStream2.close();

        dataOutputStream1.close();
        dataOutputStream2.close();

        Scanner sc = new Scanner(new File(Paths.get("").toAbsolutePath().toString() + "/src/test/resources/out1.txt"));

        sc.nextLine();
        assertEquals(sc.nextLine(), "Damir: How are you?");

        sc = new Scanner(new File(Paths.get("").toAbsolutePath().toString() + "/src/test/resources/out2.txt"));

        sc.nextLine();
        assertEquals(sc.nextLine(), "Almaz: Good day!");

        sc.close();
    }

}
