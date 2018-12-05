import Client.Client;
import Server.Server;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestChat {

    private static Server server;
    private static Client client1;
    private static Client client2;

    @BeforeClass
    public static void setUpToHexStringData() {
        server = new Server();
        client1 = new Client();
        client2 = new Client();
    }

    @Test
    public void testMessageSendingAndRetrieving() {

    }

}
