import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint("/game")
public class GameServer {
    private static Map<String, String[]> games = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, String> playerGames = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, String> currentPlayer = Collections.synchronizedMap(new HashMap<>());
    @OnOpen public void onOpen(Session session) { System.out.println("New connection: " + session.getId()); }
    @OnMessage public void onMessage(String message, Session session) throws IOException {
        String[] parts = message.split(","); String action = parts[0]; String gameId = parts[1];
        switch (action) {
            case "join":
                if (!games.containsKey(gameId)) {
                    games.put(gameId, new String[9]);
                    currentPlayer.put(gameId, "X");
                }
                playerGames.put(session.getId(), gameId);
                session.getBasicRemote().sendText("joined," + gameId); break;
            case "move": int position = Integer.parseInt(parts[2]);
                String player = parts[3]; if (games.containsKey(gameId) && currentPlayer.get(gameId).equals(player)) {
                games.get(gameId)[position] = player; broadcast(gameId, "move," + position + "," + player);
                if (checkWin(games.get(gameId), player)) {
                    broadcast(gameId, "win," + player); resetGame(gameId);
                } else if (isDraw(games.get(gameId))) { broadcast(gameId, "draw"); resetGame(gameId); }
                else { currentPlayer.put(gameId, player.equals("X") ? "O" : "X"); }}}}
    @OnClose public void onClose(Session session) {
        String gameId = playerGames.get(session.getId());
        if (gameId != null) {
            playerGames.remove(session.getId());
            games.remove(gameId);
            broadcast(gameId, "end,Opponent disconnected");
        }
    }
    private void broadcast(String gameId, String message) { playerGames.entrySet().stream()
            .filter(entry -> entry.getValue().equals(gameId))
            .forEach(entry -> { try { entry.getKey().getBasicRemote().sendText(message);
            } catch (IOException e) { e.printStackTrace(); } }); }
    private boolean checkWin(String[] board, String player) {
        int[][] winConditions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for (int[] condition : winConditions) {
            if (player.equals(board[condition[0]]) && player.equals(board[condition[1]]) && player.equals(board[condition[2]])) return true;
        }
        return false;
    }
    private boolean isDraw(String[] board) { for (String cell : board) { if (cell == null) return false; } return true; }
    private void resetGame(String gameId) { games.put(gameId, new String[9]); currentPlayer.put(gameId, "X"); }
}