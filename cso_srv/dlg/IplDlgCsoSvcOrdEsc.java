// Last Update by user PSA_PRE_SAL at 20101025120758
import java.util.*;

import de.pisa.psc.srv.dsi.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;

/** CSO_SVC_ORD_ESC */
public class IplDlgCsoSvcOrdEsc extends de.pisa.psa.frm.psa_svc.PsaSvcOrdApp
{

public IplDlgCsoSvcOrdEsc(String dsc) throws Exception{ super(dsc); }

public void INI_TAG_ACT(String tag, Integer row) throws Exception
{
	INI_TAG_ACT(tag);
}

public void INI_TAG_ACT(String tag) throws Exception
{
	PscSsn ssn = getSsn();
	PscAcc acc = ssn.getAcc();
	List<String> grp_lis = (acc==null) ? null : acc.getLis();
	if (grp_lis!=null && grp_lis.contains("78")) {
		calEvt("TAG_ACT "+tag);
	}
}

public void OPN_PRC_OBJ(String par, Integer row) throws Exception {
	PscSsn ssn = getSsn();
	PscGui gui = getGui();
	PscDto dyn_dto = getDynDto();
	String pro_gid = dyn_dto.getDat("PSC_GID", row);
	String prc_obj_gid = PsaDtoIpl.getFldDat(ssn, "PSA_PRC_OBJ", "PRO_GID", pro_gid, "PSC_GID");
	if (PscGid.isVld(prc_obj_gid)) {
		PsaFrmIpl.opnFrmGid(gui, "PSA_PRC_OBJ_ESC", prc_obj_gid, false);
	}
	else {
		ssn.wriMsg("PSA_NO_REC_FND");
	}
}

}