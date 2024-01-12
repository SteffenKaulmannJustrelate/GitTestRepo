// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20150923121511
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.dto.psa_act.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;

/** DMO_APM_RST_DAT */
public class DmoApmRstDat {

	public static void RUN(PscFrm act_frm, String par, Integer act_row) throws Exception
	{
		PscGui gui = act_frm.getGui();
		PscDto act_dto = act_frm.getDynDto();
		String act_nam = act_dto.getDat("PSA_ACT.NAM", act_row);
		String act_gid = act_dto.getDat("PSA_ACT.PSC_GID", act_row);
		if (!PscGid.isVld(act_gid)) {
			return;
		}
		if (!gui.wriBox("Sollen die Daten des Termins \""+act_nam+"\" zurückgesetzt werden?", "Q", "Q").equals("Y")) {
			return;
		}
		PscSsn ssn = act_frm.getSsn();
		SavPnt sav_pnt = new SavPnt(ssn);
		try {
			delFlwAct(ssn, act_gid);
			delDsp(ssn, act_gid);
			delQstAns(ssn, act_gid);
			delRpt(act_dto, act_row);
			setApmSta(act_dto, act_row);
			delRsm(ssn, act_gid);
			if (act_dto.chkMod()) {
				act_dto.putDat();
			}
			sav_pnt.end();
			PsaFrmIpl.rfrRel(act_frm, "CON_ACT_DSP", "ORI", "QST_RES", "DLG_PRO_ACT");
			ssn.wriMsg("PSA_DON");
		}
		finally {
			sav_pnt.abo();
		}
	}

	private static void delFlwAct(PscSsn ssn, String act_gid) throws Exception
	{
		PscDto flw_dto = ssn.newDto("PSA_ACT");
		flw_dto.setQue("ORI_GID", act_gid);
		delDat(flw_dto);
	}

	private static void delDsp(PscSsn ssn, String act_gid) throws Exception
	{
		PscDto dsp_dto = ssn.newDto("PSA_CON_ACT_DSP");
		dsp_dto.setQue("CHD_GID", act_gid);
		delDat(dsp_dto);
	}

	private static void delQstAns(PscSsn ssn, String act_gid) throws Exception
	{
		PscDto res_dto = ssn.newDto("PSA_QST_RES");
		res_dto.setQue("ACT_GID", act_gid);
		Set<String> qst_gid_set = new HashSet<>();
		try {
			while (res_dto.fetNxt()) {
				if (!res_dto.chkAcc(1, 'D')) {
					continue;
				}
				String qst_gid = res_dto.getDat("QST_GID", 1);
				if (PscGid.isVld(qst_gid)) {
					qst_gid_set.add(qst_gid);
				}
				res_dto.delDat(1);
			}
		}
		finally {
			res_dto.fetCls();
		}
		PscDto pro_act_dto = ssn.newDto("PSA_PRO_ACT");
		pro_act_dto.setQue("CHD_GID", act_gid);
		for (String qst_gid : qst_gid_set) {
			pro_act_dto.setQue("FAT_GID", qst_gid);
			delDat(pro_act_dto);
		}
	}

	private static void delRpt(PscDto act_dto, int act_row) throws Exception
	{
		PsaClbUti.clrClbFld(act_dto, "RPT", act_row);
	}

	private static void delRsm(PscSsn ssn, String act_gid) throws Exception
	{
		PscDto rsm_dto = ssn.newDto("PSA_RSM_ALL");
		rsm_dto.setQue("DTO_GID", act_gid);
		delDat(rsm_dto);
	}

	private static void setApmSta(PscDto act_dto, int act_row) throws Exception
	{
		// appointment status
		act_dto.setDat("STA_IDN", act_row, "PSA_STA_APM_OPN");
		// participant status
		String act_gid = act_dto.getDat("PSA_ACT.PSC_GID", act_row);
		if (PscGid.isVld(act_gid)) {
			PscSsn ssn = act_dto.getSsn();
			PscDto con_dto = ssn.newDto("PSA_CON_ACT_CLI_AGG");
			con_dto.setQue("CHD_GID", act_gid);
			con_dto.setQue("CON_STA_IDN", "!PSA_ACT_CON_STA_ACC | ''");
			try {
				while (con_dto.fetNxt()) {
					if (!con_dto.chkAcc(1, 'W')) {
						continue;
					}
					con_dto.setDat("CON_STA_IDN", 1, "PSA_ACT_CON_STA_ACC");
					PsaActConSta.cpyActConStaCol(con_dto, 1);
					con_dto.putDat();
				}
			}
			finally {
				con_dto.fetCls();
			}
		}
	}

	private static void delDat(PscDto dto) throws Exception
	{
		try {
			while (dto.fetNxt()) {
				if (dto.chkAcc(1, 'D')) {
					dto.delDat(1);
				}
			}
		}
		finally {
			dto.fetCls();
		}
	}

}