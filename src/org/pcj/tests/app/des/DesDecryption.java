package org.pcj.tests.app.des;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.pcj.PCJ;
import org.pcj.RegisterStorage;
import org.pcj.StartPoint;
import org.pcj.Storage;

@RegisterStorage(DesDecryption.Vars.class)
public class DesDecryption implements StartPoint {

    private byte[] secret;
    private byte[] textEncrypted;
    private boolean found;
    private long maxKey;

    @Storage(DesDecryption.class)
    enum Vars {
        secret,
        textEncrypted,
        found,
        maxKey
    }

    public void main() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher desCipher = Cipher.getInstance("DES");

        int keyBits = -1;
        if (PCJ.myId() == 0) {
            String secretText = System.getProperty("desSecretText", "1234567890");
            keyBits = Integer.parseInt(System.getProperty("desKeyBits", "25"));

            PCJ.asyncBroadcast(false, Vars.found);
            PCJ.asyncBroadcast((1L << keyBits) - 1, Vars.maxKey);

            secret = secretText.getBytes();
            PCJ.asyncBroadcast(secret, Vars.secret);

            MyDesKey encryptionKey = new MyDesKey(ThreadLocalRandom.current().nextLong(1L << keyBits));
            desCipher.init(Cipher.ENCRYPT_MODE, encryptionKey);

            PCJ.asyncBroadcast(desCipher.doFinal(secret), Vars.textEncrypted);
        }

        PCJ.waitFor(Vars.found);
        PCJ.waitFor(Vars.secret);
        PCJ.waitFor(Vars.maxKey);
        PCJ.waitFor(Vars.textEncrypted);

        long start = System.nanoTime();

        MyDesKey decryptionKey = new MyDesKey(PCJ.myId());
        for (long value = PCJ.myId(); value < maxKey; value += PCJ.threadCount()) {
            try {
                desCipher.init(Cipher.DECRYPT_MODE, decryptionKey);

                byte[] textDecrypted = desCipher.doFinal(textEncrypted);
                if (Arrays.equals(secret, textDecrypted)) {
                    System.err.println("[" + PCJ.myId() + "] Found key: '" + value + "' -> " + new String(textDecrypted));
                    found = true;
                    PCJ.asyncBroadcast(found, Vars.found);
                }
//                if (found) break;
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException ignored) {
            } finally {
                decryptionKey.addToKey(PCJ.threadCount());
            }
        }

        System.err.println("[" + PCJ.myId() + "] My time = " + (System.nanoTime() - start) / 1e9);

        PCJ.barrier();
        if (PCJ.myId() == 0) {
            long duration = System.nanoTime() - start;

            System.out.format("DesDecryption\t%5d\tkeyBits %d\ttime %12.7f%n",
                    PCJ.threadCount(), keyBits, duration / 1e9);
        }
    }

    public static class MyDesKey implements Key {

        private long keyValue;
        private final byte[] key;

        public MyDesKey(long keyValue) {
            this.keyValue = keyValue;
            this.key = new byte[8];

            updateKey();
        }

        private void updateKey() {
            key[0] = (byte) ((keyValue & 0b01111111) << 1);
            key[1] = (byte) (((keyValue >> 7) & 0b01111111) << 1);
            key[2] = (byte) (((keyValue >> 14) & 0b01111111) << 1);
            key[3] = (byte) (((keyValue >> 21) & 0b01111111) << 1);
            key[4] = (byte) (((keyValue >> 28) & 0b01111111) << 1);
            key[5] = (byte) (((keyValue >> 35) & 0b01111111) << 1);
            key[6] = (byte) (((keyValue >> 42) & 0b01111111) << 1);
            key[7] = (byte) (((keyValue >> 49) & 0b01111111) << 1);
        }

        @Override
        public String getAlgorithm() {
            return "DES";
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

        @Override
        public byte[] getEncoded() {
            return key.clone();
        }

        public void addToKey(long deltaValue) {
            keyValue += deltaValue;
            updateKey();
        }
    }
}

