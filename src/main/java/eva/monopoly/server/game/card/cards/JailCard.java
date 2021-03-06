package eva.monopoly.server.game.card.cards;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

@SuppressWarnings("serial")
public class JailCard extends eva.monopoly.api.game.card.cards.JailCard {

	public JailCard(String text, CardType type) {
		super(text, type);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());

		MonopolyServer.getInstance().getServer()
				.sendMessageToAll(new CardPulled(p.getName(), this, null, p.getMoney(), null, p.getPositionIndex()));

		p.jail();

	}
}
