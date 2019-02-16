package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class MoneyplayerCard extends eva.monopoly.api.game.card.cards.MoneyplayerCard {

	public MoneyplayerCard(String text, CardType type, int amount) {
		super(text, type, amount);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());
		for (Player pl : MonopolyServer.getInstance().getGameBoard().getPlayers()) {
			if (pl != p) {
				pl.modifyMoney(-amount);
			}
		}
		p.modifyMoney(amount * (MonopolyServer.getInstance().getGameBoard().getPlayers().size() - 1));
	}
}
