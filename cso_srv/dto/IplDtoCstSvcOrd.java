// Last Update by user CUSTOMIZER at 20101022073009
import java.util.*;import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_svc.*;
import de.pisa.psa.ifc.*;

/** CST_SVC_ORD */
public class IplDtoCstSvcOrd extends PsaSvcOrdApp
{

public IplDtoCstSvcOrd(String dsc) throws Exception{ super(dsc); }

@Override
protected void putRec(int row) throws Exception {
	PscDto drv_dto = getDrv();
	if (drv_dto.getTyp()==PscDto.TYP_COM || PsaDtoIpl.chkOut(this, row)) {
		super.putRec(row);
		return;
	}
	
	PscSsn ssn = getSsn();
	PscDto dlv_adr_dto = null;
	if (chkMod("CON_GID", row)) {
		String con_gid = getDat("CON_GID", row);
		if (PscGid.isVld(con_gid)) {
			PscDto adr_dto = ssn.newDto("PSA_ADR");
			adr_dto.setQue("CON_GID", con_gid);
			adr_dto.setQue("TYP_IDN", "DELIVERY_ADDRESS");
			int adr_cnt = adr_dto.fetDat();
			if (adr_cnt==0) {
				adr_dto.setQue("TYP_IDN", "MAIN_ADDRESS");
				adr_cnt = adr_dto.fetDat();
			}
			if (adr_cnt!=0) {
				dlv_adr_dto = adr_dto;
				String dlv_flg = getDat("DLV_FLG", row);
				if (dlv_flg.isEmpty()) {
					setDat("DLV_FLG", row, "y");
				}
			}
		}
	}	if (chkIns(row) || chkMod("DLV_DAT", row)) {		String act_beg_dat = getDat("ACT_BEG_DAT", row);		String dlv_dat = getDat("DLV_DAT", row);		if (dlv_dat.length()!=0 && act_beg_dat.length()==0) {			act_beg_dat = PsaUti.timAdd(dlv_dat, Calendar.HOUR_OF_DAY, 2);			setDat("ACT_BEG_DAT", row, act_beg_dat);		}	}
	
	super.putRec(row);
	
	if (dlv_adr_dto!=null) {
		PscDto pro_adr_dto = ssn.newDto("PSA_PRO_ADR");
		String pro_gid = getDat("PSC_GID", row);
		pro_adr_dto.setQue("FAT_GID", pro_gid);
		pro_adr_dto.setQue("ADR_TYP_IDN", "DELIVERY_ADDRESS");
		if (pro_adr_dto.cntDat()==0) {
			dlv_adr_dto.insRow(2);
			dlv_adr_dto.copRow(2, dlv_adr_dto, 1);
			dlv_adr_dto.setDat("TYP_IDN", 2, "DELIVERY_ADDRESS");
			dlv_adr_dto.putDat();
	        String adr_gid = dlv_adr_dto.getDat("PSC_GID", 2);
	        pro_adr_dto.insRow(1);
	        pro_adr_dto.setDat("FAT_GID", 1, pro_gid);
	        pro_adr_dto.setDat("CHD_GID", 1, adr_gid);
	        pro_adr_dto.setDat("ADR_TYP_IDN", 1, "DELIVERY_ADDRESS");
	        pro_adr_dto.putDat();
		}
	}
}

}