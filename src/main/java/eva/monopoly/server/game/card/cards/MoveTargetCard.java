package eva.monopoly.server.game.card.cards;

import java.util.OptionalInt;

import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.network.messages.CardPulled;
import eva.monopoly.server.MonopolyServer;
import eva.monopoly.server.game.GameBoard;

public class MoveTargetCard extends eva.monopoly.api.game.card.cards.MoveTargetCard {

	public MoveTargetCard(String text, CardType type, String target, int moneyModyfire) {
		super(text, type, target, moneyModyfire);
	}

	@Override
	public void action(Player p) {
		GameBoard.LOG.debug(this.getClass().getSimpleName() + " was pulled by Player " + p.getName());

		int amount = MonopolyServer.getInstance().getGameBoard().moveTarget(p, target, moneyModyfire);

		MonopolyServer.getInstance().getServer().sendMessageToAll(new CardPulled(p.getName(), this, OptionalInt.empty(),
				p.getMoney(), OptionalInt.of(amount), p.getPositionIndex()));
	}

}
