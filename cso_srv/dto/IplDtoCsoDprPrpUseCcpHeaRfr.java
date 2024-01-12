// Last Update by user CUSTOMIZER at 20180601135557
import de.pisa.psa.dto.*;
import de.pisa.psa.scn.ccp.*;
import de.pisa.psc.srv.glb.*;

/** CSO_DPR_PRP_USE_CCP_HEA_RFR */
public class IplDtoCsoDprPrpUseCcpHeaRfr extends de.pisa.psa.dto.PsaDto
{

public IplDtoCsoDprPrpUseCcpHeaRfr(String dsc) throws Exception { super(dsc); }

@Override
protected void putRec(int row) throws Exception {
	super.putRec(row);
	
}

@Override
protected void pstPutDat(boolean suc) throws Exception {
	super.pstPutDat(suc);
	
	if (suc) {
		PscSsn ssn = getSsn();
		PsaFrlUti frl = PsaFrlUti.get(this);
		if (frl.hasSupDto("PSA_PRS_EXT")) {
			boolean mod = new PsaCcpHeaJob(ssn, "").run(frl.Drv, frl.Row);
			if (mod && !frl.Dto.chkMod()) {
				frl.Drv.fetDat(frl.Row);
			}
		}
	}
}

}