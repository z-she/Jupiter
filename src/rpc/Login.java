package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int AUTENTICATION_FAILED = 401;
	private static final int FORBIDDEN = 403;
	private static final int DEFAULT_SESSION_LENGTH = 600;
	private static boolean BENCHMARKING = false; // TODO Shove this into a property file

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject obj = new JSONObject();
			if (BENCHMARKING) {
				HttpSession session = request.getSession();
				session.setAttribute("user_id", "1111");
				session.setAttribute("last_ip", "localhost");
				obj.put("status", "OK");
				obj.put("user_id", "1111");
				obj.put("name", connection.getFullname("1111"));

			} else {
				HttpSession session = request.getSession(false);
				if (session != null) {
					String userId = session.getAttribute("user_id").toString();
					obj.put("status", "OK");
					obj.put("user_id", userId);
					obj.put("name", connection.getFullname(userId));
				} else {
					response.setStatus(FORBIDDEN);
					obj.put("status", "Cannot find valid session");
				}
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject obj = new JSONObject();
			if (BENCHMARKING) {
				obj.put("status", "OK");
				obj.put("user_id", "1111");
				obj.put("name", connection.getFullname("1111"));
			} else {
				JSONObject input = RpcHelper.readJSONObject(request);
				String userId = input.getString("user_id");
				String password = input.getString("password");
				if (connection.verifyLogin(userId, password)) {
					HttpSession session = request.getSession();
					session.setAttribute("user_id", userId);
					session.setAttribute("last_ip", request.getRemoteAddr());
					session.setMaxInactiveInterval(DEFAULT_SESSION_LENGTH);
					obj.put("status", "OK");
					obj.put("user_id", userId);
					obj.put("name", connection.getFullname(userId));
				} else {
					response.setStatus(AUTENTICATION_FAILED);
					obj.put("status", "Cannot authenticate user");
				}
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

}
