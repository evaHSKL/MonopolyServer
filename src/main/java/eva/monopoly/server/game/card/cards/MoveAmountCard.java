package eva.monopoly.server.game.card.cards;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class MoveAmountCard extends eva.monopoly.api.game.card.cards.MoveAmountCard {

	public MoveAmountCard(String text, CardType type, int amount, int moneyModyfire) {
		super(text, type, amount, moneyModyfire);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());

		MonopolyServer.getInstance().getServer().sendMessageToAll(new CardPulled(p.getName(), this, OptionalInt.empty(),
				p.getMoney(), OptionalInt.empty(), p.getPositionIndex()));

		MonopolyServer.getInstance().getGameBoard().moveAmount(p, amount, moneyModyfire);
	}
}
