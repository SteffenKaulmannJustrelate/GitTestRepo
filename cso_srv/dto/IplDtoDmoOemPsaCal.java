// Last Update by user CUSTOMIZER at 20171211124901
import de.pisa.psa.dto.psa_act.*;
import de.pisa.psa.dto.psa_prs.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

/**
 * DMO_OEM_PSA_CAL DTO implementation
 */
public class IplDtoDmoOemPsaCal extends PsaActApp {

	/** "external person" flag */
	private boolean extPrs	= false;

	/**
	 * common constructor
	 * @param dsc DTO descriptor
	 * @throws Exception
	 */
	public IplDtoDmoOemPsaCal(String dsc) throws Exception {
		super(dsc);
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#creDto(de.pisa.psc.srv.glb.PscSsn, de.pisa.psc.srv.dto.PscDto)
	 */
	@Override
	public void creDto(PscSsn ssn, PscDto dto) throws Exception {
		super.creDto(ssn, dto);
		extPrs = !PsaPrsInt.chkInt(ssn);
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.dto.PsaDtoIpl#insRow(int, int)
	 */
	@Override
	public void insRow(int row, int cnt) throws Exception {
		super.insRow(row, cnt);
		if ( extPrs && (getImp() == null) && (getTyp() != TYP_COM) ) {
			// pre-fill call data
			PscSsn	ssn		= getSsn();
			String	agn_gid	= PsaPrsInt.getGid(ssn);
			String	prs_gid	= PsaPrsInt.getPrsExtGid(ssn);
			for ( int i=0 ; i < cnt ; ++i ) {
				int	cur_row	= row + i;
				setDat("AGN_PRS_GID", cur_row, agn_gid);
				setDat("CON_GID", cur_row, prs_gid);
				setDat("STY_IDN", cur_row, "OUT_CALL");
				PsaActSty.cpyActStyCol(this, cur_row);
			}
		}
	}

}