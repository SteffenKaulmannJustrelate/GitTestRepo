// Last update by user PSA_PRE_SAL on host PC-WEILAND-12 at 20121214111310
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psa.ssn.*;

/** TST_CRE_TSK_MON */
public class TstCreTskMon {

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		int mon = cal.get(Calendar.MONTH);
		String usr_gid = PsaPrsInt.getGid(ssn);
		PscDto tsk_dto = ssn.newDto("PSA_TSK");
		tsk_dto.setQue("AGN_PRS_GID", usr_gid);
		
		for (int day=1; day<=31; day++) {
			StringBuilder str = new StringBuilder();
			str.append(cal.get(Calendar.DAY_OF_MONTH));
			str.append('.');
			str.append(mon+1);
			str.append('.');
			str.append(cal.get(Calendar.YEAR));
			String nam_opn = str + " offen";
			String nam_don = str + " abgeschlossen";
			cal.set(Calendar.HOUR_OF_DAY, 8);
			String beg_dat = PscUti.getTim(cal.getTime());
			cal.set(Calendar.HOUR_OF_DAY, 18);
			String end_dat = PscUti.getTim(cal.getTime());

			tsk_dto.setQue("NAM", "'"+nam_opn+"'");
			tsk_dto.setQue("STA_VAL", "OPN");
			if (tsk_dto.fetDat()==0) {
				tsk_dto.insRow(1);
				tsk_dto.setDat("NAM", 1, nam_opn);
				tsk_dto.setDat("BEG_DAT", 1, beg_dat);
				tsk_dto.setDat("END_DAT", 1, end_dat);
				tsk_dto.putDat();
				PsaSsn.wriTxtImm(ssn, nam_opn);
			}

			tsk_dto.setQue("NAM", "'"+nam_don+"'");
			tsk_dto.setQue("STA_VAL", "DON");
			if (tsk_dto.fetDat()==0) {
				tsk_dto.insRow(1);
				tsk_dto.setDat("NAM", 1, nam_don);
				tsk_dto.setDat("STA_IDN", 1, "PSA_STA_DON");
				tsk_dto.setDat("BEG_DAT", 1, beg_dat);
				tsk_dto.setDat("END_DAT", 1, end_dat);
				tsk_dto.putDat();
				PsaSsn.wriTxtImm(ssn, nam_don);
			}

			cal.add(Calendar.DAY_OF_MONTH, 1);
			if (mon!=cal.get(Calendar.MONTH)) {
				break;
			}
		}
		
	}
	
}