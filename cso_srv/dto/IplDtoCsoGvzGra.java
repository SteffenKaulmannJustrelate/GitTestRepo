// Last Update by user CUSTOMIZER at 20200922085429

// Last Update by user CUSTOMIZER at 20200527200039
import de.pisa.psa.dto.PsaDto;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.EnuExc;
import de.pisa.psc.srv.glb.PscExc;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.sys.PscDtoSeq;

/** CSO_GVZ_GRA */
public class IplDtoCsoGvzGra extends de.pisa.psa.dto.PsaDto {

	public IplDtoCsoGvzGra(String dsc) throws Exception {
		super(dsc);
	}
	
	@Override
	protected void putRec(int row) throws Exception {
		super.putRec(row);
		if (!isTypCom() && !PsaDto.chkOut(this, row)) {
			IplDlgCsoUti.chkNnlThrExc(this, "IDN", row);
			String idn = getDat("IDN", row);
			IplDlgCsoUti.fetDtoOne(true, getSsn(), "CSO_GVZ_GRA", "IDN", "'"+idn+"'");
		}
	}
	
	@Override
	public void creDbo() throws Exception {
		csoCreSeq();
		super.creDbo();
	}

	private void csoCreSeq() throws Exception {
		PscSsn ssn = getSsn();
		PscDtoSeq seq_dto = (PscDtoSeq) IplDlgCsoUti.fetDtoOne(true, ssn, "PSC_SEQ", "NAM", "CSO_GVZ_GRA_IDN");
		boolean cre = false;
		try { 
			seq_dto.modSeqDbo(1);
			PscDtoSeq.fetCheSeqDto();
		}
		catch (PscExc exc) {
			if (exc.getNum()==EnuExc.DBI_SQL_NOT_SEQ) {
				// sequence does not exists -> create it
				cre = true;
			}
			else if (exc.getNum()==EnuExc.DBI_SQL_MIN_SEQ) {
				// sequence exists but the current value if lower than the minimum value
				// -> drop and create it 
				seq_dto.delSeqDbo(1);
				cre = true;
			}
			else {
				throw exc;
			}
		}
		if (cre) {
			seq_dto.creSeqDbo(1);
			PscDtoSeq.fetCheSeqDto();
		}
	}

}