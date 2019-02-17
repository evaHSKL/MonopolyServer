package eva.monopoly.server.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private int playerIsPlaying;
	private final List<Card> eventCards;
	private final List<Card> communityCards;
	private final List<BuyableStreet> buyableStreets;
	private final ArrayList<Street> streets;

	public GameBoard() {
		players = new ArrayList<>();
		playerIsPlaying = 0;
		Entry<ArrayList<Card>, ArrayList<Card>> loadedcards = Cards.loadCards();
		eventCards = loadedcards.getKey();
		communityCards = loadedcards.getValue();
		streets = Streets.loadStreets();
		buyableStreets = new ArrayList<>();
		for (Street s : streets) {
			if (s instanceof BuyableStreet) {
				buyableStreets.add((BuyableStreet) s);
			}
		}
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

	public List<BuyableStreet> getBuyableStreets() {
		return buyableStreets;
	}

	public ArrayList<Street> getStreets() {
		return streets;
	}

	public Player getStreetOwner(BuyableStreet s) {
		for (Player p : players) {
			if (p.getStreets().contains(s)) {
				return p;
			}
		}
		return null;
	}

	public Player getCardOwner(Card c) {
		for (Player p : players) {
			if (p.getCards().contains(c)) {
				return p;
			}
		}
		return null;
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

	public void shufflePlayers() {
		Collections.shuffle(players);
	}

	public void nextPlayer() {
		playerIsPlaying++;
		if (playerIsPlaying > players.size() - 1) {
			playerIsPlaying = 0;
		}
	}

	public Player getPlayerIsPlaying() {
		return players.get(playerIsPlaying);
	}

	public Card takeEventCard() {
		Card c = eventCards.get(RAND.nextInt(eventCards.size()));
		if (getCardOwner(c) != null) {
			c = takeEventCard();
		}
		return c;
	}

	public Card takeCommunityCard() {
		Card c = communityCards.get(RAND.nextInt(communityCards.size()));
		if (getCardOwner(c) != null) {
			c = takeEventCard();
		}
		return c;
	}

	public boolean pickupCard(Card c, Player p) {
		if (c instanceof UnjailCard && getCardOwner(c) == null) {
			p.addCard(c);
		}
		return false;
	}

	public boolean useCard(Card c, Player p) {
		if (getCardOwner(c) == p) {
			p.removeCard(c);
			return true;
		}
		return false;
	}

	public void setStreetOwnership(BuyableStreet s, Player p) {
		if (getStreetOwner(s) == null) {
			p.addStreet(s);
		}
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
		moveAmount(p, amount <= 0 ? streets.size() + amount : amount, moneyModifier);
	}
}
