package byeonghoon.x579.smartlock.cardapp;


import android.content.Context;
import android.nfc.cardemulation.HostApduService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CardService extends HostApduService {

    private static final String TAG = "CardService";

    private static final String SELECT_APDU_HEADER = "00A40400";
    private static final String APDU_RESPONSE_HEADER = "EA004DAC";
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    private static final byte[] UNKNOWN_COMMAND_SW = HexStringToByteArray("0000");
    private static final byte[] UNCLEAR_COMMAND_SW = HexStringToByteArray("0010");

    private Map<String, Double> input_rows;
    private String type="01";

    public CardService() {
        input_rows = new HashMap<>();

    }

    @Override public void onCreate() {
        SessionStorage.expire(getApplicationContext(), "waiting.response");
        Card.globalInit(getApplicationContext());
    }

    @Override
    public void onDeactivated(int reason) {
        SessionStorage.expire(getApplicationContext(), "waiting.response");
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String stringifiedApdu = ByteArrayToHexString(commandApdu);
        boolean is_temporary = false;
        Log.i(TAG, "Received APDU: " + stringifiedApdu);
        input_rows = new HashMap<>();
        Context ctx = getApplicationContext();

        if(SessionStorage.exists(ctx, "waiting.response")) {
            Log.i(TAG, "Waiting response");
            if (stringifiedApdu.startsWith(APDU_RESPONSE_HEADER)) {
                Log.i(TAG, "Receive response");
                SessionStorage.expire(ctx, "waiting.response");
                if(processResponse(commandApdu[4], commandApdu[5])) {
                    return UNKNOWN_COMMAND_SW; // means session close.
                }
                return HexStringToByteArray("ABCDEF");
            } else {
                Log.i(TAG, "Unclear");
                return UNCLEAR_COMMAND_SW;
            }
        }

        if(SessionStorage.exists(ctx, "register.action")) {
            SessionStorage.set(ctx, "register.cardkey", stringifiedApdu);
            is_temporary = true;
            type = "00";
            Log.i(TAG, "Type: register");
        } else if(SessionStorage.exists(ctx, "permission.time.receive.start")) {
            long start = Long.parseLong(SessionStorage.get(ctx,"permission.time.receive.start", "-1"));
            if(System.currentTimeMillis() > (start + Long.parseLong(SessionStorage.get(ctx, "permission.time.receive.duration", "-1")))) {
                SessionStorage.expire(ctx, "permission.time.receive.start");
                SessionStorage.expire(ctx, "permission.time.receive.duration");
                SessionStorage.expire(ctx, "permission.temporary.receive.code");
            }
            else {
                is_temporary = true;
                type = "07";
                Log.i(TAG, "Type: receive");
            }

        } else if(SessionStorage.exists(ctx, "permission.time.send.start")) {
            long start = Long.parseLong(SessionStorage.get(ctx,"permission.time.send.start", "-1"));
            if(System.currentTimeMillis() > (start + Long.parseLong(SessionStorage.get(ctx, "permission.time.send.duration", "-1")))) {
                SessionStorage.expire(ctx, "permission.time.send.start");
                SessionStorage.expire(ctx, "permission.time.send.duration");
                SessionStorage.expire(ctx, "permission.temporary.send.configure");
                SessionStorage.expire(ctx, "permission.temporary.send.code");
            }
            else {
                is_temporary = true;
                type = "05";
                Log.i(TAG, "Type: allow");
            }

        } else if(SessionStorage.exists(ctx, "permission.cancel")) {
            type = "06";
            Log.i(TAG, "Type: disallow");
        } else {
            type = "01";
            Log.i(TAG, "Type: open");
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
                if(Arrays.equals(c.getApdu(), commandApdu)) {
                    target = c;
                    break;
                }
            }
        }
        if (target != null) {
            String account = buildNFCResponse(type, target);

            byte[] accountBytes = account.getBytes();

            input_rows.put("inputType", Double.parseDouble(type));
            PostToServerTask task = new PostToServerTask(this, stringifiedApdu, input_rows);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Log.i(TAG, "Sending account number: " + account);
            SessionStorage.set(ctx, "waiting.response", "1");
            return ConcatArrays(HexStringToByteArray(type), accountBytes, SELECT_OK_SW);
        } else {
            Log.i(TAG, "Unknown command");
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
        // use case 1: card register
        //             accountBytes will be 00 + encrypt(user.id + card secret)
        //             if empty lock, green light/store card secret; if owner's other lock
        // use case 2: open/close by owner
        //             accountBytes: 01/02 + encrypt(user.id + card secret) // 01 for open, 02 for close
        // use case 3: allow/disallow others to open
        //             accountBytes: 05/06 + (temporary code == encrypt(permission.time.start + 00 + permission.time.duration(max 1 hour) + 15 + card secret))
        // use case 4: other's access
        //             accountBytes: 07/08 + temporary code
        String account;
        Context ctx = getApplicationContext();
        switch(type) {
            case "00": // register
                SecureRandom random = new SecureRandom();
                byte[] bytes = new byte[14];
                String secret;
                random.nextBytes(bytes);
                secret = CardService.ByteArrayToHexString(bytes);
                SessionStorage.set(ctx, "temp.secret", secret);
                account = "" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date(System.currentTimeMillis())) + "#" + SessionStorage.get(ctx, "user.id", "-1") + "#" + secret;
                break;
            case "01": // open by owner
            case "06": // disallow grant permission
                account = SessionStorage.get(ctx, "user.id", "-1") + "#" + AccountStorage.GetAccount(ctx, target.getCardId());
                break;
            case "05": // allow grant permission
                account = SessionStorage.get(ctx, "permission.temporary.send.code", "0000");
                break;
            case "07": // Using granted permission
                account = SessionStorage.get(ctx, "permission.temporary.receive.code", "0000");
                break;
            default:
                account = "0000";
        }
        return account;
    }


    private boolean processResponse(byte in_response_to, byte response_code) {
        Context ctx = getApplicationContext();
        boolean is_succeed = (response_code == 0);
        switch(in_response_to) {
            case 0:
                if(response_code == 0) {
                    String cardKey = SessionStorage.get(ctx, "register.cardkey", "F000000000");
                    String secret = SessionStorage.get(ctx, "temp.secret", "00000000");
                    String title = SessionStorage.get(ctx, "register.action.title", "smart lock");
                    Log.i(TAG, "cardKey: " + cardKey + ", secret: " + secret + ", title: " + title);
                    Card.addNewCard(ctx, cardKey, secret, title);
                    Toast.makeText(ctx, "Success!", Toast.LENGTH_SHORT).show();
                    SessionStorage.set(ctx, "register.action.complete", "0");
                    SessionStorage.expire(ctx, "temp.secret");
                } else if(response_code == 1) {
                    Toast.makeText(ctx, "It's already yours!", Toast.LENGTH_LONG).show();
                    SessionStorage.set(ctx, "register.action.complete", "1");
                } else if(response_code == 2) {
                    Toast.makeText(ctx, "It's not yours!", Toast.LENGTH_LONG).show();
                    SessionStorage.set(ctx, "register.action.complete", "1");
                }

                break;
            case 1:
                if(response_code != 0) {
                    Toast.makeText(ctx, "Incorrect lock", Toast.LENGTH_SHORT).show();
                }
                break;
            case 5:
                if(response_code == 0) {
                    Toast.makeText(ctx, "Succeed!", Toast.LENGTH_SHORT).show();
                    SessionStorage.expire(ctx, "permission.time.send.start");
                } else if(response_code == 1) {
                    Toast.makeText(ctx, "It's not yours!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                if(response_code == 0) {
                    Toast.makeText(ctx, "Succeed!", Toast.LENGTH_SHORT).show();
                } else if(response_code == 1) {
                    Toast.makeText(ctx, "It's not yours!", Toast.LENGTH_SHORT).show();
                } else if(response_code == 2) {
                    Toast.makeText(ctx, "Already canceled one!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case 7:
                if(response_code == 0) {
                    Toast.makeText(ctx, "Succeed!", Toast.LENGTH_SHORT).show();
                    SessionStorage.expire(ctx, "permission.time.receive.start");
                } else if(response_code == 1) {
                    Toast.makeText(ctx, "It's not correct one!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "Try again", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Toast.makeText(ctx, "Unknown response :(", Toast.LENGTH_SHORT).show();
                return false;
        }
        return is_succeed;
    }
}
