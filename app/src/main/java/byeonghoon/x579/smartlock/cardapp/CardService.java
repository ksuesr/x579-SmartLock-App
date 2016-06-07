package byeonghoon.x579.smartlock.cardapp;


import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CardService extends HostApduService {

    private static final String TAG = "CardService";

    private static final String SELECT_APDU_HEADER = "00A40400";
    private static final String SELECT_APDU_RESPONSE_HEADER = "EA004DAC";
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    private static final byte[] UNKNOWN_COMMAND_SW = HexStringToByteArray("0000");

    private Map<String, Double> input_rows;
    private String type="01";

    public CardService() {
        input_rows = new HashMap<>();
        Card.globalInit(getApplicationContext());
    }

    @Override
    public void onDeactivated(int reason) {}

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String stringifiedApdu = ByteArrayToHexString(commandApdu);
        boolean is_temporary = false;
        Log.i(TAG, "Received APDU: " + stringifiedApdu);
        input_rows = new HashMap<>();

        if(Arrays.toString(commandApdu).startsWith(SELECT_APDU_RESPONSE_HEADER)) {
            // Is it really work?
            Log.i(TAG, "Receive response");
            return UNKNOWN_COMMAND_SW; // means session close.
        }

        if(SessionStorage.exists(getApplicationContext(), "register.action")) {
            String cardKey = stringifiedApdu;
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[20];
            String secret;

            random.nextBytes(bytes);
            secret = CardService.ByteArrayToHexString(bytes);
            String title = SessionStorage.get(getApplicationContext(), "register.action.title", "smart lock");
            Card.addNewCard(getApplicationContext(), cardKey, secret, title);
            type = "00";
        }

        if(SessionStorage.exists(getApplicationContext(), "permission.time.receive.start")) {
            long start = Long.parseLong(SessionStorage.get(getApplicationContext(),"permission.time.receive.start", "-1"));
            if(System.currentTimeMillis() > (start + Long.parseLong(SessionStorage.get(getApplicationContext(), "permission.time.receive.duration", "-1")))) {
                SessionStorage.expire(getApplicationContext(), "permission.time.receive.start");
                SessionStorage.expire(getApplicationContext(), "permission.time.receive.duration");
                SessionStorage.expire(getApplicationContext(), "permission.temporary.receive.code");
            }
            else {
                is_temporary = true;
                type = "07";
            }

        } else if(SessionStorage.exists(getApplicationContext(), "permission.time.send.start")) {
            long start = Long.parseLong(SessionStorage.get(getApplicationContext(),"permission.time.send.start", "-1"));
            if(System.currentTimeMillis() > (start + Long.parseLong(SessionStorage.get(getApplicationContext(), "permission.time.send.duration", "-1")))) {
                SessionStorage.expire(getApplicationContext(), "permission.time.send.start");
                SessionStorage.expire(getApplicationContext(), "permission.time.send.duration");
                SessionStorage.expire(getApplicationContext(), "permission.temporary.send.configure");
                SessionStorage.expire(getApplicationContext(), "permission.temporary.send.code");
            }
            else {
                is_temporary = true;
                type = "05";
            }

        }

        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        Card target = null;


        if(is_temporary) {
            Log.i(TAG, "Now using temporary permission");
            //Assign temporary ID to temporary card
            target = new Card(stringifiedApdu, "TEMP", -1);
        } else {
            List<Card> card_list = Card.getCardList();
            for(Card c : card_list) {
                if(Arrays.equals(target.getApdu(), commandApdu)) {
                    target = c;
                    type = "01";
                    break;
                }
            }
        }
        if (target != null) {
            String account = buildNFCResponse(type, target);

            byte[] accountBytes = account.getBytes();
            Log.i(TAG, "Sending account number: " + account);

            input_rows.put("isUnlocked", 0.0);
            PostToServerTask task = new PostToServerTask(this, stringifiedApdu, input_rows);
            task.execute();

            //TODO: respond to multiple issues
            // use case 1: card register
            //             accountBytes will be 00 + encrypt(user.id + card secret)
            //             if empty lock, green light/store card secret; if owner's other lock
            // use case 2: open/close by owner
            //             accountBytes: 01/02 + encrypt(user.id + card secret) // 01 for open, 02 for close
            // use case 3: allow/disallow others to open
            //             accountBytes: 05/06 + (temporary code == encrypt(permission.time.start + 00 + permission.time.duration(max 1 hour) + 15 + card secret))
            // use case 4: other's access
            //             accountBytes: 07/08 + temporary code
            return ConcatArrays(HexStringToByteArray(type), accountBytes, SELECT_OK_SW);
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


    public String buildNFCResponse(String type, Card target) {
        String account;
        switch(type) {
            case "00": // register
                account = "" + System.currentTimeMillis() + "#" + SessionStorage.get(getApplicationContext(), "user.id", "-1") + AccountStorage.GetAccount(getApplicationContext(), target.getCardId());
            case "01": // open by owner
            case "06": // disallow grant permission
                account = SessionStorage.get(getApplicationContext(), "user.id", "-1") + AccountStorage.GetAccount(getApplicationContext(), target.getCardId());
                break;
            case "05": // allow grant permission
                account = SessionStorage.get(getApplicationContext(), "permission.temporary.send.code", "0000");
                break;
            case "07": // Using granted permission
                account = SessionStorage.get(getApplicationContext(), "permission.temporary.receive.code", "0000");
                break;
            default:
                account = "0000";
        }
        return account;
    }

}
