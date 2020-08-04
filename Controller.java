import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/*
 * Controller.java
 *
 *
 * Revisions:
 *     Final
 */

/**
 * This program is to emulate a Openflow Controller.
 * @author      Gopinath Swaminathan    .
 */
public class Controller {

    public static void main(String[] args) {

        //Creating TCP socket and listening on port 5555
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(5555);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Creating a new thread for each switch connection
        while (true) {
            socket = null;
            try {
                System.out.println("Waiting....");
                socket = serverSocket.accept();
                System.out.println("Client connected");
                Thread t = new switchHandler(socket);
                t.start();
            } catch (IOException  e) {
                e.printStackTrace();
            }
        }

    }
}
