package eva.monopoly.server.game.street.streets;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.StreetEntered;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class NonBuyableEventStreet extends eva.monopoly.api.game.street.streets.NonBuyableEventStreet {

	public NonBuyableEventStreet(String name) {
		super(name);
	}

	@Override
	public void action(Player p, int dice, int modifier) {
		GameBoard.LOG.debug(p.getName() + " has entered " + getName());

		MonopolyServer.getInstance().getServer()
				.sendMessageToAll(new StreetEntered(p.getName(), this, OptionalInt.empty(), p.getMoney()));

		MonopolyServer.getInstance().getGameBoard().takeEventCard().action(p);
	}

}
