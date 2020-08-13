package it.polito.tdp.camminoAutobus.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.camminoAutobus.model.Collegamento;


public class CorsaDao {
	
	public int cercaCorsa() {
	String sql = "SELECT * " + 
			"FROM orari_amat oa " + 
			"WHERE oa.IdCors=111087 ";
	
	Connection conn = DBConnect.getConnection();

	try {
		PreparedStatement st = conn.prepareStatement(sql);
		ResultSet res = st.executeQuery();
		 res.next();
			int a= res.getInt("linea");
			
		conn.close();
		return a;
		
	} catch (SQLException e) {
		e.printStackTrace();
		return -1;
	}


}

	public List<Integer> listAllCodiceLocale() {
		String sql = "SELECT distinct oa.CodiceLocale " + 
				"FROM orari_amat oa " + 
				"ORDER BY oa.CodiceLocale ";
		List<Integer> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(res.getInt("CodiceLocale"));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public List<String> listAllIdentificativi() {
		String sql = "SELECT DISTINCT oa.identificativo " + 
				"FROM orari_amat oa " + 
				"ORDER BY oa.identificativo ";
		List<String> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(res.getString("identificativo"));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public List<Collegamento> getCodiciLocaliByStazione(String key) {
		String sql = "SELECT DISTINCT oa.Desc_stazione, oa.CodiceLocale " + 
				"FROM orari_amat oa " + 
				"where oa.Desc_stazione LIKE ? "
				+ "ORDER BY oa.Desc_stazione ";
		List<Collegamento> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, "%"+key+"%");
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Collegamento coll=new Collegamento(res.getString("Desc_stazione"),res.getInt("CodiceLocale"));
				result.add(coll);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
