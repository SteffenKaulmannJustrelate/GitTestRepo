// Last Update by user CUSTOMIZER at 20180306131348
import de.pisa.psa.dto.*;
import de.pisa.psa.sel.psa_sap.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

/** DMO_SEL_SAP_CON_EXT */
public class IplDlgDmoSelSapConExt extends PsaSelSapApp {

	public IplDlgDmoSelSapConExt(String dsc) { 
		super(dsc); 
	}

	/**
	 * @see de.pisa.psc.srv.gui.PscSel#setSelDat(java.lang.String)
	 */
	@Override
	public String rfrSelDat(String fld_inh) throws Exception {
		// Auswahl einschränken für nicht PSI-User			
		PscSsn ssn = this.getSsn();
		int ssn_uic = ssn.getUic();

		PscDto grp_usr = ssn.newDto("PSC_USR_REL");
		String psi_men_grp_gid = PsaDtoIpl.getFldDat(ssn, "PSC_GRP", "IDC", "98", "PSC_GID");

		grp_usr.setQue("IDC", String.valueOf(ssn_uic));
		grp_usr.setQue("GRP_GID", psi_men_grp_gid);


		// *** Reimplementierung PsaSelSapCon#rfrSelDat ***********************

		PscDto sel_dto = getSelFrm().getDynDto();
		PscDto dyn_dto = getDynDto();

		// Set the search criteria for the according contact type 
		// Try to determine the contact type from the dto 
		String cla_typ = sel_dto!=null ? PsaDtoIpl.get_cla_dto(sel_dto) : null;
		String qry = "PSA_PRS | PSA_ORG";
		if (cla_typ!=null && "PSA_PRS,PSA_ORG".indexOf(cla_typ)!=-1) {
			qry = cla_typ;
		} 
		else if (sel_dto!=null && sel_dto.getSupDto("PSA_CON")!=null) {
			int sel_row = getSelRow();
			if ( sel_row>0 ) {
				// determine the contact type from the current row
				String cla = sel_dto.getDat("CLA_TYP", sel_row);
				qry = "PSA_"+cla;
			}
		}
		dyn_dto.setQue("PSA_SAP.CLA_DTO", qry);
		if(grp_usr.cntDat() < 1 ){
			// Auswahl einschränken -> ohne PSI-Prozesse
			dyn_dto.setQue("SAP_IDN", "!%PSI%");
		}

		srtOprPos();
		String chs_txt = super.rfrSelDat(fld_inh);
		delMulRow();

		return chs_txt;
		//*********************************************************************
	}

}