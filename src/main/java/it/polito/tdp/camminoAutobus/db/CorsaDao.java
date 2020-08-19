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
	private String logica;

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

	/**
	 * lista di tutte le corse in giro per la citta' dall'orario iniziale, per un certo numero di ore.
	 * @param ltorario orario di inizio
	 * @param ltOrarioFine orario oltre il quale la corsa perde di interesse
	 * @return tutte le corse che rispettano i parametri
	 */
	public List<Corsa> listAllCorseByOrario(LocalTime ltorario, LocalTime ltOrarioFine) {
		
		if(ltOrarioFine.isAfter(ltorario))
			this.logica="AND";
		else
			this.logica="OR";
		String sql = "SELECT DISTINCT oa.IdCors, oa.linea, oa.identificativo " + 
				"FROM "+this.tabella+" oa " + 
				"WHERE oa.tempoPassato>=? "+logica+" oa.tempoPassato<=? "+
				"ORDER BY oa.IdCors ";
		List<Corsa> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setTime(1, Time.valueOf(ltorario));
			st.setTime(2, Time.valueOf(ltOrarioFine));
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(
						new Corsa(res.getInt("IdCors"),res.getString("linea"),res.getString("identificativo"))
						);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//NON TOCCARE
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
	 * N.B la lista di fermate coperte dalla corsa e' ordinata secondo il numero di fermate passate, quindi l'arco va dalla fermata in 
	 * posizione 1 verso quella in posizione 2
	 * @param idCorsa corsa di interesse
	 * @param ltorario orario di inizio interesse
	 * @param orarioFineInteresse 
	 * @return restituisce tutte le fermate dell'autobus dal quale la corsa indicata passa dall'orario selezionato
	 */
	public List<FermataAutobus> getAllFermateById(int idCorsa, LocalTime ltorario, LocalTime orarioFineInteresse) {
		String sql = "SELECT oa.identificativo,oa.numeroFermata,oa.CodiceLocale,oa.Desc_stazione,oa.tempoPassato " + 
				"FROM "+this.tabella+" oa " + 
				"WHERE oa.IdCors=? AND (oa.tempoPassato>=? "+logica+" oa.tempoPassato<=?) " + 
				"ORDER BY oa.numeroFermata ";
		List<FermataAutobus> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, idCorsa);
			st.setTime(2, Time.valueOf(ltorario));
			st.setTime(3, Time.valueOf(orarioFineInteresse));
			ResultSet res = st.executeQuery();

			while (res.next()) {
				FermataAutobus fermata= new FermataAutobus(res.getString("identificativo"),res.getInt("numeroFermata"),
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
