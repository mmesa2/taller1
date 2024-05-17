package co.edu.poli.ces3.universitas.database.repositories;

import co.edu.poli.ces3.universitas.database.CRUD;
import co.edu.poli.ces3.universitas.database.ConexionMySql;
import co.edu.poli.ces3.universitas.database.dao.User;
import com.google.gson.JsonObject;

import com.password4j.Hash;
import com.password4j.Password;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements CRUD {

    private ConexionMySql cnnMysql;

    public UserRepository(){
        cnnMysql = new ConexionMySql("localhost");
    }

    @Override
    public List<User> get() throws SQLException {
        Connection con = cnnMysql.conexion();
        Statement sts = con.createStatement();
        ResultSet rs = sts.executeQuery("SELECT * FROM users");
        List<User> list = new ArrayList<>();

        while (rs.next()){
            list.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("lastName"),
                    rs.getString("mail"),
                    rs.getString("password"),
                    rs.getDate("createdAt"),
                    rs.getDate("updatedAt"),
                    rs.getDate("deletedAt")
            ));
        }

        rs.close();
        sts.close();
        con.close();

        return list;
    }

    @Override
    public User getOne(int id) throws SQLException {
        Connection con = cnnMysql.conexion();
        PreparedStatement sts = con.prepareStatement("SELECT * FROM users WHERE id = ?");
        sts.setInt(1,id);
        ResultSet rs = sts.executeQuery();
        if(rs.next())
            return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("lastName"),
                    rs.getString("mail"),
                    rs.getString("password"),
                    rs.getDate("createdAt"),
                    rs.getDate("updatedAt"),
                    rs.getDate("deletedAt")
            );
        return null;
    }

    @Override
    public User insert(String name, String password) throws SQLException {
        boolean isExist = this.validateUserExist(name);
        if(isExist) return null;

        String sql = "INSERT INTO users (name, lastName, mail, password) VALUES (?, ?, ?, ?)";
        Connection cnn = this.cnnMysql.conexion();
        Hash hash = Password.hash(password).addSalt("@#123-@").withPBKDF2();
        try (PreparedStatement sts = cnn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sts.setString(1, name);
            sts.setString(2, name);
            sts.setString(3, name);
            sts.setString(4, hash.getResult());
            int rs = sts.executeUpdate();

            if (rs > 0) {
                try (ResultSet generatedKeys = sts.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return this.getOne(generatedKeys.getInt(1));
                    } else {
                        return null;
                    }
                }
            };
            return null;
        }
    }

    @Override
    public User update(JsonObject userUpdate, int id) throws SQLException {
        String sql = "UPDATE users SET name = ?, lastName = ?, mail = ?, password = ?, updatedAt = now() WHERE id = ?";
        Connection cnn = this.cnnMysql.conexion();
        PreparedStatement sts = cnn.prepareStatement(sql);
        sts.setString(1, userUpdate.get("name").getAsString());
        sts.setString(2, userUpdate.get("lastName").getAsString());
        sts.setString(3, userUpdate.get("mail").getAsString());
        sts.setString(4, userUpdate.get("password").getAsString());
        sts.setInt(5, id);
        sts.execute();
        return this.getOne(id);
    }

    public boolean validateUserExist(String name) throws SQLException {
        String sql = "SELECT * FROM users WHERE name = LOWER(?) LIMIT 1";
        Connection cnn = this.cnnMysql.conexion();
        PreparedStatement sts = cnn.prepareStatement(sql);
        sts.setString(1, name);
        ResultSet rs = sts.executeQuery();
        return rs.next();
    }

    public User validateCredentials(String name, String password) throws SQLException  {
        String sql = "SELECT * FROM users WHERE name = LOWER(?) LIMIT 1";
        try (Connection cnn = this.cnnMysql.conexion();
             PreparedStatement sts = cnn.prepareStatement(sql)) {
            sts.setString(1, name);
            ResultSet rs = sts.executeQuery();
            String registerPassword = "";
            if(!rs.next()) return null;

            User user = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("lastName"),
                    rs.getString("mail"),
                    rs.getString("password"),
                    rs.getDate("createdAt"),
                    rs.getDate("updatedAt"),
                    rs.getDate("deletedAt")
            );
            String userPassword = user.getPassword();
            boolean isCorrect = Password.check(password, userPassword).addSalt("@#123-@").withPBKDF2();
            return isCorrect ? user : null;
        }
    }

    public void disconect() throws SQLException {
        cnnMysql.disconect();
    }
}
