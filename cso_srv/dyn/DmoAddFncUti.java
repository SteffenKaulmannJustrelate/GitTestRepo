// Last update by user PSA_PRE_SAL on host PC-REKOWSKI-11 at 20140306091648
import de.pisa.psa.dto.*;
import de.pisa.psa.frm.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** DMO_ADD_FNC_UTI */
public class DmoAddFncUti {

	/**
	 * 
	 * @param gui
	 * @throws Exception
	 */
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

	}
	
	/**
	 * 
	 * @param frm
	 * @param par
	 * @param row
	 * @throws Exception
	 */
	public static void DMO_NEW_POT_INA_OPP(PscFrm frm, String par, Integer row) throws Exception {
 		PscSsn ssn = frm.getSsn();
 		PscGui gui = ssn.getGui();
 		
 		String nam = "";
		PsaFrm pot_opp_ini_frm = (PsaFrm)gui.newFrm("DMO_NEW_POT_INA_OPP_SSF");
		PscDto mdl_dlg_dto = pot_opp_ini_frm.getDynDto();
		pot_opp_ini_frm.insRow();
		
		gui.dlgFrm(pot_opp_ini_frm);
	    if (pot_opp_ini_frm.chkExiFlg()) {
	    	nam = pot_opp_ini_frm.getEdtDat("DAT_NAM");
	    	String prl_gid = mdl_dlg_dto.getDat("PRL_GID", 1);
	    	String prl_nam = PsaDto.getFldDat(ssn, "PSA_PRL", "NAM", prl_gid);
	    	String trn_ovr = mdl_dlg_dto.getDat("DAT_TRN_OVR", 1);
	    	
	    	String ord_dat = pot_opp_ini_frm.getEdtDat("DAT_ORD_DAT");
	    	String cmt_txt = pot_opp_ini_frm.getEdtDat("DSC_CMT"); 
	    	
	    	PsaFrm new_opp_frm = (PsaFrm)gui.newFrm("PSA_OPP_MSC");
	    	new_opp_frm.insRow();
	    	PscDto opp_dto = new_opp_frm.getDynDto();
	    	
	    	opp_dto.setDat("NAM", 1, nam);
	    	opp_dto.setDat("TRN_OVR", 1, trn_ovr);
	    	opp_dto.setDat("ORD_DAT", 1, ord_dat);
	    	opp_dto.setDat("DSC", 1, cmt_txt);
	    	opp_dto.setDat("PRL_GID", 1, prl_gid);
	    	PsaDto.refCom(opp_dto, opp_dto.getFld("PSA_PRL.NAM"), 1);
	    	opp_dto.setDat("PSA_PRL.NAM", 1, prl_nam);
	    	gui.dlgFrm(new_opp_frm);
	    	
        }else 
        	return;
	    
 	}
}