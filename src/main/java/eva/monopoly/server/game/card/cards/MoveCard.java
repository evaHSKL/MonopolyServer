package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class MoveCard extends eva.monopoly.api.game.card.cards.MoveCard {

	public MoveCard(String text, CardType type, String target) {
		super(text, type, target);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());
		MonopolyServer.getInstance().getGameBoard().moveTarget(p, target, 1);
	}

}
