package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.api.game.street.streets.BuyableNormalStreet;
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

		p.modifyMoney(amountHouses * houseCosts + amountHotels * hotelCosts);
	}

}
