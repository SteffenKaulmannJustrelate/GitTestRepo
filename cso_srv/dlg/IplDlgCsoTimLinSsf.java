// Last Update by user PSA_PRE_SAL at 20120105165222

import java.util.*;

import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.svc.PscUti;

/** CSO_TIM_LIN_SSF */
public class IplDlgCsoTimLinSsf extends de.pisa.psa.frm.PsaFrm
{

   public IplDlgCsoTimLinSsf(String dsc) throws Exception{ 
	   super(dsc); 
	}

/**
 * @see de.pisa.psa.frm.PsaFrmIpl#setDat(de.pisa.psc.srv.dto.PscFld, int, java.lang.String)
 */
@Override
public void setDat(PscFld fld, int row, String str) throws Exception {
	PscSsn ssn = this.getSsn();
	PscDto dyn_dto = this.getDynDto();

	String fld_dsc = fld.getDsc();
	if(fld_dsc != null && row == 1 && str != null){
		Date old_dat = PscUti.getTim( dyn_dto.getDat("CSO_TIM_STP_OLD",row));
		long old_tim = old_dat.getTime();
		if(fld_dsc.equals("CSO_TIM_STP_NEW")){
			Date tar_dat = PscUti.getTim(str);
			long tar_tim  = tar_dat.getTime();
			long new_dif = (tar_tim - old_tim)/ 1000 / 60 / 60 /24;
			dyn_dto.setDat("CSO_TIM_DIF_DAY", row, String.valueOf(new_dif));
		}else if(fld_dsc.equals("CSO_TIM_DIF_DAY")){
			long new_day_dif = PsaUti.str2long(str,0);
			long new_tim =  old_tim + new_day_dif*24*60*60*1000;
		
			Date new_date = new Date();
		    new_date.setTime(new_tim);
		    String new_psa_dat = PscUti.getTim(new_date);
		    dyn_dto.setDat("CSO_TIM_STP_NEW", row, new_psa_dat);
		}
	}else{
		   ssn.wriTxt("Bitte erst Daten laden");
	}
	super.setDat(fld, row, str);
}
	
//	public void CSO_ADJ_TIM_LIN(String str) throws Exception{
//		CSO_ADJ_TIM_LIN(str,null);
//	}
	
	
	
//	public void CSO_ADJ_TIM_LIN(String str, Integer row) throws Exception{
//	   PscSsn ssn = this.getSsn();
//		if (row != null && row == 1){
//		   //String dif = this.getDat("CSO_TIM_DIF_DAY",row);
//		   ssn.wriTxt("Fertig (" + row + ")");
//		}
//		else{
//		   ssn.wriTxt("Bitte erst Daten laden");
//		}
//		
//	
//	}

}