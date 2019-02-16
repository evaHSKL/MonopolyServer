package eva.monopoly.server.game.street.streets;

import java.util.ArrayList;
import java.util.List;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class BuyableTrainstationStreet extends eva.monopoly.api.game.street.streets.BuyableTrainstationStreet {

	public BuyableTrainstationStreet(String name, int mortgageValue, String group, int cost, int onestation,
			int twostations, int threestations, int fourstations) {
		super(name, mortgageValue, group, cost, onestation, twostations, threestations, fourstations);
	}

	@Override
	protected List<BuyableStreet> getAllStreets() {
		return new ArrayList<>(MonopolyServer.getInstance().getGameBoard().getBuyableStreets().keySet());
	}

	@Override
	public void action(Player p, int dice, int modifier) {
		Player streetOwner = MonopolyServer.getInstance().getGameBoard().getBuyableStreets().get(this);
		int fee = chargeFee(p, dice, streetOwner, modifier);
		if (fee != 0) {
			GameBoard.LOG.debug(p.getName() + " has entered " + getName() + " owned by " + streetOwner.getName()
					+ " for a fee of " + fee);
		}
	}
}
