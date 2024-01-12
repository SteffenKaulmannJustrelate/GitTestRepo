// Last Update by user CUSTOMIZER at 20110812121452
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_sap.*;
import de.pisa.psa.frm.psa_svc.*;
import de.pisa.psa.ifc.*;

/** CST_SVC_TRB_ESC */
public class IplDlgCstSvcTrbEsc extends PsaSvcApp
{
	
public IplDlgCstSvcTrbEsc(String dsc) throws Exception{ super(dsc); }

@Override
public int insFrmGidXbm(int row, PscFrm new_frm) throws Exception {
	PscDto dyn_dto = getDynDto();
	PscDto new_dto = new_frm.getDynDto();
	if (row<=0) {
		row = getRow();
	}
	int new_row = new_frm.getRow();
	PscSsn ssn = getSsn();
	if (new_row>0 && row>0) {
		boolean svc_quo = new_dto.hasSupDto("PSA_SVC_QUO");
		boolean svc_ord = new_dto.hasSupDto("PSA_SVC_ORD");
		// set COM_PRS_GID
		if (svc_quo || svc_ord) {
			String con_gid = dyn_dto.getDat("CON_GID", row);
			String cre_prs_gid = PsaDtoIpl.getFldDat(ssn, "PSA_CON", "CRE_PRS_GID", con_gid);
			if (PscGid.isVld(cre_prs_gid)) {
				new_dto.setDat("COM_PRS_GID", new_row, cre_prs_gid);
				PsaDtoIpl.refCom(new_dto, new_dto.getFld("PSA_COM_PRS.FRN_IDN"), new_row);
			}
		}
		// set dates
		if (svc_ord) {
			Calendar cal = Calendar.getInstance();
			String src_dat = dyn_dto.getDat("SRC_DAT", row);
			if (!src_dat.isEmpty()) {
				cal.setTime(PscUti.getTim(src_dat));
			}
			cal.add(Calendar.DAY_OF_YEAR, 1);
			cal.set(Calendar.HOUR_OF_DAY, 9);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			String act_beg_dat = PscUti.getTim(cal.getTime());
			new_dto.setDat("ACT_BEG_DAT", new_row, act_beg_dat);
			cal.set(Calendar.HOUR_OF_DAY, 18);
			String act_end_dat = PscUti.getTim(cal.getTime());
			new_dto.setDat("ACT_END_DAT", new_row, act_end_dat);
			new_dto.setDat("DLV_DAT", new_row, act_beg_dat);
		}
		else if (svc_quo) {
			String src_dat = dyn_dto.getDat("SRC_DAT", row);
			Calendar cal = Calendar.getInstance();
			if (!src_dat.isEmpty()) {
				cal.setTime(PscUti.getTim(src_dat));
			}
			cal.add(Calendar.DAY_OF_YEAR, 1);
			String dat = PscUti.getTim(cal.getTime());
			new_dto.setDat("ORD_DAT", new_row, dat);
		}
		// set flags
		if (svc_ord) {
			new_dto.setDat("ACT_FLG", new_row, "y");
			new_dto.setDat("DLV_FLG", new_row, "n");
			new_dto.setDat("CLC_IDN", new_row, "PSA_SVC_ORD_CLC_10");
			PsaDtoIpl.copMulLngCol(new_dto, new_row, "PSA_SVC_ORD_CLC", "CLC_IDN", "CLC_TXT");
		}
		// set status
		String opr_idn = null;
		if (svc_ord) {
			opr_idn = PsaOpr.getFstOprIdn(ssn, "PSA_SVC_ORD_REP");
		}
		else if (svc_quo) {
			opr_idn = PsaOpr.getFstOprIdn(ssn, "PSA_SVC_QUO_REP");
		}
		if (PscUti.isStr(opr_idn)) {
			new_dto.setDat("OPR_IDN", new_row, opr_idn);
			PsaDtoIpl.refCom(new_dto, new_dto.getFld("PSA_OPR.NAM"), new_row);
		}
	}
	return super.insFrmGidXbm(row, new_frm);
}


}
