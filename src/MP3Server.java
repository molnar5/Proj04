import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * A MP3 Server for sending mp3 files over a socket connection.
 */
public class MP3Server {

    public static void main(String[] args) {
        final int port = 9478;

        ServerSocket serverSocket;
        Socket clientSocket;

        System.out.println("Starting Server");

        // Tries to open server socket
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Failed to Open Server Socket");
            return;
        }

        // Receives client sockets
        while (true) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Error Accepting Client Socket");

                try {
                    serverSocket.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }

                return;
            }

            System.out.println("Spawning Client Handler");

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();

        }



    }

}


/**
 * Class - ClientHandler
 *
 * This class implements Runnable, and will contain the logic for handling responses and requests to
 * and from a given client. The threads you create in MP3Server will be constructed using instances
 * of this class.
 */
final class ClientHandler implements Runnable {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientHandler(Socket clientSocket) {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.print("IO Exception:");
        }
    }

    /**
     * This method is the start of execution for the thread. See the handout for more details on what
     * to do here.
     */
    public void run() {
        SongRequest clientRequest;  // The message received from the client
        SongHeaderMessage header;

        // Catches IO and Class Not Found Exceptions
        try {
            // Waits for request from client
            clientRequest = (SongRequest) inputStream.readObject();

            if (clientRequest.isDownloadRequest()) {

                String songName = clientRequest.getSongName();
                String artistName = clientRequest.getArtistName();

                String fileName = artistName + " - " + songName + ".mp3";  // Formats song data into file name

                if (fileInRecord(fileName)) {
                    // Sends header with song information
                    byte [] byteArray = readSongData(fileName);
                    header = new SongHeaderMessage(true, songName, artistName, byteArray.length);
                    outputStream.writeObject(header);
                    outputStream.flush();

                    sendByteArray(byteArray);

                } else {
                    // Sends back header with file size of -1 if not available
                    header = new SongHeaderMessage(true, songName, artistName, -1);
                    outputStream.writeObject(header);
                    outputStream.flush();
                }

            } else {
                // If not a download request sends header with false and record data
                header = new SongHeaderMessage(false);
                outputStream.writeObject(header);
                outputStream.flush();

                sendRecordData();
            }

            // Closes streams to/from client
            outputStream.close();
            inputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception in run: " + e.getMessage());
        }


    }

    /**
     * Searches the record file for the given filename.
     *
     * @param fileName the fileName to search for in the record file
     * @return true if the fileName is present in the record file, false if the fileName is not
     */
    private static boolean fileInRecord(String fileName) {
        Scanner s;                 // Scanner for reading records.txt
        ArrayList<String> lines;   // The lines of records.txt

        try {
            s = new Scanner(new File("records.txt"));
        } catch (IOException e) {
            return false;
        }

        lines = new ArrayList<String>(); // Array list for holding records.txt

        // Adds each line to the lines array list
        while (s.hasNextLine()) {
            lines.add(s.nextLine());
        }

        s.close();

        return lines.contains(fileName);
    }

    /**
     * Read the bytes of a file with the given name into a byte array.
     *
     * @param fileName the name of the file to read
     * @return the byte array containing all bytes of the file, or null if an error occurred
     */
    private static byte[] readSongData(String fileName) {
        FileInputStream is;    // File input stream for establishing data input stream
        DataInputStream dis;   // Data input stream for reading mp3 to byteArray
        byte [] byteArray;     // Stores mp3 data

        try {
            is = new FileInputStream(new File(fileName));
            dis = new DataInputStream(is);

            // Reads the mp3 to a byte array
            byteArray = dis.readAllBytes();

            is.close();
            dis.close();

            return byteArray;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Split the given byte array into smaller arrays of size 1000, and send the smaller arrays
     * to the client using SongDataMessages.
     *
     * @param songData the byte array to send to the client
     */
    private void sendByteArray(byte[] songData) {
        SongDataMessage message;  // Message being sent to client at each step in for loop

        try {
            // Iterates in increments of 1000 over length of song byteArray
            for (int i = 0; i < songData.length; i += 1000) {
                // Copies the section of the byte array
                // If i + 1000 is greater than the length of the array, ends the copy section at the length of the
                // array.
                message = new SongDataMessage(Arrays.copyOfRange(songData, i, Math.min(i + 1000, songData.length)));
                outputStream.writeObject(message);
                outputStream.flush();
            }

            // Sends null to indicate no longer sending song information
            outputStream.writeObject(null);
            outputStream.flush();

        } catch (IOException e) {
            System.out.println("IO Exception: Message Failed to Send");
        }


    }

    /**
     * Read ''record.txt'' line by line again, this time formatting each line in a readable
     * format, and sending it to the client. Send a ''null'' value to the client when done, to
     * signal to the client that you've finished sending the record data.
     */
    private void sendRecordData() {
        Scanner s;            // Scanner for reading records.txt
        String [] lineParts;  // Array that stores 2 strings: one from before " - " and one from after " - "
        String message;       // Reformatted line sent to the client

        try {
            s = new Scanner(new File("records.txt"));

            while (s.hasNextLine()) {
                lineParts = s.nextLine().split(" - ");
                message = "\"" + lineParts[1].replace(".mp3","") + "\" By:" + lineParts[0];
                outputStream.writeObject(message);
                outputStream.flush();
            }

            s.close();
        } catch (IOException e) {
            return;
        }
    }
}