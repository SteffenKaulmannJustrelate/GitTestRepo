// Last Update by user CUSTOMIZER at 20171211123645
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.svc.*;

/** CSO_PRO_CON_EXT_RAT */
public class IplDtoCsoProConExtRat extends de.pisa.psa.dto.PsaDto
{

private int Rat_Idx = 0;
private int Rat[] = null;
private final int Rat_Cnt = 7;

public IplDtoCsoProConExtRat(String dsc) throws Exception{ super(dsc); }

@Override
protected void fetRec() throws Exception {
	clrDat();
	
	getRat();
	if (Rat!=null) {
		if (chkSng()) {
			if (Rat_Idx<Rat_Cnt) {
				insRow(1);
				if (Rat_Idx==0 && Rat[0]==0) {
					Rat_Idx++;
				}
				if (Rat_Idx>0) {
					modDat("RAT_NAM", 1, Integer.toString(Rat_Idx));
				}
				else {
					modDat("RAT_NAM", 1, "?");
				}
				modDat("RAT_CNT", 1, Integer.toString(Rat[Rat_Idx]));
				Rat_Idx++;
			}
		}
		else {
			int cnt = (Rat[0]==0) ? Rat_Cnt-1 : Rat_Cnt;
			int idx = (Rat[0]==0) ? 1 : 0;
			insRow(1, cnt);
			for (int row=1; idx<Rat_Cnt; idx++, row++) {
				if (idx>0) {
					modDat("RAT_NAM", row, Integer.toString(idx));
				}
				else {
					modDat("RAT_NAM", row, "?");
				}
				modDat("RAT_CNT", row, Integer.toString(Rat[idx]));
			}
			Rat = null;
		}
	}
	clnDat();
}

private void getRat() throws Exception {
	if (Rat!=null) {
		return;
	}
	String fat_gid = getQue("FAT_GID");
	fat_gid = PsaUti.rplStr(fat_gid, "%", "").trim();
	String fld_dsc = getQue("FLD_DSC");
	fld_dsc = PsaUti.rplStr(fld_dsc, "%", "").trim();
	if (!fat_gid.isEmpty() && !fld_dsc.isEmpty()) {
		PscSsn ssn = getSsn();
		PscDto pro_con_dto = PsaUti.newDto(ssn, "PSA_PRO_CON_EXT_REF", false, false, true, false);
		PscFld fld = pro_con_dto.getFld(fld_dsc);
		Rat = new int[Rat_Cnt];
		pro_con_dto.setQue("FAT_GID", fat_gid);
		try {
			while (pro_con_dto.fetNxt()) {
				String val_str = pro_con_dto.getDat(fld, 1);
				int val = PscUti.str2int(val_str, 0);
				if (val>=1 && val<=6) {
					Rat[val]++;
				}
				else {
					Rat[0]++;
				}
			}
		}
		finally {
			pro_con_dto.fetCls();
		}
	}
}

}