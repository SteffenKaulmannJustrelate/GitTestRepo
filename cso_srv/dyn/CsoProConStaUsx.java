// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20180601141503
import de.pisa.psa.dto.psa_sap.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

/** CSO_PRO_CON_STA_USX */
public class CsoProConStaUsx {

	public void setLeaCls(PscDto dto, Integer row, String sta, String par) throws Exception
	{
		PscSsn ssn = dto.getSsn();
		if (!PsaOpr.chkOprExs(ssn, "PSA_PRS_LEA_CLS")) {
			return;
		}
		String con_gid = dto.getDat("CHD_GID", row);
		if (!PscGid.isVld(con_gid)) {
			return;
		}
		PscDto prs_dto = ssn.newDto("PSA_PRS_EXT");
		prs_dto.setQue("PSC_GID", con_gid);
		if (!prs_dto.fetDatFor('W')) {
			return;
		}
		String sap_idn = prs_dto.getDat("SAP_IDN", 1);
		if (!sap_idn.equals("PSA_PRS_LEA")) {
			return;
		}
		prs_dto.setDat("OPR_IDN", 1, "PSA_PRS_LEA_CLS");
		prs_dto.putDat();
	}
	
}