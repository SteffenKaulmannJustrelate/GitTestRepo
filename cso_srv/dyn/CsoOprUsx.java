// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20160525155101
import de.pisa.psc.srv.dto.*;
import de.pisa.psa.dto.psa_oth.*;

/** CSO_OPR_USX */
public class CsoOprUsx {

	public int setLstReaAndComWon(PscDto dto, Integer row, String old_sta, String par) throws Exception 
	{
		// lost reason
		PsaSta.calUsx(dto, row, old_sta, "$PSA_PRO_QUO_LST", "opnLstReaFrm", "");
		// set competitor won
		PsaSta.calUsx(dto, row, old_sta, "de.pisa.psa.frm.psa_pro.PsaProApp", "setComWon", "");
		return 0;
	}

}