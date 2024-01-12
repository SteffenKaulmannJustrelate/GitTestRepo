// Last Update by user CUSTOMIZER at 20171211125221
import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** 
 * DMO_OEM_DOC_REF_ESL dialog implementation
 */
public class IplDlgDmoOemDocRefEsl extends PsaFrm {

	/**
	 * common constructor
	 * @param dsc dialog descriptor
	 */
	public IplDlgDmoOemDocRefEsl(String dsc) {
		super(dsc);
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.frm.PsaFrmIpl#creDlg(de.pisa.psc.srv.gui.PscGui, de.pisa.psc.srv.gui.PscFrm)
	 */
	@Override
	public void creDlg(PscGui gui, PscFrm frm) throws Exception {
		super.creDlg(gui, frm);
		PscSsn	ssn		= getSsn();
		String	usr_gid	= PsaPrsInt.getPrsExtGid(ssn);
		String	org_gid	= PsaPrsInt.getPrsExtOrgGid(ssn);
		StringBuilder	que	= new StringBuilder();
		if ( PscGid.isVld(usr_gid) ) {
			que.append("'").append(usr_gid).append("'");
		}
		if ( PscGid.isVld(org_gid) ) {
			if ( que.length() > 0 ) {
				que.append(" | ");
			}
			que.append("'").append(org_gid).append("'");
		}
		setFixQue("FAT_GID", que.toString());
		setFixQue("CTX", "%WEB%");
	}

}