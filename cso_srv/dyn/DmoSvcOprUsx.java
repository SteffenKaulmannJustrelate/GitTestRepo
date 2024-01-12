// Last update by user PSA_PRE_SAL on host nb-hendel-11 at 20120925111431
import de.pisa.psa.syn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.dto.psa_rsm.*;
import de.pisa.psa.dto.psa_sap.*;
import de.pisa.psa.dto.psa_prs.*;

import java.util.*;

/** userexit class for PSA_OPR */
public class DmoSvcOprUsx {

	//Debug Flag
	private static final boolean DEBUG = false;
	
	//GID's der internen Personen
    private static final Map<String, String> Prs_Map;
	
	//Nachricht an den kaufmännischen Bearbeiter senden
	// private static final boolean SND_COM_PRS = true;
	//Nachricht an den technischen Bearbeiter senden
	// private static final boolean SND_TEC_PRS = false;
    
    static {
		Prs_Map = new HashMap<String, String>();
		//Prs_Map.put("CUSTOMIZER", null);
    }
		
	/**
	 * Eine interne nachricht senden
	 * @param dto current dto
	 * @param row row
	 * @param old_sta old status (not used)
	 * @param par parameter (not used)
	 * @return always 0
	 * @throws Exception 
	 */ 
	public int sndIntNot(PscDto dto, Integer row, String old_sta, String par) throws Exception {
		PscSsn ssn = dto.getSsn();
		PscDto drv_dto = dto.getDrv();
		if (DEBUG) ssn.wriTxt("PsaSvcOprUsx.sndIntNot()");
		if (PsaSynFlg.isImp(ssn)) {
			return 0;
		}
		
		UsxPar usx_par = new UsxPar(par);
		//  
		String snd_prs = usx_par.getPar("SND_PRS");
		String msg_nam = usx_par.getPar("MSG_NAM");
		
		String opr_idn = dto.getDat("OPR_IDN", row);
		String cst_gid = dto.getDat("CON_GID", row);
		PscFld opr_nam_fld = drv_dto.getFld("PSA_OPR.NAM");
		PscFld sap_nam_fld = drv_dto.getFld("PSA_SAP.NAM");
		PscFld cst_fld = drv_dto.getFld("PSA_CST_CON_EXT.FRM_IDN");
		String opr_nam = (opr_nam_fld!=null) ? drv_dto.getDat(opr_nam_fld, row) : "";
		String sap_nam = (sap_nam_fld!=null) ? drv_dto.getDat(sap_nam_fld, row) : "";
		String cst_nam = (cst_fld!=null) ? drv_dto.getDat(cst_fld, row) : "";
		String ori_frm = PsaOprXtd.getFrm(ssn, opr_idn);	
		String ori_gid = dto.getDat("PSC_GID", row);
		String ori_nam = dto.getDat("NAM", row);
		String ori_dto = drv_dto.getDsc();
		String cre_prs_gid = PsaRsm.getCrePrsDfv(ssn);
		
		if (ori_gid.length()==0) {
			ori_gid = dto.getDbi().creGid();
			PsaDtoIpl.setSysDat(dto, "PSC_GID", row, ori_gid);
		}
		if (opr_nam.length()==0 && opr_idn.length()>0) {
			PscDto opr_dto = ssn.newDto("PSA_OPR_XTD");
			opr_dto.setQue("IDN", opr_idn);
			if (opr_dto.fetDat()!=0) {
				opr_nam = opr_dto.getDat("NAM", 1);
				sap_nam = opr_dto.getDat("PSA_SAP.NAM", 1);
			}
		}
		if (cst_nam.length()==0 && cst_gid.length()>0) {
			cst_nam = PsaDtoIpl.getFldDat(ssn, "PSA_CON", "FRN_IDN", cst_gid);
			if (cst_nam==null) cst_nam = "";
		}
		Map<String, String> msg = null;
		if(msg_nam.equals("DMO_SVC_ORD_INT_MSG_OPR_CHA"))
			msg = PsaUti.getMsgAllLng(msg_nam, ssn, opr_nam, "\r\n");
		else
			msg = PsaUti.getMsgAllLng(msg_nam, ssn, ori_nam, sap_nam, opr_nam, "\r\n", cst_nam);
		
		
		if (DEBUG) ssn.wriTxt("Message: "+msg.toString());
		
		synchronized (Prs_Map) {
			for (Map.Entry<String, String> ent : Prs_Map.entrySet()) {
				String prs_gid = ent.getValue();
				String usr_nam = ent.getKey();
				if (prs_gid==null) {
					String idc = PsaDtoIpl.getFldDat(ssn, "PSC_USR", "NAM", usr_nam, "IDC");
					prs_gid = PsaDtoIpl.getFldDat(ssn, "PSA_PRS_INT", "CIC", idc, "PSC_GID");
					if (prs_gid==null) {
						prs_gid = "";
					}
					ent.setValue(prs_gid);
				}
				if (prs_gid.length()>0) {
					if (DEBUG) ssn.wriTxt("-> "+usr_nam);
					String lng = PsaPrsInt.getLng(ssn, prs_gid);
					PsaRsm.creAlr(ssn, ori_frm, ori_dto, ori_nam, ori_gid, prs_gid, null, msg.get(lng), cre_prs_gid);
				}
			}
			
			
			if(snd_prs == null || snd_prs.length() < 1) {
				snd_prs = "TEC_PRS";
			}
			if (snd_prs.equals("COM_PRS")) {
				String com_prs_gid = dto.getDat("COM_PRS_GID", row);
				if ( com_prs_gid.length()>0 && !com_prs_gid.startsWith("OUT_") &&
						!Prs_Map.containsValue(com_prs_gid)) 
				{
					if (DEBUG) ssn.wriTxt("-> Kaufmännischer Bearbeiter");
					String lng = PsaPrsInt.getLng(ssn, com_prs_gid);
					PsaRsm.creAlr(ssn, ori_frm, ori_dto, ori_nam, ori_gid, com_prs_gid, null, msg.get(lng), cre_prs_gid);
				}
			}
			if (snd_prs.equals("TEC_PRS")) {
				String tec_prs_gid = dto.getDat("TEC_PRS_GID", row);
				if ( tec_prs_gid.length()>0 && !tec_prs_gid.startsWith("OUT_") &&
						!Prs_Map.containsValue(tec_prs_gid)) 
				{
					if (DEBUG) ssn.wriTxt("-> Technischer Bearbeiter");
					String lng = PsaPrsInt.getLng(ssn, tec_prs_gid);
					PsaRsm.creAlr(ssn, ori_frm, ori_dto, ori_nam, ori_gid, tec_prs_gid, null, msg.get(lng), cre_prs_gid);
				}
			}
		}
		
		return 0;
	}

}