package com.cooptrack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main {

    private static final String DB_URL = "jdbc:sqlite:data/cooptrack.db";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("data"));
        initialiseDatabase();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");

            // HOMEPAGE
            config.routes.get("/", ctx -> ctx.redirect("/index.html"));

            // HEALTH CHECK
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "ok",
                            "application", "CoopTrack"
                    ))
            );

            // MEMBERS
            config.routes.get("/api/members", ctx ->
                    ctx.json(getMembers())
            );

            config.routes.post("/api/members", ctx -> {
                JsonNode body = mapper.readTree(ctx.body());

                String name = requiredText(body, "name");
                String email = requiredText(body, "email");

                try (Connection con = connect();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO members(name, email) VALUES(?, ?)")) {

                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.executeUpdate();

                    ctx.status(201).json(Map.of(
                            "message", "Member added successfully",
                            "name", name,
                            "email", email
                    ));
                } catch (SQLException e) {
                    if (e.getMessage() != null &&
                            e.getMessage().toLowerCase().contains("unique")) {
                        ctx.status(409).json(Map.of(
                                "error",
                                "A member with this email already exists."
                        ));
                    } else {
                        throw e;
                    }
                }
            });

            // TASKS
            config.routes.get("/api/tasks", ctx ->
                    ctx.json(getTasks())
            );

            config.routes.post("/api/tasks", ctx -> {
                JsonNode body = mapper.readTree(ctx.body());

                String title = requiredText(body, "title");
                String assignee = requiredText(body, "assignee");
                String deadline = optionalText(body, "deadline");
                int workDays = integerValue(body, "workDays", 0);

                if (workDays < 0) {
                    ctx.status(400).json(Map.of(
                            "error", "Work days cannot be negative."
                    ));
                    return;
                }

                try (Connection con = connect();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO tasks " +
                                     "(title, assignee, deadline, work_days, progress) " +
                                     "VALUES (?, ?, ?, ?, 'Pending')")) {

                    ps.setString(1, title);
                    ps.setString(2, assignee);
                    ps.setString(3, deadline);
                    ps.setInt(4, workDays);
                    ps.executeUpdate();

                    ctx.status(201).json(Map.of(
                            "message", "Task added successfully"
                    ));
                }
            });

            config.routes.put("/api/tasks/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));
                JsonNode body = mapper.readTree(ctx.body());
                String progress = requiredText(body, "progress");

                try (Connection con = connect();
                     PreparedStatement ps = con.prepareStatement(
                             "UPDATE tasks SET progress = ? WHERE id = ?")) {

                    ps.setString(1, progress);
                    ps.setInt(2, id);

                    int changed = ps.executeUpdate();

                    if (changed == 0) {
                        ctx.status(404).json(Map.of(
                                "error", "Task not found."
                        ));
                    } else {
                        ctx.json(Map.of(
                                "message", "Task progress updated."
                        ));
                    }
                }
            });

            // CONTRIBUTIONS
            config.routes.get("/api/contributions", ctx ->
                    ctx.json(getRecords("contributions"))
            );

            config.routes.post("/api/contributions", ctx -> {
                JsonNode body = mapper.readTree(ctx.body());

                String member = requiredText(body, "member");
                String type = requiredText(body, "type");
                String description = requiredText(body, "description");
                double amount = decimalValue(body, "amount", 0);

                if (!Double.isFinite(amount) || amount < 0) {
                    ctx.status(400).json(Map.of(
                            "error", "Amount must be a valid non-negative number."
                    ));
                    return;
                }

                insertRecord(
                        "contributions", member, type, description, amount
                );

                ctx.status(201).json(Map.of(
                        "message", "Contribution saved successfully"
                ));
            });

            // MEMBER SUPPORT
            config.routes.get("/api/support", ctx ->
                    ctx.json(getRecords("support_records"))
            );

            config.routes.post("/api/support", ctx -> {
                JsonNode body = mapper.readTree(ctx.body());

                String member = requiredText(body, "member");
                String type = requiredText(body, "type");
                String description = requiredText(body, "description");
                double amount = decimalValue(body, "amount", 0);

                if (!Double.isFinite(amount) || amount < 0) {
                    ctx.status(400).json(Map.of(
                            "error", "Amount must be a valid non-negative number."
                    ));
                    return;
                }

                insertRecord(
                        "support_records", member, type, description, amount
                );

                ctx.status(201).json(Map.of(
                        "message", "Member support saved successfully"
                ));
            });

            // INVITATION KEYS
            config.routes.get("/api/invitations", ctx ->
                    ctx.json(getInvitations())
            );

            config.routes.post("/api/invitations", ctx -> {
                JsonNode body = mapper.readTree(ctx.body());
                String note = optionalText(body, "note");

                String key = UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 10)
                        .toUpperCase();

                try (Connection con = connect();
                     PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO invitations " +
                                     "(invitation_key, note, status) " +
                                     "VALUES (?, ?, 'Active')")) {

                    ps.setString(1, key);
                    ps.setString(2, note);
                    ps.executeUpdate();

                    ctx.status(201).json(Map.of(
                            "message", "Invitation key created successfully",
                            "key", key,
                            "note", note,
                            "status", "Active"
                    ));
                }
            });

            // VALIDATE AND USE AN INVITATION KEY
            config.routes.post("/api/invitations/validate", ctx -> {
                JsonNode body = mapper.readTree(ctx.body());
                String key = requiredText(body, "key").toUpperCase();

                try (Connection con = connect()) {
                    con.setAutoCommit(false);

                    try {
                        String currentStatus = null;

                        try (PreparedStatement find = con.prepareStatement(
                                "SELECT status FROM invitations " +
                                        "WHERE invitation_key = ?")) {

                            find.setString(1, key);

                            try (ResultSet rs = find.executeQuery()) {
                                if (rs.next()) {
                                    currentStatus = rs.getString("status");
                                }
                            }
                        }

                        if (currentStatus == null) {
                            throw new InvalidKeyException(
                                    "Invitation key not found."
                            );
                        }

                        if (!"Active".equalsIgnoreCase(currentStatus)) {
                            con.rollback();
                            ctx.status(400).json(Map.of(
                                    "valid", false,
                                    "message",
                                    "This invitation key has already been used."
                            ));
                            return;
                        }

                        try (PreparedStatement update = con.prepareStatement(
                                "UPDATE invitations SET status = 'Used' " +
                                        "WHERE invitation_key = ? " +
                                        "AND status = 'Active'")) {

                            update.setString(1, key);
                            int changed = update.executeUpdate();

                            if (changed == 1) {
                                con.commit();
                                ctx.json(Map.of(
                                        "valid", true,
                                        "message", "Invitation key accepted."
                                ));
                            } else {
                                con.rollback();
                                ctx.status(400).json(Map.of(
                                        "valid", false,
                                        "message",
                                        "This key is no longer active."
                                ));
                            }
                        }
                    } catch (InvalidKeyException e) {
                        con.rollback();
                        ctx.status(400).json(Map.of(
                                "valid", false,
                                "message", e.getMessage()
                        ));
                    } catch (Exception e) {
                        con.rollback();
                        throw e;
                    } finally {
                        con.setAutoCommit(true);
                    }
                }
            });
        });

        app.start(7070);

        System.out.println(
                "CoopTrack is running at http://localhost:7070/"
        );
    }

    // DATABASE CONNECTION
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // CREATE TABLES IF THEY DO NOT EXIST
    private static void initialiseDatabase() throws SQLException {
        try (Connection con = connect();
             Statement st = con.createStatement()) {

            st.execute("""
                    CREATE TABLE IF NOT EXISTS members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL COLLATE NOCASE UNIQUE
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        assignee TEXT NOT NULL,
                        deadline TEXT,
                        work_days INTEGER NOT NULL DEFAULT 0,
                        progress TEXT NOT NULL DEFAULT 'Pending'
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS contributions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        member TEXT NOT NULL,
                        type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS support_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        member TEXT NOT NULL,
                        type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS invitations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        invitation_key TEXT NOT NULL UNIQUE,
                        note TEXT,
                        status TEXT NOT NULL DEFAULT 'Active',
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    // GET MEMBERS
    private static List<Map<String, Object>> getMembers()
            throws SQLException {

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, name, email FROM members ORDER BY id DESC")) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("email", rs.getString("email"));
                result.add(row);
            }
        }

        return result;
    }

    // GET TASKS
    private static List<Map<String, Object>> getTasks()
            throws SQLException {

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, title, assignee, deadline, work_days, progress " +
                             "FROM tasks ORDER BY id DESC")) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("title", rs.getString("title"));
                row.put("assignee", rs.getString("assignee"));
                row.put("deadline", rs.getString("deadline"));
                row.put("workDays", rs.getInt("work_days"));
                row.put("progress", rs.getString("progress"));
                result.add(row);
            }
        }

        return result;
    }

    // GET CONTRIBUTIONS OR SUPPORT RECORDS
    private static List<Map<String, Object>> getRecords(String table)
            throws SQLException {

        if (!table.equals("contributions") &&
                !table.equals("support_records")) {
            throw new IllegalArgumentException("Invalid records table.");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        String sql = "SELECT id, member, type, description, amount, created_at " +
                "FROM " + table + " ORDER BY id DESC";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("member", rs.getString("member"));
                row.put("type", rs.getString("type"));
                row.put("description", rs.getString("description"));
                row.put("amount", rs.getDouble("amount"));
                row.put("createdAt", rs.getString("created_at"));
                result.add(row);
            }
        }

        return result;
    }

    // SAVE A CONTRIBUTION OR SUPPORT RECORD
    private static void insertRecord(
            String table,
            String member,
            String type,
            String description,
            double amount
    ) throws SQLException {

        if (!table.equals("contributions") &&
                !table.equals("support_records")) {
            throw new IllegalArgumentException("Invalid records table.");
        }

        String sql = "INSERT INTO " + table +
                " (member, type, description, amount) VALUES (?, ?, ?, ?)";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, member);
            ps.setString(2, type);
            ps.setString(3, description);
            ps.setDouble(4, amount);
            ps.executeUpdate();
        }
    }

    // GET INVITATION KEYS
    private static List<Map<String, Object>> getInvitations()
            throws SQLException {

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, invitation_key, note, status, created_at " +
                             "FROM invitations ORDER BY id DESC")) {

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("key", rs.getString("invitation_key"));
                row.put("note", rs.getString("note"));
                row.put("status", rs.getString("status"));
                row.put("createdAt", rs.getString("created_at"));
                result.add(row);
            }
        }

        return result;
    }

    // READ A REQUIRED TEXT FIELD
    private static String requiredText(JsonNode body, String field) {
        String value = optionalText(body, field);

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "The '" + field + "' field is required."
            );
        }

        return value;
    }

    // READ AN OPTIONAL TEXT FIELD
    private static String optionalText(JsonNode body, String field) {
        JsonNode value = body.get(field);

        if (value == null || value.isNull()) {
            return "";
        }

        return value.asText("").trim();
    }

    // READ A WHOLE NUMBER
    private static int integerValue(
            JsonNode body,
            String field,
            int defaultValue
    ) {
        JsonNode value = body.get(field);

        if (value == null || value.isNull() || value.asText().isBlank()) {
            return defaultValue;
        }

        if (!value.canConvertToInt()) {
            throw new IllegalArgumentException(
                    "The '" + field + "' field must be a whole number."
            );
        }

        return value.asInt();
    }

    // READ A DECIMAL NUMBER
    private static double decimalValue(
            JsonNode body,
            String field,
            double defaultValue
    ) {
        JsonNode value = body.get(field);

        if (value == null || value.isNull() || value.asText().isBlank()) {
            return defaultValue;
        }

        if (!value.isNumber() &&
                !value.asText().matches("-?\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException(
                    "The '" + field + "' field must be a number."
            );
        }

        return value.asDouble();
    }
}
