// Last Update by user CUSTOMIZER at 20171212093302
import de.pisa.psa.frm.psa_art.*;
import de.pisa.psc.srv.gui.*;

/** PSA_ART_SML_LUB_PRJ_SSC */
public class IplDlgPsaArtSmlLubPrjSsc extends PsaArtApp {

	public IplDlgPsaArtSmlLubPrjSsc(String s) {
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
