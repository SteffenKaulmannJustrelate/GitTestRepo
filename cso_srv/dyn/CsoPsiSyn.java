// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211123303
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/**
 * synchronizes organizations and transactions (quotes and orders) to PSIPENTA<br>
 * CSO_PSI_SYN
 * @since 04.08.2009
 * @author bastek
 */
public class CsoPsiSyn {

	/**
	 * starts the standard procedure
	 * @param gui GUI object
	 * @throws Exception
	 */
	public void run(PscGui gui) throws Exception {
	}

	/**
	 * synchronizes all supported objects to PiSA sales
	 * @param ssn PiSA sales GUI object
	 * @throws Exception
	 */
	private static void PSI_IMP_ALL_JOB(PscSsn ssn) throws Exception {
		ssn.wriMsg("CSO_PSI_CHK_CON");
		PscCon con = ssn.getCon();
		if ( con!=null ) {
			con.sndAct();
		}
		try {
			PsaJob dto = ((PsaJob)ssn.newDto("PSA_JOB", false, false, true, false));
			dto.setQue("IDN", "CSO_PSI_IMP_DMO_ALL");
			int num = dto.fetDat();
			if ( num==1 ) {
				dto.PSA_JOB_BEG("ONCE", 1);
				ssn.wriMsg("CSO_PSI_SYN_RUN");
			} else {
				ssn.wriMsg("CSO_PSI_SYN_ERR", "Missing Job: CSO_PSI_IMP_STD_ALL");
			}
		} catch(Exception exc) {
			ssn.wriMsg("CSO_PSI_SYN_ERR", PsaUti.getStkTrc(exc));
		}
	}
	
	/**
	 * synchronizes all supported objects to PiSA sales
	 * @param gui PiSA sales GUI object
	 * @param par parameter
	 * @throws Exception
	 */
	public static void PSI_IMP_ALL_JOB (PscGui gui, String par) throws Exception {
		PscSsn ssn = gui.getSsn();
		PSI_IMP_ALL_JOB(ssn);
	}

	/**
	 * synchronizes all supported objects to PiSA sales
	 * @param frm PiSA sales form object
	 * @param par parameter
	 * @param row row position
	 * @throws Exception
	 */
	public static void PSI_IMP_ALL_JOB (PscFrm frm, String par, Integer row) throws Exception {
		PscSsn ssn = frm.getSsn();
		PSI_IMP_ALL_JOB(ssn);
	}

}