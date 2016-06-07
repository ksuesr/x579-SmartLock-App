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
    public String getTitle() { return desc; }

    public Card(String cardKey, String desc, int id) {
        matched_apdu = CardService.HexStringToByteArray(cardKey);
        this.desc = desc;
        this.card_id = id;
    }

    //
    // static members
    //
    // consider to use Map instance instead
    private static List<Card> card_list;
    private static int head = 0;

    static {
        card_list = new ArrayList<>();
    }

    public static void globalInit(Context c) {
        int max_id = Integer.parseInt(SessionStorage.get(c, "max_id", "0"));
        for(int i = 0; i <= max_id; i++) {
            if(SessionStorage.exists(c, i)) {
                card_list.add(new Card(SessionStorage.get(c, "card[" + i + " ].key", "empty"),
                                       SessionStorage.get(c, "card[" + i + "].title", "empty"), i));
            }
        }
    }

    public static void addNewCard(Context c, String cardKey, String secret, String title) {
        int id = Integer.parseInt(SessionStorage.get(c, "max_id", "0"));
        card_list.add(new Card(cardKey, title, id));
        AccountStorage.SetAccount(c, secret, id);
        SessionStorage.set(c, "max_id", "" + id);
        SessionStorage.set(c, "card[" + id + "].key", cardKey);
        SessionStorage.set(c, "card[" + id + "].title", title);
    }

    public static void deleteCard(Context c, int id) {
        Iterator<Card> it = card_list.iterator();
        Card card = null;
        while(it.hasNext()) {
            card = it.next();
            if(card.getCardId() != id) break;
        }
        if(card != null) {
            card_list.remove(card);
            SessionStorage.expire(c, "card[" + id + "].key");
            SessionStorage.expire(c, "card[" + id + "].title");
        }
    }

    public static List<Card> getCardList() {
        return card_list;
    }

    public static int listCount() {
        return card_list.size();
    }
}
