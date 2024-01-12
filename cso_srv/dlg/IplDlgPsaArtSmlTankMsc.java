// Last Update by user CUSTOMIZER at 20160509070508
import de.pisa.psa.frm.psa_art.*;
import de.pisa.psa.dto.*;
import de.pisa.psc.srv.gui.*;

public class IplDlgPsaArtSmlTankMsc extends PsaArtApp {

	public IplDlgPsaArtSmlTankMsc(String s) throws Exception { super(s); }

	@Override
	public void creDlg(PscGui gui, PscFrm frm) throws Exception {
		super.creDlg(gui, frm);
		PsaDto dyn_dto = (PsaDto)getDynDto();
		dyn_dto.setGlbCtx(true);
		dyn_dto.setFldCtx(false);
	}

}
