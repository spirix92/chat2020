package server;

import java.sql.*;
import java.util.logging.Logger;

public class SQLiteAuthService implements AuthService {

    private Connection connection;
    private Statement stmt;
    private PreparedStatement psInsert;
    private Logger logger;

    public SQLiteAuthService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname = null;

        try {
            psInsert = connection.prepareStatement("SELECT nickname FROM peoples WHERE login = ? AND password = ?;");
            psInsert.setString(1, login);
            psInsert.setString(2, password);
            ResultSet rs = psInsert.executeQuery();

            while (rs.next()) {
                nickname = rs.getString(1);
                logger.info(nickname);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nickname;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        boolean result = false;

        try {
            psInsert = connection.prepareStatement("INSERT INTO peoples (login, password, nickname) VALUES (?,?,?);");
            psInsert.setString(1, login);
            psInsert.setString(2, password);
            psInsert.setString(3, nickname);
            int chgStr = psInsert.executeUpdate();
            if (chgStr == 1) result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/clients.db");
        stmt = connection.createStatement();
        logger.config("БД SQLite подключена");
    }

    @Override
    public void disconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.config("БД SQLite отключена");
    }

    @Override
    public boolean chgNickname(String oldNickname, String newNickname) {

        boolean result = false;
        try {
            psInsert = connection.prepareStatement("UPDATE peoples SET nickname = ? WHERE nickname = ?;");
            psInsert.setString(1, newNickname);
            psInsert.setString(2, oldNickname);
            int chgStr = psInsert.executeUpdate();
            if (chgStr != 0) result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
