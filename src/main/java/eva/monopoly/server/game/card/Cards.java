package eva.monopoly.server.game.card;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eva.monopoly.api.game.card.Card;
import eva.monopoly.api.game.card.Card.CardType;
import eva.monopoly.server.game.GameBoard;
import eva.monopoly.server.game.card.cards.JailCard;
import eva.monopoly.server.game.card.cards.MoneyBuildingsCard;
import eva.monopoly.server.game.card.cards.MoneyCard;
import eva.monopoly.server.game.card.cards.MoneyPlayerCard;
import eva.monopoly.server.game.card.cards.MoveAmountCard;
import eva.monopoly.server.game.card.cards.MoveTargetCard;
import eva.monopoly.server.game.card.cards.UnjailCard;
import eva.monopoly.server.util.ResourceReaderUtil;

public class Cards {

	public static Entry<ArrayList<Card>, ArrayList<Card>> loadCards() {
		GameBoard.LOG.debug("Loading Cards from File...");
		ArrayList<Card> eventCards = new ArrayList<>();
		ArrayList<Card> communityCards = new ArrayList<>();
		try {
			ResourceReaderUtil.getResourcePath("monopoly/resources/Cards.json", (path) -> {
				JsonArray json;
				try {
					json = ResourceReaderUtil.getObjectAsJsonFromFile(path, JsonArray.class);
					iterrateCards(json, eventCards, communityCards);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		} catch (URISyntaxException | IOException | IllegalArgumentException e) {
			e.printStackTrace();
		}

		GameBoard.LOG.debug("Loaded Cards");
		return new AbstractMap.SimpleEntry<>(eventCards, communityCards);
	}

	private static void iterrateCards(JsonArray json, ArrayList<Card> eventCards, ArrayList<Card> communityCards) {
		for (int i = 0; i < json.size(); i++) {
			JsonElement element = json.get(i);
			JsonObject obj = element.getAsJsonObject();

			Card card = loadCard(obj);
			switch (card.getType()) {
			case EVENT:
				GameBoard.LOG.debug("Eventcard '" + card.getText() + "' loaded");
				eventCards.add(card);
				break;
			case COMMUNITY:
				GameBoard.LOG.debug("Cummunitycard '" + card.getText() + "' loaded");
				communityCards.add(card);
				break;
			}
		}
	}

	private static Card loadCard(JsonObject obj) {
		String text = obj.get("text").getAsString();
		CardType type = CardType.valueOf(obj.get("type").getAsString().toUpperCase());

		JsonObject objAction = obj.get("action").getAsJsonObject();
		String actionType = objAction.get("type").getAsString();

		String target;
		int amount;
		int house;
		int hotel;
		int modifier = 1;
		boolean buyable = false;

		Card card = null;

		switch (actionType) {
		case "jail":
			card = new JailCard(text, type);
			break;
		case "unjail":
			card = new UnjailCard(text, type);
			break;
		case "movetarget":
			target = objAction.get("target").getAsString();
			if (objAction.has("modifier")) {
				modifier = objAction.get("modifier").getAsInt();
			}
			if (objAction.has("buyable")) {
				buyable = objAction.get("buyable").getAsBoolean();
			}
			card = new MoveTargetCard(text, type, target, modifier, buyable);
			break;
		case "moveamount":
			amount = objAction.get("amount").getAsInt();
			if (objAction.has("modifier")) {
				modifier = objAction.get("modifier").getAsInt();
			}
			card = new MoveAmountCard(text, type, amount, modifier);
			break;
		case "money":
			amount = objAction.get("amount").getAsInt();
			card = new MoneyCard(text, type, amount);
			break;
		case "moneybuildings":
			house = objAction.get("house").getAsInt();
			hotel = objAction.get("hotel").getAsInt();
			card = new MoneyBuildingsCard(text, type, house, hotel);
			break;
		case "moneyplayer":
			amount = objAction.get("amount").getAsInt();
			card = new MoneyPlayerCard(text, type, amount);
			break;
		default:
			throw new IllegalArgumentException("type '" + actionType + "' is unknown");
		}
		return card;
	}
}
