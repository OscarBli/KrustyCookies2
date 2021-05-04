package krusty;

import spark.Request;
import spark.Response;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static krusty.Jsonizer.toJson;

public class Database {
	/**
	 * Modify it to fit your environment and then use this string when connecting to your database!
	 */
	private static final String jdbcString = "vm23.cs.lth.se";

	// For use with MySQL or PostgreSQL
	private static final String jdbcUsername = "fi5004sj";
	private static final String jdbcPassword = "f601lghw";
	private Connection conn;
	//private static final String database=""

	public Boolean connect() {
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection("jdbc:mysql://"+jdbcString+"/"+jdbcUsername,jdbcUsername,jdbcPassword
			);
			return true;
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}


	// TODO: Implement and change output in all methods below!



	public String getCustomers(Request req, Response res) {
		return "{}";
	}

	public String getRawMaterials(Request req, Response res) {
		return "{}";
	}

	public String getCookies(Request req, Response res) {
			if(req.params("cookie")!=null){
				String sql ="SELECT * FROM Cookie";
				try(Statement st =conn.createStatement()){
					ResultSet rs=st.executeQuery(sql);
					return Jsonizer.toJson(rs,"cookie");
				} catch (SQLException e){

				}
			}

		return "{\"cookies\":[]}";
	}

	public String getRecipes(Request req, Response res) {
		return "{}";
	}

	public String getPallets(Request req, Response res) {
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		return "{}";
	}
}
