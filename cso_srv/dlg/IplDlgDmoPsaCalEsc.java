// Last Update by user CUSTOMIZER at 20171211125033
import de.pisa.psa.dto.psa_act.*;
import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psa.frm.psa_act.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/**
 * DMO_PSA_CAL_ESC dialog implementation
 */
public class IplDlgDmoPsaCalEsc extends PsaCalApp {

	/**
	 * common constructor
	 * @param dsc dialog descriptor
	 * @throws Exception
	 */
	public IplDlgDmoPsaCalEsc(String dsc) {
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
		{	// set fixed query condition
			if ( !PsaPrsInt.chkInt(ssn) ) {
				setFixQue("CON_GID", PsaPrsInt.getPrsExtGid(ssn));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.psa_act.PsaCalApp#insRow()
	 */
	@Override
	public int insRow() throws Exception {
		int		rec		= super.insRow();
		int		row		= getRow();
		PscDto	dyn_dto	= getDynDto();
		dyn_dto.setDat("PSA_CAL.STY_IDN", row, "OUT_CALL");
		PsaActSty.cpyActStyCol(dyn_dto, row);
		return rec;
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.psa_act.PsaCalApp#putDat()
	 */
	@Override
	public void putDat() throws Exception {
		PscSsn	ssn	= getSsn();
		String	val	= ssn.getEnv("PSA_ACT_SEL_PRO");
		if ( val == null ) {
			val = "";
		}
		try {
			ssn.putEnv("PSA_ACT_SEL_PRO", "n");
			super.putDat();
		}
		finally {
			ssn.putEnv("PSA_ACT_SEL_PRO", val);
		}
	}
	
}