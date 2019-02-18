package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

@SuppressWarnings("serial")
public class MoneyPlayerCard extends eva.monopoly.api.game.card.cards.MoneyPlayersCard {

	public MoneyPlayerCard(String text, CardType type, int amount) {
		super(text, type, amount);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());

		for (Player pl : MonopolyServer.getInstance().getGameBoard().getPlayers()) {
			if (pl != p) {
				MonopolyServer.getInstance().getServer().getSocketConnector(pl.getName()).sendMessage(new CardPulled(
						p.getName(), this, -amount, pl.getMoney() - amount, null, pl.getPositionIndex()));
				pl.modifyMoney(-amount);
			}
		}
		int totalAmount = amount * (MonopolyServer.getInstance().getGameBoard().getPlayers().size() - 1);

		MonopolyServer.getInstance().getServer().sendMessageToAll(
				new CardPulled(p.getName(), this, totalAmount, p.getMoney() + totalAmount, null, p.getPositionIndex()));

		p.modifyMoney(totalAmount);
	}
}
