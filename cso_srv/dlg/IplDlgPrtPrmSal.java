// Last Update by user CUSTOMIZER at 20200604153439
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.glb.PscSsn;

/** PRT_PRM_SAL 
 * creating missing entries on first fet
 * (missing entries are also created on server rest request; this is just for convenience)
 * */
public class IplDlgPrtPrmSal extends de.pisa.psa.frm.PsaFrm{
	boolean fst_fet = true;

	public IplDlgPrtPrmSal(String dsc) { 
		super(dsc); 
		
	}
	
	@Override
	public int fetDat() throws Exception {
		if(fst_fet) {
			fst_fet = false;
			PscSsn ssn = getSsn();
			PscDto prs_ext_dto = ssn.newDto("PSA_PRS_EXT");
			PscDto prm_dto = ssn.newDto("PRT_USR_PRM");
			prs_ext_dto.setQue("CIC", "!'' & !'2'");
			int num_prs = prs_ext_dto.cntDat();
			int num_ent = prm_dto.cntDat();
			if(num_ent < num_prs) {
				try {
					while(prs_ext_dto.fetNxt()) {
						String uic = prs_ext_dto.getDat("CIC", 1);
						prm_dto.setQue("UIC", "'" + uic + "'");
						if(!prm_dto.fetFst()) {
							prm_dto.insRow(1);
							prm_dto.setDat("UIC", 1, uic);
							prm_dto.putDat();
						}
					}					
				}finally {
					prs_ext_dto.fetCls();
				}
			}
		}
		return super.fetDat();
	}

}