// Last Update by user CUSTOMIZER at 20171211125348
import de.pisa.psa.frm.psa_pro.*;
import de.pisa.psc.srv.gui.*;

/********************************************************************
* <DL>
* <DT>module</DT>       <DD>DMO_PRO_ACT_AGG_SCD</DD>
* <DT>description</DT> <DD>PiSAsales object: </DD>
* </DL>
* @since 29.09.2011
* @author rekowski
*********************************************************************/
public class IplDlgDmoProActAggScd extends PsaProActAggScd
{

	/////////////////////////////////////////////////////////////
	//variables
	/////////////////////////////////////////////////////////////
/*
	private final boolean min_pro = true;
	private final boolean oly_con_pro = false;
*/
	/////////////////////////////////////////////////////////////
	//constructor
	/////////////////////////////////////////////////////////////

	/**
	 * IplDlgDmoProActAggScd constructor
	 * @param dsc descriptor
	 * @throws Exception
	 */
	public IplDlgDmoProActAggScd(String dsc) throws Exception {
		super(dsc);
	}

	/////////////////////////////////////////////////////////////
	// overloaded methods
	/////////////////////////////////////////////////////////////

	/**
	 * @see PsaProActAggScd#mchFrmXbm(PscFrm)
	 */
	@Override
	public void mchFrmXbm(PscFrm new_frm) throws Exception {
		super.mchFrmXbm(new_frm);
/*
		if(oly_con_pro || min_pro){
			PsaFrm top_frm = (PsaFrm)getTopChdFrm();
			PsaFrm syn_dlg = (PsaFrm)getSupFrm(top_frm, "PSA_EMA_EXT_SYN_SSF");
			PscFrm pro_mch_frm = PsaFrmIpl.getSupFrm(new_frm, "PSA_PRO_MCH_ESC");
		
			if (pro_mch_frm instanceof PsaProMchEsc && 
				syn_dlg instanceof PsaEmaExtSynSsf) 	{
				
				Set<String> con_gid_set = new HashSet<String>();
				String snd_prs_gid = (String)top_frm.getValBuf(PsaEmaExtSynSsf.KEY_SND_PRS_GID);
				if (snd_prs_gid!=null) {
					con_gid_set.add(snd_prs_gid);
				}
				ProPreFet pre_fet = new ProPreFet(con_gid_set, oly_con_pro, min_pro);
				((PsaProMchEsc)pro_mch_frm).setPreFet(pre_fet);
				
			}
		} else{
			String dmo_ema_syn_pro_con_num = this.getSsn().getEnv("DMO_EMA_SYN_PRO_CON_NUM");
			if(dmo_ema_syn_pro_con_num != null && (dmo_ema_syn_pro_con_num.equalsIgnoreCase("false")
					|| dmo_ema_syn_pro_con_num.equalsIgnoreCase("null"))){
				return;
			}
			if(dmo_ema_syn_pro_con_num != null && dmo_ema_syn_pro_con_num.length() > 0){
				PsaFrm top_frm = (PsaFrm)getTopChdFrm();
				PsaFrm syn_dlg = (PsaFrm)getSupFrm(top_frm, "PSA_EMA_EXT_SYN_SSF");
				PscFrm pro_mch_frm = PsaFrmIpl.getSupFrm(new_frm, "PSA_PRO_MCH_ESC");
			
				if (pro_mch_frm instanceof PsaProMchEsc && 
					syn_dlg instanceof PsaEmaExtSynSsf) 
				{
					
					// Maier Schmiertechnik Vertriebsabteilung: C-06.004064; 11B056E7D8117216E040A8C00F011F46
					Set<String> con_gid_set = new HashSet<String>();
					
					String dmo_ema_syn_pro_con_gid = PsaDtoIpl.getFldDat(this.getSsn(),
							"PSA_CON_STR","NUM", dmo_ema_syn_pro_con_num, "PSC_GID");
					
					con_gid_set.add(dmo_ema_syn_pro_con_gid);
					ProPreFet pre_fet = new ProPreFet(con_gid_set);
					
					((PsaProMchEsc)pro_mch_frm).setPreFet(pre_fet);
					
					// nur test:
					// this.getSsn().wriTxt("NUM aus Env.: " + dmo_ema_syn_pro_con_num);
				}
			}
		}
*/
	}

