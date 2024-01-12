// Last Update by user CUSTOMIZER at 20171211124931
import de.pisa.psa.frm.psa_doc.*;
import de.pisa.psc.srv.gui.*;


/**
 * DMO_PSA_PRO_DOC_REF_SCI dialog implementation
 */
public class IplDlgDmoPsaProDocRefSci extends PsaProDocRefApp {

	/**
	 * common constructor
	 * @param dsc dialog descriptor
	 * @throws Exception
	 */
	public IplDlgDmoPsaProDocRefSci(String dsc) {
		super(dsc);
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.psa_doc.PsaRelDocApp#creDlg(de.pisa.psc.srv.gui.PscGui, de.pisa.psc.srv.gui.PscFrm)
	 */
	@Override
	public void creDlg(PscGui gui, PscFrm frm) throws Exception {
		super.creDlg(gui, frm);
		setFixQue("CTX", "%WEB%");
	}

}