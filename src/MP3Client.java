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
 * An MP3 Client to request .mp3 files from a server and receive them over the socket connection.
 */
public class MP3Client {

    public static void main(String[] args) {
        //TODO: Implement main

        Socket serverConnection = null;
        Scanner scan = new Scanner(System.in);
        ObjectOutputStream outServer;
        String response;

        try {
            while (true) {
                serverConnection = new Socket("localhost", 9478);
                //serverConnection = new Socket("66.70.189.118", 9478);

                outServer = new ObjectOutputStream(serverConnection.getOutputStream());
                outServer.flush();

                System.out.println("<Connected to the server>");

                System.out.println("============ Options ============");
                System.out.println("(1) See list of available songs");
                System.out.println("(2) Request song download");
                System.out.println(" *  Exit");
                System.out.println("=================================");

                System.out.println("Please enter an option number or 'exit' to leave.");

                //outServer.writeObject(scan.nextLine());

                if (scan.hasNextLine()) {
                    response = scan.nextLine();
                } else {
                    System.out.println("<Lost the connection with the server>");

                    return;
                } //end if

                //System.out.printf("Response from the server: %s\n", response);

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

            do {

                SongHeaderMessage header = (SongHeaderMessage) ois.readObject();

                if (header == null) {

                } else if (header instanceof SongHeaderMessage){
                    if ((header).isSongHeader()) { // is is a download request

                        if ((header).getFileSize() != -1) { //there are bytes to be written

                            //format: SongHeaderMessage(true, songName, artistName, byteArray.length);

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

                            } while (fromServer != null);

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

                        // TODO: how to make sure that ALL the strings are printed
                    }
                    break;

                } else {
                    //TODO: this is not a songHeaderObject... what to do now?

                    // I dunno
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

            //FileWriter fw = new FileWriter(file);
            //bw = new BufferedWriter(fw);

            //TODO: not sure if this is right
            //create loop to write song bytes
            /*
            if (songBytes.length > 0) {
                for (int i = 0; i < fileName.length(); i++) {
                    fos.write(songBytes[i]);
                    fos.flush();
                }

            }
            */
            fos.write(songBytes);
            fos.close();

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
