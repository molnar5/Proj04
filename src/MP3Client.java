import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;

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
            do {
                serverConnection = new Socket("localhost", 9478);

                inServer = new Scanner(serverConnection.getInputStream());

                outServer = new PrintWriter(serverConnection.getOutputStream(), true);

                System.out.println("<Connected to the server>");

                System.out.println("============ Options ============");
                System.out.println("(1) See list of available songs");
                System.out.println("(2) Request song download");
                System.out.println(" *  Exit");
                System.out.println("=================================");

                System.out.println("Please enter an option number or 'exit' to leave." );

                outServer.println(scan.nextLine());

                if (inServer.hasNextLine()) {
                    response = inServer.nextLine();
                } else {
                    System.out.println("<Lost the connection with the server>");

                    return;
                } //end if

                System.out.printf("Response from the server: %s\n", response);

                if (response == null) {
                    //response invalid
                    System.out.println("Response invalid.");
                    System.out.println("Please enter an option number or 'exit' to leave." );

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


                    outServer.println(showList);

                    ResponseListener listThread = new ResponseListener(serverConnection);
                    new Thread (listThread).start();

                    if (!(listThread.isAlive())) { //the thread has finished
                        //close the socket
                        serverConnection.close();

                    }

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

                    outServer.println(songRequest);

                    ///////////format in Server class to create thread//////////////
                    //ClientHandler clientHandler = new ClientHandler(clientSocket);
                    //new Thread(clientHandler).start();

                    ResponseListener downloadThread = new ResponseListener(serverConnection);
                    new Thread (downloadThread).start();

                    if (!(downloadThread.isAlive())) { //the thread has finished
                        //close the socket
                        serverConnection.close();
                    }

                } else if (response.equalsIgnoreCase("exit")) {
                    // stop the program
                    break;
                } else {
                    //input must be invalid
                    System.out.println("Response invalid.");
                    System.out.println("Please enter an option number or 'exit' to leave." );
                }
            } while (scan.hasNextLine());

            scan.close();

            inServer.close();

            outServer.close();

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
    } //end try catch
}


/**
 * This class implements Runnable, and will contain the logic for listening for
 * server responses. The threads you create in MP3Server will be constructed using
 * instances of this class.
 */
final class ResponseListener implements Runnable {

    private ObjectInputStream ois;

    public ResponseListener(Socket clientSocket) throws IOException {
        //This constructor takes in a socket and builds the ObjectInputStream with it.

        /*
        if (clientSocket == null) {
            throw new IllegalArgumentException("clientSocket argument is null");
        } else {
            ois = new ObjectInputStream(clientSocket.getInputStream());
        }
        */
        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException f) {
            f.printStackTrace();
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

        //This is the only method, you will need to implement in this class.
        // Simply put, you will need to read in the first object you get as a
        // SongHeader message (after checking that it isn't null and a different object).
        // Once you do, you determine what type of message it is. If it is a download message
        // you will need to receive all of the bytes from the output stream and write it to a
        // file in the savedSongs directory with the name

        try {
            //Scanner scan = new Scanner(ois);

            Object header = ois.readObject();

            do {

                if (header == null) {

                    header = ois.readObject();

                } else if (header instanceof SongHeaderMessage){
                    if (((SongHeaderMessage) header).isSongHeader()) { // is is a download request

                        if (((SongHeaderMessage) header).getFileSize() != -1) { //there are bytes to be written

                            //format: SongHeaderMessage(true, songName, artistName, byteArray.length);

                            // you will need to receive all of the bytes from the output stream and write it to a
                            // file in the savedSongs directory with the name
                            //“<Artist> - <Song name>.mp3”

                            String filename = String.format("<%s> - <%s>.mp3", ((SongHeaderMessage) header).getArtistName(),
                                    ((SongHeaderMessage) header).getSongName());

                            byte[] songBytes = ois.readAllBytes();

                            this.writeByteArrayToFile(songBytes, filename);
                        } // else there are NO bytes to be written
                          // TODO: do I need to wait until there are bytes to be written? or continue with reading?

                    } else { // the user wants to see the list

                        // print all the strings you are receiving
                        // (Since you will just be receiving a list of stuff in the record).
                        // SongDataMessages, takes the byte data from those data messages and writes it into a
                        // properly named file.

                        // TODO: how to make sure that ALL the strings are printed
                        System.out.println(ois.read());
                    }
                    break;

                } else {
                    //TODO: this is not a songHeaderObject... what to do now?
                    break;
                }

            } while (true);
        } catch (Exception a) {
            //either the class is not found or I/O exception
            a.printStackTrace();
        }
    }

    /**
     * Writes the given array of bytes to a file whose name is given by the fileName argument.
     *
     * @param songBytes the byte array to be written
     * @param fileName  the name of the file to which the bytes will be written
     */
    private void writeByteArrayToFile(byte[] songBytes, String fileName) {
        FileOutputStream fos = null;

        try {

            //File file = new File(fileName);
            fos = new FileOutputStream(fileName, true);

            //FileWriter fw = new FileWriter(file);
            //bw = new BufferedWriter(fw);

            //TODO: not sure if this is right
            //create loop to write song bytes
            if (songBytes.length > 0) {
                for (int i = 0; i < fileName.length(); i++) {
                    fos.write(songBytes[i]);
                    fos.flush();
                }
                System.out.println("File written Successfully");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch(Exception a) {
                System.out.println("Error in closing fos" + a);
            }
        } // end finally
    }
}
}