package byeonghoon.x579.smartlock.cardapp;


import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CardService extends HostApduService {

    private static final String TAG = "CardService";

    private static final String SELECT_APDU_HEADER = "00A40400";
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    private static final byte[] UNKNOWN_COMMAND_SW = HexStringToByteArray("0000");

    private Map<String, Double> input_rows;

    public CardService() {
        input_rows = new HashMap<>();
    }

    @Override
    public void onDeactivated(int reason) {}

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String stringifiedApdu = ByteArrayToHexString(commandApdu);
        Log.i(TAG, "Received APDU: " + stringifiedApdu);
        input_rows = new HashMap<>();

        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        Card target = null;
        if("F0000000000000".equals(stringifiedApdu)) {
            Log.i(TAG, "Now using temporary permission");
            //Assign temporary ID to temporary card
            target = new Card(stringifiedApdu, "TEMP", -1);
        }
        Iterator<Card> iterator = Card.getCardList().iterator();
        while(iterator.hasNext()) {
            target = iterator.next();
            if(Arrays.equals(target.getApdu(), commandApdu)) {
                break;
            }
        }
        if (target != null) {
            String account;
            if(target.getCardId() >= 0) {
                account = AccountStorage.GetAccount(this, target.getCardId());
            } else {
                account = "Please generate from input random code!!!";
            }
            byte[] accountBytes = account.getBytes();
            Log.i(TAG, "Sending account number: " + account);

            //TODO: add something useful to server
            PostToServerTask task = new PostToServerTask(input_rows);
            task.execute();

            return ConcatArrays(accountBytes, SELECT_OK_SW);
        } else {
            return UNKNOWN_COMMAND_SW;
        }

    }

    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }

    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
