// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211123132
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** CSO_QUO_SUP_CMP */
public class CsoQuoSupCmp {

	public static void RUN(PscFrm frm, String par, Integer row) throws Exception
	{
		int rows[] = frm.getLisRow();
		if (rows==null || rows.length==0 || rows[0]!=row) {
			return;
		}
		PscSsn ssn = frm.getSsn();
		PscDto doc_dto = ssn.newDto("PSA_DOC");
		doc_dto.setQue("NUM", "'D-16-005100'");
		doc_dto.fetDat();
		doc_dto.calFnc("DOC_VIE", "", 1);
	}
	
}