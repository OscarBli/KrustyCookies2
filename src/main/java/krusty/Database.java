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
			Class.forName("com.mysql.cj.jdbc.Driver");
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
		String sql = "SELECT Adress as address, customerName as name FROM Customer";
		return getQuery(sql, "customers");

	}

	public String getRawMaterials(Request req, Response res) {
		String sql = "SELECT ingredientName as name, amountInStock as amount, unit FROM Ingredient";
		return getQuery(sql,"raw-materials");
	}

	public String getCookies(Request req, Response res) {
		String sql ="SELECT cookieName as name FROM Cookie";
		return getQuery(sql,"cookies");
//	return "{\"cookies\":[]}";
	}

	public String getRecipes(Request req, Response res) {

		String sql ="SELECT cookieName as cookie, Ingredient.ingredientName as raw_material, amountIngredient as amount, unit FROM Ingredient, Recipe " +
		"WHERE Recipe.ingredientName=Ingredient.ingredientName";

		return getQuery(sql,"recipes");
	}

	public String getPallets(Request req, Response res) {

		String sql = "SELECT palletId as id,cookieName as cookie, createdDate as production_date, customerName as customer, Blocked as blocked "+
		"FROM ordern,Pallet WHERE Pallet.orderId = ordern.orderId";
		return getQuery(sql, "pallets");
		//return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		return "{}";
	}

	private String getQuery(String sql, String name){
		try(Statement st=conn.createStatement()){
			ResultSet rs=st.executeQuery(sql);
			return Jsonizer.toJson(rs, name);
		} catch (SQLException e){
			e.printStackTrace();
		}
		return "{]";
	}
}
