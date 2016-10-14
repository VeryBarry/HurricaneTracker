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
        createTable(conn);

        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = users.get(name);
                    HashMap m = new HashMap();
                    if (user != null) {
                        m.put("name", user.name);
                    }

                    selectHurricane(conn);

                    return new ModelAndView(hurricanesArray, "home.html");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("loginName");
                    String password = request.queryParams("password");

                    User user = users.get(name);
//                    if (user == null) {
//                        user = new User(null, name, password);
//                        users.put(name, user);
//                    }
//                    else if (!password.equals(user.password)) {
//                        response.redirect("/");
//                        return null;
//                    }
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
    }
    public static void createTable(Connection conn) throws SQLException {
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
            String hName = results.getString("hName");
            String hLocation = results.getString("hLocation");
            int hCategory = results.getInt("hCategory");
            String hImage = results.getString("hImage");
            int user_Id = results.getInt("user_Id");

            //still need to convert int to user name

            Hurricane h = new Hurricane(id, hName, hLocation, hCategory, hImage, user_Id);
            hurricanesArray.add(h);
        }
        return hurricanesArray;
    }
    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }
}



//        Write a static method selectHurricane that returns an ArrayList<Hurricane> containing all the hurricanes in the database.
//        Remove the global ArrayList<Hurricane> and instead just call selectHurricanes inside the "/" route.
//        Optional:
//        Write a static method deleteHurricane and run it in the /delete-hurricane route. It should remove the correct row using id.
//        Add a form to edit the hurricane name and other attributes, and create an /edit-hurricane route. Write a static method updateHurricane and use it in that route. Then redirect to "/".
//        Add a search form which filters the hurricane list to only those hurricanes whose name contains the (case-insensitive) search string