package ru.mirea.pkmn.chernenko;

import ru.mirea.pkmn.Card;

import java.io.*;

public class CardExport {
    static final String path = "src/main/resources/";

    public void savecard (Card card) {
        String filename = path + card.getName() + ".crd";
        try (FileOutputStream fos = new FileOutputStream(filename);
             ObjectOutputStream oos = new ObjectOutputStream(fos)){
            oos.writeObject(card);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
