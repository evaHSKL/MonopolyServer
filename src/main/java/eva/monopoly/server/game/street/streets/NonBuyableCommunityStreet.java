package eva.monopoly.server.game.street.streets;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.StreetEntered;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

@SuppressWarnings("serial")
public class NonBuyableCommunityStreet extends eva.monopoly.api.game.street.streets.NonBuyableCommunityStreet {

	public NonBuyableCommunityStreet(String name) {
		super(name);
	}

	@Override
	public void action(Player p, int dice, int modifier) {
		GameBoard.LOG.debug(p.getName() + " has entered " + getName());

		MonopolyServer.getInstance().getServer()
				.sendMessageToAll(new StreetEntered(p.getName(), this, OptionalInt.empty(), p.getMoney()));

		MonopolyServer.getInstance().getGameBoard().takeCommunityCard().action(p);
	}

}
