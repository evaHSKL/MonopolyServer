package eva.monopoly.server.game.card.cards;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class JailCard extends eva.monopoly.api.game.card.cards.JailCard {

	public JailCard(String text, CardType type) {
		super(text, type);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());
		p.sendToJail();

		MonopolyServer.getInstance().getServer().sendMessageToAll(new CardPulled(p.getName(), this, OptionalInt.empty(),
				p.getMoney(), OptionalInt.empty(), p.getPositionIndex()));
	}
}
