// Last update by user PSA_PRE_SAL on host NB-Rekowski-08 at 20110530151803
import java.util.*;

import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/**
 * @since 30.05.2011
 * @version
 * @author Rekowski
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/********************************************************************
* <DL>
* <DT>module</DT>       <DD>DMO_FIT_MAINT_ADM</DD>
* <DT>description</DT> <DD>PiSAsales helpful object: </DD>
* </DL>
*********************************************************************/
public class DmoFitMaintAdm 
{

	/////////////////////////////////////////////////////////////
	//variables
	/////////////////////////////////////////////////////////////
	private static boolean leere_alle_Zugriffe_an_Basis_Dtos = true;
	private static int SAFE_BREAK_CNT = 999;
	/////////////////////////////////////////////////////////////
	//constructor
	/////////////////////////////////////////////////////////////

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();

		if(leere_alle_Zugriffe_an_Basis_Dtos)
			cleBasDtoAcc(ssn, false);
	}

	public static void cleBasDtoAcc(PscSsn ssn, boolean sho_inf_no_mod) throws Exception{
		((PsaSsn) ssn).wriTxtImm("Start Zugriffsrechte leeren");
		PscDto all_bas = ssn.newDto("PSC_DTO");
		all_bas.setQue("NAM", "PSA% & !%PSI%");
		all_bas.setQue("TAB", "!''");
		all_bas.setQue("SUP", "''");
		
		HashSet<String> dto_ign_lis = new HashSet<String>();
		
		dto_ign_lis.add("PSA_LOG_MSG");
		dto_ign_lis.add("PSA_LIC_HIS");
		dto_ign_lis.add("PSA_CHG_OWN");
		dto_ign_lis.add("PSA_DRL_DWN_HIS");
		dto_ign_lis.add("PSA_DRL_DWN_HIS_SAV");
		dto_ign_lis.add("PSA_DUB_DIC");
		dto_ign_lis.add("PSA_DYN_JAV");	
		dto_ign_lis.add("PSA_JOB_LOG");
		dto_ign_lis.add("PSA_JOB_LOG_DAT");
		dto_ign_lis.add("PSA_JRN_DAT");
		dto_ign_lis.add("PSA_JRN_DTO");
		dto_ign_lis.add("PSA_JRN_FLD");
		dto_ign_lis.add("PSA_LIC_HIS");
		dto_ign_lis.add("PSA_LOA_OBJ");
		dto_ign_lis.add("PSA_LOG_MSG");	
		dto_ign_lis.add("PSA_MOD_DAT");
		dto_ign_lis.add("PSA_RPT_TIM");
		
		String dto_nam = null;
		int cnt_fnd_dto = 0;
		int cnt_mod_dto = 0;
		while(all_bas.fetNxt()){
			try{
				dto_nam = all_bas.getDat("NAM",1);
				if(dto_ign_lis.contains(dto_nam))
					continue;
				cnt_fnd_dto++;	
				int cnt_fnd_acc = 0;
				PscDto wrk_dto = ssn.newDto(dto_nam);
				PscFld acc_fld = wrk_dto.getFld("PSC_ACC");
				if(acc_fld != null){
					wrk_dto.setQue("PSC_ACC", "!D0 & !''");
					String psc_gid = null;
					
					while (wrk_dto.fetNxt()){
						
						psc_gid = wrk_dto.getDat("PSC_GID", 1);
						try{
							PsaUti.updFldDat(wrk_dto.getFld("PSC_ACC"), psc_gid, "");
							cnt_fnd_acc++;
						}catch (Exception cle_exc){
							((PsaSsn) ssn).wriTxtImm("Fehler am Dto '" + dto_nam 
									+ "', gid '" + psc_gid + "':" );
							ssn.wriExc( cle_exc);
						}
						if(cnt_fnd_acc > SAFE_BREAK_CNT){
							((PsaSsn) ssn).wriTxtImm("Mehr als '" + SAFE_BREAK_CNT 
									+"' Einträge am Dto '" + dto_nam 
									+ "' geleert: 'BREAK'!" );
							break;
						}
					}
					if(cnt_fnd_acc > 0 || sho_inf_no_mod){
						((PsaSsn) ssn).wriTxtImm(cnt_fnd_acc + " Einträge in 'PSC_ACC' ' am Dto" 
								+ " '" + dto_nam + " geleert.");
					}
					if(cnt_fnd_acc > 0) {
						cnt_mod_dto++;
					}
				}		
			}catch(Exception all_exc){
				((PsaSsn) ssn).wriTxtImm("Fehler am Dto '" + dto_nam + "':");
				ssn.wriExc( all_exc);
			}
		}
		((PsaSsn) ssn).wriTxtImm("Insgesamt " + cnt_mod_dto  + " Dto von " 
				+ cnt_fnd_dto + " bearbeitet.");
		
		ssn.wriTxt("Ende Zugriffsrechte leeren");
	}
}


/*
 * $History: $
 * 
 */
