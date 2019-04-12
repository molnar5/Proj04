import java.io.Serializable;

/**
 * Proj 4 -- Song Data Message
 *
 * A class that stores pieces of a song.
 *
 * @author Aidan Molnar, lab 17
 * @author Annah Aunger, lab 17
 *
 * @version March 20, 2019
 *
 */
public class SongDataMessage implements Serializable {
    private byte[] data;

    public SongDataMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
