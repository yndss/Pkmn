package ru.mirea.pkmn.chernenko;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mirea.pkmn.*;
import ru.mirea.pkmn.chernenko.web.http.PkmnHttpClient;
import ru.mirea.pkmn.chernenko.web.jdbc.DatabaseServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

public class PkmnApplication {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        //CardImport cardImport = new CardImport("my_card.txt");
        //Card my = cardImport.load();
        //System.out.println(my);

        //CardExport cardExport = new CardExport();
        //cardExport.savecard(my);

//        CardImport reccard = new CardImport("Pyroar");
//        Card des = reccard.deserialize();
//        System.out.println(des);

        CardImport cardImport = new CardImport("my_card.txt");
        Card my = cardImport.load();
        String name = my.getName();
        String cardnum = my.getNumber();
        System.out.println(my);

        PkmnHttpClient pkmnHttpClient = new PkmnHttpClient();

        JsonNode card = pkmnHttpClient.getPokemonCard("Pyroar", "32");
        //System.out.println(card.toPrettyString());


        for (JsonNode attack : card.findValues("attacks").get(0)) {
            for (AttackSkill skill : my.getSkills()) {
                if (skill.getName().equals(attack.findValue("name").asText())) {
                    skill.setDescription(attack.findValue("text").asText());
                }
            }
        }
        System.out.println(my.getSkills());

        CardExport cardExport = new CardExport();
        cardExport.savecard(my);
        //System.out.println(card.findValues("attacks").toString());
        /*        .stream()
                .map(JsonNode::toPrettyString)
                .collect(Collectors.toSet())); */

        DatabaseServiceImpl dsi = new DatabaseServiceImpl();
        //dsi.saveCardToDatabase(my);
        Card vcard = dsi.getCardFromDatabase("Pyroar");
        System.out.println(vcard);

        //dsi.createPokemonOwner(my.getPokemonOwner());
        //Student me = dsi.getStudentFromDatabase();

        //dsi.saveCardToDatabase(my);
        Card card2 = dsi.getCardFromDatabase("Manaphy");
        System.out.println(card2);
    }
}
