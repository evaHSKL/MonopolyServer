package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class UnjailCard extends eva.monopoly.api.game.card.cards.UnjailCard {

	public UnjailCard(String text, CardType type) {
		super(text, type);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());
		MonopolyServer.getInstance().getGameBoard().pickupCard(this, p);
	}
}
