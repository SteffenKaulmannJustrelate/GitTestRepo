// Last Update by user CUSTOMIZER at 20171211151643
import de.pisa.psa.sel.psa_ccp.*;
import de.pisa.psc.srv.dto.*;

/** CSO_SEL_CCP_QFL_EVT_NAM */
public class IplDlgCsoSelCcpQflEvtNam extends PsaSelCcpQflApp
{

public IplDlgCsoSelCcpQflEvtNam(String dsc) { super(dsc); }

@Override
protected String getQflValFld() {
	return "PSC_GID";
}

@Override
protected PscDto getQflValDto() {
	return getDynDto();
}

}