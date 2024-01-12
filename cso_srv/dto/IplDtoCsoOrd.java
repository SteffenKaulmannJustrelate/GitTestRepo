// Last Update by user PSA_PRE_SAL at 20110414122227
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.ifc.*;

/** CSO_ORD */
public class IplDtoCsoOrd extends de.pisa.psa.dto.psa_pro.PsaProApp
{

public IplDtoCsoOrd(String dsc) throws Exception{ super(dsc); }

@Override
protected void putRec(int row) throws Exception {
	PscSsn ssn = getSsn();
	
	super.putRec(row);

	if (!PsaDtoIpl.chkOut(this, row) &&	
		!isTypCom() && 
		 PsaUti.getEnv(ssn, "CSO_ORD_CST_MOD_SAP", false) && 
		 chkMod("CON_GID", row))
	{
		String con_gid = getDat("CON_GID", row);
		if (PscGid.isVld(con_gid)) {
			PscDto con_dto = PsaUti.newDto(ssn, "PSA_CON", false, false, false, true);
			con_dto.setQue("PSC_GID", con_gid);
			if (con_dto.fetDat()!=0 && con_dto.chkAcc(1, 'R')) {
				String opr_idn = con_dto.getDat("OPR_IDN", 1);
				String cla_typ = con_dto.getDat("CLA_TYP", 1);
				if (cla_typ.equals("ORG")) {
					setOrgCst(con_dto, 1);
				}
				else if (opr_idn.startsWith("PSA_PRS_LEA_") ||
						 opr_idn.startsWith("PSA_PRS_PRP_"))
				{
					setPrsCst(con_dto, 1);
				}
			}
		}
	}
}

private void setOrgCst(PscDto con_dto, int con_row) throws Exception
{
	if (!con_dto.chkAcc(con_row, 'W')) {
		return;
	}
	PscSsn ssn = getSsn();
	String opr_idn = con_dto.getDat("OPR_IDN", con_row);
	if (opr_idn.startsWith("PSA_ORG_LEA_") || 
		opr_idn.startsWith("PSA_ORG_PRP_")) 
	{
		con_dto.setDat("OPR_IDN", con_row, "PSA_ORG_CST_WRK");
		con_dto.putDat();
		ssn.wriMsg("CSO_CST_CHG_CST");
	}
	String con_gid = con_dto.getDat("PSC_GID", con_row);
	if (!PscGid.isVld(con_gid)) {
		return;
	}
	PscDto prs_dto = ssn.newDto("PSA_PRS");
	prs_dto.setQue("ORG_GID", con_gid);
	prs_dto.setQue("OPR_IDN", "PSA_PRS_LEA_% | PSA_PRS_PRP_%");
	int prs_cnt = prs_dto.fetDat();
	if (prs_cnt>0) {
		int cnt = 0;
		for (int prs_row=1; prs_row<=prs_cnt; prs_row++) {
			if (prs_dto.chkAcc(prs_row, 'W')) {
				prs_dto.setDat("OPR_IDN", prs_row, "PSA_PRS_CST_WRK");
				cnt++;
			}
		}
		if (cnt>0) {
			prs_dto.putDat();
			ssn.wriMsg("CSO_PRS_CHG_CST", Integer.toString(prs_cnt));
		}
	}
}

private void setPrsCst(PscDto con_dto, int con_row) throws Exception
{
	if (!con_dto.chkAcc(con_row, 'W')) {
		return;
	}
	PscSsn ssn = getSsn();
	con_dto.setDat("OPR_IDN", con_row, "PSA_PRS_CST_WRK");
	con_dto.putDat();
	ssn.wriMsg("CSO_CST_CHG_CST");
}

}