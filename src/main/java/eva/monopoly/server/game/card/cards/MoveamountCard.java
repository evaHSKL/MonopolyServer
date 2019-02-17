package eva.monopoly.server.game.card.cards;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class MoveamountCard extends eva.monopoly.api.game.card.cards.MoveamountCard {

	public MoveamountCard(String text, CardType type, int amount) {
		super(text, type, amount);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());

		MonopolyServer.getInstance().getGameBoard().moveAmount(p, amount, 1);

		MonopolyServer.getInstance().getServer().sendMessageToAll(new CardPulled(p.getName(), this, OptionalInt.empty(),
				p.getMoney(), OptionalInt.of(amount), p.getPositionIndex()));

	}
}
