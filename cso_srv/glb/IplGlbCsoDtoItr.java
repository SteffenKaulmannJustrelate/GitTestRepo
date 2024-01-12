// Last Update by user CUSTOMIZER at 20200922085407
//Last Update by user CUSTOMIZER at 20180823125050
import de.pisa.psa.dto.UsxPar;
import de.pisa.psa.dto.psa_scn.JobThr;
import de.pisa.psa.ifc.PscGid;
import de.pisa.psa.ifc.SavPnt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.svc.LamUti.Consumer_WithExceptions;
import de.pisa.psc.srv.svc.LamUti.Function_WithExceptions;
import de.pisa.psc.srv.svc.LamUti.Predicate_WithExceptions;
import de.pisa.psc.srv.svc.LamUti.Runnable_WithExceptions;
import de.pisa.psc.srv.svc.PscUsxPar.Ent;

/** CSO_DTO_ITR */
public class IplGlbCsoDtoItr {
	
	PscSsn SSN;
	private UsxPar set_fld_usx_lis = null;
	private String whe_usx_que_str = null;
	protected PscDto itr_dto = null;
	private Integer max_cyc = null;
	protected int cnt_cyc = 0;
	protected int num_upd = 0;
	private boolean is_upd = false;
	private boolean is_del = false;
	private boolean is_trn = false;
	private boolean is_per_rec_trn = false;
	private Consumer_WithExceptions<PscDto> itr_fnc;
	private boolean use_qte = false;
	private Predicate_WithExceptions<PscDto> ign_fnc;
	private Predicate_WithExceptions <PscDto> end_loo_chk_fnc;
	private JobThr job_thr = null;
	private Runnable_WithExceptions on_job_end_fnc = null;
	private boolean is_itr_end = false;
	private Object res = null;
	
	public IplGlbCsoDtoItr() {
		this(false, false, false);
	}
	
	public IplGlbCsoDtoItr(boolean end_loo_if_job_end, boolean use_trn_per_rec, boolean use_ful_trn) {
		if (end_loo_if_job_end) {
			endLooIfJobEndAndDo(null);
		}
		setTrn(use_ful_trn);
		is_per_rec_trn = use_trn_per_rec;
	}
	
	@Deprecated
	public IplGlbCsoDtoItr setTrn(boolean use_trn) {
		this.is_trn = use_trn;
		return this;
	}

	public IplGlbCsoDtoItr upd(PscSsn ssn, String dto_dsc) throws Exception {
		return upd(ssn.newDto(dto_dsc, false, false, true, true));
	}

	public IplGlbCsoDtoItr upd(PscDto dto_for_upd) {
		is_upd = true;
		is_del = false;
		ini(dto_for_upd);
		return this;
	}
	
	public IplGlbCsoDtoItr itr(PscSsn ssn, String dto_dsc) throws Exception {
		return itr(ssn.newDto(dto_dsc, false, false, true, true));
	}

	public IplGlbCsoDtoItr itr(PscDto itr_dto) {
		is_upd = false;
		is_del = false;
		ini(itr_dto);
		return this;
	}
	
	public IplGlbCsoDtoItr delFro(PscSsn ssn, String dto_dsc) throws Exception {
		return delFro(ssn.newDto(dto_dsc, false, false, true, true));
	}
	
	public IplGlbCsoDtoItr delFro(PscDto itr_dto) {
		is_upd = false;
		is_del = true;
		ini(itr_dto);
		return this;
	}
	
	
	private void ini(PscDto itr_dto) {
		is_itr_end = false;
		res = null;
		this.itr_dto = itr_dto;
		this.SSN = itr_dto.getSsn();
		set_fld_usx_lis = null;
		whe_usx_que_str = null;
//		max_cyc = null;
		cnt_cyc = 0;
		num_upd = 0;
//		is_trn = false;
		use_qte = false;
		itr_fnc = null;
		ign_fnc = null;
		end_loo_chk_fnc = null;
//		job_thr = null;
//		on_job_end_fnc = null;
	}

	public IplGlbCsoDtoItr set(String set_fld_usx_lis_str) {
		if (!isUpd()) {
			throw new UnsupportedOperationException("Use upd() not itr() if you want so set fields");
		}
		if (itr_dto == null) {
			throw new UnsupportedOperationException("No DTO");
		}
		set_fld_usx_lis = new UsxPar(set_fld_usx_lis_str);
		chkAllFld(set_fld_usx_lis);
		return this;
	}


	private boolean isUpd() {
		return is_upd;
	}

