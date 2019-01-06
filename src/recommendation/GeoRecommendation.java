package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

//Recommendation based on geo-distance and similar categories.
public class GeoRecommendation {

	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();

		// get all liked itemIDs
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

		// get all categories, record the frequency of their appearance
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemID : favoritedItemIds) {
			Set<String> categories = connection.getCategories(itemID);
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}

		// Sorting on the servlet thread reduces work on DB side, allows for better concurrency 
		List<Map.Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) -> {
			// DESC ordering
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		

		// Search based on category, once each category, ignore already liked items
		Set<String> visitedItemIds = new HashSet<>();
		for (Map.Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey());
			for (Item item : items) {
				String itemID = item.getItemId();
				if (!favoritedItemIds.contains(itemID) && !visitedItemIds.contains(itemID)) {
					recommendedItems.add(item);
					visitedItemIds.add(itemID);
				}
			}
		}

		connection.close();
		return recommendedItems;
	}
}
