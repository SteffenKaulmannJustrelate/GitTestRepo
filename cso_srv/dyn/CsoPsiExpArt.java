// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211123558
import de.pisa.psc.srv.com.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** CSO_PSI_EXP_ART */
public class CsoPsiExpArt {

	public void run(PscGui gui) throws Exception {
		expArt(gui);
	}
	
	private void expArt(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		String[] arr_dto = {"$NUM","$NUM","$NAM_GER","50000"};
		String[] arr_bob = {"ARTIKEL_NR","BEZEICHNUNG_1","BEZEICHNUNG_2","AUFWANDSKONTO"};
		int[] len_bob = {35, 30, 30, 8};
		boolean[] arr_sea = {true,false,false,false};
		byte[] arr_ope = {'I','O','O','I'};
		BobExp exp = null;
		try {
			exp = new BobExp(gui, "PSA_NTR_ART", "PART");
			exp.setQue("CAT_IDN", "PSA_ART_ART");
			exp.setQue("PSI_NUM", "''");
			int cnt=0;
			int mxc=200;
			while ( cnt<mxc && exp.fetNxt() ) {
				++cnt;
				String nam = exp.getDat("NAM_GER", 1);
				String num = exp.getDat("NUM", 1);
				int rcd = exp.wriBobRec('I', arr_dto, arr_bob, arr_sea, arr_ope, len_bob);
				ssn.wriTxt(Integer.toString(rcd)+':'+num+' '+nam);
			}
		} catch(PscComExc e) {
			int ret = e.getCode();
			ssn.wriTxt("FEHLER: "+e.getMessage()+" Code: "+ret);
		} catch(Exception exc) {
			ssn.wriTxt(exc.getMessage());
		} finally {
			if (exp!=null) {
				exp.clsObj();
			}
		}
//		expDto(gui, dto_fld, bob_fld, bob_len, 100, true, null, null, null, null, null);
	}

	private class BobExp {
		
		private PscCom sta = null;
		private PscCom app = null;
		private PscCom ses = null;
		private PscCom bob = null;
		private PscCom dtl = null;
		private PscDto dto = null;
		boolean fet = false;
		
		public void clsObj() throws Exception {
			UpdateNUI(sta, app);
			if ( dto!=null ) {
				if ( fet ) {
					dto.fetCls();
				}
				dto = null;
			}
			if ( dtl!=null && bob!=null ) {
				setActiveWin(bob,dtl);
			}
			if ( dtl!=null ) {
				dtl.Release();
				dtl = null;
			}
			if ( bob!=null ) {
				Visible(bob);
				bob.Release();
				bob = null;
			}
			if ( ses!=null ) {
				ses.Release();
				ses = null;
			}
			if ( app!=null && sta!=null ) {
				UpdateNUI(sta, app);
			}
			if ( app!=null ) {
				app.Release();
				app = null;
			}
			if ( sta!=null ) {
				sta.Release();
				sta = null;
			}
		}
		
		public int wriBobRec(char ope, String[] arr_dto, String[] arr_bob, boolean[] arr_sea, byte[] arr_ope, int[] len_bob) throws Exception {
			int rcd = -99999;
			if ( dtl!=null && dto!=null ) {
				String num = null;
				for (int idx=0; idx<arr_dto.length; ++idx) {
					String nam = arr_dto[idx];
					String val = nam;
					if ( nam.charAt(0)=='$' ) {
						val = dto.getDat(nam.substring(1),1);
					}
					if ( idx==0 ) {
						num = val;
					}
					if ( arr_sea[idx] ) {
						setFieldContent(dtl,arr_bob[idx], val);
					}
				}
				rcd = read(dtl);
				if ( (rcd<0 && (ope=='O' || ope=='I')) || (rcd==0 && (ope=='O' || ope=='U')) ) {
					for (int idx=1; idx<arr_dto.length; ++idx) {
						String nam = arr_dto[idx];
						String val = nam;
						if ( nam.charAt(0)=='$' ) {
							val = dto.getDat(nam.substring(1),1);
						}
						int len = val.length();
						if ( len>len_bob[idx] ) {
							val = val.substring(0,len_bob[idx]);
						}
						if ( (arr_ope[idx]=='I' || arr_ope[idx]=='O') && arr_ope[idx]!='U') {
							setFieldContent(dtl,arr_bob[idx], val);
						}
					}
					rcd = save(dtl);
				}
				dto.setDat("PSI_NUM",1,num);
				dto.setDat("PSI_VER",1,"1");
				dto.putDat();
			}
			return rcd;
		}
		
