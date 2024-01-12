// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171211123232
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

/** PSI_IFC_TST */
public class PsiIfcTst {

	private String chkPsiDto(PscSsn ssn, String nam) {
	String err=null;
		PscDto dto = null;
		int cnt=-1;
		boolean fnd=false;
		try {
			cnt = -1;
			fnd = false;
			dto = ssn.newDto(nam);
			cnt = dto.cntDat();
			fnd = dto.fetNxt();
			ssn.wriTxt(String.format("%s: CNT=%d, FND=%s", nam, cnt, fnd));
		} catch(Exception exc) {
			err = String.format("CNT=%d, FND=%s, MSG=%s", cnt, fnd, exc.getMessage());
		} finally {
			if ( cnt>=0 && dto!=null ) {
				dto.fetCls();
			}
		}
		return err;
	}
	
	public void chkPsiLnkIfcDto(PscSsn ssn) throws Exception {
		PscDto dto = ssn.newDto("PSC_DTO");
		dto.setQue("NAM", "!'%IFC' & !'%GENERIC' & !'%SNM'");
		dto.setQue("PSC_OWN", "58");
		int cnt=0;
		int cnt_err=0;
		try {
			while ( dto.fetNxt() ) {
				++cnt;
				String nam = dto.getDat("NAM", 1);
				String err = chkPsiDto(ssn, nam);
				if ( err!=null ) {
					++cnt_err;
					ssn.wriTxt(String.format("%s: ERR (%s)", nam, err));
				}
			}
			ssn.wriTxt(String.format("Fehlerhafte DTOs ermittelt: %d von %d", cnt_err, cnt));
		} catch(Exception exc) {
			ssn.wriTxt(exc.getMessage());
		} finally {
			dto.fetCls();
		}
	}
	
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		PsiIfcTst itz = new PsiIfcTst();
		itz.chkPsiLnkIfcDto(ssn);
	}
	
}