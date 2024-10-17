package ru.mirea.pkmn.chernenko;

import ru.mirea.pkmn.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CardImport {
    static final String path = "src/main/resources/";
    private String name;
    private Card card;

    public CardImport(String name) {
        this.name = name;
    }
    public Card load() {
        File file = new File(path + name);
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            //1
            PokemonStage stage = PokemonStage.valueOf(br.readLine().split("\\.")[1].trim().toUpperCase());
            //2
            String name = br.readLine().split("\\.")[1].trim();
            //3
            int hp = Integer.parseInt(br.readLine().split("\\.")[1].trim());
            //4
            EnergyType pokemontype = EnergyType.valueOf(br.readLine().split("\\.")[1].trim().toUpperCase());
            //5
            String evolvesFromStr = br.readLine().split("5.")[1].trim();
            Card evolfrm = null;
            if (!evolvesFromStr.equals("-")) {
                evolfrm = new CardImport(evolvesFromStr).load();
            }
            //6
            String skillstr = br.readLine().split("\\.")[1];
            List<AttackSkill> skill = new ArrayList<>();

            String[] skillsData = skillstr.split(",");
            for (String skillData : skillsData) {
                String[] data = skillData.split("/");
                String cost = data[0].trim();
                String skillname = data[1];
                int damage = Integer.parseInt(data[2]);

                skill.add(new AttackSkill(skillname, "", cost, damage));
            }
            //7
            EnergyType weakness = EnergyType.valueOf(br.readLine().split("\\.")[1].trim().toUpperCase());
            //8
            String res = br.readLine().split("\\.")[1].trim().toUpperCase();
            EnergyType resistance = null;
            if (!res.equals("-")) {
                resistance = EnergyType.valueOf(res);
            }
            //9
            String tetreat = br.readLine().split("\\.")[1].trim();
            //10
            String games = br.readLine().split("\\.")[1].trim();
            //11
            String regulstr = br.readLine().split("\\.")[1].trim();
            Character regulm = regulstr.charAt(0);
            //12
            String ststr = br.readLine();
            Student own = null;
            if (ststr != null) {
                ststr.split("\\.")[1].trim();
                String[] stud = ststr.split("/");
                own = new Student(stud[1], stud[0], stud[2], stud[3]);
            }
            return new Card(stage, name, hp, pokemontype, evolfrm, skill, weakness, resistance, tetreat, games, regulm, own);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Card getCard() {
        return card;
    }

    public Card deserialize() throws IOException, ClassNotFoundException {
        File file = new File(path + name + ".crd");
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis);) {
            return (Card) ois.readObject();
        }
    }
}
