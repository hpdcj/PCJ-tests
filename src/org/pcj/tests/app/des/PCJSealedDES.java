//    PCJSealdDES.java
//    based on Nick Carey code

////////////////////////////////////////////////////////////////////////////////
//
//  class: PCJSealedDES
//
//  SealedDES encapsulates the DES encryption and decryption of Strings
//  into SealedObjects.  It represesnts keys as integers (for simplicity).
//
////////////////////////////////////////////////////////////////////////////////
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Random;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Scanner;
import org.pcj.*;

@RegisterStorage(PCJSealedDES.Shared.class)
class PCJSealedDES implements StartPoint {

    @Storage(PCJSealedDES.class)
    enum Shared {
        key, plainstr, finish
    }
    public long key;
    String plainstr;
    boolean finish;

    // Cipher for the class
    Cipher des_cipher;

    // Variable controlling debug print statements
    private final static int DEBUG = 1;

    // Key for the class
    SecretKeySpec the_key = null;

    // Byte arrays that hold key block
    byte[] deskeyIN = new byte[8];
    byte[] deskeyOUT = new byte[8];

    //ITS CHRISTMAS TIME OPEN UP YOUR PRESENTS
    //oh my goodness I have my very own SealedObject!
    // (I have my own copy so my brothers and sisters wont fight me for it)
    SealedObject mySealedObj;
    // Starting key value to test. run() will test keys starting in the range
    // of myStartKey(inclusive) to myEndKey (exclusive)
    long myStartKey;
    long myEndKey;
    // Execution runtime starttime, for bookeeping and output
    long runstart;
    // known plaintext to search decryption output for
    String searchPlaintext;

    // Deploying nodes
    public static void main(String[] args) throws IOException {
        String nodesFile = "nodes.txt";
        PCJ.deploy(PCJSealedDES.class, new NodesDescription("nodes.txt"));
    }

    public PCJSealedDES() {
    }

    /**
     * PCJ implementation will test keys in range of myStartKey
     * and myEndKey on mySealedObj - will output progress reports every so often
     * - Tests keys by looking for some known plaintext in the possibly
     * decrypted output - in this case, "komputer"
     *
     */
    @Override
    public void main() throws Throwable {

        int numThreads = PCJ.threadCount();
        long keybits = 32;

        long maxkey = ~(0L);
        maxkey = maxkey >>> (64 - keybits);

        if (PCJ.myId() == 0) {
            System.out.println("Key lengths  "  + keybits);
            System.out.println("Algoritihm:  "  + "DES");
            System.out.println("Number of configurations tosearch: " + (maxkey + 1));
            System.out.println("Number of pCJ threads (cores): "  + PCJ.threadCount());
        }

        // Create a simple cipher
        try {
            des_cipher = Cipher.getInstance("DES");
        } catch (Exception e) {
            System.out.println("Failed to create cipher.  Exception: " + e.toString() + " Message: " + e.getMessage());
        }

        long keyg = 0;
        // Thread 0 generates a key and broadcasts it
        if (PCJ.myId() == 0) {
            Random generator = new Random();
            keyg = generator.nextLong();
            // Mask off the high bits so we get a short key
            keyg = keyg & maxkey;
            PCJ.broadcast(keyg, Shared.key);
        }
        // Wait for a key
        PCJ.waitFor(Shared.key);
        // Set up a key
        this.setKey(key);

        // Generate a sample string
        plainstr = "Tu okeanos - superkomputer w ICM UW";
        // Thread 0 inputs text and broadcasts it

        if (PCJ.myId() == 0) {
            System.out.println("Give text do encode (has to contain string >>komputer<<:)" );

            Scanner sc = new Scanner(System.in);
            // String str = sc.nextLine();
            String str = "Tu okeanos - superkomputer w ICM UW";
            // Mask off the high bits so we get a short key
            PCJ.broadcast(str, Shared.plainstr);
        }
        // Wait for a key
        PCJ.waitFor(Shared.plainstr);

        long runstart = System.currentTimeMillis();
        // Encrypt and get a copy of SealedObject for each thread
        mySealedObj = this.encrypt(plainstr);

        // Here ends the set-up.  Pretending like we know nothing except sldObj,
        // discover what key was used to encrypt the message.
        // calculate interval to test
        long interval = maxkey / numThreads;
        myStartKey = PCJ.myId() * interval;
        myEndKey = myStartKey + interval;
        if (PCJ.myId() == numThreads - 1) {
            myEndKey = maxkey;
        }
        finish = false;

        //plaintext hint we are searching for
        searchPlaintext = "komputer";

        // create object to printf to the console
        // PrintStream p = new PrintStream(System.out);
        // Search for the right key
        for (long i = myStartKey; i < myEndKey; i++) {
            // Set the key and decipher the object
            this.setKey(i);
            String decryptstr = this.decrypt(mySealedObj);

            // Does the object contain the known plaintext
            if ((decryptstr != null) && (decryptstr.indexOf(searchPlaintext) != -1)) {
                //  Remote printlns if running for time.
                System.out.printf("Klucz %016x koduje tekst: %s\n", i, decryptstr);
                System.out.printf("Czas "+ 0.001*(System.currentTimeMillis() - runstart)+"s\n");
                finish = true;
                PCJ.broadcast(finish, Shared.finish);
            }

            // Update progress every once in awhile.
            //  Remote printlns if running for time.
            if (i % 100000 == 0 && DEBUG == 1) {
                long elapsed = System.currentTimeMillis() - runstart;
                System.out.println("Thread " + PCJ.myId() + " searched key number " + i + " at " + elapsed + " milliseconds.");
            }

            if (finish) {
             //   For tests we do not break
             //   break;
            }
        }
                System.out.printf("Czas całkowity "+PCJ.myId() + " " + 0.001*(System.currentTimeMillis() - runstart)+"s\n");
    }

