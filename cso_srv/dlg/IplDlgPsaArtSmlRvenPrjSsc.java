// Last Update by user CUSTOMIZER at 20171212094022
import de.pisa.psa.frm.psa_art.*;
import de.pisa.psc.srv.gui.*;

/** PSA_ART_SML_RVEN_PRJ_SSC */
public class IplDlgPsaArtSmlRvenPrjSsc extends PsaArtApp {

	public IplDlgPsaArtSmlRvenPrjSsc(String s) {
		super(s);
	}

	@Override
	public PscFrm newSub(String dsc) throws Exception {
		PscFrm sub_frm = super.newSub(dsc);
		int row = getRow();
		if (row>0 && sub_frm instanceof PsaArtApp) {
			((PsaArtApp)sub_frm).setRowXba(row);
		}
		return sub_frm;
	}

}
