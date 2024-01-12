// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211122905
import java.sql.*;
import java.util.*;

import de.pisa.psc.srv.dsi.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** SHW_ALL_UNU_OBJ */
public class ShwAllUnuObj {

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		shw_unu_vie(ssn, false);
		shw_unu_syn(ssn, false);
		shw_unu_tab(ssn, false);
	}
	
	public void shw_unu_tab(PscSsn ssn, boolean drp) throws Exception {
		ssn.wriTxt("Unused tables");
		PscDbi dbi = ssn.getDbi();
		@SuppressWarnings("resource")
		Connection con = dbi.getCon();
		String sql = "SELECT table_name"
		+" FROM user_tables"
		+" WHERE table_name NOT IN"
		+" (SELECT tab FROM psc_dto_tab WHERE tab IS NOT NULL)"
		+" ORDER BY table_name";

		String nam="NONE";
		List<String> namlst = new ArrayList<>();
		try (Statement stm = con.createStatement()) {
			try (ResultSet rst = stm.executeQuery(sql)) {
				while ( rst.next() ) {
					nam = rst.getString(1);
					ssn.wriTxt(nam);
					if ( drp ) {
						namlst.add(nam);
					}
				}
			}
			for (String elm : namlst) {
				stm.execute("DROP TABLE "+elm);
			}
		}
	}
	
	public void shw_unu_vie(PscSsn ssn, boolean drp) throws Exception {
		ssn.wriTxt("Unused views");
		PscDbi dbi = ssn.getDbi();
		@SuppressWarnings("resource")
		Connection con = dbi.getCon();
		String sql = "SELECT view_name"
		+" FROM user_views"
		+" WHERE view_name NOT IN"
		+" (SELECT nam||'_VIE' FROM psc_dto_tab)"
		+" ORDER BY view_name";

		String nam="NONE";
		List<String> namlst = new ArrayList<>();
		try (Statement stm = con.createStatement()) {
			try (ResultSet rst = stm.executeQuery(sql)) {
				while ( rst.next() ) {
					nam = rst.getString(1);
					ssn.wriTxt(nam);
					if ( drp ) {
						namlst.add(nam);
					}
				}
			}
			for (String elm : namlst) {
				stm.execute("DROP VIEW "+elm);
			}
		}
	}

	public void shw_unu_syn(PscSsn ssn, boolean drp) throws Exception {
		ssn.wriTxt("Unused synonyms");
		PscDbi dbi = ssn.getDbi();
		@SuppressWarnings("resource")
		Connection con = dbi.getCon();
		String sql = "SELECT synonym_name"
		+" FROM user_synonyms"
		+" WHERE synonym_name NOT IN"
		+" (SELECT nam||'_VIE' FROM psc_dto_tab)"
		+" ORDER BY synonym_name";

		String nam="NONE";
		List<String> namlst = new ArrayList<>();
		try (Statement stm = con.createStatement()) {
			try (ResultSet rst = stm.executeQuery(sql)) {
				while ( rst.next() ) {
					nam = rst.getString(1);
					ssn.wriTxt(nam);
					if ( drp ) {
						namlst.add(nam);
					}
				}
			}
			for (String elm : namlst) {
				stm.execute("DROP SYNONYM "+elm);
			}
		}
	}

}