	public IplGlbCsoDtoItr whe(String fld1, String que1, String... oth_que_par_lis) throws Exception {
		if (oth_que_par_lis != null && oth_que_par_lis.length % 2 != 0) { // uneven param number
			throw new RuntimeException("Invalid query");
		}
		List<String> que_par_lis = new ArrayList<>();
		que_par_lis.add(fld1);
		que_par_lis.add(que1);
		if (oth_que_par_lis != null) {
			for (String que_par : oth_que_par_lis) {
				que_par_lis.add(que_par);
			}
		}
		String usx_que_str = "";
		for (int i = 0; i < que_par_lis.size(); i++) {
			if (i % 2 == 0) { // field dsc
				usx_que_str += "  "+que_par_lis.get(i);
			} else { // que
				usx_que_str += ":="+que_par_lis.get(i);
			}
		}
		return whe(usx_que_str);
	}
	
	public IplGlbCsoDtoItr whe(String usx_que_str) throws Exception {
		if (itr_dto == null) {
			throw new UnsupportedOperationException("No DTO");
		}
		if (isUpd() && (set_fld_usx_lis == null || set_fld_usx_lis.getParLis().isEmpty())) {
			throw new UnsupportedOperationException("You need to specify fields to set via set(...)");
		}
		whe_usx_que_str = "";
		or(usx_que_str);
		return this;
	}
	
	public IplGlbCsoDtoItr or(String usx_que_str) {
		if (whe_usx_que_str == null) {
			throw new UnsupportedOperationException("First call where(...)");
		}
		whe_usx_que_str = whe_usx_que_str.isEmpty() ? usx_que_str : 
				whe_usx_que_str + " " + IplDlgCsoUti.BIG_USX_QUE_OR + " " + usx_que_str;
		return this;
	}
	
	public IplGlbCsoDtoItr setSgl() {
		setMax(1);
		return this;
	}
	
	public void clrWhe() throws Exception {
		whe_usx_que_str = null;
		itr_dto.delQue();
	}
	
	public IplGlbCsoDtoItr setQte(boolean use_qte) throws Exception {
		this.use_qte = use_qte;
		return this;
	}
	
	public int exeSgl() throws Exception {
		return exeSgl(null);
	}
	
	public int exeSgl(Consumer_WithExceptions<PscDto> fnc) throws Exception {
		Integer bck_max = max_cyc;
		max_cyc = 1;
		exe(fnc);
		max_cyc = bck_max;
		return num_upd;
	}
	
	public int getCycCnt() {
		return cnt_cyc;
	}
	
	public int getUpdCnt() {
		return num_upd;
	}
	
	public PscDto getItrDto() {
		return itr_dto;
	}
	
	public int exe() throws Exception {
		return exe(null);
	}
	
	public int exe(Consumer_WithExceptions<PscDto> fnc) throws Exception {
		chkExeOk();
		setIfSgl();
		cnt_cyc = 0;
		this.itr_fnc = fnc;
		SavPnt sav = null;
		try {
			if (is_trn) {
				sav = new SavPnt(SSN);
			}
			if (whe_usx_que_str != null) {
				itr_dto.delQue();
				IplDlgCsoUti.setUsxQue(itr_dto, whe_usx_que_str, use_qte);
			}
			runExe();
			if (sav != null) {
				sav.end();
			}
		} finally {
			if (sav != null) {
				sav.abo();
			}
		}
		return num_upd;
	}
	
	private void setIfSgl() {
		if (whe_usx_que_str != null && !whe_usx_que_str.contains(IplDlgCsoUti.BIG_USX_QUE_OR)) {
			UsxPar usx_que = new UsxPar(whe_usx_que_str);
			List<Ent> que_lis = usx_que.getParLis();
			if (que_lis.size() == 1) {
				Ent ent = que_lis.get(0);
				if (ent.Key.equals("PSC_GID") && PscGid.isVld(ent.Val) && ent.Val.trim().length() == 32) {
					setSgl(); // only one record to find
				}
			}
		}
	}

	private void chkExeOk() {
		if (itr_dto == null || (isUpd() && (set_fld_usx_lis == null || set_fld_usx_lis.getParLis().isEmpty()))) {
			throw new UnsupportedOperationException("DTO or the field to set are not defined");
		}
	}


	public PscDto getDto() {
		return itr_dto;
	}
	
	public IplGlbCsoDtoItr setMax(Integer max_cyc) {
		this.max_cyc = max_cyc;
		return this;
	}

	/**
	 * Use {@link #ignIf(Function_WithExceptions)} instead
	 */
	@Deprecated
	protected boolean ignRec() throws Exception  {
		return false; // default: handle all records
	}
	
	public IplGlbCsoDtoItr ignIf(Predicate_WithExceptions<PscDto> ign_fnc) {
		chkItrIni();
		this.ign_fnc = ign_fnc;
		return this;
	}

	/**
	 * Never directly call this method. It is not what you expect. It is for overloading while construction.
	 * @Deprecated !!!!!!!!!!!!
	 */
	@Deprecated
	protected void pst(boolean is_dto_mod) throws Exception {/*empty*/}

