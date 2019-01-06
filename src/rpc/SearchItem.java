package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	
	/**
	 * Tomcat server handles request, depend on the Request's end-point, 
	 * Trigger a specific servlet. It only takes and gives calls. 
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(RpcHelper.DEFAULT_REDIRECTION_PAGE);
			return;
		}
		String keyword = request.getParameter("keyword"); 
		Double lat = Double.parseDouble(request.getParameter("lat"));
		Double lon = Double.parseDouble(request.getParameter("lon"));
		
		String userId = session.getAttribute("user_id").toString().toString();
		if (lat != null && lon != null) {
			DBConnection connection = DBConnectionFactory.getConnection();
			try {
				
				List<Item> items = connection.searchItems(lat, lon, keyword);
				Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

				JSONArray array = new JSONArray();
				for (Item item : items) {
					JSONObject obj = item.toJSONObject();
					obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
					array.put(obj);
				}
				RpcHelper.writeJsonArray(response, array);
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				connection.close();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
 