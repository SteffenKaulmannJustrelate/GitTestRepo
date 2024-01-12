// Last Update by user CUSTOMIZER at 20200604153439
import de.pisa.psa.ifc.PscGid;
import de.pisa.psc.srv.dto.PscDto;

/** PRT_MDI_THK_ADD_EAL 
 * function to add Document to media library
 * */
public class IplDlgPrtMdiThkAddEal extends de.pisa.psa.frm.PsaFrm{

	public IplDlgPrtMdiThkAddEal(String dsc) { super(dsc); }
	
	/**
	 * add selected docs to media library
	 * */	
	public void ADD_DOC(String par, Integer row) throws Exception {
		PscDto dyn_dto = getDynDto();
		String doc_gid = dyn_dto.getDat("PSC_GID", row);
		if(!PscGid.isVld(doc_gid)) {
			return;
		}
		PscDto mdi_thk_dto = getSsn().newDto("PRT_MDI_THK_DOC");
		mdi_thk_dto.setQue("DOC_GID", doc_gid);
		if(!mdi_thk_dto.fetFst()) {
			mdi_thk_dto.insRow(1);
			mdi_thk_dto.setDat("DOC_GID", 1, doc_gid);
			mdi_thk_dto.putDat();
		}
	}

}