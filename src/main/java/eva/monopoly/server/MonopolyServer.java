package eva.monopoly.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.api.ExchangeMessage;
import eva.monopoly.api.network.api.SocketConnector;
import eva.monopoly.api.network.messages.BuyStreet;
import eva.monopoly.api.network.messages.GameStateChanged;
import eva.monopoly.api.network.messages.GameStateChanged.GameState;
import eva.monopoly.api.network.messages.GetConnectedClients;
import eva.monopoly.api.network.messages.GetConnectedClients.Client;
import eva.monopoly.api.network.messages.GetMoveData;
import eva.monopoly.api.network.messages.GetPlayers;
import eva.monopoly.api.network.messages.PawnChanged;
import eva.monopoly.api.network.messages.PlayerStatusChanged;
import eva.monopoly.api.network.messages.PlayerStatusChanged.ConnectionState;
import eva.monopoly.api.network.messages.RollDice;
import eva.monopoly.api.network.messages.StartStopRound;
import eva.monopoly.api.network.messages.Unjail;
import eva.monopoly.api.network.messages.Unjail.UnjailReason;
import eva.monopoly.api.network.server.Server;
import eva.monopoly.server.game.GameBoard;

public class MonopolyServer {
	public final static Logger LOG = LoggerFactory.getLogger(MonopolyServer.class);
	private final static Random RANDOM = new Random();

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

	public Server getServer() {
		return server;
	}

	public void registerHandler() {
		registerPlayerStatusChanged();
		registerPawnChanged();
		registerGameStateChanged();
		registerGetConnectedClients();
		registerGetPlayers();
		registerStartStopRound();
		registerRollDice();
		registerUnjail();
		registerBuyStreet();
		registerGetMoveData();
	}

	private void registerPlayerStatusChanged() {
		server.registerClientHandle(PlayerStatusChanged.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();

			switch (msg.getState()) {
			case CONNECTED:
				if (gameState == GameState.PREGAME) {
					players.put(clientName, new Client(false, null));
				} else {
					ConnectionState stateToSend = (gameState.equals(GameState.FINISHED)
							|| !disconnectedPlayers.containsKey(clientName)) ? ConnectionState.DISCONNECTED
									: ConnectionState.RECONNECTED;
					con.sendMessage(new PlayerStatusChanged(clientName, stateToSend));
					if (stateToSend.equals(ConnectionState.DISCONNECTED)) {
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
			LOG.debug("Client '" + clientName + "' " + msg.getState());
			server.sendMessageToAll(msg);
		});
	}

	private void registerPawnChanged() {
		server.registerClientHandle(PawnChanged.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();

			Client client = players.get(clientName);
			if (client.isReady()) {
				con.sendMessage(new PawnChanged(clientName, client.getPlayerPawn()));
				return;
			}
			players.get(clientName).setPlayerPawn(msg.getPawn());
			server.sendMessageToAll(msg);
		});
	}

	private void registerGameStateChanged() {
		server.registerClientHandle(GameStateChanged.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();

			boolean ready = GameState.READY.equals(msg.getGameState());
			boolean pregame = GameState.PREGAME.equals(msg.getGameState());
			if (ready || pregame) {
				if (ready) {
					Client c = players.get(clientName);
					for (Client cl : players.values()) {
						if (c != cl && c.getPlayerPawn() != null && !c.getPlayerPawn().equals(cl.getPlayerPawn())) {
							con.sendMessage(new GameStateChanged(clientName, GameState.PREGAME));
							return;
						}
					}
				}
				players.get(clientName).setReady(ready);
				server.sendMessageToAll(msg);
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
		server.registerClientHandle(GetConnectedClients.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			con.sendMessage(new GetConnectedClients(players));
		});
	}

	private void registerGetPlayers() {
		server.registerClientHandle(GetPlayers.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			con.sendMessage(new GetPlayers(gameBoard.getPlayers()));
		});
	}

	private void registerStartStopRound() {
		server.registerClientHandle(StartStopRound.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			gameBoard.nextPlayer();
			server.sendMessageToAll(new StartStopRound(gameBoard.getPlayerIsPlaying().getName()));
		});
	}

	private void registerRollDice() {
		final Map<String, Integer> playerDoublets = new HashMap<>();

		server.registerClientHandle(RollDice.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();

			if (isPlayingPlayer(clientName)) {
				boolean doublets = false;
				int dice1 = RANDOM.nextInt(6) + 1;
				int dice2 = RANDOM.nextInt(6) + 1;
				if (dice1 == dice2) {
					doublets = true;
					Integer diceBefor = playerDoublets.get(clientName);
					playerDoublets.put(clientName, diceBefor == null ? 1 : diceBefor++);
				} else {
					playerDoublets.put(clientName, 0);
				}
				Player p = gameBoard.getPlayer(clientName);
				int amount = dice1 + dice2;
				if (p.isJailed()) {
					if (doublets) {
						p.unjail();
						server.sendMessageToAll(new RollDice(clientName, amount, doublets, false));
					} else {
						if (p.hasToLeaveJail()) {
							p.unjail();
							p.modifyMoney(-50);
							server.sendMessageToAll(new Unjail(clientName, UnjailReason.PAYED));
							server.sendMessageToAll(new RollDice(clientName, amount, doublets, false));
						} else {
							server.sendMessageToAll(new RollDice(clientName, amount, doublets, true));
						}
					}
					return;
				}
				if (playerDoublets.get(clientName) == 3) {
					p.jail();
				} else {
					gameBoard.moveAmount(p, amount, 1);
				}
				server.sendMessageToAll(new RollDice(msg.getName(), amount, doublets, p.isJailed()));
			}
		});
	}

	private void registerUnjail() {
		server.registerClientHandle(Unjail.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();

			Player p = gameBoard.getPlayer(clientName);

			switch (msg.getReason()) {
			case CARD:
				if (!p.useUnjailCard()) {
					return;
				}
				break;
			case PAYED:
				p.modifyMoney(-50);
				break;
			}
			server.sendMessageToAll(msg);
		});
	}

	private void registerBuyStreet() {
		server.registerClientHandle(BuyStreet.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();
		});
	}

	private void registerGetMoveData() {
		server.registerClientHandle(GetMoveData.class, (con, msg) -> {
			if (!checkClient(con, msg)) {
				return;
			}
			String clientName = msg.getName();
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

	private boolean checkClient(SocketConnector con, ExchangeMessage msg) {
		return server.getSocketConnectorName(con).equals(msg.getName());
	}

	private boolean isPlayingPlayer(String name) {
		return name.equals(gameBoard.getPlayerIsPlaying().getName());
	}
}
