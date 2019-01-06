package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;

/**
 * Calls Ticket Master's discovery API
 */
public class TicketMasterClient {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "dTFZp8CjGZtAVnyUANW8dEFpAW215MCn"; // TODO shove this to a property file?
	private static final int GEO_HASH_PRECISION = 9;

	public List<Item> search(double latitude, double longitude, String keyword) {

		// ensure keyword is valid 
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;			
		}
		try {
			// watch out for space, plus, minus, non-English characters, etc.
			// "Google Search" -> "Google%20Search"
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// preparing the query
		String geopoint = GeoHash.encodeGeohash(latitude, longitude, GEO_HASH_PRECISION);
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%d", API_KEY, geopoint, keyword, 50);
		String url = URL + "?" + query;

		try {
			// Send the request
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				System.out.printf("Unknown error: %d; request: %s\n", responseCode, url);
				return new ArrayList<>();
			}

			// read everything
			// As client, we acquire responses as input
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = input.readLine()) != null) {
				response.append(inputLine);
			}
			input.close();

			// parse the result
			JSONObject responseJson = new JSONObject(response.toString());
			if (!responseJson.isNull("_embedded")) {
				JSONObject embedded = (JSONObject) responseJson.get("_embedded");
				if (!embedded.isNull("events")) {
					return getItemList((JSONArray)embedded.get("events"));
				}
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		if (events == null) {
			return itemList;
			
		}

		for (int i = 0, size = events.length(); i < size; i++) {
			JSONObject event = events.getJSONObject(i);
			Item.ItemBuilder builder = new Item.ItemBuilder();
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			builder.setCategories(getCategories(event));
			itemList.add(builder.build());
		};
		
		return itemList;
	}

	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for (int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder result = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							result.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							if (result.length() > 0) {
								result.append(','); // JavaScript delimiter is ','
							}
							result.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							if (result.length() > 0) {
								result.append(',');
							}
							result.append(address.getString("line3"));
						}
					}
					
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							if (result.length() > 0) {
								result.append(',');
							}
							result.append(city.getString("name"));
						}
					}
					// look for the first one, only
					if (result.length() > 0) {
						return result.toString();
					}
				}
			}
		} 		
		return "";
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			for (int i =  0, size = images.length(); i < size; i++) {
				JSONObject imageObject = images.getJSONObject(i);
				if (!imageObject.isNull("url")) {
					return imageObject.getString("url");
				}
			}			
		}		
		return "";
	}
	
	
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i =  0, size = classifications.length(); i < size; i++) {
				JSONObject classification = classifications.getJSONObject(i);
				// StringBuilder sb = new StringBuilder();
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
//						if (i > 0) {
//							// sb.append('|');
//							sb.append(',');
//						}
						// sb.append(segment.getString("name"));
						categories.add(segment.getString("name"));
					}
//					if (!segment.isNull("genres")) {
//						JSONArray genres = segment.getJSONArray("genres");
//						for (int j = 0, genresSize = genres.length(); j < genresSize; j++) {
//							JSONObject genre = genres.getJSONObject(j);
//							if (!genre.isNull("name")) {
//								if (j > 0) {
//									sb.append(';');
//								}
//								sb.append(genre.getString("name"));
//							}
//							if (!genre.isNull("subGenres")) {
//								JSONArray subGenres = genre.getJSONArray("subGenres");
//								for (int k = 0, subGenresSize = subGenres.length(); k < subGenresSize; k++) {
//									JSONObject subGenre = subGenres.getJSONObject(k);
//									if (!subGenre.isNull("name")) {
//										if (k > 0) {
//											sb.append(',');
//										}
//										sb.append(genre.getString("name"));										
//									}
//								}
//							}
//						}
//					}
				}
//				if (!classification.isNull("genre")) {
//					JSONObject genre = classification.getJSONObject("genre");
//					if (!genre.isNull("name")) {
//						if (sb.length() > 0) {
//							sb.append(';');
//						}
//						sb.append(genre.getString("name"));						
//					}
//				}
//				if (!classification.isNull("subGenre")) {
//					JSONObject subGenre = classification.getJSONObject("subGenre");
//					if (!subGenre.isNull("name")) {
//						if (sb.length() > 0) {
//							sb.append(',');
//						}
//						sb.append(subGenre.getString("name"));						
//					}
//				}
//				categories.add(sb.toString());
			}			
		}		
		return categories;
	}
	 
	private void queryAPI(double latitude, double longitude) {
		List<Item> events = search(latitude, longitude, null);
		for (Item event : events) {
			System.out.println(event.toJSONObject());
		}

	}

	/**
	 * Main entry for sample TicketMaster API requests.
	 */
	public static void main(String[] args) {
		TicketMasterClient tmApi = new TicketMasterClient();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}

}
