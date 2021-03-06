import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.*;
import java.util.concurrent.TimeUnit;

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

public class MP3Client {

    public static void main(String[] args) {

        Socket serverConnection = null;
        Scanner scan = new Scanner(System.in);
        ObjectOutputStream outServer;
        String response;

        try {
            while (true) {
                serverConnection = new Socket("10.192.31.224", 9478);
                //serverConnection = new Socket("66.70.189.118", 9478);

                try {
                    (new File("savedSongs")).mkdirs();
                } catch (Exception e) {
                    System.out.println("Couldn't make savedSongs directory!");
                }

                outServer = new ObjectOutputStream(serverConnection.getOutputStream());
                outServer.flush();

                System.out.println("<Connected to the server>\n\n");

                System.out.println("============ Options ============");
                System.out.println("(1) See list of available songs");
                System.out.println("(2) Request song download");
                System.out.println(" *  Exit");
                System.out.println("=================================\n\n");

                System.out.println("Please enter an option number or 'exit' to leave.");

                if (scan.hasNextLine()) {
                    response = scan.nextLine();
                } else {
                    System.out.println("<Lost the connection with the server>");

                    return;
                } //end if

                if (response == null) {
                    //response invalid
                    System.out.println("Response invalid.");
                    System.out.println("Please enter an option number or 'exit' to leave.");

                } else if (response.length() == 0) {
                    //response invalid
                    System.out.println("Response invalid.");
                    System.out.println("Please enter an option number or 'exit' to leave.");

                } else if (response.equals("1")) {
                    //they want to see the songs
                    SongRequest showList = new SongRequest(false);

                    outServer.writeObject(showList);
                    outServer.flush();

                    ResponseListener listThread = new ResponseListener(serverConnection);
                    Thread t = new Thread(listThread);
                    t.start();

                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        System.out.println("Thread Join Interrupted");
                    }

                } else if (response.equals("2")) {
                    //they want to download the song

                    System.out.println("Please enter the song title");
                    String songName = scan.nextLine();
                    System.out.println("Please enter the artist name");
                    String artist = scan.nextLine();
                    SongRequest songRequest = new SongRequest(true, songName, artist);

                    outServer.writeObject(songRequest);
                    outServer.flush();

                    ResponseListener downloadThread = new ResponseListener(serverConnection);

                    Thread t = new Thread(downloadThread);
                    t.start();

                    try {
                        t.join();
                        System.out.println("Download Successful!");
                    } catch (InterruptedException e) {
                        System.out.println("Thread Join Interrupted");
                    }

                } else if (response.equalsIgnoreCase("exit")) {
                    // stop the program
                    break;
                } else {
                    //input must be invalid
                    System.out.println("Response invalid.");
                    System.out.println("Please enter an option number or 'exit' to leave.");
                }

                outServer.close();
                serverConnection.close();
            }

            scan.close();

        } catch (IOException e) {
            System.out.println("A file input/output exception occurred");

            System.out.printf("Exception message: %s\n", e.getMessage());

            if (serverConnection != null) {
                try {
                    serverConnection.close();
                } catch (IOException i) {
                    i.printStackTrace();
                } //end try catch
            } //end if
        } //end try catch
    }
}

/**
 *
 */

/**
 * Proj 4 -- MP3 Server
 *
 * This class implements Runnable, and will contain the logic for listening for
 * server responses. The threads you create in MP3Server will be constructed using
 * instances of this class.
 *
 * @author Aidan Molnar, lab 17
 * @author Annah Aunger, lab 17
 *
 * @version April 11, 2019
 *
 */
final class ResponseListener implements Runnable {

    private ObjectInputStream ois;

    public ResponseListener(Socket clientSocket) throws IOException {
        //This constructor takes in a socket and builds the ObjectInputStream with it.

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

        //This is the only method, you will need to implement in this class.
        // Simply put, you will need to read in the first object you get as a
        // SongHeader message (after checking that it isn't null and a different object).
        // Once you do, you determine what type of message it is. If it is a download message
        // you will need to receive all of the bytes from the output stream and write it to a
        // file in the savedSongs directory with the name

        try {

            do {

                SongHeaderMessage header = (SongHeaderMessage) ois.readObject();

                if (header == null) {

                } else if (header instanceof SongHeaderMessage) {
                    if ((header).isSongHeader()) { // is is a download request

                        if ((header).getFileSize() != -1) { //there are bytes to be written

                            // you will need to receive all of the bytes from the output stream and write it to a
                            // file in the savedSongs directory with the name
                            //“<Artist> - <Song name>.mp3”

                            String filename = String.format("savedSongs/%s - %s.mp3", header.getArtistName(),
                                    header.getSongName());

                            Object fromServer;

                            byte[] songBytes = new byte [header.getFileSize()];
                            int offset = 0;

                            do {
                                fromServer = ois.readObject();

                                if (fromServer != null) {
                                    //System.out.println(offset);
                                    SongDataMessage dataMessage = (SongDataMessage) fromServer;
                                    System.arraycopy(dataMessage.getData(), 0, songBytes , offset,
                                            Math.min(1000, songBytes.length - offset));
                                    offset += 1000;
                                }

                            } while (fromServer != null && offset < songBytes.length);

                            this.writeByteArrayToFile(songBytes, filename);
                        } else {
                            // else there are NO bytes to be written
                            System.out.println("Song not available!");
                        }

                    } else { // the user wants to see the list

                        // print all the strings you are receiving
                        // (Since you will just be receiving a list of stuff in the record).
                        // SongDataMessages, takes the byte data from those data messages and writes it into a
                        // properly named file.
                        Object fromServer;

                        do {
                            fromServer = ois.readObject();

                            if (fromServer != null) {
                                System.out.println((String) fromServer);
                            }
                        } while (fromServer != null);

                    }
                    break;

                } else {
                    //this is not a songHeaderObject

                    System.out.println("Server did not send a header...");
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

            fos.write(songBytes);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception a) {
                System.out.println("Error in closing fos" + a);
            }
        } // end finally
    }
}