// Last update by user CUSTOMIZER on host WSAMZN-GCNRJ0KM at 20220324110037
import de.pisa.psa.ssn.*;
// Last update by user CUSTOMIZER on host WSAMZN-GCNRJ0KM at 20220324110034
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psa.ifc.*;
import java.util.*;
import java.io.*;
import org.apache.log4j.*;

/** CSO_CRE_PSC_DTO */
public class CsoCrePscDto {

	public void run(PscGui gui) throws Exception {
		PsaSsn ssn = (PsaSsn)gui.getSsn();
      new PsaSysUpg(ssn).crePscDto();
	}
	
}