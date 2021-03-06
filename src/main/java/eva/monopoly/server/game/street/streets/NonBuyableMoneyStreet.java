package eva.monopoly.server.game.street.streets;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.StreetEntered;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

@SuppressWarnings("serial")
public class NonBuyableMoneyStreet extends eva.monopoly.api.game.street.streets.NonBuyableMoneyStreet {

	public NonBuyableMoneyStreet(String name, int amount) {
		super(name, amount);
	}

	@Override
	public void action(Player p, int dice, int modifier) {
		GameBoard.LOG.debug(p.getName() + " has entered " + getName());

		MonopolyServer.getInstance().getServer()
				.sendMessageToAll(new StreetEntered(p.getName(), this, amount, p.getMoney()));

		p.modifyMoney(amount);
	}

}
