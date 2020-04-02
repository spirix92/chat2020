package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService executorService;
    private final Logger logger;
    private final Handler handler;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {

        logger = Logger.getLogger(Server.class.getName());
        handler = new ConsoleHandler();
        logger.setLevel(Level.CONFIG);
        handler.setLevel(Level.CONFIG);
        handler.setFormatter(new SimpleFormatter());
//        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        clients = new Vector<>();
//        authService = new SimpleAuthService();
        authService = new SQLiteAuthService(logger);
        ServerSocket server = null;
        Socket socket = null;

        try {
            authService.connect();
            server = new ServerSocket(8189);
            logger.config("Сервер запустился");
            executorService = Executors.newCachedThreadPool();
            while (true) {
                socket = server.accept();
                logger.config("Клиент подключился");
                new ClientHandler(socket, executorService, this);
            }

        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            authService.disconnect();
            if (executorService != null) {
                executorService.shutdown();
            }
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String nick, String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(nick + " : " + msg);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] private [ %s ] : %s", sender.getNick(), receiver, msg);

        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver)) {
                c.sendMsg(message);
                if (!sender.getNick().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }

        sender.sendMsg("not found user :" + receiver);
    }


    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }


    public boolean isLoginAuthorized(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");

        for (ClientHandler c : clients) {
            sb.append(c.getNick()).append(" ");
        }

        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public Logger getLogger() {
        return logger;
    }
}
