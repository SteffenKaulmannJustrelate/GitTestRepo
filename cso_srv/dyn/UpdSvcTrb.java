// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211122546
import java.util.*;

import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/** UPD_SVC_TRB */
public class UpdSvcTrb {

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

		/* verschiebt Datum "Erstellt am" und "Fällig am" von offenen
		 * Servicemeldungen um definierte Anzahl von Tagen:
		 */
		PsaFrm frm = (PsaFrm)gui.newFrm("CST_SVC_DLI_MOV_SSF");
		gui.dlgFrm(frm);

		long day_add 	= 0;
		if (frm.chkExiFlg()) {
			String day_add_str = frm.getEdtDat("CNT_DAY");
			day_add = Long.parseLong(day_add_str);
		}
		run(ssn, day_add);
	}

	public static void run(PscSsn ssn, Long day_add) throws Exception{

		if(day_add > 0 || day_add < 0){

			long dat_dif_l	= 1000l * 3600l * 24l * day_add ; 

			PscDto svc_trb = ssn.newDto("PSA_SVC_TRB");
			svc_trb.setQue("PSA_OPR_SVC_TRB_CPL.OPN", "OPN");
			int cnt = svc_trb.fetDat();
			ssn.wriTxt("Datum für '" + cnt + "' Servicemeldungen werden verschoben:" );

			for(int idx = 1; idx <= cnt; idx++){
				try{		
					String src_dat = svc_trb.getDat("SRC_DAT",idx);   	// ertellt am
					String sap_dat = svc_trb.getDat("SAP_DAT",idx);		// Fällig am 

					Date sap_d 		= PscUti.getTim(sap_dat);
					long sap_l = (sap_d==null) ? 0 : sap_d.getTime();

					Date src_d = PscUti.getTim(src_dat);
					long src_l = (src_d==null) ? 0 : src_d.getTime(); 

					src_l = src_l + dat_dif_l;
					sap_l = sap_l + dat_dif_l;

					if (src_d!=null) {
						src_d.setTime(src_l);
					}
					if (sap_d!=null) {
						sap_d.setTime(sap_l);
					}

					String mod_src_dat = PscUti.getTim(src_d);
					String mod_sap_dat = PscUti.getTim(sap_d);

					String gid = svc_trb.getDat("PSC_GID",idx);
					PsaUti.updFldDat(svc_trb, "SRC_DAT", gid, mod_src_dat );
					PsaUti.updFldDat(svc_trb, "SAP_DAT", gid, mod_sap_dat );

					ssn.wriTxt("geändert: " + svc_trb.getDat("NAM",idx));
				}catch(Exception ae){
					try{
						ssn.wriTxt(ae.getMessage());

					}catch(Exception ie){}
				}	
			}
		}
	}

}