// Last Update by user CUSTOMIZER at 20140402073749
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.frm.*;
import de.pisa.psa.frm.psa_art.*;
import de.pisa.psa.ifc.*;

public class IplDlgCstSvcOrdArtRefSsx extends PsaArtStrRefSsxApp
{
	
public IplDlgCstSvcOrdArtRefSsx(String dsc) throws Exception{ super(dsc); }

private boolean Spr_Par = false;

public void ADD_SPR_PAR(String par) throws Exception
{
	Spr_Par = true;
	try {
		mchFrm(par);
	}
	finally {
		Spr_Par = false;
	}
}

@Override
public void mchFrmXbm(PscFrm mch_esc) throws Exception {
	super.mchFrmXbm(mch_esc);
	
	if (Spr_Par) {
		((PsaFrm)mch_esc).calCliFnc("DTO.PSA_XPL_TRE CHD_DSC:=PSA_ART_STR_REF CHD_FLD:=PSC_GID CHD_DTO_GID_FLD:=PSA_ART.PSC_GID", 0, null);
		addNtrSprPar(mch_esc);
		
	}
}

private void addNtrSprPar(PscFrm mch_esc) throws Exception
{
	PscDto dyn_dto = getDynDto();
	PscRel frl = dyn_dto.getFrl();
	int frw = dyn_dto.getFrw();
	PscDto frl_dto = (frl==null) ? null : frl.getDto();
	String pro_gid = (frl_dto==null || frw<1) ? null : frl_dto.getDat("PSC_GID", frw);
	if (!PscGid.isVld(pro_gid)) {
		return;
	}
	PscSsn ssn = getSsn();
	PscDto svc_art_dto = ssn.newDto("PSA_PRO_SVC_ART_REF");
	svc_art_dto.setQue("FAT_GID", pro_gid);
	int svc_art_cnt = svc_art_dto.fetDat();
	StringBuilder gid_que = new StringBuilder();
	for (int svc_art_row=1; svc_art_row<=svc_art_cnt; svc_art_row++) {
		String ori_gid = svc_art_dto.getDat("PSA_SVC_ART.ORI_GID", svc_art_row);
		if (!PscGid.isVld(ori_gid)) {
			continue;
		}
		gid_que.append(' ');
		gid_que.append(ori_gid);
	}
	PscDto mch_dto = mch_esc.getDynDto();
	mch_dto.setQue("PSC_GID", gid_que.toString());
	mch_esc.fetDat();	mch_dto.setQue("PSC_GID", "");
}

}