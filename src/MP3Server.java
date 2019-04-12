import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * Proj 4 -- MP3 Server
 *
 * A class that creates a server for mp3 files.
 *
 * @author Aidan Molnar, lab 17
 * @author Annah Aunger, lab 17
 *
 * @version April 11, 2019
 *
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

            System.out.println("    Starting Client Handler");

            new Thread(clientHandler).start();
        }
    }
}


/**
 * Proj 4 -- MP3 Server
 *
 * A task that handles client requests for the mp3 server.
 *
 * @author Aidan Molnar, lab 17
 * @author Annah Aunger, lab 17
 *
 * @version April 11, 2019
 *
 */
final class ClientHandler implements Runnable {

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientHandler(Socket clientSocket) {
        try {

            System.out.println("    Creating output stream");
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();

            System.out.println("    Creating input stream");
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            System.out.println("IO Exception");
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
            System.out.println("    Waiting for client Request");
            // Waits for request from client
            clientRequest = (SongRequest) inputStream.readObject();
            System.out.println("Received Client Request");

            if (clientRequest.isDownloadRequest()) {
                System.out.println("Download Request");

                String songName = clientRequest.getSongName();
                String artistName = clientRequest.getArtistName();

                String fileName = artistName + " - " + songName + ".mp3";  // Formats song data into file name

                System.out.println("    Client Requested: <" + fileName +">");

                if (fileInRecord(fileName)) {
                    System.out.println("    File in Record!");

                    // Sends header with song information
                    byte [] byteArray = readSongData(fileName);
                    header = new SongHeaderMessage(true, songName, artistName, byteArray.length);
                    outputStream.writeObject(header);
                    outputStream.flush();

                    sendByteArray(byteArray);

                } else {
                    System.out.println("    File not in Record :(");

                    // Sends back header with file size of -1 if not available
                    header = new SongHeaderMessage(true, songName, artistName, -1);
                    outputStream.writeObject(header);
                    outputStream.flush();
                }

            } else {
                System.out.println("List Request");
                // If not a download request sends header with false and record data
                header = new SongHeaderMessage(false);
                outputStream.writeObject(header);
                outputStream.flush();

                System.out.println("    Sent Header");

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

        System.out.println("Checking Database");

        try {
            s = new Scanner(new File("record.txt"));
        } catch (IOException e) {
            return false;
        }

        lines = new ArrayList<String>(); // Array list for holding records.txt

        // Adds each line to the lines array list
        while (s.hasNextLine()) {
            String nextLine = s.nextLine();
            lines.add(nextLine);
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
            is = new FileInputStream(new File("songDatabase/" + fileName));
            dis = new DataInputStream(is);

            /*
            File file = new File("myFile");
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();
            */

            // Reads the mp3 to a byte array
            byteArray = dis.readAllBytes();

            is.close();
            dis.close();

            if (byteArray == null) {
                System.out.println("    No Data for song found");
            }

            return byteArray;
        } catch (IOException e) {
            System.out.println("IO Exception reading song");
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

        System.out.println("Sending record data");

        try {
            s = new Scanner(new File("record.txt"));

            while (s.hasNextLine()) {
                lineParts = s.nextLine().split(" - ");
                message = "\"" + lineParts[1].replace(".mp3","") + "\" By: " + lineParts[0];
                System.out.println("Sending " + message);
                outputStream.writeObject(message);
                outputStream.flush();
            }

            s.close();

            outputStream.writeObject(null);
            outputStream.flush();

        } catch (IOException e) {
            System.out.println("IO Exception when sending record data");
            e.printStackTrace();
            return;
        }
    }
}