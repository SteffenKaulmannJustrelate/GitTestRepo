// Last update by user CUSTOMIZER on host dmoref at 20180523122540
import java.lang.reflect.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** CSO_PSI_CHG_ADR_TAB */
public class CsoPsiChgAdrTab {

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		chgTab(ssn);
	}
	
	public static int run(PscSsn ssn) throws Exception {
		new CsoPsiChgAdrTab().chgTab(ssn);
		return -1;
	}
	
	private PscSsn Ssn;
	private PscFrm Frm;
	
	public void chgTab(PscSsn ssn) throws Exception {
		Ssn = ssn;
		UsxPar env = new UsxPar(ssn.getEnv("CSO_PRO_CHG_ADR_TAB"));
		String con = env.getPar("CON");
		String pro = env.getPar("PRO");
		String svc_pro = env.getPar("SVC_PRO");
		if (!con.isEmpty()) {
			ssn.wriTxt("CON = "+con);
			RefUti.invMthDynJav(ssn, "PSI_ADM_BOX", "CHG_CON_ADR_RGT", getFrm(), "PAR:="+con);
		}
		if (!pro.isEmpty()) {
			ssn.wriTxt("PRO = "+pro);
			RefUti.invMthDynJav(ssn, "PSI_ADM_BOX", "CHG_PRO_ADR_RGT", getFrm(), "PAR:="+pro);
		}
		if (!svc_pro.isEmpty()) {
			ssn.wriTxt("PRO_SVC = "+svc_pro);
			RefUti.invMthDynJav(ssn, "PSI_ADM_BOX", "CHG_SVC_PRO_ADR_RGT", getFrm(), "PAR:="+svc_pro);
		}
		PscChe.delFrm();
	}
	
	private PscFrm getFrm() throws Exception {
		if (Frm==null) {
			PscGui gui = Ssn.getGui();
			if (gui==null) {
				gui = new PscGui();
				Field ssn_fld = PscGui.class.getDeclaredField("Ssn");
				ssn_fld.setAccessible(true);
				ssn_fld.set(gui, Ssn);
			}
			Frm = new PscFrm("DUMMY");
			Field gui_fld = PscDlg.class.getDeclaredField("Gui");
			gui_fld.setAccessible(true);
			gui_fld.set(Frm, gui);
		}
		return Frm;
	}
	
}