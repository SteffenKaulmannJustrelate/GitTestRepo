// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211122632
import java.util.*;

import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/**
 * Verschiebt Beginn-, Ende- und ggf. Versand-Datum von Korrespondenzen.<br>
 * UPD_ENT_PRT
 * 
 * @since 14.09.2006
 * @author rekowski
 */
public class UpdEntPrt {
	
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

		String usr_idn 		= null;
		boolean mod_add_day = true;	
		boolean opn_cpd = false;
		boolean don_inc = false;
		boolean don_out = false;
		
		String mov_par = "0l";
		
		PsaFrm frm = (PsaFrm)gui.newFrm("CST_EPT_DAT_MOV_SSF");
        gui.dlgFrm(frm);
        
        if (frm.chkExiFlg()) {
        	mov_par = frm.getEdtDat("CNT_DAY");
       	 	usr_idn = frm.getEdtDat("USR_IDN");
       	 	String par = frm.getEdtDat("CPD_OPN_CHK");
       	 	if(par != null && (par.equalsIgnoreCase("y") || par.equalsIgnoreCase("j")) ) 
       	 		opn_cpd = true;
       	 	par = frm.getEdtDat("CPD_DON_INC");
    	 	if(par != null && (par.equalsIgnoreCase("y") || par.equalsIgnoreCase("j")) ) 
    	 		don_inc = true;
    	 	par = frm.getEdtDat("CPD_DON_OUT");
    	 	if(par != null && (par.equalsIgnoreCase("y") || par.equalsIgnoreCase("j")) )
    	 		don_out = true;
       }
		
        PscDto prs_int = ssn.newDto("PSA_PRS_INT");
        if(usr_idn != null && usr_idn.length() > 0
        		&& mov_par != null && mov_par.length() > 0 ){
        	prs_int.setQue("CIC",usr_idn );
        }
        else {
        	ssn.wriMsg("PSA_INV_VAL", "User-ID oder Anzahl Tage fehlen!");
        	return;
        }
        	
		String frn_idn = null;
		if(prs_int.fetDat() != 1)
			throw new Exception("ungültige Benutzer-ID");
		else
			frn_idn = prs_int.getDat("PSC_GID", 1);
		
		if(!opn_cpd && !don_inc & !don_out)
				ssn.wriMsg("CST_MOV_DIL_PRE_NOT");	
		
		PscDto con_act_usr = ssn.newDto("PSA_ACT");
		con_act_usr.setQue("STA_VAL", "OPN");
		con_act_usr.setQue("AGN_PRS_GID", frn_idn); 
		if(opn_cpd)
			movCpd_DatVal(con_act_usr, "opn", mov_par, true, mod_add_day);
		else
			movCpd_DatVal(con_act_usr, "opn", mov_par, false, mod_add_day);
		
		if(	!mod_add_day){
			// *** Datumsangabe, falls 'versandt' aller abgeschlossenen  *******
			//     Korrespondenzen auf einen Tag geschoben werden sollen,
			//     wenn benötigt idealer Weise auch über Formular einlesen
			mov_par	= "2006" + "09" + "14" + "130039" ; 	
			// *****************************************************************
		}
		PscDto psa_cpd = ssn.newDto("PSA_CPD");
		
		// eingehende, abgeschlossene Post/Mail 
		psa_cpd.setQue("STA_VAL", "DON");
		psa_cpd.setQue("STY_IDN", "INC_%");
		psa_cpd.setQue("AGN_PRS_GID", frn_idn); 
		if(don_inc)
			movCpd_DatVal(psa_cpd, "inc", mov_par, true, mod_add_day);
		else
			movCpd_DatVal(psa_cpd, "inc", mov_par, false, mod_add_day);
		
		//	ausgehende,  abgeschlossene DS Post/Fax 
		psa_cpd.delQue();
		psa_cpd.setQue("STA_VAL", "DON");
		psa_cpd.setQue("STY_IDN", "OUT_FAX OUT_CORRESPONDENCE");
		psa_cpd.setQue("AGN_PRS_GID", frn_idn);  
		if(don_out)
			movCpd_DatVal(psa_cpd, "out", mov_par, true, mod_add_day);
		else
			movCpd_DatVal(psa_cpd, "out", mov_par, false, mod_add_day);
		
