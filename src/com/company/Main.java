package com.company;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");

                    ArrayList<Hurricane> hurricanesArray = selectHurricane(conn);
                    HashMap m = new HashMap();

                    m.put("Hurricanes", selectHurricane(conn));

                    return new ModelAndView(m, "home.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("loginName");
                    String password = request.queryParams("password");

                    insertUser(conn, name, password);

                    Session session = request.session();
                    session.attribute("loginName", name);
                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        return null;
                    }
                    String hName = request.queryParams("hName");
                    String hLocation = request.queryParams("hLocation");
                    int hCategory = Integer.parseInt(request.queryParams("hCategory"));
                    String hImage = request.queryParams("hImage");
                    insertHurricane(conn, hName, hLocation, hCategory, hImage, user.id);
                    response.redirect("/");
                    return null;
                }
        );
        Spark.post(
                "/delete",
                (request, response) -> {
                    int id = Integer.valueOf((request.queryParams("id")));
                    deleteHurricane(conn, id);
                    response.redirect("/");
                    return null;
                }
        );
        Spark.get(
                "/edit-hurricane",
                (request, response) -> {
                    int id = Integer.valueOf(request.queryParams("id"));
                   // Hurricane hurricane = get id for object from db
                    return new ModelAndView(null, "edit.html");
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/edit-hurricane",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    if (name == null) {
                        return null;
                    }
                    int id = Integer.parseInt(request.queryParams("hurrId"));
                    String hName = request.queryParams("hName");
                    String hLocation = request.queryParams("hLocation");
                    int hCat = Integer.parseInt(request.queryParams("hCategory"));
                    String hImage = request.queryParams("hImage");
                    editHurricane(conn, id, hName, hLocation, hCat, hImage);
                    response.redirect("/");
                    return null;

                }
        );
    }
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS hurricanes (id IDENTITY, name VARCHAR, location VARCHAR, category INT, image VARCHAR, user_Id INT)");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
    }
    public static void insertHurricane(Connection conn, String hName, String hLocation, int hCategory, String hImage, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO hurricanes VALUES(NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, hName);
        stmt.setString(2, hLocation);
        stmt.setInt(3, hCategory);
        stmt.setString(4, hImage);
        stmt.setInt(5, userId);
        stmt.execute();
    }
    public static ArrayList<Hurricane> selectHurricane(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hurricanes");
        ResultSet results = stmt.executeQuery();
        ArrayList<Hurricane> hurricanesArray = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String hName = results.getString("name");
            String hLocation = results.getString("location");
            int hCategory = results.getInt("category");
            String hImage = results.getString("image");
            int user_Id = results.getInt("user_Id");

            //still need to convert int to user name

            Hurricane h = new Hurricane(id, hName, hLocation, hCategory, hImage, user_Id);
            hurricanesArray.add(h);
        }
        return hurricanesArray;
    }
    public static void editHurricane(Connection conn, int id, String hName, String hLocation, int hCategory, String hImage) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE hurricanes SET name = ?, location = ?, category = ?, image = ? WHERE id = ?");
        stmt.setInt(5, id);
        stmt.setString(1, hName);
        stmt.setString(2, hLocation);
        stmt.setInt(3, hCategory);
        stmt.setString(4, hImage);
        stmt.execute();
    }
    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }
    public static ArrayList<User> selectUser(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet results = stmt.executeQuery();
        ArrayList<User> usersArray = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String name = results.getString("name");
            String password = results.getString("password");
            User u = new User(id, name, password);
            usersArray.add(u);
        }
        return usersArray;
    }
    public static void deleteHurricane(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM hurricanes WHERE user_Id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }
}