// Last update by user CUSTOMIZER on host webdmo at 20220919102409
import de.pisa.psc.srv.glb.*;
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psc.srv.dto.*;

/** CSO_JOB_APL_VOC */
public class CsoJobAplVoc {

	public static void run(PscSsn ssn, String par) throws Exception
	{
		JobLog log = JobThr.getJobLog(CsoJobAplVoc.class);
		PscDto dto = ssn.newDto("PSA_ACT_TYP");
		dto.setQue("PSC_OWN", "45");
		dto.setQue("NAM_FRA", "''");
		if (dto.cntDat()==0) {
			log.logNfo("nothing to translate");
		}
		else {
			log.logWrn("starting translation");
			PscDto voc_dto = ssn.newDto("PSA_VOC_DIC");
			voc_dto.setQue("IDN", "PSA_FRA");
			voc_dto.fetFst();
			voc_dto.calFnc("APL_VOC", "", 1);
			log.logWrn("translation finished");
		}
	}
	
}