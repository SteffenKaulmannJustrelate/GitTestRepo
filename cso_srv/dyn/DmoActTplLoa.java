// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211123046
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** DMO_ACT_TPL_LOA */
public class DmoActTplLoa {

	public static int INS_APM_TPL(PscFrm frm, String par, Integer row) throws Exception{
		return InsActTpl(frm, "DMO_APM_TPL_NAM", row);
	}

	public static int INS_TSK_TPL(PscFrm frm, String par, Integer row) throws Exception{
		return InsActTpl(frm, "DMO_TSK_TPL_NAM", row);
	}
	public static int INS_CAL_TPL(PscFrm frm, String par, Integer row) throws Exception{
		return InsActTpl(frm, "DMO_CAL_TPL_NAM", row);
	}


	public static int InsActTpl(PscFrm frm, String par, Integer irow) throws Exception{

		int row = irow.intValue();
		PscSsn ssn = frm.getSsn();
		PscDto apm_dto = frm.getDynDto();
		String blb_nam = null;
		//if(apm_dto.getDat("STY_TYP_IDN", row).equalsIgnoreCase("APPOINTMENT")){
			blb_nam = ssn.getEnv(par);
		//}

		if(blb_nam != null && BlbUtl.hasDat(ssn, blb_nam)){
			boolean cmp = BlbUtl.isBlbCmp(ssn, blb_nam);
			String ctt = new String(BlbUtl.getBlb(ssn, blb_nam, cmp) );
			PscFld ctt_fld = apm_dto.getFld("CTT");
			frm.setDat(ctt_fld, row, ctt);
			frm.setEdt(ctt_fld, row);
			return 0;
		}
		return -1;
	}

}