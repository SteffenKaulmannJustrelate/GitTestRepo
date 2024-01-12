// Last Update by user CUSTOMIZER at 20171211124605
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;

/********************************************************************
* <DL>
* <DT>module</DT>       <DD>PSD_CSO_SEL_DSP_NUM</DD>
* <DT>description</DT> <DD>PiSAsales dialog: </DD>
* </DL>
* @since 04.11.2009
* @author Bodo Rekowski
*********************************************************************/
public class IplDlgPsdCsoSelDspNum extends de.pisa.psa.sel.PsaSel
{

	/////////////////////////////////////////////////////////////
	//variables
	/////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////
	//constructor
	/////////////////////////////////////////////////////////////

	/**
	 * IplDlgPsdCsoSelDspNum constructor
	 * @param dsc form-descriptor
	 * @throws Exception
	 */
	public IplDlgPsdCsoSelDspNum (String dsc) {
		super(dsc);
	}

	/////////////////////////////////////////////////////////////
	// methods
	/////////////////////////////////////////////////////////////
	
	@Override
	public String rfrSelDat(String str) throws Exception{
		PscFrm dlg_frm = getSelFrm();
		PscDto dyn_dto = getDynDto();
		int dlg_frm_row = this.getSelRow();
		if(dlg_frm.getDsc().equals("PSD_CSO_ORG_EXT_PSI_SSO")
				|| dlg_frm.getDsc().equals("PSA_CSO_ORG_EXT_PSI_SSO") ){
			
			String acc_man_gid = dlg_frm.getDynDto().getDat("CRE_PRS_GID", dlg_frm_row);
			if(acc_man_gid != null && acc_man_gid.length() > 0 && !acc_man_gid.startsWith("OUT_")){			
				dyn_dto.setQue("CON_GID", acc_man_gid);
			}else{
				return super.rfrSelDat(null);
			}
		}else if(dlg_frm.getDsc().equals("PSD_CSO_QUO_PSI_SSO")
				|| dlg_frm.getDsc().equals("PSA_CSO_QUO_PSI_SSO")
				|| dlg_frm.getDsc().equals("PSD_CSO_ORD_PSI_SSO")
				|| dlg_frm.getDsc().equals("PSA_CSO_ORD_PSI_SSO" )){
			String com_prs_gid = dlg_frm.getDynDto().getDat("COM_PRS_GID", dlg_frm_row);
			if(com_prs_gid != null && com_prs_gid.length() > 0 && !com_prs_gid.startsWith("OUT_")){			
				dyn_dto.setQue("CON_GID", com_prs_gid);
			}else{
				return super.rfrSelDat(null);
			}
			
		}
		return super.rfrSelDat(str);
	}
	
}