// Last Update by user CUSTOMIZER at 20200407065409
import de.pisa.psa.frm.psa_art.*;
import de.pisa.psa.dto.*;
import de.pisa.psc.srv.gui.*;

/** PSA_ART_SML_PRES_MSC */
public class IplDlgPsaArtSmlPresMsc extends PsaArtApp {

	public IplDlgPsaArtSmlPresMsc(String dsc) { super(dsc); }

	@Override
	public void creDlg(PscGui gui, PscFrm frm) throws Exception {
		super.creDlg(gui, frm);
		PsaDto dyn_dto = (PsaDto)getDynDto();
		dyn_dto.setGlbCtx(true);
		dyn_dto.setFldCtx(false);
	}

}