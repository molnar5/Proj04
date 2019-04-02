import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;



//hello this is a test message!!
//hello!

//Annah Aunger

/**
 * An MP3 Client to request .mp3 files from a server and receive them over the socket connection.
 */
public class MP3Client {

    public static void main(String[] args) {
        //TODO: Implement main

        Socket serverConnection = null;
        Scanner scan = new Scanner(System.in);
        Scanner inServer = null;
        PrintWriter outServer;
        String response;

        try {
            serverConnection = new Socket("localhost", 423);

            inServer = new Scanner(serverConnection.getInputStream());

            outServer = new PrintWriter(serverConnection.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("A file input/output exception occurred");

            System.out.printf("Exception message: %s\n", e.getMessage());

            if (inServer != null) {
                inServer.close();
            } //end if

            if (serverConnection != null) {
                try {
                    serverConnection.close();
                } catch (IOException i) {
                    i.printStackTrace();
                } //end try catch
            } //end if

            return;
        } //end try catch

        System.out.println("<Connected to the server>");

        System.out.println("============ Options ============");
        System.out.println("(1) See list of available songs");
        System.out.println("(2) Request song download");
        System.out.println(" *  Exit");
        System.out.println("=================================");

        System.out.println("Please enter an option number or 'exit' to leave." );

        while (scan.hasNextLine()) { // while the user continues to enter requests to the server
            outServer.println(scan.nextLine());

            if (inServer.hasNextLine()) {
                response = inServer.nextLine();
            } else {
                System.out.println("<Lost the connection with the server>");

                return;
            } //end if

            System.out.printf("Response from the server: %s\n", response);

            //System.out.println("Please enter an option number or 'exit' to leave." );

            if (response == null) {

            } else if (response.length() == 0 ) {
                //response invalid
                System.out.println("Response invalid.");
                System.out.println("Please enter an option number or 'exit' to leave." );
            } else if (response.equals("1")) {

                //they want to see the songs

                SongRequest showList = new SongRequest(false);

                //TODO:
                // send the server the song request
                // After sending a request to the server, create a Thread with a ResponseListener
                // to listen for the server's response. Wait for this thread to finish, then close
                // the socket and continue with the client.

            } else if (response.equals("2")) {

                //they want to download the song

                System.out.println("Please enter the song title");
                String songName = inServer.nextLine();
                System.out.println("Please enter the artist name");
                String artist = inServer.nextLine();
                SongRequest songRequest = new SongRequest(true, songName, artist);

                //TODO:
                // send the sever the song request
                // After sending a request to the server, create a Thread with a ResponseListener
                // to listen for the server's response. Wait for this thread to finish, then close
                // the socket and continue with the client.

            } else if (response.equalsIgnoreCase("exit")) {
                // stop the program
                break;
            } else {
                //input must be invalid
                System.out.println("Response invalid.");
                System.out.println("Please enter an option number or 'exit' to leave." );
            }

        } //end while

         scan.close();

        inServer.close();

        outServer.close();

        try {
            serverConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } //end try catch

        // The main method for this class should create a socket to
        // connect to the server and get a reference to the socket's
        // output stream as a ObjectOutputStream. Then, ask the user
        // if they want to see the list of available songs or just
        // request to download a song. If the user enters the word
        // “exit”, you should stop asking for their input. Also, if
        // the user enters an invalid choice, prompt them again.
    }
}


/**
 * This class implements Runnable, and will contain the logic for listening for
 * server responses. The threads you create in MP3Server will be constructed using
 * instances of this class.
 */
final class ResponseListener implements Runnable {

    private ObjectInputStream ois;

    public ResponseListener(Socket clientSocket) throws IOException {
        //TODO: Implement constructor

        if (clientSocket == null) {
            throw new IllegalArgumentException("clientSocket argument is null");
        } else {
            clientSocket = // need to assign client socket to something
        }
    }

    /**
     * Listens for a response from the server.
     * <p>
     * Continuously tries to read a SongHeaderMessage. Gets the artist name, song name, and file size from that header,
     * and if the file size is not -1, that means the file exists. If the file does exist, the method then subsequently
     * waits for a series of SongDataMessages, takes the byte data from those data messages and writes it into a
     * properly named file.
     */
    public void run() {
        //TODO: Implement run

    }

    /**
     * Writes the given array of bytes to a file whose name is given by the fileName argument.
     *
     * @param songBytes the byte array to be written
     * @param fileName  the name of the file to which the bytes will be written
     */
    private void writeByteArrayToFile(byte[] songBytes, String fileName) {
        //TODO: Implement writeByteArrayToFile
        BufferedWriter bw = null;
        try {

            File file = new File(fileName);

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            //TODO: create loop to write song bytes
            bw.write(songBytes);

            System.out.println("File written Successfully");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception a){
                System.out.println("Error in closing the BufferedWriter"+a);
            }        }
    }
}