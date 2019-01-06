package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;

public class RpcHelper {

	public static final String DEFAULT_REDIRECTION_PAGE = "index.html";

	private static void writeObject(HttpServletResponse response, Object object) throws IOException {
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*"); // for the front end; * -> IP address for access control
		PrintWriter writer = response.getWriter();
		writer.print(object);
		writer.close();
	}

	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
		writeObject(response, array);
	}

	public static void writeJsonObject(HttpServletResponse response, JSONObject object) throws IOException {
		writeObject(response, object);
	}

	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sBuilder = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sBuilder.append(line);
			}
			return new JSONObject(sBuilder.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new JSONObject();
	}

	// Converts a list of Item objects to JSONArray.
	public static JSONArray getJSONArray(List<Item> items) {
		JSONArray result = new JSONArray();
		try {
			for (Item item : items) {
				result.put(item.toJSONObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
