package eva.monopolyserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eva.monopoly.api.game.GameBoard;
import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.api.SocketConnector;
import eva.monopoly.api.network.messages.PlayerStatusChanged;
import eva.monopoly.api.network.server.Server;

public class MonopolyServer {
	public final static Logger LOG = LoggerFactory.getLogger(MonopolyServer.class);

	private Server server;
	private GameBoard gameBoard;
	private Map<String, Player> clientsToPlayers;
	private Map<String, Player> disconnectedPlayers;

	public static void main(String[] args) {
		String name = args.length > 0 ? args[0] : "Server";
		int port = args.length > 1 ? Integer.valueOf(args[1]) : SocketConnector.STD_PORT;
		new MonopolyServer(port, name);
	}

	public MonopolyServer(int port, String name) {
		this.gameBoard = new GameBoard();
		this.clientsToPlayers = new HashMap<>();

		try {
			this.server = new Server(port, name, (con, e) -> {
				String clientName = server.getSocketConnectorName(con);
				if (disconnectedPlayers.containsKey(clientName)) {
					LOG.error("Der client " + name + " hat die Verbindung mit dem Server getrennt", e);
				} else {
					LOG.error("Der client " + name + " hat die Verbindung mit dem Server unerwartet getrennt", e);
				}
				server.closeConnection();
			});
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public void registerHandler(Server s) {
		s.registerClientHandle(PlayerStatusChanged.class, (con, state) -> {
			String clientName = server.getSocketConnectorName(con);
			switch (state.getState()) {
			case CONNECTED:
				clientsToPlayers.put(clientName, null);
				break;
			case DISCONNECTED:
				disconnectedPlayers.put(clientName, clientsToPlayers.get(clientName));
				clientsToPlayers.remove(clientName);
				break;
			case LOSTCONNECTION:
				disconnectedPlayers.put(clientName, clientsToPlayers.get(clientName));
				clientsToPlayers.remove(clientName);
				break;
			case RECONNECTED:
				clientsToPlayers.put(clientName, disconnectedPlayers.get(clientName));
				disconnectedPlayers.remove(clientName);
				break;
			}
			LOG.debug("Client '" + clientName + "' " + state.getState());
			server.sendMessageToAllExcept(new PlayerStatusChanged(clientName, state.getState()), con);
		});
	}
}
