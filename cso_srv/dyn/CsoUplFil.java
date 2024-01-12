// Last update by user CUSTOMIZER on host pc-weiland-07 at 20110812141108
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.ifc.*;

/** CSO_UPL_FIL */
public class CsoUplFil {

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

		String srv_fil = PsaUti.inpTxt(gui, "Serverfile", null, null, 0);
		if (!PscUti.isStr(srv_fil)) {
			return;
		}
		String cli_fil = gui.selFil("Clientfile", null, null, "*.*");
		if (!PscUti.isStr(cli_fil)) {
			return;
		}
		
		byte dat[] = gui.getFil(cli_fil, "rb");
		if (dat==null || dat.length==0) {
			return;
		}
		
		PscUti.wriFil(srv_fil, dat);
		ssn.wriTxt(dat.length+" bytes nach \""+srv_fil+"\" geschrieben");
	}
	
}