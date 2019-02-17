package eva.monopoly.server.game.card.cards;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.api.game.street.streets.BuyableNormalStreet;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class MoneybuildingsCard extends eva.monopoly.api.game.card.cards.MoneybuildingsCard {

	public MoneybuildingsCard(String text, CardType type, int house, int hotel) {
		super(text, type, house, hotel);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());

		int amountHouses = 0;
		int amountHotels = 0;
		for (BuyableStreet i : p.getStreets()) {
			if (i instanceof BuyableNormalStreet) {
				BuyableNormalStreet s = (BuyableNormalStreet) i;
				amountHouses += (s.getHouses() == 5 ? 0 : s.getHouses());
				amountHotels += (s.getHouses() == 5 ? 1 : 0);
			}
		}
		int amount = -(amountHouses * houseCosts + amountHotels * hotelCosts);
		p.modifyMoney(amount);

		MonopolyServer.getInstance().getServer().sendMessageToAll(new CardPulled(p.getName(), this,
				OptionalInt.of(amount), p.getMoney(), OptionalInt.empty(), p.getPositionIndex()));
	}

}
