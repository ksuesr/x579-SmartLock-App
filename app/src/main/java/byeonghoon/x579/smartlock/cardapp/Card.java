package byeonghoon.x579.smartlock.cardapp;

import android.content.Context;

import java.util.*;

/**
 * Created by Byeonghoon on 31-May-16.
 */
public class Card {

    private byte[] matched_apdu;
    private int card_id;
    private String desc;



    public byte[] getApdu() { return matched_apdu; }
    public int getCardId() { return card_id; }

    public Card(String cardKey, String desc, int id) {
        matched_apdu = CardService.BuildSelectApdu(cardKey);
        this.desc = desc;
        this.card_id = id;
    }

    //
    // static members
    //
    private static List<Card> card_list;

    static {
        card_list = new ArrayList<>();
        //TODO: restore from preference storage
    }

    public static void addNewCard(Context c, String cardKey, String secret, String desc) {
        int id = card_list.size();
        card_list.add(new Card(cardKey, desc, id));
        AccountStorage.SetAccount(c, secret, id);
    }

    public static List<Card> getCardList() {
        return card_list;
    }

    public static int listCount() {
        return card_list.size();
    }
}