	/////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////
	// protected methods
	/////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////
	// public methods
	/////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////
	// repository methods
	/////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////
	// inner class
	/////////////////////////////////////////////////////////////

//	/** project pre fetch */
//	private static class ProPreFet implements PsaPreFet {
//		private final Set<String> Con_Gid;
//		private boolean Oly_Con_Pro = false;
//		private boolean Min_Pro = true;
//
//		/**
//		 * constructor
//		 * @param con_gid contact gids
//		 */
//		public ProPreFet(Set<String> con_gid, boolean oly_con_pro, boolean min_pro) {
//			Con_Gid = con_gid;
//			Oly_Con_Pro = oly_con_pro;
//			Min_Pro = min_pro;
//		}
//		/**
//		 * constructor
//		 * @param con_gid contact gids
//		 */
//		public ProPreFet(Set<String> con_gid) {
//			Con_Gid = con_gid;
//		}
//
//		/**
//		 * @see de.pisa.psa.frm.psa_ifc.PsaPreFet#fet(de.pisa.psa.frm.PsaFrm)
//		 */
//		@Override
//		public void fet(PsaFrm frm) throws Exception {
//			PscSsn ssn = frm.getSsn();
//			PscFrm top_chd = frm.getTopChdFrm();
//			PscFrm pro_mch_frm = PsaFrmIpl.getSupFrm(frm, "PSA_PRO_MCH_ESC");
//			String opn_que;
//			if (pro_mch_frm instanceof PsaProMchEsc) {
//				opn_que = ((PsaProMchEsc)pro_mch_frm).isOprFlt() ? "OPN" : "";
//			}
//			else {
//				opn_que = "OPN";
//			}
//			Set<String> pro_gid = getOpnPro(ssn, opn_que);
//			PsaActApp.addPrePro(top_chd, "PSA_PRO.PSC_GID", pro_gid);
//		}
//
//		private Set<String> getOpnPro(PscSsn ssn, String opn_que) 
//			throws Exception
//		{
//
//			Set<String> ret = new HashSet<String>();
//			if(Oly_Con_Pro){
//				ret = Con_Gid;
//				return ret;
//			}
//			if (Con_Gid==null || Con_Gid.isEmpty()) {
//				return ret;
//			}
//			PscDto con_dto = ssn.newDto("PSA_CON_EXT");
//			PscDto pro_dto = ssn.newDto("PSA_PRO_CON_AGG");
//			pro_dto.setQue("PSA_OPR.OPN", opn_que);
//			for (String con_gid : Con_Gid) {
//				if (!PscGid.isVld(con_gid)) {
//					continue;
//				}
//				con_dto.setQue("PSC_GID", con_gid);
//				if (con_dto.fetDat()==0) {
//					continue;
//				}
//				String org_gid = con_dto.getDat("ORG_GID", 1);
//				pro_dto.setQue("CHD_GID", con_gid);
//				int pro_cnt = pro_dto.fetDat();
//				//   pro_dto.getSsn().wriTxt("pro_cnt 1 = " + pro_cnt);
//				for (int pro_row=1; pro_row<=pro_cnt; pro_row++) {
//					ret.add(pro_dto.getDat("FAT_GID", pro_row));
//				}
//
//				if (!Min_Pro && PscGid.isVld(org_gid)) {
//					pro_dto.setQue("CHD_GID", org_gid);
//					pro_cnt = pro_dto.fetDat();
//					//  pro_dto.getSsn().wriTxt("pro_cnt 2 = " + pro_cnt);
//					for (int pro_row=1; pro_row<=pro_cnt; pro_row++) {
//						ret.add(pro_dto.getDat("FAT_GID", pro_row));
//					}
//				}
//			}
//			return ret;
//		}
//	}

}