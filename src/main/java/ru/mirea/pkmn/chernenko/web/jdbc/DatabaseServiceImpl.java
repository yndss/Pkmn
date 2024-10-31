package ru.mirea.pkmn.chernenko.web.jdbc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ru.mirea.pkmn.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class DatabaseServiceImpl implements DatabaseService {
    private final Connection connection;

    private final Properties databaseProperties;

    public DatabaseServiceImpl() throws SQLException, IOException {

        // Загружаем файл database.properties

        databaseProperties = new Properties();
        databaseProperties.load(new FileInputStream("src/main/resources/database.properties"));

        // Подключаемся к базе данных

        connection = DriverManager.getConnection(
                databaseProperties.getProperty("database.url"),
                databaseProperties.getProperty("database.user"),
                databaseProperties.getProperty("database.password")
        );
        System.out.println("Connection is "+(connection.isValid(0) ? "up" : "down"));
    }




    @Override
    public Card getCardFromDatabase(String cardName) throws SQLException {
        String request = "SELECT * FROM card WHERE \"name\" = ?";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setString(1, cardName);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return constructCardFromResultSet(rs);
        }
        ps.close();
        return null;
    }

    public Card getCardFromDatabaseByUUID(UUID cardUUID) throws SQLException {
        String request = "SELECT * FROM card WHERE \"id\" = ?";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setObject(1, cardUUID);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return constructCardFromResultSet(rs);
        }
        ps.close();
        return null;
    }

    private Card constructCardFromResultSet(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setPokemonStage(PokemonStage.valueOf(rs.getString("stage")));
        card.setName(rs.getString("name"));
        card.setHp(rs.getInt("hp"));
        card.setPokemonType(EnergyType.valueOf(rs.getString("pokemon_type")));
        if (rs.getString("evolves_from") != null) {
            card.setEvolvesFrom(getCardFromDatabaseByUUID(UUID.fromString(rs.getString("evolves_from"))));
        } else {
            card.setEvolvesFrom(null);
        }
        Type type = new TypeToken<List<AttackSkill>>() {}.getType();
        card.setSkills(new Gson().fromJson(rs.getString("attack_skills"), type));
        if (rs.getString("weakness_type") != null) {
            card.setWeaknessType(EnergyType.valueOf(rs.getString("weakness_type")));
        } else {
            card.setWeaknessType(null);
        }
        if(rs.getString("resistance_type") != null){
            card.setResistanceType(EnergyType.valueOf(rs.getString("resistance_type")));
        } else {
            card.setResistanceType(null);
        }
        card.setRetreatCost(rs.getString("retreat_cost"));
        card.setGameSet(rs.getString("game_set"));
        card.setRegulationMark(rs.getString("regulation_mark").charAt(0));
        if (rs.getString("pokemon_owner") != null) {
            card.setPokemonOwner(getStudentFromDatabaseByUUID(UUID.fromString(rs.getString("pokemon_owner"))));
        } else {
            card.setPokemonOwner(null);
        }
        card.setNumber(rs.getString("card_number"));

        return card;
    }

    @Override
    public Student getStudentFromDatabase(String studentName) throws SQLException {
        if (studentName == null) { return null; }

        String request = "SELECT * FROM student WHERE \"familyName\" = ? AND \"firstName\" = ? AND \"patronicName\" = ?";
        PreparedStatement ps = connection.prepareStatement(request);
        String[] stud = studentName.split(" ");
        ps.setString(1, stud[0]);
        ps.setString(2, stud[1]);
        ps.setString(3, stud[2]);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return constructStudentFromResultSet(rs);
        }
        return null;
    }

    public Student getStudentFromDatabaseByUUID(UUID studentUUID) throws SQLException {
        String request = "SELECT * FROM student WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setObject(1, studentUUID);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return constructStudentFromResultSet(rs);
        }
        return null;
    }

    private Student constructStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();

        if (rs.getString("firstName") != null) {
            student.setFirstName(rs.getString("firstName"));
        } else student.setFirstName(null);

        if (rs.getString("familyName") != null) {
            student.setSurName(rs.getString("familyName"));
        } else student.setSurName(null);

        if (rs.getString("patronicName") != null) {
            student.setFamilyName(rs.getString("patronicName"));
        } else student.setFamilyName(null);

        if (rs.getString("group") != null) {
            student.setGroup(rs.getString("group"));
        } else student.setGroup(null);

        return student;
    }
    @Override
    public void saveCardToDatabase(Card card) throws SQLException {
        String request = "INSERT INTO card(id, name, hp, evolves_from, game_set, pokemon_owner, stage, retreat_cost, weakness_type," +
                " resistance_type, attack_skills, pokemon_type, regulation_mark, card_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::json, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setObject(1, UUID.randomUUID());
        ps.setString(2, card.getName());
        ps.setInt(3, card.getHp());
        Card evolvesFrom;
        UUID evolvesFromId = null;
        if((evolvesFrom = card.getEvolvesFrom()) != null) {
            if(getCardFromDatabase(evolvesFrom.getName()) == null) {
                saveCardToDatabase(evolvesFrom);
            }
            evolvesFromId = getCardIdByName(evolvesFrom.getName());
        }
        ps.setObject(4, evolvesFromId);
        ps.setString(5, card.getGameSet());
        Student pokemonOwner;
        UUID ownerId = null;
        if((pokemonOwner = card.getPokemonOwner()) != null) {
            if(getStudentFromDatabase(pokemonOwner.getSurName() + " " + pokemonOwner.getFirstName() + " " + pokemonOwner.getFamilyName()) == null) {
                createPokemonOwner(card.getPokemonOwner());
            }
            ownerId = getStudentIdByName(pokemonOwner.getSurName(), pokemonOwner.getFirstName(), pokemonOwner.getFamilyName());
        }
        ps.setObject(6, ownerId);
        ps.setString(7, card.getPokemonStage().name());
        ps.setString(8, card.getRetreatCost());
        if (card.getWeaknessType() != null) {
            ps.setString(9, card.getWeaknessType().name());
        } else {
            ps.setString(9, null);
        }
        if (card.getResistanceType() != null) {
            ps.setString(10, card.getResistanceType().name());
        } else {
            ps.setString(10, null);
        }
        ps.setObject(11, new Gson().toJson(card.getSkills()));
        ps.setString(12, card.getPokemonType().name());
        ps.setString(13, String.valueOf(card.getRegulationMark()));
        ps.setString(14, card.getNumber());

        ps.execute();
        ps.close();
    }

    @Override
    public void createPokemonOwner(Student owner) throws SQLException {
        String request = "INSERT INTO student(id, \"familyName\", \"firstName\", \"patronicName\", \"group\") VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setObject(1, UUID.randomUUID());
        ps.setString(2, owner.getSurName());
        ps.setString(3, owner.getFirstName());
        ps.setString(4, owner.getFamilyName());
        ps.setString(5, owner.getGroup());
        ps.execute();
        ps.close();
    }

    private UUID getCardIdByName(String cardName) throws SQLException {
        String request = "SELECT * FROM card WHERE \"name\" = ?";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setString(1, cardName);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return UUID.fromString(rs.getString("id"));
        }
        ps.execute();
        ps.close();
        return null;
    }

    private UUID getStudentIdByName(String familyName, String firstName, String patronymicName) throws SQLException {
        String request = "SELECT id FROM student WHERE \"familyName\" = ? AND \"firstName\" = ? AND \"patronicName\" = ?";
        PreparedStatement ps = connection.prepareStatement(request);
        ps.setString(1, familyName);
        ps.setString(2, firstName);
        ps.setString(3, patronymicName);
        ResultSet rs = ps.executeQuery();

        if(rs.next()){
            return UUID.fromString(rs.getString("id"));
        }
        ps.execute();
        ps.close();
        return null;
    }
}
