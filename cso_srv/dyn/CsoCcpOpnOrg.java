// Last update by user CUSTOMIZER on host dmoref62 at 20140307134833
import de.pisa.psc.srv.svc.*;
// Last update by user CUSTOMIZER on host dmoref62 at 20140307134829
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psa.dto.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.frm.psa_ccp.*;
import de.pisa.psa.ifc.*;

/** CSO_CCP_OPN_ORG */
public class CsoCcpOpnOrg {

	public void run(PscGui gui) throws Exception
	{
		// Test
		PscSsn ssn = gui.getSsn();
		PscDto org_dto = ssn.newDto("PSA_ORG_EXT");
		org_dto.fetNxt();
		org_dto.fetCls();
		OPN(org_dto, "", 1);
	}

	public static void OPN(PscFrm frm, String par, Integer row) throws Exception	
	{
		PscDto dto = frm.getDynDto();
		OPN(dto, par, row);
	}

	public static void OPN(PscDto dto, String par, Integer row) throws Exception
	{
		if (dto==null) {
			return;
		}
		UsxPar usx_par = new UsxPar(par);
		String fld_dsc = usx_par.getPar("FLD", "PSC_GID");
		String psc_gid = dto.getDat(fld_dsc, row);
		if (!PscGid.isVld(psc_gid)) {
			return;
		}
		PscSsn ssn = dto.getSsn();
		PscDto org_dto = getOrgDto(ssn, psc_gid);
		if (org_dto==null || org_dto.numRec()==0) {
			return;
		}
		PscGui gui = ssn.getGui();
		opnCcp(gui, org_dto);
	}

	private static PscDto getOrgDto(PscSsn ssn, String psc_gid) throws Exception
	{
		if (!PscGid.isVld(psc_gid)) {
			return null;
		}
		PscDto org_dto = ssn.newDto("PSA_ORG_EXT");
		org_dto.setQue("PSC_GID", psc_gid);
		if (org_dto.fetDat()==0) {
			PscDto prs_dto = ssn.newDto("PSA_PRS_EXT");
			prs_dto.setQue("PSC_GID", psc_gid);
			if (prs_dto.fetDat()==0) {
				return null;
			}
			psc_gid = prs_dto.getDat("ORG_GID", 1);
			if (!PscGid.isVld(psc_gid)) {
				return null;
			}
			org_dto.setQue("PSC_GID", psc_gid);
			org_dto.fetDat();
		}
		return org_dto;
	}

	private static void opnCcp(PscGui gui, PscDto org_dto) throws Exception
	{
		PscSsn ssn = gui.getSsn();
		int tab = fndCtlPag(ssn);
		String org_gid = org_dto.getDat("PSC_GID", 1);
		String org_nam = org_dto.getDat("FRN_IDN", 1);
		PsaFrm ccp_frm = (PsaFrm)gui.newFrm("PSA_CCP_SSC");
		if (tab>0) {
			String tab_nam = "PSA_CCP_00"+tab+"_SSC";
			ccp_frm.setTagPrp("TAB_BTM", "PAR", tab_nam);
			ccp_frm.setTagPrp("SEL_NUM", "DFV", "SEL_NUM_00"+tab);
			PscFrm tab_frm = ccp_frm.creSubFrm(tab_nam);
			if (tab_frm!=null) {
				tab_frm.creSubFrm();
			}
		}
		UsxPar ini_par = new UsxPar("");
		ini_par.setPar("GID_TAG", new PsaCcpCtlOrgParOrg().getGidTag());
		ini_par.setPar("GID", org_gid);
		ini_par.setPar("NAM", org_nam);
		ccp_frm.setValBuf("PSA_CCP_CTL_ORG_SCI_INI", ini_par.getOriPar());
	}

	private static int fndCtlPag(PscSsn ssn) throws Exception
	{
		PscDto cnf_dto = ssn.newDto("PSA_CCP_CNF");
		cnf_dto.setQue("PSC_OWN", Integer.toString(ssn.getUic()));
		cnf_dto.setQue("DLG", "PSA_CCP_CTL_ORG_SCI");
		if (cnf_dto.fetDat()==0) {
			return 0;
		}
		int tab = PscUti.str2int(cnf_dto.getDat("TAB", 1), 0);
		if (tab==0) {
			return 0;
		}
		return ((tab-1)/9)+1;
	}

}