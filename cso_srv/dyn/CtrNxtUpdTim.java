// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211123115
import java.util.*;

import de.pisa.psa.scn.*;
import de.pisa.psc.srv.gui.*;

/** CTR_NXT_UPD_TIM */
public class CtrNxtUpdTim {
	
	public void run(PscGui gui) throws Exception {
		Date nxt_run = PsaScnSvcImp.getNxtOpnTimRun();
		if (nxt_run!=null) {
			String box_ret = gui.wriBox("Nächster Durchlauf: "+nxt_run+"\nAuf \"null\" setzen?", "Q", "Q");
			if (box_ret.equals("Y")) {
				PsaScnSvcImp.setNxtOpnTimRun(null);
			}
		}
		else {
			gui.wriBox("Nächster Durchlauf: "+nxt_run, "W", "W");
		}
	}
	
}