// Last Update by user CUSTOMIZER at 20091001143130

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.frm.psa_org.*;
import de.pisa.psa.ifc.*;

public class IplDlgCstOrgExtEsc extends PsaOrgApp
{
	
public IplDlgCstOrgExtEsc(String dsc) throws Exception{ super(dsc); }

@Override
public int insFrmGidXbm(int row, PscFrm new_frm) throws Exception {
	PscDto dyn_dto = getDynDto();
	PscDto new_dto = new_frm.getDynDto();
	if (row<0) {
		row = getRow();
	}
	int new_row = new_frm.getRow();
	if (new_row>0 && row>0 && new_dto.getSupDto("PSA_SVC_ORD")!=null) {
		String cre_prs_gid = dyn_dto.getDat("CRE_PRS_GID", row);
		if (PscGid.isVld(cre_prs_gid)) {
			new_dto.setDat("COM_PRS_GID", new_row, cre_prs_gid);
			PsaDtoIpl.refCom(new_dto, new_dto.getFld("PSA_COM_PRS.FRN_IDN"), new_row);
		}
	}
	return super.insFrmGidXbm(row, new_frm);
}


}