    // Decrypt the SealedObject
    //
    //   arguments: SealedObject that holds on encrypted String
    //   returns: plaintext String or null if a decryption error
    //     This function will often return null when using an incorrect key.
    //
    public String decrypt(SealedObject cipherObj) {
        try {
            return (String) cipherObj.getObject(the_key);
        } catch (Exception e) {
            //do nothing :)
        }
        return null;
    }

    // Encrypt the message
    //
    //  arguments: a String to be encrypted
    //  returns: a SealedObject containing the encrypted string
    //
    public SealedObject encrypt(String plainstr) {
        try {
            des_cipher.init(Cipher.ENCRYPT_MODE, the_key);
            return new SealedObject(plainstr, des_cipher);
        } catch (Exception e) {
            System.out.println("Failed to encrypt message. " + plainstr + ". Exception: " + e.toString() + ". Message: " + e.getMessage());
        }
        return null;
    }

    // Encrypt the message
    // This method is a copy of the one above but is useful for returning mass copies for parallel execution
    //  arguments: a String to be encrypted, num of copies in array
    //  returns: an array of SealedObject copies containing the encrypted string
    public SealedObject[] encryptArr(String plainstr, int copies) {
        try {
            des_cipher.init(Cipher.ENCRYPT_MODE, the_key);
            SealedObject[] arr = new SealedObject[copies];
            for (int i = 0; i < copies; i++) {
                arr[i] = new SealedObject(plainstr, des_cipher);
            }
            return arr;
        } catch (Exception e) {
            System.out.println("Failed to encrypt message. " + plainstr + ". Exception: " + e.toString() + ". Message: " + e.getMessage());
        }
        return null;
    }

    //  Build a DES formatted key
    //
    //  Convert an array of 7 bytes into an array of 8 bytes.
    //
    private static void makeDESKey(byte[] in, byte[] out) {
        out[0] = (byte) ((in[0] >> 1) & 0xff);
        out[1] = (byte) ((((in[0] & 0x01) << 6) | (((in[1] & 0xff) >> 2) & 0xff)) & 0xff);
        out[2] = (byte) ((((in[1] & 0x03) << 5) | (((in[2] & 0xff) >> 3) & 0xff)) & 0xff);
        out[3] = (byte) ((((in[2] & 0x07) << 4) | (((in[3] & 0xff) >> 4) & 0xff)) & 0xff);
        out[4] = (byte) ((((in[3] & 0x0F) << 3) | (((in[4] & 0xff) >> 5) & 0xff)) & 0xff);
        out[5] = (byte) ((((in[4] & 0x1F) << 2) | (((in[5] & 0xff) >> 6) & 0xff)) & 0xff);
        out[6] = (byte) ((((in[5] & 0x3F) << 1) | (((in[6] & 0xff) >> 7) & 0xff)) & 0xff);
        out[7] = (byte) (in[6] & 0x7F);

        for (int i = 0; i < 8; i++) {
            out[i] = (byte) (out[i] << 1);
        }
    }

    // Set the key (convert from a long integer)
    public void setKey(long theKey) {
        try {
            // convert the integer to the 8 bytes required of keys
            deskeyIN[0] = (byte) (theKey & 0xFF);
            deskeyIN[1] = (byte) ((theKey >> 8) & 0xFF);
            deskeyIN[2] = (byte) ((theKey >> 16) & 0xFF);
            deskeyIN[3] = (byte) ((theKey >> 24) & 0xFF);
            deskeyIN[4] = (byte) ((theKey >> 32) & 0xFF);
            deskeyIN[5] = (byte) ((theKey >> 40) & 0xFF);
            deskeyIN[6] = (byte) ((theKey >> 48) & 0xFF);

            // theKey should never be larger than 56-bits, so this should always be 0
            deskeyIN[7] = (byte) ((theKey >> 56) & 0xFF);

            // turn the 56-bits into a proper 64-bit DES key
            makeDESKey(deskeyIN, deskeyOUT);

            // Create the specific key for DES
            the_key = new SecretKeySpec(deskeyOUT, "DES");
        } catch (Exception e) {
            System.out.println("Failed to assign key" + theKey + ". Exception: " + e.toString() + ". Message: " + e.getMessage());
        }
    }
}