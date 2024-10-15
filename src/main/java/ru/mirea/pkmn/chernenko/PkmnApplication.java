package ru.mirea.pkmn.chernenko;

import ru.mirea.pkmn.*;

import java.io.IOException;

public class PkmnApplication {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CardImport cardImport = new CardImport("my_card.txt");
        Card my = cardImport.load();
        System.out.println(my);

        //CardExport cardExport = new CardExport();
        //cardExport.savecard(my);
        //CardExport cardExport = new CardExport.savecard(my);

        CardImport reccard = new CardImport("Pyroar");
        Card des = reccard.deserialize();
        System.out.println(des);
    }
}
