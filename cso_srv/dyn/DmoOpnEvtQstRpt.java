// Last update by user PSA_PRE_SAL on host dmoref63 at 20141002141140

import de.pisa.psa.dto.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** DMO_OPN_EVT_QST_RPT */
public class DmoOpnEvtQstRpt {

	/**
	 * open wizard
	 * @param gui 
	 * @throws Exception
	 */
	public void run(PscGui gui) throws Exception {
		OPN(gui, "");
	}

	/**
	 * 
	 * @param frm
	 * @param par
	 * @param row
	 * @throws Exception
	 */
	public static void INI_MAI_CON(PscFrm frm, String par)throws Exception{
		PscSsn ssn = frm.getSsn();
		String mai_con_gid = ((PsaFrm) frm).getEdtDat("DMO_MAI_CON_GID");
		if(mai_con_gid != null && PscGid.isVld(mai_con_gid)){
			PscDto con_dto = ssn.newDto("PSA_CON_XRO");
			con_dto.setQue("PSC_GID", mai_con_gid);
			PscFrm prs_sub_frm = frm.getSub("PSA_EVT_QST_WIZ_PRS_SCI");
			
			if(con_dto.fetDat() == 1 && prs_sub_frm!= null){
				int con_row = 1; 
				String con_nam = con_dto.getDat("CMP_NAM", con_row); 	
				String org_nam = con_dto.getDat("PSA_ORG.FRN_IDN", con_row);
				String org_gid = con_dto.getDat("ORG_GID", con_row);
				String pos = con_dto.getDat("ORG_POS", con_row);	
				
				PscDto sub_dto = prs_sub_frm.getDynDto();
				sub_dto.insRow(1);
				((PsaDto) sub_dto).setSysDat("PSC_GID", 1, mai_con_gid);
				sub_dto.setDat("NAM", 1, con_nam);
				sub_dto.setDat("ORG_NAM", 1, org_nam);
				sub_dto.setDat("ORG_POS", 1, pos);
				
				String sta_idn = ((PsaFrm) frm).getEdtDat("DMO_MAI_CON_STA");
				if(!(sta_idn != null && sta_idn.length() > 0
						&& !sta_idn.equalsIgnoreCase("EMPTY"))){
					sta_idn = "PSA_ACT_CON_STA_UPT";
				}
				String sta_nam = PsaDto.getFldDat(ssn, "PSA_ACT_CON_STA", 
						"IDN", sta_idn, "NAM");
				sub_dto.setDat("STA_IDN", 1, sta_idn);
				sub_dto.setDat("STA_NAM", 1, sta_nam);
				sub_dto.setDat("MAI_CON", 1, "y");
				sub_dto.setDat("ORG_GID", 1, org_gid);						
				
				sub_dto.putDat();
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
	public static void DMO_OPN_EQR(PscFrm frm, String par, Integer row) throws Exception{
		PscSsn ssn = frm.getSsn();
		PscGui gui = ssn.getGui();
		int act_row = 0;
		String dlg_dsc = "PSA_EVT_QST_WIZ_SSF";
		String pro_gid = null;
		String pro_nam = "";
		
		PscDto dyn_dto = frm.getDynDto();
		String chd_gid = dyn_dto.getDat("CHD_GID", row);
		PscDto act_dto = ssn.newDto("PSA_ACT");
		act_dto.setQue("PSC_GID", chd_gid);
		if(act_dto.fetDat() == 1){
			act_row = 1;
			
			pro_gid = act_dto.getDat("PRO_GID", act_row);
			if(pro_gid != null && PscGid.isVld(pro_gid)){
				pro_nam = PsaDto.getFldDat(ssn, "PSA_PRO_MKT", "NAM", pro_gid);
			}
			
		}
		
		SavPnt sav_pnt = new SavPnt(ssn);
		try {
			PsaFrm wiz_frm = (PsaFrm)gui.newFrm(dlg_dsc);
			if(pro_nam != null && pro_nam.length() > 0){
				wiz_frm.setEdtDat("EVT_NAM", pro_nam);
				wiz_frm.setEdtDat("EVT_GID", pro_gid);
				
				PscFrm prs_sub_frm = wiz_frm.getSub("PSA_EVT_QST_WIZ_PRS_SCI");
				if(!(prs_sub_frm!= null)){
					prs_sub_frm = wiz_frm.creSubFrm("PSA_EVT_QST_WIZ_PRS_SCI");
				}
				if(prs_sub_frm!= null){
					
					String mai_con_gid = act_dto.getDat("CON_GID", act_row);
					if(mai_con_gid != null && PscGid.isVld(mai_con_gid)){
						
						wiz_frm.setEdtDat("DMO_MAI_CON_GID", mai_con_gid);
						String con_sta =  "PSA_ACT_CON_STA_UPT";
						
						PscDto con_act_dto = ssn.newDto("PSA_CON_ACT_CLI_AGG");
						con_act_dto.setQue("CHD_GID", chd_gid);
						con_act_dto.setQue("FAT_GID", mai_con_gid);
						String con_sta_nam = null;
						if(con_act_dto.fetDat() > 0){
							con_sta_nam = con_act_dto.getDat("CON_STA_IDN", 1);
						}
						
						if(con_sta_nam != null && con_sta_nam.length() > 0
								&& !con_sta_nam.equalsIgnoreCase("EMPTY")){
							con_sta = con_sta_nam;
						}
						wiz_frm.setEdtDat("DMO_MAI_CON_STA", con_sta);
					}
				}	
			}
			
			PsaFrmIpl.setModFrm(wiz_frm, true);
			gui.dlgFrm(wiz_frm);
			if (wiz_frm.chkExiFlg()) {
				sav_pnt.end();
			}
		}
		finally {
			sav_pnt.abo();
		}
	}
	
	/**
	 * open wizard
	 * @param gui gui
	 * @param par
	 * @throws Exception
	 */
	public static void OPN(PscGui gui, String par) throws Exception
	{
		PscSsn ssn = gui.getSsn();
		UsxPar usx_par = new UsxPar(par);
		String dlg_dsc = usx_par.getPar("DLG_DSC", "PSA_EVT_QST_WIZ_SSF");
		UsxPar env_par = new UsxPar(ssn.getEnv("PSA_EVT_QST_WIZ_PRO"));
		String pro_gid = env_par.getPar("GID");
		String pro_nam = env_par.getPar("NAM");
		String pro_num = env_par.getPar("NUM");

		// search project
		PscDto pro_dto = ssn.newDto("PSA_PRO");
		boolean fet = false;
		if (PscGid.isVld(pro_gid)) {
			pro_dto.setQue("PSC_GID", pro_gid);
			fet = true;
		}
		else {
			if (!pro_nam.isEmpty()) {
				pro_dto.setQue("NAM", "'"+pro_nam+"'");
				fet = true;
			}
			if (!pro_num.isEmpty()) {
				pro_dto.setQue("NUM", "'"+pro_num+"'");
				fet = true;
			}
		}
		pro_gid = pro_nam = pro_num = "";
		if (fet && pro_dto.fetDat()==1) {
			pro_gid = pro_dto.getDat("PSC_GID", 1);
			pro_nam = pro_dto.getDat("NAM", 1);
			pro_num = pro_dto.getDat("NUM", 1);
		}
		
		SavPnt sav_pnt = new SavPnt(ssn);
		try {
			PsaFrm wiz_frm = (PsaFrm)gui.newFrm(dlg_dsc);
			wiz_frm.setEdtDat("EVT_NAM", pro_nam);
			wiz_frm.setEdtDat("EVT_GID", pro_gid);
			PsaFrmIpl.setModFrm(wiz_frm, true);
			gui.dlgFrm(wiz_frm);
			if (wiz_frm.chkExiFlg()) {
				sav_pnt.end();
			}
		}
		finally {
			sav_pnt.abo();
		}
	}

}