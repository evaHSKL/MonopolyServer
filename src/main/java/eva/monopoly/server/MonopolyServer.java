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
import eva.monopoly.api.network.messages.GetPlayers;
import eva.monopoly.api.network.messages.PawnChanged;
import eva.monopoly.api.network.messages.PlayerStatusChanged;
import eva.monopoly.api.network.messages.PlayerStatusChanged.ConnectionState;
import eva.monopoly.api.network.messages.StartStopRound;
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
		this.gameState = GameState.PREGAME;
		this.gameBoard = new GameBoard();
		this.players = new HashMap<>();
		this.disconnectedPlayers = new HashMap<>();

		try {
			this.server = new Server(port, name, (con, e) -> {
				String clientName = server.getSocketConnectorName(con);
				server.closeConnection(clientName);
				if (disconnectedPlayers.containsKey(clientName)) {
					LOG.error("The client " + clientName + " disconnected");
				} else {
					disconnectedPlayers.put(clientName, players.get(clientName));
					players.remove(clientName);
					server.sendMessageToAll(new PlayerStatusChanged(clientName, ConnectionState.LOSTCONNECTION));
					LOG.error("The client " + clientName + " disconnected unexpected");
				}
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
		registerPlayerStatusChanged();
		registerPawnChanged();
		registerGameStateChanged();
		registerGetConnectedClients();
		registerGetPlayers();
		registerStartStopRound();
	}

	private void registerPlayerStatusChanged() {
		server.registerClientHandle(PlayerStatusChanged.class, (con, state) -> {
			String clientName = state.getName();
			switch (state.getState()) {
			case CONNECTED:
				if (gameState == GameState.PREGAME) {
					players.put(clientName, new Client(false, null));
				} else {
					ConnectionState toSend = !disconnectedPlayers.containsKey(clientName)
							|| gameState.equals(GameState.FINISHED) ? ConnectionState.DISCONNECTED
									: ConnectionState.RECONNECTED;
					con.sendMessage(new PlayerStatusChanged(clientName, toSend));
					if (toSend.equals(ConnectionState.DISCONNECTED)) {
						server.closeConnection(clientName);
					}
					return;
				}
				break;
			case DISCONNECTED:
				disconnectedPlayers.put(clientName, players.get(clientName));
				players.remove(clientName);
				break;
			case RECONNECTED:
				players.put(clientName, disconnectedPlayers.get(clientName));
				disconnectedPlayers.remove(clientName);
				break;
			case LOSTCONNECTION:
				return;
			}
			LOG.debug("Client '" + clientName + "' " + state.getState());
			server.sendMessageToAll(state);
		});
	}

	private void registerPawnChanged() {
		server.registerClientHandle(PawnChanged.class, (con, pawn) -> {
			Client client = players.get(pawn.getName());
			if (client.isReady()) {
				con.sendMessage(new PawnChanged(pawn.getName(), client.getPlayerPawn()));
				return;
			}
			players.get(pawn.getName()).setPlayerPawn(pawn.getPawn());
			server.sendMessageToAll(pawn);
		});
	}

	private void registerGameStateChanged() {
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
					if (players.size() == 1) {
						return;
					}
					for (Client c : players.values()) {
						if (!c.isReady()) {
							return;
						}
					}
					startGame();
				}
			}
		});
	}

	private void registerGetConnectedClients() {
		server.registerClientHandle(GetConnectedClients.class, (con, clients) -> {
			con.sendMessage(new GetConnectedClients(players));
		});
	}

	private void registerGetPlayers() {
		server.registerClientHandle(GetPlayers.class, (con, get) -> {
			con.sendMessage(new GetPlayers(gameBoard.getPlayers()));
		});
	}

	private void registerStartStopRound() {
		server.registerClientHandle(StartStopRound.class, (con, round) -> {
			gameBoard.nextPlayer();
			server.sendMessageToAll(new StartStopRound(gameBoard.getPlayerIsPlaying().getName()));
		});
	}

	private void startGame() {
		for (Entry<String, Client> c : players.entrySet()) {
			gameBoard.addPlayer(new Player(c.getKey(), c.getValue().getPlayerPawn()));
		}
		server.sendMessageToAll(new GameStateChanged(GameState.INGAME));
		gameState = GameState.INGAME;
		server.sendMessageToAll(new GetPlayers(gameBoard.getPlayers()));

		gameBoard.shufflePlayers();
		server.sendMessageToAll(new StartStopRound(gameBoard.getPlayerIsPlaying().getName()));
	}
}
