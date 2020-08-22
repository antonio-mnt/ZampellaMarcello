package it.polito.tdp.camminoAutobus.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.camminoAutobus.model.Collegamento;
import it.polito.tdp.camminoAutobus.model.Corsa;
import it.polito.tdp.camminoAutobus.model.FermataAutobus;


public class CorsaDao {

	
	private String tabella;

	public CorsaDao(String scelta) {
		this.tabella=scelta;
	}

	public List<Collegamento> listAllCollegamenti() {
		String sql = "select DISTINCT oa.CodiceLocale, oa.Desc_stazione " + 
				"FROM "+this.tabella+" oa " + 
				"ORDER BY oa.CodiceLocale ";
		List<Collegamento> result = new ArrayList<Collegamento>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Collegamento(res.getString("Desc_stazione"),res.getInt("CodiceLocale")));
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
				"FROM "+this.tabella+" oa " + 
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

	/**
	 * 
	 * @param ltorario Orario di inizio interesse
	 * @param orarioFineInteresse Orario di fine interesse
	 * @return Fermate di ogni autobus nell'intervallo indicato
	 */
	public List<FermataAutobus> getCorseByOrari(LocalTime ltorario, LocalTime orarioFineInteresse) {
		String logica;
		if(orarioFineInteresse.isAfter(ltorario)) {
			//i 2 orari appartengono allo stesso giorno
			logica="AND";
		} else {
			//giorni diversi
			logica="OR";
		}
		String sql = "SELECT oa.IdCors,oa.identificativo,oa.linea,oa.numeroFermata,oa.CodiceLocale,oa.Desc_stazione,oa.tempoPassato, oa.tempoPartenza " + 
				"FROM "+this.tabella+" oa " + 
				"WHERE (oa.tempoPassato>=? "+logica+" oa.tempoPassato<=?) " + 
				"ORDER BY oa.IdCors, oa.numeroFermata ";
		List<FermataAutobus> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setTime(1, Time.valueOf(ltorario));
			st.setTime(2, Time.valueOf(orarioFineInteresse));
			ResultSet res = st.executeQuery();

			while (res.next()) {
				FermataAutobus fermata= new FermataAutobus(new Corsa(res.getInt("IdCors"),res.getString("linea"),res.getString("identificativo"), res.getTime("tempoPartenza").toLocalTime()),res.getInt("numeroFermata"),
						 new Collegamento(res.getString("Desc_stazione"),res.getInt("CodiceLocale")),res.getTime("tempoPassato").toLocalTime());
				result.add(fermata);
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
