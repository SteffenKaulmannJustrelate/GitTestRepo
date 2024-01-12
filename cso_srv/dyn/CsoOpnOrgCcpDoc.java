// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20160531113427
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;

/** CSO_OPN_ORG_CCP_DOC */
public class CsoOpnOrgCcpDoc {

	public static void OPN_CCP_ORG_DOC(PscFrm frm, String par, Integer row) throws Exception
	{
		PscSsn ssn = frm.getSsn();
		PscDto dyn_dto = frm.getDynDto();
		String org_gid = dyn_dto.getDat("PSA_CON.PSC_GID", row);
		if (!PscGid.isVld(org_gid)) {
			return;
		}
		PscDto con_doc_dto = ssn.newDto("PSA_CON_DOC_REF");
		con_doc_dto.setQue("FAT_GID", org_gid);
		con_doc_dto.setQue("PSA_DOC.STY_IDN", "CSO_ORG_CCP");
		if (!con_doc_dto.fetFst()) {
			new PscMsg("W", null, null, "Kein Unternehmenscockpit gefunden!").wri(ssn);
			return;
		}
		PsaDocFnc doc_fnc = PsaObjFac.get(ssn).newPsaDocFnc(con_doc_dto);
		doc_fnc.vieDoc(1, true);
	}
	
}