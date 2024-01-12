// Last Update by user CUSTOMIZER at 20171211125102
import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/** 
 * DMO_PRO_SAL_ART_ESL dialog implementation
 */
public class IplDlgDmoProSalArtEsl extends PsaFrm {

	/**
	 * common constructor
	 * @param dsc dialog descriptor
	 * @throws Exception
	 */
	public IplDlgDmoProSalArtEsl(String dsc) throws Exception {
		super(dsc);
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.PsaFrmIpl#creDlg(de.pisa.psc.srv.gui.PscGui, de.pisa.psc.srv.gui.PscFrm)
	 */
	@Override
	public void creDlg(PscGui gui, PscFrm frm) throws Exception {
		super.creDlg(gui, frm);
		PscSsn	ssn	= getSsn();
		{	// setup dialog tags
			PscDto	tag_dto	= getTagDto();
			int		tag_cnt	= tag_dto.numRec();
			PscFld	fld_nam	= tag_dto.getFld("NAM");
			PscFld	fld_typ	= tag_dto.getFld("TYP");
			PscFld	fld_fnc	= tag_dto.getFld("FNC");
			boolean	hit		= false;
			for ( int tix=1 ; tix <= tag_cnt ; ++tix ) {
				if ( tag_dto.getDat(fld_typ, tix).equals("DAT") && !tag_dto.getDat(fld_nam, tix).equals("NAM") && PscUti.isStr(tag_dto.getDat(fld_fnc, tix))) {
					// remove function descriptor --> no hyperlink effect in GUI
					tag_dto.modDat(fld_fnc, tix, "");
					hit = true;
				}
			}
			if ( hit ) {
				// Refresh tag data
				setTagDto(tag_dto);
			}
		}
		{	// set fixed query
			String	usr_gid	= PsaPrsInt.getPrsExtGid(ssn);
			if ( PscGid.isVld(usr_gid) ) {
				String	org_gid	= PsaPrsInt.getPrsExtOrgGid(ssn);
				if ( PscGid.isVld(org_gid) ) {
					setFixQue("PSA_CST_CON_EXT.PSC_GID", org_gid);
					PscDto	dyn_dto	= getDynDto();
					dyn_dto.setMax(10000);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.PsaFrmIpl#insDlgFrmGid(java.lang.String, int)
	 */
	@Override
	public PscFrm insDlgFrmGid(String dlg_dsc, int row) throws Exception {
		// we force the use of some special dialogs here...
		return super.insDlgFrmGid("DMO_" + dlg_dsc, row);
	}

}