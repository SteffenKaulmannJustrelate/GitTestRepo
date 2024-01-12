// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20140414130136
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;

/** CSO_CTR_RUN_JOB */
public class CsoCtrRunJob {

	public void run(PscGui gui) throws Exception {
		RUN(gui, "");
	}
	
	public static void RUN(PscGui gui, String par) throws Exception
	{
		PscSsn ssn = gui.getSsn();
		runJob(ssn, "PSA_CRE_CTR_SVC_ORD");
		runJob(ssn, "PSA_CRE_CTR_APM");
		ssn.wriMsg("PSA_DON");
	}

	private static void runJob(PscSsn ssn, String job_idn) throws Exception
	{
		PscDto job_dto = ssn.newDto("PSA_JOB");
		job_dto.setQue("IDN", "'"+job_idn+"'");
		if (job_dto.fetDat()==0) {
			PsaSsn.wriTxtImm(ssn, "Job \""+job_idn+"\" nicht gefunden!");
			return;
		}
		String nam = job_dto.getDat("NAM", 1);
		PsaSsn.wriTxtImm(ssn, "Starte \""+nam+"\"");
		String usx_cla = job_dto.getDat("USX_CLA", 1);
		String usx_fnc = job_dto.getDat("USX_FNC", 1);
		String usx_par = job_dto.getDat("USX_PAR", 1);
		String usr = job_dto.getDat("USR", 1);
		Class<?> clz = PsaUti.getCls(ssn, usx_cla);
		PscSsn job_ssn = NewSsn.newSsn(ssn, usr);
		try {
			PscChe.delSsn(job_ssn);
			RefUti.invMth(clz, usx_fnc, job_ssn, usx_par);
		}
		finally {
			job_ssn.exiSsn();
		}
	}

}