		//	ausgehende, abgeschlossene DS Mail 
		psa_cpd.delQue();
		psa_cpd.setQue("STA_VAL", "DON");
		psa_cpd.setQue("STY_IDN", "OUT_EMAIL");
		psa_cpd.setQue("SND_PRS_GID", frn_idn);  // Ausnahme bei ausgehender Mail
		if(don_out)
			movCpd_DatVal(psa_cpd, "out", mov_par, true, mod_add_day);
		else
			movCpd_DatVal(psa_cpd, "out", mov_par, false, mod_add_day);
		
	}
	/** verschiebt Beginn-, Ende- und Versand-Datum  von Korrespondenzen 
	 * 
	 */ 
	protected static void movCpd_DatVal(PscDto act_dto, String typ, String mov_par,
										boolean sav, boolean mod_add_day)throws Exception {
		PscSsn ssn = act_dto.getSsn();
		int cnt = act_dto.fetDat();
		if(cnt < 1)
			return;
		String cls_new = mov_par;	
		long day_add = 0l;
		boolean opn_cpd = typ.equals("opn");
		
		if(mod_add_day)
			day_add = Long.parseLong(mov_par);
        long day_dif	= 1000l * 3600l * 24l * day_add ; 
        
		Date cls_new_d 	= new Date(); // today_d;
		long cls_new_l 	= 0l;
		if(!opn_cpd && !mod_add_day) {
			cls_new_d 	= PscUti.getTim(cls_new);
			cls_new_l 	= cls_new_d.getTime();
		}
		String pfx 	= "Offene Aktivitäten: ";
		if(typ.equals("inc")) 	pfx = "Posteingang: ";
		if(typ.equals("out"))	pfx = "Postausgang: ";
		if(!sav)				pfx = "(keine Änderungen) "+ pfx;
		
		if(mod_add_day){
			ssn.wriTxt(pfx + "'Beginn', 'Fällig am', sowie ggf. 'versandt' werden/würden um '" + mov_par + " Tage verschoben!!!");
		}else if(!opn_cpd){
			ssn.wriTxt(pfx + "Datum 'versandt' wird auf: " 
					+ PscUti.fmtVal("D:%d.%m.%y %H:%M:%S", cls_new) + " gesetzt:");
		}
		
		for(int idx = 1; idx <= cnt; idx++){
			String beg 		= act_dto.getDat("BEG_DAT",idx);
			String end 		= act_dto.getDat("END_DAT",idx);
			String sfx		= "";
			
			Date beg_d 		= PscUti.getTim(beg);
			Date end_d		= PscUti.getTim(end);	
					
			long beg_l 		= beg_d.getTime();
			long end_l 		= end_d.getTime();
			
			long cls_old_l  = 0l;
			if(!opn_cpd){
				String cls_old 	= act_dto.getDat("CLS_DAT",idx);
				Date cls_d		= PscUti.getTim(cls_old);
				cls_old_l 		= cls_d.getTime();
				cls_new_l 		= cls_old_l + day_dif;
				
				if(mod_add_day){
					cls_new_d.setTime(cls_new_l);	
					cls_new = PscUti.getTim(cls_new_d);
				}
				cls_old = PscUti.fmtVal("D:%d.%m.%y %H:%M:%S", cls_old);
				sfx = sfx + "; versandt: '" + cls_old + "'";
				if(beg_l > cls_new_l)
					sfx = sfx + " (Hinweis: 'Versandt am:' liegt VOR 'Erzeugt' !!!!)";
			}
			
			end 	= PscUti.fmtVal("D:%d.%m.%y %H:%M:%S", end);
			if(beg_l > end_l) 
				sfx = sfx + " (Hinweis: 'Erstellt' liegt VOR 'Fällig am' !!!)";
			
			ssn.wriTxt(act_dto.getDat("STY_IDN",idx)
			  + "; " + act_dto.getDat("STY_NAM_GER",idx)
			  + "; " + act_dto.getDat("NAM_GER",idx)
			  + "; fällig am: '" + end + "'" + sfx);
			
			String gid = act_dto.getDat("PSC_GID",idx);
		
			if(!opn_cpd && !mod_add_day) {
				day_dif = cls_new_l - cls_old_l;
			}
			
			beg_l = beg_l + day_dif;
			end_l = end_l + day_dif;
			
			beg_d.setTime(beg_l);
			end_d.setTime(end_l);
			
			beg = PscUti.getTim(beg_d);
			end = PscUti.getTim(end_d);
			
			if(sav){
				PsaUti.updFldDat(act_dto, "BEG_DAT", gid, beg);
				PsaUti.updFldDat(act_dto, "END_DAT", gid, end);
				
				if(!opn_cpd)
					PsaUti.updFldDat(act_dto, "CLS_DAT", gid, cls_new);
			}
			
		}if(sav)
			ssn.wriTxt(cnt + " DS gesichert; Query (Typ): "+  act_dto.getQue("STY_IDN"));
	}
}