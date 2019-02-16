package eva.monopoly.server.game.street;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eva.monopoly.api.game.street.BuyableStreet;
import eva.monopoly.api.game.street.NonBuyableStreet;
import eva.monopoly.api.game.street.Street;
import eva.monopoly.server.game.GameBoard;
import eva.monopoly.server.game.street.streets.BuyableFactoryStreet;
import eva.monopoly.server.game.street.streets.BuyableNormalStreet;
import eva.monopoly.server.game.street.streets.BuyableTrainstationStreet;
import eva.monopoly.server.game.street.streets.NonBuyableCommunityStreet;
import eva.monopoly.server.game.street.streets.NonBuyableEventStreet;
import eva.monopoly.server.game.street.streets.NonBuyableMoneyStreet;
import eva.monopoly.server.game.street.streets.NonBuyableNormalStreet;
import eva.monopoly.server.util.ResourceReaderUtil;

public class Streets {
	public static ArrayList<Street> loadStreets() {
		GameBoard.LOG.debug("Loading Streets from File...");
		ArrayList<Street> streets = new ArrayList<>();
		try {
			Path path = ResourceReaderUtil.getResourcePath("monopoly/resources/Streets.json");
			JsonObject json = ResourceReaderUtil.getObjectAsJsonFromFile(path, JsonObject.class);
			iterrateStreets(json, streets);
		} catch (URISyntaxException | IOException | IllegalArgumentException e) {
			e.printStackTrace();
		}

		GameBoard.LOG.debug("Loaded Streets");
		return streets;
	}

	private static void iterrateStreets(JsonObject json, ArrayList<Street> streets) {
		JsonObject streetorder = json.get("streetorder").getAsJsonObject();
		JsonArray objStreets = json.get("streets").getAsJsonArray();
		JsonArray objSpecialstreets = json.get("specialstreets").getAsJsonArray();
		for (int i = 1; streetorder.has(String.valueOf(i)); i++) {
			String value = streetorder.get(String.valueOf(i)).getAsString();
			String[] args = value.split(":");
			int index = Integer.valueOf(args[1]);
			Street street = null;
			if ("specialstreets".equals(args[0])) {
				street = loadNonBuyableStreet(objSpecialstreets.get(index).getAsJsonObject());
			} else if ("street".equals(args[0])) {
				street = loadBuyableStreet(objStreets.get(index).getAsJsonObject());
			}
			GameBoard.LOG.debug("Street '" + street.getName() + "' loaded");
			streets.add(street);
		}
	}

	private static BuyableStreet loadBuyableStreet(JsonObject obj) {
		String name = obj.get("name").getAsString();
		String group = obj.get("group").getAsString();
		int mortgageValue = obj.get("mortgagevalue").getAsInt();
		int cost = obj.get("cost").getAsInt();

		JsonObject objRent = obj.get("rent").getAsJsonObject();

		int nohouse;
		int onehouse;
		int twohouses;
		int threehouses;
		int fourhouses;
		int hotel;
		int housecost;
		int factorsingle;
		int factorgroup;
		int onestation;
		int twostations;
		int threestations;
		int fourstations;

		BuyableStreet street = null;

		switch (group) {
		case "factory":
			factorsingle = objRent.get("factorsingle").getAsInt();
			factorgroup = objRent.get("factorgroup").getAsInt();
			street = new BuyableFactoryStreet(name, mortgageValue, group, cost, factorsingle, factorgroup);
			break;
		case "trainstation":
			onestation = objRent.get("onestation").getAsInt();
			twostations = objRent.get("twostations").getAsInt();
			threestations = objRent.get("threestations").getAsInt();
			fourstations = objRent.get("fourstations").getAsInt();
			street = new BuyableTrainstationStreet(name, mortgageValue, group, cost, onestation, twostations,
					threestations, fourstations);
			break;
		default:
			nohouse = objRent.get("nohouse").getAsInt();
			onehouse = objRent.get("onehouse").getAsInt();
			twohouses = objRent.get("twohouses").getAsInt();
			threehouses = objRent.get("threehouses").getAsInt();
			fourhouses = objRent.get("fourhouses").getAsInt();
			hotel = objRent.get("hotel").getAsInt();
			housecost = objRent.get("housecost").getAsInt();
			street = new BuyableNormalStreet(name, mortgageValue, group, cost, nohouse, onehouse, twohouses,
					threehouses, fourhouses, hotel, housecost);
			break;
		}
		return street;
	}

	private static Street loadNonBuyableStreet(JsonObject obj) {
		String name = obj.get("name").getAsString();

		JsonObject objAction = obj.get("action").getAsJsonObject();

		int amount;

		NonBuyableStreet street = null;

		switch (objAction.get("type").getAsString()) {
		case "money":
			amount = objAction.get("amount").getAsInt();
			street = new NonBuyableMoneyStreet(name, amount);
			break;
		case "community":
			street = new NonBuyableCommunityStreet(name);
			break;
		case "event":
			street = new NonBuyableEventStreet(name);
			break;
		default:
			street = new NonBuyableNormalStreet(name);
			break;
		}
		return street;
	}
}
