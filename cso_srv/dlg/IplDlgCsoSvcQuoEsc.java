// Last Update by user CUSTOMIZER at 20110812122241
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.dto.*;

/** CSO_SVC_QUO_ESC */
public class IplDlgCsoSvcQuoEsc extends de.pisa.psa.frm.psa_svc.PsaSvcQuoApp
{

public IplDlgCsoSvcQuoEsc(String dsc) throws Exception{ super(dsc); }

@Override
protected void copProXbm(int row, PscFrm new_frm, int new_row) throws Exception {
	PscDto dyn_dto = getDynDto();
	PscDto new_dto = new_frm.getDynDto();
	if (new_dto.hasSupDto("PSA_SVC_ORD") &&
		dyn_dto.getDat("SAP_IDN", row).equals("PSA_SVC_QUO_REP") &&
		new_dto.getDat("SAP_IDN", new_row).equals("PSA_SVC_ORD_REP")) 
	{
		String ord_dat = dyn_dto.getDat("ORD_DAT", new_row);
		
		Calendar cal = Calendar.getInstance();
		if (!ord_dat.isEmpty()) {
			cal.setTime(PscUti.getTim(ord_dat));
		}
		cal.set(Calendar.HOUR_OF_DAY, 9);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		String act_beg_dat = PscUti.getTim(cal.getTime());
		new_dto.setDat("ACT_BEG_DAT", new_row, act_beg_dat);
		cal.set(Calendar.HOUR_OF_DAY, 18);
		String act_end_dat = PscUti.getTim(cal.getTime());
		new_dto.setDat("ACT_END_DAT", new_row, act_end_dat);
		new_dto.setDat("DLV_DAT", new_row, act_beg_dat);
		
		new_dto.setDat("ACT_FLG", new_row, "y");
		new_dto.setDat("DLV_FLG", new_row, "n");
		new_dto.setDat("CLC_IDN", new_row, "PSA_SVC_ORD_CLC_10");
		PsaDtoIpl.copMulLngCol(new_dto, new_row, "PSA_SVC_ORD_CLC", "CLC_IDN", "CLC_TXT");
	}
	
	super.copProXbm(row, new_frm, new_row);
}

}