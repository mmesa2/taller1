package co.edu.poli.ces3.universitas.servlets;


import co.edu.poli.ces3.universitas.database.dao.User;
import co.edu.poli.ces3.universitas.database.repositories.UserRepository;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Objects;

@WebServlet(name = "userServlet", value = "/api/user")
public class UserServlet extends MyServlet {

    private GsonBuilder gsonBuilder;
    private Gson gson;

    public void init(){
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        UserRepository repo = new UserRepository();
        try {
            out.print(gson.toJson(repo.get()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        int id = Integer.parseInt(req.getParameter("id"));
        JsonObject userUpdate = this.getParamsFromPost(req);
        UserRepository repo = new UserRepository();
        try {
            User user = repo.update(userUpdate, id);
            out.println(gson.toJson(user));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        JsonObject jsonObject = JsonParser.parseString(jsonBody.toString()).getAsJsonObject();
        String username = jsonObject.get("username").getAsString();
        String password = jsonObject.get("password").getAsString();

        UserRepository repo = new UserRepository();

        try {
            boolean isExist = repo.validateUserExist(username);

            if(isExist) {
                User user = repo.validateCredentials(username, password);
                out.println(gson.toJson(Objects.requireNonNullElse(user, "invalid credentials")));
            } else {
                User userCreate = repo.insert(username, password);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write((gson.toJson(userCreate)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
