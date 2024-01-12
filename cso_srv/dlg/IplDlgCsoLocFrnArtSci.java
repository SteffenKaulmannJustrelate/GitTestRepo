// Last Update by user PSA_PRE_SAL at 20090721140955
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.dto.*;

public class IplDlgCsoLocFrnArtSci extends de.pisa.psa.frm.psa_art.PsaFrnArtApp
{
public IplDlgCsoLocFrnArtSci(String dsc) throws Exception{ super(dsc); }

@Override
public int insFrmGidXbm(int row, PscFrm new_frm) throws Exception {
    if (new_frm!=null) {
        PscDto new_frm_dto = new_frm.getDynDto();
        int new_frm_row = new_frm.getRow();
        PscDto dyn_dto = getDynDto();
        PscRel frl = dyn_dto.getFrl();
        PscDto frl_dto = (frl!=null) ? frl.getDto() : null;
        int frw = dyn_dto.getFrw();
        if (new_frm_dto!=null && new_frm_row>0 && frl_dto!=null && frw>0) 
        {
            String frl_gid = frl_dto.getDat("PSC_GID", frw);
            if (frl_dto.getSupDto("CSO_LOC")!=null) { 
                if (frl_gid.length()==0 || frl_gid.equals("NULL")) {
                    frl_gid = new_frm_dto.getDbi().creGid();
                    PsaDtoIpl.setSysDat(frl_dto, "PSC_GID", frw, frl_gid);
                }
                new_frm_dto.setDat("CSO_LOC_GID", new_frm_row, frl_gid);
            }
        }           
    }
    return super.insFrmGidXbm(row, new_frm);
}

@Override
public PscFrm insFrmGid(String dlg_dsc, int row, boolean cre_gid, boolean mod)
	throws Exception 
{
	PscFrm new_frm = super.insFrmGid(dlg_dsc, row, cre_gid, mod);
	if (new_frm!=null && new_frm.chkExiFlg()) {
		fetDat();
	}
	return new_frm;
}

}