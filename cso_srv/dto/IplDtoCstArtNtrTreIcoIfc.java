// Last Update by user CUSTOMIZER at 20081023134324

import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

import de.pisa.psa.dto.psa_ifc.*;
import de.pisa.psa.ifc.*;

public class IplDtoCstArtNtrTreIcoIfc extends PsaTreIcoIfc
{
	
public IplDtoCstArtNtrTreIcoIfc(String dsc) throws Exception { super(dsc); }

@Override
protected int psaClkIcoMin(int row) throws Exception {
	PscDto dto = getDto();
	PscDto chd = dto.getChd()==null ? dto : dto.getChd();
	PscDto top = dto.getTop();
	PscFld dto_ico_fld = dto.getFld("TRE_ICO");
	PscFld psc_gid_fld = dto.getFld("PSC_GID");
	modDat(dto_ico_fld, row, PsaTreIcoIfc.Tre_Ico_Pls);
	String psc_gid = dto.getDat(psc_gid_fld, row);
	Set<String> del_gid = new HashSet<String>();
	getChdArt(psc_gid, del_gid);
	int del_cnt = 0;
	int cnt = numRec();
	for ( int del_row=row+1; del_row<=cnt; del_row++ ) {
		psc_gid = dto.getDat(psc_gid_fld, del_row);
		if ( del_gid.contains(psc_gid) ) {
			getChdArt(psc_gid, del_gid);
			del_cnt++;
			PscRec rec = chd.getRec(del_row);
			getRecLevMap().remove(rec.getIdn());
		} 
		else {
			break;
		}
	}
	if ( del_cnt>0 ) {
		top.delRow(row+1,del_cnt);
	}
	return del_cnt*-1;
}

private void getChdArt(String art_gid, Set<String> chd_set) throws Exception
{
	if (!PscGid.isVld(art_gid)) {
		return;
	}
	PscSsn ssn = getSsn();
	PscDto str_dto = ssn.newDto("PSA_ART_STR");
	str_dto.setQue("FAT_GID", art_gid);
	int str_cnt = str_dto.fetDat();
	for (int str_row=1; str_row<=str_cnt; str_row++) {
		String chd_gid = str_dto.getDat("CHD_GID", str_row);
		chd_set.add(chd_gid);
	}
}

}