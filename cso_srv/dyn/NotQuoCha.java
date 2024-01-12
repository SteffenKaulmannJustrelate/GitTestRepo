import java.util.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psa.dto.psa_rsm.*;
import de.pisa.psa.dto.psa_sap.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;


public class NotQuoCha {
	//	Test Flag
	private static final boolean TEST = false;
	
	//Debug Flag
	private static final boolean DEBUG = false;

	
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
	}
	
	/**
	 * Eine interne nachricht senden
	 */ 
	public int sndMsg(PscDto dto, Integer row, String old_sta, String par)
		throws Exception {
		int ret = 0;
		PscSsn ssn = dto.getSsn();
		PscDto drv_dto = dto.getDrv();
		if (DEBUG) ssn.wriTxt("creIntMsg.sndIntMsg()");

		String opr_idn = dto.getDat("OPR_IDN", row);
		String cst_gid = dto.getDat("CON_GID", row);
		PscFld opr_nam_fld = drv_dto.getFld("PSA_OPR.NAM");
		PscFld cst_fld = drv_dto.getFld("PSA_CST_CON_EXT.FRM_IDN");
		String opr_nam = (opr_nam_fld!=null) ? drv_dto.getDat(opr_nam_fld, row) : "";
		
		String ori_frm = PsaOprXtd.getFrm(ssn, opr_idn);	
		String ori_gid = dto.getDat("PSC_GID", row);
		String quo_nam = dto.getDat("NAM", row);
		String ori_dto = drv_dto.getDsc();
		
		PscDto opr_dto = ssn.newDto("PSA_OPR_XTD");
		
		if (opr_idn !=null && opr_idn.length()>0) {
			opr_dto.setQue("IDN", opr_idn);
			if (opr_dto.fetDat()!=0) {
				opr_nam = opr_dto.getDat("NAM", 1);
				
			}
		}
		String opr_old = "";
		if ( old_sta  != null && old_sta.length()>0) {
			opr_dto.setQue("IDN", old_sta);
			if (opr_dto.fetDat()!=0) {
				opr_old = opr_dto.getDat("NAM", 1);
			}
		}
		
		if(opr_nam != null && opr_nam.length() >0 && !opr_nam.equals(opr_old)){
			
			
			if(TEST){
				String cst_nam = (cst_fld!=null) ? drv_dto.getDat(cst_fld, row) : "";
				if (cst_nam.length()==0 && cst_gid.length()>0) {
					cst_nam = PsaDto.getFldDat(ssn, "PSA_CON", "FRN_IDN", cst_gid);
					if (cst_nam==null) cst_nam = "";
				}
				ssn.wriTxt("cst_nam: "+cst_nam);
			}
			Map<String, String> msg = PsaUti.getMsgAllLng("PSD_QUO_STA_CHA", ssn, quo_nam, "\r\n", opr_old, opr_nam);
			
			if (DEBUG) ssn.wriTxt("Message: "+msg.toString());
			
			//kfm. Bearbeiter
			String com_prs_gid = dto.getDat("COM_PRS_GID", row);
			if ( com_prs_gid.length()>0 && !com_prs_gid.startsWith("OUT_")) {
				if (DEBUG) ssn.wriTxt("-> Kfm. Bearbeiter");
				String lng = PsaPrsInt.getLng(ssn, com_prs_gid);
				PsaRsm.creAlr(ssn, 0, ori_frm, ori_dto, quo_nam, ori_gid, com_prs_gid, msg.get(lng));
			}else{
				new Exception("Kein gültiger Kfm. Bearbeiter am Angebot!");
			}
			// techn. Bearbeiter
			String tec_prs_gid = dto.getDat("TEC_PRS_GID", row);
			if ( tec_prs_gid.length()>0 && !tec_prs_gid.startsWith("OUT_")) {
				if (DEBUG) ssn.wriTxt("-> Techn. Bearbeiter");
				String lng = PsaPrsInt.getLng(ssn, tec_prs_gid);
				PsaRsm.creAlr(ssn, 0, ori_frm, ori_dto, quo_nam, ori_gid, tec_prs_gid, msg.get(lng));
			}else{
				if(TEST) ssn.wriTxt("Kein techn. Bearbeiter am Angebot vorhanden.");
			}
		}
		return ret;
	}
	/**
	 * @see de.pisa.psa.dto.psa_oth.PsaSta#stdStaChgFnc(...)
	 * Reimplementierung  
	 * 
	 * @param dto
	 * @param row
	 * @param old_sta
	 * @param par
	 * @return
	 * @throws Exception
	 */
	public int quoWin(PscDto dst_dto, Integer row, String old_sta, String par)
	throws Exception {
		if( par.startsWith("SET_FLD:")) {
            PscStrTok tok = new PscStrTok(par.substring(8), ';');
			while (tok.hasMoreTokens()) {
                String str = tok.nextToken();
                int str_idx = str.indexOf('=');
                if (str_idx!=-1) {
    				String fld_nam = str.substring(0, str_idx);
    				String val = str.substring(str_idx+1);
    				String new_val = PsaDto.mapCtxVal(dst_dto.getSsn(), val);
    				dst_dto.setDat( fld_nam, row.intValue(), new_val);
                }
			}
		}
		sndMsg(dst_dto, row, old_sta, par);
		return new Integer(0);
	}

}
