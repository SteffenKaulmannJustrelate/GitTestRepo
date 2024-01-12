// Last Update by user PSA_PRE_SAL at 20090721140954
import de.pisa.psc.srv.gui.*;

public class IplDlgCsoLocArtNtrRefSci extends de.pisa.psa.frm.PsaFrm
{
	
public IplDlgCsoLocArtNtrRefSci(String dsc) throws Exception{ super(dsc); }

@Override
public void mchFrmXbm(PscFrm new_frm) throws Exception {
	super.mchFrmXbm(new_frm);
	
	setTagPrp(new_frm, "BUT_NEW", "MOD", "0");
}

}