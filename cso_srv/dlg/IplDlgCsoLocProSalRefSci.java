// Last Update by user CUSTOMIZER at 20171211124451
import de.pisa.psa.frm.*;
import de.pisa.psa.frm.psa_ifc.*;
import de.pisa.psa.frm.psa_pro.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;

/** CSO_LOC_PRO_SAL_REF_SCI */
public class IplDlgCsoLocProSalRefSci extends de.pisa.psa.frm.PsaFrm
{

	public IplDlgCsoLocProSalRefSci(String dsc) { super(dsc); }

	@Override
	public void mchFrmXbm(PscFrm new_frm) throws Exception {
		super.mchFrmXbm(new_frm);

		PscDto dyn_dto = getDynDto();
		int fat_row = dyn_dto.getFrw();
		PscFrm fat_frm = getFat();
		PscDto fat_dto = (fat_frm==null) ? null : fat_frm.getDynDto();
		if (fat_dto!=null && fat_row>0 && new_frm instanceof PsaProApp) {
			String cst_gid = fat_dto.getDat("CST_GID", fat_row);
			PsaPreFet pre_fet = new ProPreFet(cst_gid);
			pre_fet.fet((PsaFrm)new_frm);
			((PsaProApp)new_frm).setPreFet(pre_fet);
		}
	}

	private static class ProPreFet implements PsaPreFet {

		private final String Cst_Gid;

		ProPreFet(String cst_gid) {
			Cst_Gid = PscGid.isVld(cst_gid) ? cst_gid : "";
		}

		@Override
		public void fet(PsaFrm frm) throws Exception {
			PscDto dto = frm.getDynDto();
			dto.setQue("CON_GID", Cst_Gid);
			dto.fetDat();
			dto.setQue("CON_GID", "");
		}

	}

}