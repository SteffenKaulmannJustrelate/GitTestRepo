// Last Update by user PSA_PRE_SAL at 20090721140954
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;

public class IplDlgCsoLocArtPrjRefSci extends de.pisa.psa.frm.PsaFrm
{
	
public IplDlgCsoLocArtPrjRefSci(String dsc) throws Exception{ super(dsc); }

@Override
public void mchFrmXbm(PscFrm new_frm) throws Exception {
	super.mchFrmXbm(new_frm);
	
	PscDto dyn_dto = getDynDto();
	int fat_row = dyn_dto.getFrw();
	PscFrm fat_frm = getFat();
	PscDto fat_dto = (fat_frm==null) ? null : fat_frm.getDynDto();
	if (fat_dto!=null && fat_row>0) {
		String cst_gid = fat_dto.getDat("CST_GID", fat_row);
		Set<String> pro_set = getPro(cst_gid);
		String pro_gid_que = PsaUti.set2str(pro_set, " ");
		((PsaFrm)new_frm).setFixQue("PRO_GID", pro_gid_que);
	}
	setTagPrp(new_frm, "BUT_NEW", "MOD", "0");
}

private Set<String> getPro(String cst_gid) throws Exception {
	Set<String> ret = new HashSet<String>();
	if (PscGid.isVld(cst_gid)) {
		PscSsn ssn = getSsn();
		PscDto pro_dto = ssn.newDto("PSA_PRO");
		pro_dto.setQue("CON_GID", cst_gid);
		FetMor pro_fet = new FetMor(pro_dto);
		do {
			int pro_cnt = pro_fet.fetDat();
			for (int pro_row=1; pro_row<=pro_cnt; pro_row++) {
				if (!pro_dto.chkAcc(pro_row, 'R')) {
					continue;
				}
				ret.add(pro_dto.getDat("PSC_GID", pro_row));
			}
		} while (pro_fet.fetMor());
	}
	if (ret.isEmpty()) {
		ret.add("#4711#");
	}
	return ret;
}

}