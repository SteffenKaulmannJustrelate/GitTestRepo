// Last update by user SALESADMIN on host PC-WEILAND-12 at 20171121154341
import de.pisa.psa.dto.psa_svc.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

/** CSO_OBJ_FAC */
public class CsoObjFac extends PsaObjFac
{

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.ssn.PsaObjFac#newPsaActCreSvc(de.pisa.psc.srv.dto.PscDto)
	 */
	@Override
	public PsaActCreSvc newPsaActCreSvc(PscDto act_dto) {
		PscSsn ssn = act_dto.getSsn();
		try {
			return RefUti.invConDynJav(ssn, "CSO_ACT_CRE_SVC", act_dto);
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
}