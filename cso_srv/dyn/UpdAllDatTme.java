// Last update by user CUSTOMIZER on host WSAMZN-GCNRJ0KM at 20220711135418
import java.util.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import org.apache.logging.log4j.*;

/**
 * Dyn. Java class UPD_ALL_DAT_TME
 * 
 * @since 15.12.2011
 * @version
 * @author rekowski
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdAllDatTme {
	
	private static final boolean AUCH_VERANSTALTUNGEN = true;
	
	private static final String CSO_SEM_LCK_REF_DAT_TIM = "CSO_SEM_LCK_REF_DAT_TIM";
	private static final String SEM_LCK_ERR_MSG_STR = "Fehler. Womöglich starten mehrere Benutzer dieses Script gleichzeitig. Falls nicht, bitte Dirk Kosellek und/oder Oleg Geger kontaktieren. Ansonsten einfach eine Umgebungsvariable mit dem Namen CSO_SEM_LCK_REF_DAT_TIM anlegen und nochmal versuchen.";
	
	
	public void run(PscGui gui) throws Exception {
		DMO_ADM_RUN(gui, "SIM:=y");
	}
	
	private static void takLckSem(PscSsn ssn) throws Exception {
		try {
			PscDto env_dto = PsaUti.newDto(ssn, "PSC_ENV_GLB", false, false, true, false);
			env_dto.setQue("NAM", CSO_SEM_LCK_REF_DAT_TIM);
			if (env_dto.fetDat() == 1) {
				env_dto.delDat(1);
			} else {
				throw new Exception();
			}
		} catch (Exception all_exc) {
			throw new Exception(SEM_LCK_ERR_MSG_STR);
		}
	}
	
	private static void freLckSem(PscSsn ssn) throws Exception {
		if (PsaDtoIpl.getFldDat(ssn, "PSC_ENV_GLB", "NAM", CSO_SEM_LCK_REF_DAT_TIM, "NAM", false) == null) {
			PscDto env_dto = PsaUti.newDto(ssn, "PSC_ENV_GLB", false, false, true, false);
			env_dto.insRow(1);
			env_dto.setDat("NAM", 1, CSO_SEM_LCK_REF_DAT_TIM);
			env_dto.setDat("DAT", 1, "$EMPTY");
			env_dto.putDat();
		}
	}
	
	public static void DMO_ADM_RUN(PscGui fnc_frm, String par) throws Exception {
		PscSsn ssn = fnc_frm.getSsn();
		PscGui gui = ssn.getGui();
		UsxPar usxPar = new UsxPar(par);
		boolean sim = usxPar.getPar("SIM", false);
		logNfo(ssn, "SIM: " + sim);
		// von Frm auslesen:
		PsaFrm frm = (PsaFrm) gui.newFrm("PSD_ALL_DAT_MOV_SSF");
		String ref_dat = ssn.getEnv("CSO_ACT_REF_DAT");
		String act_dat = PscUti.getTim();
		frm.setEdtDat("ACT_REF_DAT", ref_dat);
		frm.setEdtDat("NEW_REF_DAT", act_dat);
		gui.dlgFrm(frm);

		if (frm.chkExiFlg()) {
			String old_dat = frm.getEdtDat("ACT_REF_DAT");
			String new_dat = frm.getEdtDat("NEW_REF_DAT");
			dmoRunTimLinUpd(ssn, old_dat, new_dat, false, sim);

		} else {
			return;
		}
	}
	
	public static void DMO_JOB_RUN(PscSsn ssn, String par) throws Exception {
		String old_dat = ssn.getEnv("CSO_ACT_REF_DAT");
		String new_dat = PscUti.getTim();
		dmoRunTimLinUpd(ssn, old_dat, new_dat, true, false);

	}
	
	public static void dmoRunTimLinUpd(PscSsn ssn, String old_dat, String new_dat, boolean job, boolean sim) throws Exception {
		Date old_dat_obj = PscUti.getTim(old_dat);
		logNfo(ssn, String.valueOf("OLD_DAT: "+old_dat_obj));
		Date new_dat_obj = PscUti.getTim(new_dat);
		logNfo(ssn, String.valueOf("NEW_DAT: "+new_dat_obj));
		
		// Zielwochentag anpassen (Wochenende auf Montag schieben)
		Calendar c = Calendar.getInstance();
		c.setTime(new_dat_obj);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 2);
		int day_of_week = c.get(Calendar.DAY_OF_WEEK);
		if(day_of_week == 1 /*Sonntag*/ ) {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		else if (day_of_week == 7 /*Samstag*/) {
			c.add(Calendar.DAY_OF_MONTH, 2);
		}
		new_dat_obj = c.getTime();
		logNfo(ssn, String.valueOf("NEW_DAT: "+new_dat_obj));
		
		long old_tim = old_dat_obj.getTime();
		long new_tim = new_dat_obj.getTime();
		long day_dif = (new_tim - old_tim)/ 1000 / 60 / 60 /24;
		
		logNfo(ssn, String.valueOf("DAY_DIF: "+day_dif));
		new_dat = PsaUti.timAdd(old_dat,Calendar.DAY_OF_YEAR,(int)day_dif);
		logNfo(ssn, String.valueOf("NEW_DAT: "+new_dat));												
		
		Date tar_ref_dat = PscUti.getTim(new_dat);
		long ref_tim_dat = tar_ref_dat.getTime();																	
		logNfo(ssn, String.valueOf("DIF_LNG: "+day_dif));			
		logNfo(ssn, String.valueOf("DAT_LNG: "+ref_tim_dat));	
		if(day_dif!=0){		
			try{
				takLckSem(ssn);
				dmoRunTimLinUpd(ssn, ref_tim_dat, day_dif, job, sim);
				if(!sim) {
					PsaUti.newDto(ssn, "PSC_ENV_GLB", false, false, true, false);
					PscDto env_dto = PsaUti.newDto(ssn, "PSC_ENV_GLB", false, false, true, false);
					env_dto.setQue("NAM","CSO_ACT_REF_DAT");
					if(env_dto.fetDat()==1){
						env_dto.setDat("DAT",1,new_dat);
						env_dto.putDat();
					}
				}
				freLckSem(ssn);
			}
			catch(Exception e){
				throw e;
			}
		}
	}
	
		/**
	 * 
	 * @param frm
	 * @param par
	 * @param row
	 * @throws Exception
	 */
	public static void DMO_CHK_TIM_LIN(PscFrm frm, String par, Integer row)throws Exception{
		DMO_RUN_TIM_LIN_UPD(frm, par, row, true);
	}
	
	/**
	 * 
	 * @param frm
	 * @param par
	 * @param row
	 * @throws Exception
	 */
	public static void DMO_MOV_TIM_LIN(PscFrm frm, String par, Integer row)throws Exception{
		DMO_RUN_TIM_LIN_UPD(frm, par, row, false);
	}
	
	/**
	 * 
	 * @param frm
	 * @param par
	 * @param row
	 * @param sim_mod
	 * @return
	 * @throws Exception
	 */
	public static int DMO_RUN_TIM_LIN_UPD (PscFrm frm, String par, Integer row, Boolean sim_mod) throws Exception {
		PscSsn ssn = frm.getSsn();
		PscGui gui = ssn.getGui();
		String wrn_msg_str = "Bitte diese Funktion nicht mehr benutzen. Sie beisst sich mit der Funktion unter Administration->Demofunktionen->Terminverschiebung (-Oleg Geger)";
		if (gui != null) {
			gui.wriBox(wrn_msg_str, "C", "W");
		} else {
			ssn.wriTxt(wrn_msg_str);
		}
		int ret = 0; 
		
		if(row != null && sim_mod != null && row.longValue() > 0){
			PscDto dyn_dto = frm.getDynDto();
//			PscGui gui = ssn.getGui();
			long day_add =  PsaUti.str2long(dyn_dto.getDat("CSO_TIM_DIF_DAY", row), 0);
			if(day_add > 0l || day_add < 0l || sim_mod){
				String tim_stp_new = dyn_dto.getDat("CSO_TIM_STP_NEW", row);
				Date tar_ref_dat = PscUti.getTim(tim_stp_new);
				long ref_tim_dat = tar_ref_dat.getTime();
				
				if(!sim_mod.booleanValue()){
					String msg = PsaUti.getMsg("DMO_UPD_TIM_LIN", ssn, String.valueOf(day_add), getFinOraOutDat(ref_tim_dat));
			        String box_ret = PsaDtoIpl.wriBox(gui, msg, "Q", "Q", "Y");
			        if (!box_ret.equals("Y")) return 0;
				}
				
		        ret += dmoRunTimLinUpd(frm.getSsn(), ref_tim_dat, day_add, false, sim_mod.booleanValue());
		        if(!sim_mod.booleanValue()){
		        	dyn_dto.setDat("CSO_TIM_STP_OLD", row, tim_stp_new);
		        	dyn_dto.setDat("CSO_TIM_DIF_DAY", row, "0");
		        	dyn_dto.putDat();
		        }
			}else{
				 ssn.wriTxt("Nothing to do / Nix geändert.");
			}
			
		}else{
			   ssn.wriTxt("Bitte erst Daten laden");
		}
		return ret;
	}
	
	public static long getEndDat(PscSsn ssn, String gid) throws Exception{
		long end_dat = 0l;
		
		PscDto sea_dto = PsaUti.newDto(ssn, "PSA_ACT", false, false, true, true);
		sea_dto.setQue("PSC_GID", gid );
		if(sea_dto.fetDat() == 1){
			String dat_str = sea_dto.getDat("END_DAT", 1);
			
			if(dat_str != null && dat_str.length() > 0){
				Date dat_dat = PscUti.getTim(dat_str);
				end_dat  = dat_dat.getTime();
			}
		}
		return end_dat;
	}
	
	public static int dmoRunTimLinUpd(PscSsn ssn, long ref_tim_dat, long day_add, boolean job, boolean sim) throws Exception {
        int upd_cnt_res = 0;
        SavPnt sav_pnt = new SavPnt(ssn);
        try{
       	 PscDto fld_dto = PsaUti.newDto(ssn, "PSC_FLD", false, false, true, true);
            PscDto bas_dto_lst = PsaUti.newDto(ssn, "PSC_DTO", false, false, true, true);
	         bas_dto_lst.setQue("TAB", "!''");
	         bas_dto_lst.setQue("SUP", "!PSA_ART | ''");
	         String que = "'PSA_ACT' | 'PSA_CON_STR' | 'PSA_PRO_STR' | 'PSA_NOT' "
	                      + "| PSA_WRK% | PSA_HOU%" ;
	         bas_dto_lst.setQue("NAM", que);
	         String dto_nam = null;
	        
	         PscDto upd_dto = null;
	       	
	         int dto_cnt = bas_dto_lst.fetDat();
	         int dto_don_cnt = 0;  
	         int rec_don_cnt = 0;
	        
	         logNfo(ssn, dto_cnt + " Basis-DTOs werden geprüft, ggf. Datumsangaben um '"
	        		              + day_add + "' Tage verschoben");
	         for (int dto_idx = 1 ; dto_idx <= dto_cnt ; dto_idx++){
	        	 dto_nam = bas_dto_lst.getDat("NAM", dto_idx);	
	        	 
	        	 try{
		        	 fld_dto.setQue("DTO", dto_nam );
		        	 fld_dto.setQue("TYP", "D" );
		        	
		        	 int fld_cnt = fld_dto.fetDat();
		        	 if(fld_cnt > 0){
		        		 upd_dto = PsaUti.newDto(ssn, dto_nam, false, false, true, true);
		        		 List <String> upd_fld_lis = new ArrayList<>();
		        		 String fld_nam = null;
			        	 for (int fld_idx = 0 ; fld_idx < fld_cnt ; fld_idx++){
			        		 fld_nam = fld_dto.getDat("NAM", fld_idx+1);
			        		 if(fld_nam != null
			        				 && !fld_nam.equals("SYN_DAT")
			        				 && !fld_nam.equals("BRT_DAY")
			        				 && !fld_nam.endsWith("MOD_DAT")){
			        			 upd_fld_lis.add(fld_nam);	
			        		 }
			        	 }
			        	 if(upd_fld_lis.size() > 0){
			        		 int ret = updDatFld(ssn, upd_dto, upd_fld_lis, ref_tim_dat, day_add, sim);
			        		  rec_don_cnt += ret;
			        		 if(ret > 0 ){
			        			 dto_don_cnt++;
			        			String prt_msg_sfx = ret + " DS aktualisiert!";
			        			if(sim){
			        				prt_msg_sfx =  " für '" + ret + " DS simuliert!";
			        			}
			        			String msg = "Dto '"+ dto_nam + "' fertig: " + prt_msg_sfx;
			        			logNfo(ssn, msg);
			        		 }
			        	 }
		        	 }
		         }catch(Exception upd_dat_fld_exc){
		        	 String msg = "Fehler bei DTO '"+ dto_nam + "': ";
		        	 System.out.println(msg);
		        	if(!job){		        		
		        		logErr(ssn, msg, upd_dat_fld_exc);
		        		throw upd_dat_fld_exc;
		        	}else
		        		continue;
		         } 
	         }
			 
			 // Servicemeldungen aktualisieren
			 logNfo(ssn, "Verschiebe Servicemeldungen um " + day_add + " Tage:");
			 RefUti.invMthDynJav(ssn, "UPD_SVC_TRB", "run", ssn, day_add);
			 
	         sav_pnt.end();
	         upd_cnt_res = rec_don_cnt;
	         String end_msg_sfx = rec_don_cnt + " DS geändert!";
	         if(sim){
	        	 end_msg_sfx = " Update für '" + rec_don_cnt + "' DS simuliert!";
	         }
	         logNfo(ssn, "Bei "+ dto_don_cnt + " DTOs Datumswerte geprüft, "  + end_msg_sfx);
	       
	         
        }catch(Exception all_exc){
       	 	sav_pnt.abo();
       	 	all_exc.printStackTrace();
       	 	throw all_exc;
        }
        return upd_cnt_res;
	}
	
	protected static int updDatFld(PscSsn ssn, PscDto upd_dto, List <String> upd_fld_lis, 
			long ref_tim_dat, long day_add, boolean sim ) throws Exception {
		int ret = 0;
	
		String dto_nam = upd_dto.getBas().getDsc();
		System.out.println(dto_nam);
		String old_dat_str = null;
		String new_dat_str = null;
		String fld_nam 	   = null;
		String dat_out_ref = getFinOraOutDat(ref_tim_dat);    
		while(upd_dto.fetNxt()){	
			int dat_idx = 1;	
			boolean psa_evt    = false;
			if(dto_nam.equals("PSA_PRO_STR")){
				String psc_cls = upd_dto.getDat("PSC_CLS", dat_idx);
				if( psc_cls != null && psc_cls.endsWith("_EVT_XTD")){
					// it's an Event 
					psa_evt = true;
				}else
					psa_evt    = false;
			}
			if(!psa_evt || AUCH_VERANSTALTUNGEN) for(int fld_idx = 0; fld_idx < upd_fld_lis.size(); fld_idx++){
				
				fld_nam = upd_fld_lis.get(fld_idx);
				if(!dto_nam.equals("PSA_ACT")){
					
					if(fld_nam.equals("PSC_CRE") || fld_nam.equals("PSC_UPD"))
						continue;
				}else{
					// Bei Aktivitäten sollen auch Erstellungs- und Update-Datum verschoben werden
				}
				
				old_dat_str = upd_dto.getDat(fld_nam, dat_idx);
				
				if(old_dat_str != null && old_dat_str.length() > 0	 ){
					Date old_date = PscUti.getTim(old_dat_str);
					long old_dat  = old_date.getTime();
					
			        long day_dif  = 1000l * 3600l * 24l * day_add ; 
				    long new_dat  = old_dat +  day_dif;
				    Date new_date = new Date();
				    new_date.setTime(new_dat);
				    new_dat_str = PscUti.getTim(new_date);
				    
				    String dat_out_old = old_dat_str.substring(0, 4);
				    dat_out_old += "-";
				    dat_out_old += old_dat_str.substring(4, 6);
				    dat_out_old += "-";
				    dat_out_old += old_dat_str.substring(6, 8);
				    String dat_out_new = new_dat_str.substring(0, 4);
				    dat_out_new += "-";
				    dat_out_new += new_dat_str.substring(4, 6);
				    dat_out_new += "-";
				    dat_out_new += new_dat_str.substring(6, 8);
				    if(new_dat > ref_tim_dat){
				    	String tit = upd_dto.getFld(fld_nam).getTit();
				    	String add_inf = "";
				    	PscFld num_fld =  upd_dto.getFld("NUM");
				    	if(num_fld != null){
				    		add_inf = ", " +  upd_dto.getDat(num_fld, dat_idx) ; 			    		
				    	}
				    	PscFld nam_fld = upd_dto.getFld("NAM");				    	
				    	if(nam_fld != null){
				    		add_inf += " (" + upd_dto.getDat(nam_fld, dat_idx) 
				    		           + ")";
				    	}
				    	if(!tit.equalsIgnoreCase("'gültig bis'")
				    			&& !fld_nam.equals("STA_DAT")
								&& !fld_nam.equals("ACT_BEG_DAT")
								&& !fld_nam.equals("ACT_END_DAT")
				    			&& !fld_nam.equals("TRM_DAT")
								&& !fld_nam.equals("SRC_DAT")
				    			&& !fld_nam.equals("ORD_DAT")
				    			&& !fld_nam.equals("DLV_DAT")
								&& !fld_nam.equals("STA_DAT")
				    			&& !fld_nam.equals("SAP_DAT")
				    			&& !fld_nam.equals("POC_DAT")
				    			&& !fld_nam.equals("RSP_DAT")
				    			&& !fld_nam.endsWith("BEG_DAT")
				    			&& !fld_nam.endsWith("END_DAT")
				    			&& !dto_nam.equals("PSA_ACT")){
				    		logNfo(ssn, "größer/hinter'" + dat_out_ref+ "' (Refdat): " +  dat_out_old + " (bisher) =>"+ dat_out_new + "(neu), Fld ("
					    	           + upd_dto.getTit() + "." + tit +") "
					    	           + upd_dto.getDsc() + "." + fld_nam + add_inf);
				    	}
				    }
				    if(!sim){
					    PsaUti.updFldDat(upd_dto.getFld(fld_nam), 
					    		         upd_dto.getDat("PSC_GID", dat_idx),
					    		         new_dat_str);			  
					}			   
				    ret++;
				}
			}
			// Zugriffscheck - für Gruppenkalender ...
		    try{
		    	String psc_acc = upd_dto.getDat("PSC_ACC", dat_idx);
		    	if(!psc_acc.contentEquals("D0")){
		    		 PsaUti.updFldDat(upd_dto.getFld("PSC_ACC"), 
		    		         upd_dto.getDat("PSC_GID", dat_idx), 
		    		         "D0");
		    		 logNfo(ssn, upd_dto.getDat("NAM", dat_idx) + " - Zugriff geä.");
		    	}
		    }catch (Exception acc_chk_exc){
		    	logErr(ssn, "Fehler bei Prüfung/Aktualisierung Zugriff!", acc_chk_exc);
		    	 acc_chk_exc.printStackTrace();
		    }// end for ... (nächster DS)
		  // TODO Anzahl Wert in der Zukunft in nuewmPar (HashMap?)  zurückgeben:  ((PsaSsn) ssn).wriTxtImm(cnt_fut_ure_val + " Datumsfelder mit Werten in der Zukunft!");
		}
		return ret;
	}	
	
	public static String getFinOraOutDat(long mli_sec_dat) throws Exception {
		Date ref_dat  = new Date();
		ref_dat.setTime(mli_sec_dat);
		String ref_dat_str = PscUti.getTim(ref_dat);
		String dat_out_ref = ref_dat_str.substring(0, 4);
		dat_out_ref += "-";
		dat_out_ref += ref_dat_str.substring(4, 6);
		dat_out_ref += "-";
		dat_out_ref += ref_dat_str.substring(6, 8);
		return dat_out_ref;
	}
	

	private static void logErr(PscSsn ssn, String txt, Exception exc){
		log(Level.ERROR, ssn, txt, exc);
	}
	
	private static void logNfo(PscSsn ssn, String txt){
		log(Level.INFO, ssn, txt, null);
	}
	
	/**
	 * 
	 * @param lev use (Level.Error / Level.WARN / Level.INFO / Level.DEBUG)
	 * @param ssn
	 * @param txt
	 * @param exc
	 */
	private static void log(Level lev, PscSsn ssn, String txt, Exception exc) {
		if (JobThr.getJobThr()!=null) {
			JobLog log = JobThr.getJobLog(UpdAllDatTme.class);
			if (lev==Level.ERROR) {
				log.logErr(txt, exc);
			}
			else {
				log.log(txt, lev);
			}
		}
		else {
			if (ssn!=null) {
				PsaSsn.wriTxtImm(ssn, txt);
				if (exc!=null) {
					PsaSsn.wriTxtImm(ssn, exc.getMessage());
				}
			}
		}
	}

}