// Last Update by user CUSTOMIZER at 20171211124714
import de.pisa.psa.frm.psa_doc.*;
import de.pisa.psc.srv.dto.*;

/**
 * CSO_PRO_DOC_REF_SCI dialog implementation
 */
public class IplDlgCsoProDocRefSci extends PsaProDocRefApp {

	/**
	 * common constructor
	 * @param dsc dialog descriptor
	 * @throws Exception
	 */
	public IplDlgCsoProDocRefSci(String dsc) {
		super(dsc);
	}

	/**
	 * "release for web" user exit
	 * @param par user exit parameter
	 * @param row current row
	 * @throws Exception
	 */
	public void WEB_RLS_DOC(String par, Integer row) throws Exception {
		int rows[]	= getLisRow();
		if ( (rows.length < 1) || rows[0] != row ) {
			return;
		}
		PscDto	dyn_dto	= getDynDto();
		boolean	edt		= dyn_dto.chkMod();
		PscFld	fld		= dyn_dto.getFld("WEB_PUB");
		for ( int cur_row : rows ) {
			setDat(fld, cur_row, "y");
		}
		if ( !edt ) {
			getTop().putDat();
		}
	}

}