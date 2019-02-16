package eva.monopoly.server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eva.monopoly.api.game.card.Card;
import eva.monopoly.api.game.card.cards.UnjailCard;
import eva.monopoly.api.game.player.Player;
import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.api.game.street.Street;
import eva.monopoly.server.game.card.Cards;
import eva.monopoly.server.game.street.Streets;

public class GameBoard {

	public final static Logger LOG = LoggerFactory.getLogger(GameBoard.class);
	public final static Random RAND = new Random();

	private final List<Player> players;
	private final List<Card> eventCards;
	private final List<Card> communityCards;
	private final Map<BuyableStreet, Player> buyableStreets;
	private final ArrayList<Street> streets;
	private final Map<Card, Player> cardsInHand;

	public GameBoard() {
		players = new ArrayList<>();
		Entry<ArrayList<Card>, ArrayList<Card>> loadedcards = Cards.loadCards();
		eventCards = loadedcards.getKey();
		communityCards = loadedcards.getValue();
		streets = Streets.loadStreets();
		buyableStreets = new HashMap<>();
		for (Street s : streets) {
			if (s instanceof BuyableStreet) {
				buyableStreets.put((BuyableStreet) s, null);
			}
		}
		cardsInHand = new HashMap<>();
	}

	public List<Player> getPlayers() {
		return players;
	}

	public List<Card> getEventCards() {
		return eventCards;
	}

	public List<Card> getCommunityCards() {
		return communityCards;
	}

	public Map<BuyableStreet, Player> getBuyableStreets() {
		return buyableStreets;
	}

	public ArrayList<Street> getStreets() {
		return streets;
	}

	public Map<Card, Player> getCardsInHand() {
		return cardsInHand;
	}

	public boolean addPlayer(Player p) {
		for (Player pl : players) {
			if (pl.getName().equalsIgnoreCase(p.getName()) || pl.getPawn() == p.getPawn()) {
				return false;
			}
		}
		players.add(p);
		return true;
	}

	public Player getPlayer(String name) {
		for (Player pl : players) {
			if (pl.getName().equalsIgnoreCase(name)) {
				return pl;
			}
		}
		return null;
	}

	public Card takeEventCard() {
		Card c = eventCards.get(RAND.nextInt(eventCards.size()));
		if (cardsInHand.containsKey(c)) {
			c = takeEventCard();
		}
		return c;
	}

	public Card takeCommunityCard() {
		Card c = communityCards.get(RAND.nextInt(communityCards.size()));
		if (cardsInHand.containsKey(c)) {
			c = takeEventCard();
		}
		return c;
	}

	public boolean pickupCard(Card c, Player p) {
		if (c instanceof UnjailCard && !cardsInHand.containsKey(c)) {
			cardsInHand.put(c, p);
		}
		return false;
	}

	public boolean useCard(Card c, Player p) {
		if (cardsInHand.containsKey(c)) {
			if (cardsInHand.get(c) == p) {
				cardsInHand.remove(c);
				return true;
			}
		}
		return false;
	}

	public List<Card> getCards(Player p) {
		List<Card> cardsOfPlayer = new ArrayList<>();
		for (Entry<Card, Player> e : cardsInHand.entrySet()) {
			if (e.getValue() == p) {
				cardsOfPlayer.add(e.getKey());
			}
		}
		return cardsOfPlayer;
	}

	public boolean transferCard(Card c, Player source, Player target) {
		if (cardsInHand.get(c) == source) {
			cardsInHand.put(c, target);
			return true;
		}
		return false;
	}

	public void setStreetOwnership(BuyableStreet s, Player p) {
		buyableStreets.put(s, p);
	}

	public List<BuyableStreet> getStreetsOfPlayer(Player p) {
		List<BuyableStreet> streetsOfPlayer = new ArrayList<>();
		for (Entry<BuyableStreet, Player> e : buyableStreets.entrySet()) {
			if (e.getValue() == p) {
				streetsOfPlayer.add(e.getKey());
			}
		}
		return streetsOfPlayer;
	}

	public boolean transferStreet(BuyableStreet s, Player source, Player target) {
		if (buyableStreets.get(s) == source) {
			buyableStreets.put(s, target);
			return true;
		}
		return false;
	}

	public void moveAmount(Player p, int amount, int moneyModifier) {
		int pos = p.getPositionIndex();
		pos += amount;
		if (pos > streets.size() - 1) {
			pos -= streets.size() - 1;
			if (pos != 0) {
				streets.get(0).action(p, amount, 1);
			}
		}
		streets.get(pos).action(p, amount, moneyModifier);
	}

	public void moveTarget(Player p, String target, int moneyModifier) {
		int targetIndex = -1;
		for (Street s : streets) {
			if (s.getName().equals(target)) {
				targetIndex = streets.indexOf(s);
				break;
			}
		}
		if (targetIndex == -1) {
			int i = p.getPositionIndex();
			do {
				i = i > streets.size() - 1 ? 0 : i + 1;
				Street s = streets.get(i);
				if (s instanceof BuyableStreet) {
					if (((BuyableStreet) s).getGroup().equals(target)) {
						targetIndex = streets.indexOf(s);
					}
				}

			} while (i != p.getPositionIndex());
		}

		int amount = targetIndex - p.getPositionIndex();
		moveAmount(p, amount <= 0 ? streets.size() - amount : amount, moneyModifier);
	}
}
