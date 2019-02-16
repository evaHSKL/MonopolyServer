package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.server.game.GameBoard;

public class MoneyCard extends eva.monopoly.api.game.card.cards.MoneyCard {

	public MoneyCard(String text, CardType type, int amount) {
		super(text, type, amount);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());
		p.modifyMoney(amount);
	}

}
