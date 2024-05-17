package co.edu.poli.ces3.universitas.database;

import co.edu.poli.ces3.universitas.database.dao.User;
import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.util.List;

public interface CRUD {

    public List<User> get() throws SQLException;

    public User getOne(int id) throws SQLException;

    public User insert(String name, String password) throws SQLException;

    public User update(JsonObject userUpdate, int id) throws SQLException;
}