	/**
	 * Never directly call this method. It is not what you expect. It is for overloading while construction.
	 * @Deprecated Use consumer in {@link #exe(Consumer_WithExceptions)} instead.
	 */
	@Deprecated
	protected void pre(boolean is_dto_mod) throws Exception  {/*empty*/}


	/**
	 * Never directly call this method. It is not what you expect. It is for overloading while construction.
	 * @Deprecated: Use consumer in {@link #endLooIf(Predicate_WithExceptions)} instead.
	 */
	@Deprecated
	protected boolean endLoo() throws Exception {
		return false; // default: never break
	}
	
	public IplGlbCsoDtoItr endLooIf(Predicate_WithExceptions<PscDto> end_loo_chk_fnc) {
		chkItrIni();
		this.end_loo_chk_fnc = end_loo_chk_fnc;
		return this;
	}
	
	public <T> T getRes() {
		return (T) res;
	}
	
	public void setRes(Object res) {
		this.res = res;
	}

	private void chkItrIni() {
		if (itr_dto == null) {
			throw new RuntimeException("First call the operator function ( itr(), upd(), delFro() ... )");
		}
	}

	private void runExe() throws Exception {
		try {
			num_upd = 0;
			while (itr_dto.fetNxt()) {
				if (isJobEnd() || isItrEnd() || endLoo() || (end_loo_chk_fnc != null && end_loo_chk_fnc.test(itr_dto))) {
					break;
				}
				if (!ignRec() && (ign_fnc == null || !ign_fnc.test(itr_dto))) {
					SavPnt sav = null;
					try {
						if (is_per_rec_trn) {
							sav = new SavPnt(SSN);
						}
						
						boolean is_mod = false;
						if (isUpd()) {
							for (Ent ent: set_fld_usx_lis) {
								String val = prpSet(ent.Val);
								itr_dto.setDat(ent.Key, 1, val);
							}
						}
						is_mod = itr_dto.chkMod();
						pre(is_mod);
						if (itr_fnc != null) {
							itr_fnc.accept(itr_dto);
						}
						is_mod = is_mod || itr_dto.chkMod();
						if (is_del) {
							itr_dto.delDat(1);
							num_upd++;
							is_mod = false;
						}
						if (is_mod) {
							itr_dto.putDat();
							num_upd++;
						}
						pst(is_mod);
						
						if (sav != null) {
							sav.end();
						}
					} finally {
						if (sav != null) {
							sav.abo();
						}
					}
				}
				cnt_cyc++;
				if (max_cyc != null && cnt_cyc >= max_cyc) {
					break;
				}
			}
		} finally {
			itr_dto.fetCls();
		}
	}
	
	protected void endItr() {
		is_itr_end = true;
	}
	
	protected void endItrSetRes(Object res) {
		setRes(res);;
		endItr();
	}
	
	private boolean isItrEnd() {
		return is_itr_end;
	}

	private boolean isJobEnd() throws Exception {
		boolean is_job_end = job_thr != null && !job_thr.isRun();
		if (is_job_end && on_job_end_fnc != null) {
			on_job_end_fnc.accept();
		}
		return is_job_end;
	}

	private final static Pattern SRC_SET_FLD_PAT = Pattern.compile("\\[[_\\.\\w]+\\]"); // match e.g. [PSA_SPT_CON.FRN_IDN]
	
	private String prpSet(String val) throws Exception {
		if (val == null || val.equals("null")) {
			val = "";
		}
		if (SRC_SET_FLD_PAT.matcher(val).matches()) {
			String src_fld_dsc = val.substring(1, val.length()-1);
			val = itr_dto.getDat(src_fld_dsc, 1);
		}
		return val;
	}

	private void chkAllFld(UsxPar usx_que) {
		for (Ent ent: usx_que) {
			if (itr_dto.getFld(ent.Key) == null) {
				throw new RuntimeException("No such field: '"+ent.Key+"'");
			}
		}
	}
	
	/**
	 * 
	 * @param on_job_end_fnc - end action - can be null
	 * @return
	 */
	@Deprecated
	public IplGlbCsoDtoItr endLooIfJobEndAndDo(Runnable_WithExceptions on_job_end_fnc) {
		JobThr job_thr = JobThr.getJobThr();
		return endLooIfJobEndAndDo(job_thr, on_job_end_fnc);
	}

	/**
	 * 
	 * @param job_thr
	 * @param on_job_end_fnc - end action - can be null
	 * @return
	 */
	private IplGlbCsoDtoItr endLooIfJobEndAndDo(JobThr job_thr, Runnable_WithExceptions on_job_end_fnc) {
		this.job_thr = job_thr;
		this.on_job_end_fnc = on_job_end_fnc;
		return this;
	}

	
	
}
