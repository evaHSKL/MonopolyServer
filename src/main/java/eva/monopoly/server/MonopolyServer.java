package eva.monopoly.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.api.SocketConnector;
import eva.monopoly.api.network.messages.GameStateChanged;
import eva.monopoly.api.network.messages.GameStateChanged.GameState;
import eva.monopoly.api.network.messages.GetConnectedClients;
import eva.monopoly.api.network.messages.GetConnectedClients.Client;
import eva.monopoly.api.network.messages.PawnChanged;
import eva.monopoly.api.network.messages.PlayerStatusChanged;
import eva.monopoly.api.network.server.Server;
import eva.monopoly.server.game.GameBoard;

public class MonopolyServer {
	public final static Logger LOG = LoggerFactory.getLogger(MonopolyServer.class);

	private static MonopolyServer instance;

	private Server server;
	private GameState gameState;
	private GameBoard gameBoard;
	private Map<String, Client> players;
	private Map<String, Client> disconnectedPlayers;

	public static void main(String[] args) {
		String name = args.length > 0 ? args[0] : "Server";
		int port = args.length > 1 ? Integer.valueOf(args[1]) : SocketConnector.STD_PORT;
		instance = new MonopolyServer(port, name);
	}

	public MonopolyServer(int port, String name) {
		gameState = gameState.PREGAME;
		this.gameBoard = new GameBoard();
		this.players = new HashMap<>();

		try {
			this.server = new Server(port, name, (con, e) -> {
				String clientName = server.getSocketConnectorName(con);
				if (disconnectedPlayers.containsKey(clientName)) {
					LOG.error("The client " + name + " disconnected", e);
				} else {
					disconnectedPlayers.put(clientName, players.get(clientName));
					players.remove(clientName);
					LOG.error("Der client " + name + " disconnected unexpected", e);
				}
				server.closeConnection(clientName);
			});
			registerHandler();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public static MonopolyServer getInstance() {
		return instance;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}

	public void registerHandler() {
		server.registerClientHandle(PlayerStatusChanged.class, (con, state) -> {
			String clientName = state.getName();
			switch (state.getState()) {
			case CONNECTED:
				players.put(clientName, new Client(false, null));
				break;
			case DISCONNECTED:
			case LOSTCONNECTION:
				disconnectedPlayers.put(clientName, players.get(clientName));
				players.remove(clientName);
				break;
			case RECONNECTED:
				players.put(clientName, disconnectedPlayers.get(clientName));
				disconnectedPlayers.remove(clientName);
				break;
			}
			LOG.debug("Client '" + clientName + "' " + state.getState());
			server.sendMessageToAll(state);
		});
		server.registerClientHandle(PawnChanged.class, (con, pawn) -> {
			Client client = players.get(pawn.getName());
			if (client.isReady()) {
				con.sendMessage(new PawnChanged(pawn.getName(), client.getPlayerPawn()));
				return;
			}
			players.get(pawn.getName()).setPlayerPawn(pawn.getPawn());
			server.sendMessageToAll(pawn);
		});
		server.registerClientHandle(GameStateChanged.class, (con, state) -> {
			boolean ready = GameState.READY.equals(state.getGameState());
			boolean pregame = GameState.PREGAME.equals(state.getGameState());
			if (ready || pregame) {
				if (ready) {
					Client c = players.get(state.getName());
					for (Client cl : players.values()) {
						if (c != cl && c.getPlayerPawn() != null && !c.getPlayerPawn().equals(cl.getPlayerPawn())) {
							con.sendMessage(new GameStateChanged(state.getName(), GameState.PREGAME));
							return;
						}
					}
				}
				players.get(state.getName()).setReady(ready);
				server.sendMessageToAll(state);
				if (ready) {
					for (Client c : players.values()) {
						if (!c.isReady()) {
							return;
						}
					}
					startGame();
				}
			}
		});
		server.registerClientHandle(GetConnectedClients.class, (con, clients) -> {
			con.sendMessage(new GetConnectedClients(players));
		});
	}

	private void startGame() {
		for (Entry<String, Client> c : players.entrySet()) {
			gameBoard.addPlayer(new Player(c.getKey(), c.getValue().getPlayerPawn()));
		}
		server.sendMessageToAll(new GameStateChanged(GameState.INGAME));
	}
}
