// Last update by user SALESADMIN on host PC-WEILAND-12 at 20171121154211
import de.pisa.psa.dto.psa_svc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.svc.*;

/** CSO_ACT_CRE_SVC */
public class CsoActCreSvc extends PsaActCreSvc
{

	public CsoActCreSvc(PscDto act_dto) {
		super(act_dto);
	}
	
	@Override
	public void creSvc(int act_row, PscDto svc_dto, int svc_row) throws Exception {
		super.creSvc(act_row, svc_dto, svc_row);
		
		// set current date as creation date
		svc_dto.setDat("SRC_DAT", svc_row, PscUti.getTim());
	}

	
}