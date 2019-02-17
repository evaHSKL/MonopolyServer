package eva.monopoly.server.game.street.streets;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.api.network.messages.StreetEntered;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class BuyableTrainstationStreet extends eva.monopoly.api.game.street.streets.BuyableTrainstationStreet {

	public BuyableTrainstationStreet(String name, int mortgageValue, String group, int cost, int onestation,
			int twostations, int threestations, int fourstations) {
		super(name, mortgageValue, group, cost, onestation, twostations, threestations, fourstations);
	}

	@Override
	protected List<BuyableStreet> getAllStreets() {
		return new ArrayList<>(MonopolyServer.getInstance().getGameBoard().getBuyableStreets());
	}

	@Override
	public void action(Player p, int dice, int modifier) {
		Player streetOwner = MonopolyServer.getInstance().getGameBoard().getStreetOwner(this);
		int fee = chargeFee(p, dice, streetOwner, modifier);

		GameBoard.LOG.debug(p.getName() + " has entered " + getName() + " owned by "
				+ (streetOwner != null ? streetOwner.getName() + (fee != 0 ? " for a fee of " + fee : "") : "noone"));

		MonopolyServer.getInstance().getServer().sendMessageToAll(new StreetEntered(p.getName(), this,
				fee == 0 ? OptionalInt.empty() : OptionalInt.of(fee), p.getMoney()));
	}
}
