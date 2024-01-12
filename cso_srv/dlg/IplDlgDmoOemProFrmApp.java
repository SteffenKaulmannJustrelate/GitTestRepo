// Last Update by user CUSTOMIZER at 20171211125150
import de.pisa.psa.frm.psa_pro.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;


/**
 * generic order/quoting/opportunity dialog implementation for OEM users<br>
 * DMO_OEM_PRO_FRM_APP
 */
public class IplDlgDmoOemProFrmApp extends PsaQuoApp {

	/**
	 * common constructor
	 * @param dsc dialog descriptor
	 */
	public IplDlgDmoOemProFrmApp(String dsc) {
		super(dsc);
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.PsaFrmIpl#creDlg(de.pisa.psc.srv.gui.PscGui, de.pisa.psc.srv.gui.PscFrm)
	 */
	@Override
	public void creDlg(PscGui gui, PscFrm frm) throws Exception {
		super.creDlg(gui, frm);
		PscDto	tag_dto	= getTagDto();
		int		tag_cnt	= tag_dto.numRec();
		PscFld	fld_typ	= tag_dto.getFld("TYP");
		PscFld	fld_fnc	= tag_dto.getFld("FNC");
		boolean	hit		= false;
		for ( int tix=1 ; tix <= tag_cnt ; ++tix ) {
			if ( tag_dto.getDat(fld_typ, tix).equals("DAT") && PscUti.isStr(tag_dto.getDat(fld_fnc, tix))) {
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

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.psa_pro.PsaProApp#modSucPrb(int)
	 */
	@Override
	public void modSucPrb(int row) throws Exception {
		// do nothing!!!
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psc.srv.gui.PscFrm#setRow(int)
	 */
	@Override
	public void setRow(int row) throws Exception {
		super.setRow(row);
      PscDto	tag_dto	= getTagDto();
      int		suc_pos	= getTagPos("SUC_PRB");
      if ( suc_pos > 0 ) {
      	// check "MOD" property
      	if ( !tag_dto.getDat("MOD", suc_pos).equals("-") ) {
      		// make it invisible in ALL cases!
      		tag_dto.modDat("MOD", suc_pos, "-");
      	}
      }
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.PsaFrmIpl#newFrmGid(java.lang.String, int, de.pisa.psc.srv.dto.PscFld)
	 */
	@Override
	public PscFrm newFrmGid(String dlg_dsc, int row, PscFld fld) throws Exception {
		// disable any hyperlink
		return null;
	}

}