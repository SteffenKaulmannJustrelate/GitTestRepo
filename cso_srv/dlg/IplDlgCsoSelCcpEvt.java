// Last Update by user CUSTOMIZER at 20171211160412
import de.pisa.psa.frm.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.gui.*;

/** CSO_SEL_CCP_EVT */
public class IplDlgCsoSelCcpEvt extends de.pisa.psa.sel.PsaSel
{

public IplDlgCsoSelCcpEvt(String dsc) { super(dsc); }

/**
 * {@inheritDoc}
 * @see de.pisa.psc.srv.gui.PscSel#setSelDat(int)
 */
@Override
public void setSelDat(int pos) throws Exception {
	super.setSelDat(pos);
	
	setSel(pos);
}

@Override
public void setSelDat(String dat) throws Exception {
	super.setSelDat(dat);
	
	if (dat==null || dat.isEmpty()) {
		setSel(0);
	}
}

private void setSel(int row) throws Exception
{
	PscFrm sel_frm = getSelFrm();
	PscDto dyn_dto = getDynDto();
	String evt_gid = (row>0) ? dyn_dto.getDat("PSC_GID", row) : null;
	PsaFrmIpl.setTagPrp(sel_frm, "EVT_GID", "TIT", evt_gid);
}

}