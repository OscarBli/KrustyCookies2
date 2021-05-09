package krusty;

import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;


import static krusty.Jsonizer.anythingToJson;
import static krusty.Jsonizer.toJson;

public class Database {

	private static final String jdbcString = "vm23.cs.lth.se";
	// For use with MySQL or PostgreSQL
	private static final String jdbcUsername = "fi5004sj";
	private static final String jdbcPassword = "f601lghw";
	private Connection conn;

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

	public String getCustomers(Request req, Response res) {
		String sql = "SELECT address, customerName as name FROM Customer";
		return getQuery(sql, "customers");
	}

	public String getRawMaterials(Request req, Response res) {
		String sql = "SELECT ingredientName as name, amountInStock as amount, unit FROM Ingredient";
		return getQuery(sql,"raw-materials");
	}

	public String getCookies(Request req, Response res) {
		String sql ="SELECT cookieName as name FROM Cookie";
		return getQuery(sql,"cookies");
	}

	public String getRecipes(Request req, Response res) {

		String sql ="SELECT cookieName as cookie, Ingredient.ingredientName as raw_material, amountIngredient as amount, unit FROM Ingredient, Recipe " +
		"WHERE Recipe.ingredientName=Ingredient.ingredientName";
		return getQuery(sql,"recipes");
	}

	public String getPallets(Request req, Response res) {

		String sql = "SELECT palletId as id,cookieName as cookie, createdDate as production_date," +
				"Orders.customerName as customer," +
				"IF(blocked = 0, 'no', 'yes') as blocked "+
				"FROM Pallet LEFT JOIN Orders ON Pallet.orderId";

		ArrayList<String> values = new ArrayList<String>();

		if(req.queryParams("cookie")!=null){
			values.add(req.queryParams("cookie"));
			sql += " WHERE cookieName=?";
		}
		if(req.queryParams("from")!=null && req.queryParams("to")==null){
			sql += "AND createdDate BETWEEN ? AND NOW()";
			values.add(req.queryParams("from"));
		}
		if(req.queryParams("from")!=null && req.queryParams("to")!=null){
			sql += "AND createdDate BETWEEN ? AND ?";
			values.add(req.queryParams("from"));
			values.add(req.queryParams("to"));
		}
		if(req.queryParams("blocked") != null){
			if(req.queryParams("blocked").equals("yes")){
				sql += "AND blocked = '1'";
			}else{
				sql += "AND blocked = '0'";
			}
	}

		try(PreparedStatement ps=conn.prepareStatement(sql)){
			for(int i = 0; i < values.size(); i++){
				ps.setString(i+1, values.get(i));
			}
			ResultSet rs=ps.executeQuery();
			return Jsonizer.toJson(rs, "pallets");
		}catch (SQLException e){
			e.printStackTrace();
		}
		return "{\"pallets\":[]}";
	}


	public String reset(Request req, Response res) {
		String[] tables={"Cookie","Customer","Ingredient","Orders","Pallet","Recipe"};
		setForeignKeyCheck(true);
		for(int i =0;i< tables.length;i++){
			System.out.println(truncateTable(tables[i]));
		}
		setForeignKeyCheck(false);
		String line;
		try{
			for(int i =1; i<5;i++){
				StringBuilder sb= new StringBuilder();
				BufferedReader reader= new BufferedReader(new FileReader("sql-script"+i+".txt"));
				while((line=reader.readLine())!=null){
					sb.append(line);
					sb.append("\n");
				}
				updateQuery(sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "{}";
	}

	private Boolean setForeignKeyCheck(Boolean check){
		String sql;
		if(check==true){
			sql="SET FOREIGN_KEY_CHECKS = 0";
			updateQuery(sql);
			return true;
		} else {
			sql="SET FOREIGN_KEY_CHECKS = 1";
			updateQuery(sql);
			return false;
		}

	}
	private boolean updateQuery(String sql){
		try(Statement st=conn.createStatement()){
			st.execute(sql);
			return true;
		} catch (SQLException e){
			e.printStackTrace();
			return false;
		}
	}

	private Boolean truncateTable(String table){
		try(Statement st = conn.createStatement()){
			String sql="TRUNCATE TABLE "+table;
			st.execute(sql);
			return true;
		} catch (SQLException e){
			return false;
		}

	}

	public String createPallet(Request req, Response res) {
		int error;
		int id=-1;
		String cookie = req.queryParams("cookie");
		if(cookie==null){
			error = 1;
		} else {
			String sql = "INSERT INTO Pallet(cookieName, createdDate, Blocked) VALUES (?,NOW(),?)";
			try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
				ps.setString(1, cookie);
				ps.setInt(2, 0);
				ps.executeUpdate();
				ResultSet key=ps.getGeneratedKeys();
				if(key.next()){
					id=key.getInt(1);
				}
				error=0;
			} catch (SQLException e){
				e.printStackTrace();
				error=2;
			}
		}

		String sql = "SELECT ingredientName, amountIngredient FROM Recipe WHERE Recipe.cookieName=?";
		try(PreparedStatement ps=conn.prepareStatement(sql)){
			ps.setString(1, cookie);
			ResultSet rs= ps.executeQuery();
			while(rs.next()){
				String ingredient = rs.getString("ingredientName");
				int amount = 54 * rs.getInt("amountIngredient");

				String sql2 = "UPDATE Ingredient SET amountInStock = amountInStock-? WHERE ingredientName=?";
				try(PreparedStatement ps2 = conn.prepareStatement(sql2)){
					ps2.setInt(1, amount);
					ps2.setString(2, ingredient);
					ps2.executeUpdate();
				} catch (SQLException e){
					e.printStackTrace();
				}
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		if(error==0){
			return  "{\"status\": \"ok\" ," +
					"\n\"id\": " + id + "}";
		} else if(error==1){
			return "{\"status\": \"unknown cookie\"}";
		} else {
			return "{\n\t\"status\": \"error\"\n}";
		}
	}

	private String getQuery(String sql, String name){
		try(Statement st=conn.createStatement()){
			ResultSet rs=st.executeQuery(sql);
			return Jsonizer.toJson(rs, name);
		} catch (SQLException e){
			e.printStackTrace();
			return "{]";
		}
	}
}
