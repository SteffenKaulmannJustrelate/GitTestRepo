// Last Update by user CUSTOMIZER at 20081023131013
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.frm.psa_art.*;

public class IplDlgCstArtMchEsx extends PsaArtApp
{

public IplDlgCstArtMchEsx(String dsc) throws Exception{ super(dsc); }

public void ADD_REC (String par, Integer act_row) throws Exception {
	addRec(act_row);
}

@Override
public PscDto getAddRecSrcDto(PscDto chc_ssc_dto, PscDto mch_esl_dto) throws Exception {
	PscDto art_dto = mch_esl_dto.getDto("PSA_ART").getDrv();
	return chc_ssc_dto.getDto(art_dto.getDsc());
}

@Override
public void creDlg(PscGui gui, PscFrm frm) throws Exception {
	super.creDlg(gui, frm);
	
	((PsaDto)getDynDto()).setGlbCtx(true);
}

@Override
public PscFrm dlgFrmGid(String dlg_dsc, int row, PscFld fld) throws Exception {
	PscFrm new_frm = null;

	// When multiple rows selected, call only one time
	int rows[] = getLisRow();
	if ( rows!=null && rows.length!=0 && row==rows[0] ) {
		new_frm = super.dlgFrmGid(dlg_dsc, row, fld);
	}

	return new_frm;
}

@Override
public PscFrm newSub(String dsc) throws Exception {
	PscFrm sub = super.newSub(dsc);
	if ( dsc.equals("PSA_ART_MCH_ESL") ) {
		sub.rfrDat();
	}
	return sub;
}

@Override
public void putDat() throws Exception{
	PscFrm mch_esl_frm = getSub(2);
	if ( mch_esl_frm!=null ) {
		mch_esl_frm.putDat();
	}
}

@Override
protected void setAddQue() throws Exception {
	PscDto chc_dto = getDynDto();

	chc_dto.setQue("PRO_GID","0");
	chc_dto.setQue("TYP","!'PRL'");

	super.setAddQue();
}

}