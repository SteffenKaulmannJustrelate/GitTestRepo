// Last Update by user CUSTOMIZER at 20171211124758
import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psa.dto.psa_str.*;
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

/**
 * PSD_PRO_STR DTO implementation
 */
public class IplDtoPsdProStr extends PsaStr {

	/** "external person" flag */
	private boolean extPrs	= false;

	/**
	 * common constructor
	 * @param dsc DTO descriptor
	 * @throws Exception
	 */
	public IplDtoPsdProStr(String dsc) throws Exception {
		super(dsc);
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#creDto(de.pisa.psc.srv.glb.PscSsn, de.pisa.psc.srv.dto.PscDto)
	 */
	@Override
	public void creDto(PscSsn ssn, PscDto dto) throws Exception {
		super.creDto(ssn, dto);
		
		if (ssn instanceof PsaSsn) {
			if (((PsaSsn)ssn).getImpMod()==null) {
				extPrs = !PsaPrsInt.chkInt(ssn);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#hdlDlgNam(java.lang.String, de.pisa.psc.srv.dto.PscFld, int, boolean)
	 */
	@Override
	public ParObj hdlDlgNam(String dlg_dsc, PscFld act_fld, int row, boolean mod) throws Exception {
		ParObj	par	= super.hdlDlgNam(dlg_dsc, act_fld, row, mod);
		if ( extPrs ) {
			String dlg_nam = par.getDlgNam();
			String dmo_dlg_nam = "DMO_" + dlg_nam;
			PscDto dlg_dto = getSsn().newDto("PSC_DLG");
			dlg_dto.setQue("NAM", dmo_dlg_nam);
			if (dlg_dto.cntDat()!=0) {
				par.setDlgNam(dmo_dlg_nam);
			}
		}
		return par;
	}

	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#insRow(int, int)
	 */
	@Override
	public void insRow(int row, int cnt) throws Exception {
		super.insRow(row, cnt);
		if ( extPrs && (getTyp() != TYP_COM) && (getImp() == null) ) {
			PscSsn	ssn		= getSsn();
			String	org_gid	= PsaPrsInt.getPrsExtOrgGid(ssn);
			String	com_prs	= PsaPrsInt.getGid(ssn);
			for ( int rix=row ; rix < (row+cnt) ; ++rix) {
				setDat("CON_GID", rix, org_gid);
				setDat("COM_PRS_GID", rix, com_prs);
			}
		}
	}

}