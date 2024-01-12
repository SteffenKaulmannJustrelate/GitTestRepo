// Last Update by user CUSTOMIZER at 20171211124837
import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_act.*;
import de.pisa.psa.syn.*;
import de.pisa.psc.ipc.xdr.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/**
 * PSD_ACT DTO implementation
 */
public class IplDtoPsdAct extends de.pisa.psa.dto.psa_act.PsaActApp {

	/**
	 * common constructor
	 * @param dsc DTO descriptor
	 * @throws Exception
	 */
	public IplDtoPsdAct(String dsc) throws Exception {
		super(dsc);
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#getMapPntTit(int)
	 */
	@Override
	public String getMapPntTit(int row) throws Exception {
		PscSsn ssn = getSsn();
		PscGui gui = ssn.getGui();
		IpcXdrEvt evt = gui.getCurEvt();
		String tit;
		if (evt==null) {
			String con_gid = getDat("CON_GID", row);
			tit = PsaDtoIpl.getFldDat(ssn, "PSA_CON", "FRN_IDN", con_gid);
			if (!PscUti.isStr(tit)) {
				tit = super.getMapPntTit(row);
			}
		}
		else {
			tit = super.getMapPntTit(row);
		}
		return tit;
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#insRow(int, int)
	 */
	@Override
	public void insRow(int row, int cnt) throws Exception {
		super.insRow(row, cnt);
		if (!chkOut(this,row)) {
			for (int idx=0; idx<cnt; idx++) {
				int cur_row = row + idx;
				if (getDat("CLA_ADD_IDN", cur_row).contains("PSA_ACT_CLA_REQ")) {
					setStaIdn(cur_row);
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psc.srv.dto.PscDto#setDat(de.pisa.psc.srv.dto.PscFld, int, java.lang.String)
	 */
	@Override
	public void setDat(PscFld fld, int row, String dat) throws Exception {
		String fld_dsc = fld.getDsc();
		if (fld_dsc.equals("CLA_ADD_IDN") && 
				dat!=null &&
				dat.contains("PSA_ACT_CLA_REQ"))
		{
			setStaIdn(row);
		}
		
		super.setDat(fld, row, dat);
	}
	
	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#putRec(int)
	 */
	@Override
	protected void putRec(int row) throws Exception {
		if (!isTypCom() && !chkOut(this, row) && chkIns(row)) {
			PscSsn ssn = getSsn();
			if (PsaSynFlg.GRP_IMP.chk(ssn)) {
				if (getDat("CLA_ADD_IDN", row).contains("PSA_ACT_CLA_REQ")) {
					setStaIdn(row);
				}
			}
		}
		super.putRec(row);	
	}
	
	/**
	 * sets default status ID
	 * @param row row number
	 * @throws Exception
	 */
	private void setStaIdn(int row) throws Exception {
		String sta_idn;
		switch (getStyTyp(this, row)) {
		case APM: sta_idn = "PSA_STA_APM_PST_PRC"; break;
		case TSK: sta_idn = "PSA_STA_TSK_PST_PRC"; break;
		case CAL: sta_idn = "PSA_STA_CAL_PST_PRC"; break;
		case CPD: sta_idn = "PSA_CPD_PST_PRC"; break;
		default: sta_idn = null;
		}
		if (sta_idn!=null) {
			super.setDat("STA_IDN", row, sta_idn);
			PsaActSta.cpyActStaCol(this, row);
		}
	}

}