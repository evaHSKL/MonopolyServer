package eva.monopoly.server.game.street.streets;

import java.util.ArrayList;
import java.util.List;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.api.network.messages.StreetEntered;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

@SuppressWarnings("serial")
public class BuyableFactoryStreet extends eva.monopoly.api.game.street.streets.BuyableFactoryStreet {

	public BuyableFactoryStreet(String name, int mortgageValue, String group, int cost, int factorsingle,
			int factorgroup) {
		super(name, mortgageValue, group, cost, factorsingle, factorgroup);
	}

	@Override
	protected List<BuyableStreet> getAllStreets() {
		return new ArrayList<>(MonopolyServer.getInstance().getGameBoard().getBuyableStreets());
	}

	@Override
	public void action(Player p, int dice, int modifier) {
		int fee = -chargeFee(p, dice, getOwner(), modifier);

		GameBoard.LOG.debug(p.getName() + " has entered " + getName() + " owned by "
				+ (getOwner() != null ? getOwner().getName() + (fee != 0 ? " for a fee of " + fee : "") : "noone"));

		MonopolyServer.getInstance().getServer()
				.sendMessageToAll(new StreetEntered(p.getName(), this, fee == 0 ? null : fee, p.getMoney()));
	}
}
