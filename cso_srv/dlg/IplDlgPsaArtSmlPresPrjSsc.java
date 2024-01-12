// Last Update by user CUSTOMIZER at 20200407065408
import de.pisa.psa.frm.psa_art.*;
import de.pisa.psc.srv.gui.*;

/** PSA_ART_SML_PRES_PRJ_SSC */
public class IplDlgPsaArtSmlPresPrjSsc extends PsaArtApp {

	public IplDlgPsaArtSmlPresPrjSsc(String dsc) { super(dsc); }

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