		public String getDat(String nam, int row) throws Exception {
			if ( dto==null ) {
				return null;
			} else {
				return dto.getDat(nam, row);
			}
		}
		
		public boolean fetNxt() throws Exception {
			if ( dto!=null ) {
				fet = true;
				return dto.fetNxt();
			} else {
				return false;
			}
		}
		
		public void setQue(String fld, String que) throws Exception {
			if ( dto!=null ) {
				dto.setQue(fld, que);
			}
		}
		
		public BobExp(PscGui gui, String src, String dst) {
		PscSsn ssn = gui.getSsn();
			try {
			sta = new PscCom(gui, "Psipenta_NuiStart.NuiStart");
			app = ConnectNUI(sta, "walnut(penta-kunde)_D", "system", "system");
			if ( app==null ) {
				ssn.wriTxt("ERP Client nicht geöffnet");
				return;
			}
			ses = SessionSettings(app);
			String ver = Version(ses);
			ssn.wriTxt(ver);
			bob = createBO(app,dst);
			dtl = getDetail(bob);
			dto = ssn.newDto(src);
			} catch(PscComExc e) {
				int ret = e.getCode();
				ssn.wriTxt("FEHLER: "+e.getMessage()+" Code: "+ret);
			} catch(Exception exc) {
				ssn.wriTxt(exc.getMessage());
			}
		}
		
	}
	
	static PscCom ConnectNUI(PscCom sta, String pol, String usr, String pwd) throws Exception {
		Variant[] var = sta.Invoke(String.format("ConnectNUI(\"%s\", \"%s\", \"%s\")", pol, usr, pwd));
		return var[0].getObject();
	}
	
	static PscCom SessionSettings(PscCom app) throws Exception {
		Variant[] var = app.Invoke("SessionSettings()");
		return var[0].getObject();
	}
	
	static String Version(PscCom ver) throws Exception {
		Variant[] var = ver.Invoke("Version");
		return var[0].getString();
	}
	
	static PscCom createBO(PscCom app, String nam) throws Exception {
		Variant[] var = app.Invoke(String.format("createBO(\"%s\")", nam));
		return var[0].getObject();
	}

	static PscCom getDetail(PscCom bob) throws Exception {
		Variant[] var = bob.Invoke("getDetail()");
		return var[0].getObject();
	}
	
	static void setFieldContent(PscCom flt, String nam, String val) throws Exception {
		flt.Invoke(String.format("setFieldContent(\"%s\",\"%s\")", nam, val));
	}
	
	static void setActiveWin(PscCom bob, PscCom flt) throws Exception {
		bob.Invoke(String.format("setActiveWin(%s)",flt));
	}

	static int read(PscCom dtl) throws Exception {
		Variant[] var = dtl.Invoke("read()");
		return var[0].getInt();
	}

	static int save(PscCom dtl) throws Exception {
		Variant[] var = dtl.Invoke("save()");
		return var[0].getInt();
	}

	static void Visible(PscCom bob) throws Exception {
		bob.Invoke("Visible()");
	}

	static void UpdateNUI(PscCom sta, PscCom app) throws Exception {
		sta.Invoke(String.format("UpdateNUI(%s)",app));
	}

}