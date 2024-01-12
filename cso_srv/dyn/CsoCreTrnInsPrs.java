// Last update by user CUSTOMIZER on host dmoref at 20211027104416

import java.util.ArrayList;

import de.pisa.psa.frm.PsaFrm;
import de.pisa.psa.ifc.PscGid;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.gui.PscGui;
import de.pisa.psc.srv.svc.PscUti;

/** CSO_CRE_TRN_INS_PRS */
public class CsoCreTrnInsPrs {

	private static String Tem_num = "C-20-019327";
	private static String Pro_num = "P-20-009130";

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

		//inpMltPrs(gui, ssn);
		tst(gui, ssn);
	}

	private void tst(PscGui gui, PscSsn ssn) throws Exception {
		chgLogIn(ssn, "Tester", "2214");
		
	}

	public static void CSO_CRE_TRN_INS_PRS(PscGui gui, String par) throws Exception {

		PscSsn ssn = gui.getSsn();
		inpMltPrs(gui, ssn);

	}

	public static void inpMltPrs(PscGui gui, PscSsn ssn) throws Exception {
		PsaFrm new_frm = (PsaFrm) gui.newFrm("CSO_CRE_TRN_INP_SSC");
		gui.dlgFrm(new_frm);
		boolean sav = new_frm.chkExiFlg();
		if (!sav)
			return;

		String[] nam_lis = new String[12];
		String pro_nam = new_frm.getEdtDat("CSO_TRN_NAM");

		for (int num = 1; num <= 12; num++) {
			if (num < 10) {
				nam_lis[num - 1] = new_frm.getEdtDat("CSO_CMP_NAM_00" + num);
			} else {
				nam_lis[num - 1] = new_frm.getEdtDat("CSO_CMP_NAM_0" + num);
			}
		}

		// Team 9761F709D15F4ACF9FF404C70FEF1428
		PscDto tem_dto = ssn.newDto("PSA_PRS_GRP_TEA_XTD", false, false, true, true);
		tem_dto.setQue("NUM", "'" + Tem_num + "'");
		if (tem_dto.fetDat() == 1) {
			String tem_gid = tem_dto.getDat("PSC_GID", 1);
			if(PscUti.isStr(pro_nam)){
				tem_dto.setDat("CMP_NAM", 1, pro_nam);
				tem_dto.putDat();				
			}
			// Mitglieder
			PscDto tem_mem_dto = ssn.newDto("PSA_GRP_CON_INT_REF", false, false, true, true);
			tem_mem_dto.setQue("FAT_GID", "'" + tem_gid + "'");
			try {
				int prs_cnt = 0;
				ArrayList <String> par_aph = new ArrayList<String>();
				while (tem_mem_dto.fetNxt()) {
					String prs_gid = tem_mem_dto.getDat("CHD_GID", 1);
					PscDto prs_dto = ssn.newDto("PSA_PRS_INT", false, false, true, true);
					prs_dto.setQue("PSC_GID", "'" + prs_gid + "'");
					if (prs_dto.fetDat() == 1) {
						if (PscUti.isStr(nam_lis[prs_cnt]) && nam_lis[prs_cnt].length() > 3) {
							String cmp_nam = nam_lis[prs_cnt];
							prs_dto.setDat("CMP_NAM", 1, cmp_nam);
							prs_dto.putDat();
							String fst_nam = prs_dto.getDat("FST_NAM", 1);
							String snd_nam = prs_dto.getDat("NAM", 1);
							if (PscUti.isStr(fst_nam) && PscUti.isStr(snd_nam)) {
								String fst_let = fst_nam.substring(0, 1);
								String snd_let = snd_nam.substring(0, 1);
								String par = fst_let + snd_let;
								if(	par_aph.contains(par)){
									par = par + snd_nam.substring(1, 2);
								}
								par_aph.add(par);
								prs_dto.setDat("INI", 1, par);									

								nam_lis[prs_cnt] = prs_dto.getDat("NAM", 1);
								String ben_num = prs_dto.getDat("CIC", 1);
								chgLogIn(ssn, nam_lis[prs_cnt], ben_num);
							} else {
								ssn.wriTxt("Name der Person " + nam_lis[prs_cnt] + " konnte nicht eingelesen werden.");
							}
						}else{
							// dont change/delete anything keep members for last minute joins
						}
						prs_dto.putDat();
					}
					prs_cnt++;
				}
			} finally {
				tem_mem_dto.fetCls();
			}
		}

		// Projekt
		PscDto pro_dto = ssn.newDto("PSA_FOL_PRC_XTD", false, false, true, true);
		pro_dto.setQue("NUM", "'" + Pro_num + "'");
		if (pro_dto.fetDat() == 1) {
			if(PscUti.isStr(pro_nam)){
				pro_dto.setDat("NAM", 1, pro_nam);
				pro_dto.putDat();				
			}
			String pro_gid = pro_dto.getDat("PSC_GID", 1);
			if(PscGid.isVld(pro_gid)){
				PscDto pro_con_dto = ssn.newDto("PSA_FOL_PRC_CON_REF", false, false, true, true);
				pro_con_dto.setQue("FAT_GID", "'" + pro_gid + "'");
				pro_con_dto.setQue("PSA_CON_XRO.CMP_NAM", "Teilnehmer");
				try{
					while (pro_con_dto.fetNxt()){
						pro_con_dto.delDat(1); // remove members from project
						pro_con_dto.putDat();
					}
				}
				finally{
					pro_con_dto.fetCls();
				}
			}
		} else {
			ssn.wriTxt("Projekt konnte nicht gefunden werden. Bitte händisch anpassen.");
		}

	}

	public static void chgLogIn(PscSsn ssn, String nam, String ben_num) throws Exception {
		// Benutzerverwaltung
		PscDto log_dto = ssn.newDto("PSA_USR", false, false, true, true);
		log_dto.setQue("IDC", ben_num);
		PscDto log_nam_fre = ssn.newDto("PSA_USR");
		if (log_dto.fetDat() == 1 && PscUti.isStr(nam)) {
			try {
				// replace german Umlaut
				nam = nam.replace("Ü", "Ue");
				nam = nam.replace("Ö", "Oe");
				nam = nam.replace("Ä", "Ae");
				nam = nam.replace("ü", "ue");
				nam = nam.replace("ö", "oe");
				nam = nam.replace("ä", "ae");
				nam = nam.replace("ß", "ss");
				
				nam = nam.toUpperCase();	// Login in caps
				
				// test if the login name was not set before  i.e. there can only be one MUELLER
				boolean nam_unq = false;
				while (!nam_unq) {
					log_nam_fre.setQue("NAM", nam);
					if( log_nam_fre.cntDat()>0) {
						nam = nam + "_1";
					}else {
						nam_unq = true;
					}
				}
				
				log_dto.setDat("NAM", 1, nam);
				log_dto.setDat("PWD", 1, "");
				ssn.wriTxt("name " + nam);
				log_dto.putDat();

			}
			catch (Exception e) {
				ssn.wriTxt("Benutzerverwaltung für User\n" + nam + "\nbitte händisch anpassen.");
			}
		} else {
			ssn.wriTxt("Benutzerverwaltung für User\n" + nam + "\nbitte händisch anpassen.");
		}
	}
}