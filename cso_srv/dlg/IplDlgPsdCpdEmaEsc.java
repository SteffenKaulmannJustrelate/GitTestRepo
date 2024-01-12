// Last Update by user CUSTOMIZER at 20091209140522
import de.pisa.psc.srv.dto.*;
// Last Update by user CUSTOMIZER at 20091209140505

import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.dto.psa_cpd.*;
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.frm.psa_act.*;

public class IplDlgPsdCpdEmaEsc extends de.pisa.psa.frm.psa_cpd.PsaCpdApp
{

public IplDlgPsdCpdEmaEsc(String dsc) throws Exception { super(dsc); }

@Override
public void EMA_SND(String par, Integer row) throws Exception{	PscGui gui = getGui();	PscSsn ssn = getSsn();
	PscDto dyn_dto = getDynDto();	PsaCpd cpdDto = chkDsp("EML", row);	cpdDto.chkWriAcc(row, true, true, false, true);
	String snd_msg = gui.getMsg("PSA_SND_EML");
	String box_ret = cpdDto.wriBox(snd_msg, "Q", "Q", "Y");
	if (!box_ret.equals("Y")) {
		return;
	}
	
	// check attachments
	PsaCpdFnc cpd_fnc = new PsaCpdFnc(dyn_dto);
	if (!cpd_fnc.chkNonRlsDoc(row)) {
		return;
	}

	Sta sta = CpdSta.getSta(cpdDto, row);	if (sta.modSta(Sta.OPR.SND)) {		ssn.wriMsg("PSA_CPD_SND");
        PsaActApp.setProConSta(this, new int[]{row});
	}
	else {
		if (sta.isSta(CpdSta.STA_EDT)) {
			String msg1 = gui.getMsg("PSA_SND_NOT_ALW");
			String msg2 = gui.getMsg("PSA_CPD_IN_EDT");
			cpdDto.wriBox(msg1+" "+msg2, "W", "W", "O");
		}
		else {
			ssn.wriMsg("PSA_SND_NOT_ALW");
		}
	}}

}