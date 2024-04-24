import java.io.*;
import java.net.*;

public class TFTPUDPServer {
    //private static final int PORT = 1600;
    private static final int PORT = 69; //port number for the TFTP protocol
    private static final int BLOCKSIZE = 512; //516 because of RFC 1350
    //private static final int TIMEOUT = 5000; // Timeout in milliseconds

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT); //creates new socket with the port number
            socket.setSoTimeout(3000); //Sets the timeout value

            byte[] buffer = new byte[BLOCKSIZE]; //sets buffer to buffer size(516) from incoming datagram packets
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("TFTP Server is running...");

            while (true) {
                try {
                    socket.receive(packet); //this receives the packet from sent from clients
                    new TFTPServerThread(socket, packet).start(); // creates a new thread for each request
                } catch (SocketTimeoutException e)  { //exception when timeout expires

                }
            }
        } catch (SocketException e) { //catches the socket and IO exceptions
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class TFTPServerThread extends Thread {
    private DatagramSocket socket; //creates a socket
    private DatagramPacket packet; //creates a packet

    public TFTPServerThread(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket; //socket to send back the request data back to client
        this.packet = packet; //packet containing the client's request
    }

    public void run() {
        try {
            String request = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received request: " + request);

            String[] parts = request.split(" "); //splits the different inputs to store them in one variable
            String input = parts[0]; // stores the initial input, create, read or write
            String filename = parts[1]; //stores the filename to input, create or write

            switch (input) {
                case "create":
                    handleCreate(filename); //function to handle create requests
                    break;
                case "read":
                    handleRead(filename); //function to handle read requests
                    break;
                case "write":
                    StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < parts.length; i++) {
                        if (i > 2) sb.append(" "); // add space before all but the first word
                        sb.append(parts[i]);
                    }
                    String text = sb.toString();
                    handleWrite(filename, text); //function to handle write requests
                    break;
                default:
                    System.out.println("Error: ");
                    break;
            }
        } catch (IOException e) { //exception for input output errors
            e.printStackTrace();
        }
    }

    private void handleCreate(String filename) throws IOException {
        File file = new File(filename);
        if (file.createNewFile()) { //creates a file with the filename and checks if it was created succesfully
            System.out.println("File created: " + filename);
            sendResponse("File created successfully.");
        } else {
            System.out.println("Failed to create file: " + filename);
            sendResponse("Failed to create file.");
        }
    }

    private void handleRead(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) { //checks if file exists and reads file and print it out into the console
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            System.out.println("Read file: " + filename);
            sendResponse("File content: " + content.toString());
        } else {
            System.out.println("File not found: " + filename);
            sendResponse("File not found.");
        }
    }

    private void handleWrite(String filename, String text) throws IOException {
        File file = new File(filename);
        if (file.exists()) {//checks if file exists
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(text + " "); //takes the text input and write it to the file
            writer.close();
            System.out.println("Wrote to file: " + filename);
            sendResponse("Written to file successfully.");
        } else {
            System.out.println("File not found: " + filename);
            sendResponse("File not found.");
        }
    }

    private void sendResponse(String response) throws IOException {
        byte[] sendData = response.getBytes();// takes what was passed into sendResponse and set them to bytes
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());// crafts packet to the port, ip address and how long the packet is
        socket.send(sendPacket); //sends packet back to client through the socket
    }
}