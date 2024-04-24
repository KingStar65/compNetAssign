import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TFTPUDPClient {
    //private static final int PORT = 1600;
    private static final int PORT = 69;
    private static final String SERVER_IP = "127.0.0.1"; // Replace with server IP
    private static final int BLOCKSIZE = 512; // As per RFC 1350

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("TFTP Client Menu:");
                System.out.println("1. Create a file");
                System.out.println("2. Read a file");
                System.out.println("3. Write to a file");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline left-over

                switch (choice) {
                    case 1:
                        System.out.print("Enter the filename to create: ");
                        String filename = scanner.nextLine();
                        // Send request to create file
                        sendRequest(socket, serverAddress, "create " + filename);
                        break;
                    case 2:
                        System.out.print("Enter the filename to read: ");
                        filename = scanner.nextLine();
                        // Send request to read file
                        sendRequest(socket, serverAddress, "read " + filename);
                        break;
                    case 3:
                        System.out.print("Enter the filename to write: ");
                        filename = scanner.nextLine();
                        System.out.print("Enter the text to write: ");
                        String text = scanner.nextLine();
                        System.out.println(text);
                        // Send request to write to file
                        sendRequest(socket, serverAddress, "write " + filename + " " + text);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        scanner.close();
                        socket.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //to send a request to the server
    private static void sendRequest(DatagramSocket socket, InetAddress serverAddress, String request) throws IOException {
        byte[] buffer = request.getBytes(); //converts to byte array
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, PORT);//crafts packet with necessary information
        socket.send(packet);//sends the packet through the socket

        // Receive response from server
        buffer = new byte[BLOCKSIZE];
        packet = new DatagramPacket(buffer, buffer.length); //prepare the packet
        socket.receive(packet);//

        // Process the response
        String response = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Server response: " + response);
    }
}