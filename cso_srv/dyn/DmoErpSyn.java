// Last update by user PSA_PRE_SAL on host NB-Rekowski-08 at 20100528182647
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.glb.*;
import java.util.*;
import java.io.*;
import org.apache.log4j.*;

/* DMO_ERP_SYN */
public class DmoErpSyn {
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

	}
	public static void ERP_QUO_EXP(PscFrm frm, String par, Integer row) throws Exception {
		notIplMsg(frm);
	}
	
	public static void ERP_ORD_EXP(PscFrm frm, String par, Integer row) throws Exception {
		notIplMsg(frm);
	}
	public static void ERP_ORG_EXP(PscFrm frm, String par, Integer row) throws Exception {
		notIplMsg(frm);
	}
	public static void notIplMsg(PscFrm frm) throws Exception {
		frm.getSsn().wriTxt("No implementation exist");
	}
}