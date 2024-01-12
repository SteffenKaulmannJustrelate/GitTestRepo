// Last Update by user CUSTOMIZER at 20171211160200
import java.util.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.frm.psa_ccp.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.svc.*;

/** CSO_CCP_CNF_EVT_CON_STA_SSF */
public class IplDlgCsoCcpCnfEvtConStaSsf extends PsaCcpCnfApp
{

public IplDlgCsoCcpCnfEvtConStaSsf(String dsc) { super(dsc); }

@Override
protected void ini() throws Exception {
	PscSsn ssn = getSsn();
	String dlg = getCcpDlg();
	PscDto cnf_dto = getCnfDtoDia(dlg);

	UsxPar par = new UsxPar(getCnf(cnf_dto, "DLG", dlg, "PAR"));
	if (par.numPar()==0) {
		par = new UsxPar(ssn.getEnv(dlg+"_DEF_PAR"));
	}

	// additional configuration parameter
	Map<String, String> edt_map = iniCnfPar(par);

	// budget
	String val_str = edt_map.get("EVT_GID");
	val_str = PsaDtoIpl.getFldDat(ssn, "PSA_PRO", "NAM", val_str);
	setEdtDat("EVT_NAM", val_str);

	// quick filter
	iniQfl(par);
}

@Override
protected void sav() throws Exception {
	String dlg = getCcpDlg();
	PscDto cnf_dto = getCnfDtoDia(dlg);
	
	String old_par = getCnf(cnf_dto, "DLG", dlg, "PAR");
	Map<String, String> par_map = getQue(new UsxPar(old_par));
	
	// quick filter
	savQfl(par_map);

	// Additional configuration parameter
	savCnfPar(par_map);
	
	setCnf(cnf_dto, "DLG", dlg, "PAR", PscUsxPar.genParStr(par_map, true));
	cnf_dto.putDat();
}

@Override
public boolean chkCnf() throws Exception {
	return chkCnfDia();
}

}