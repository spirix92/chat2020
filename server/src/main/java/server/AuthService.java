package server;

import java.sql.SQLException;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
    boolean registration(String login, String password, String nickname);
    boolean chgNickname(String oldNickname, String newNickname);
    void connect() throws ClassNotFoundException, SQLException;
    void disconnect();
}
