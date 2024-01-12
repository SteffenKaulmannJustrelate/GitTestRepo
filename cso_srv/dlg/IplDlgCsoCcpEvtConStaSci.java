// Last Update by user CUSTOMIZER at 20171212092334
import java.awt.*;
import java.util.*;
import java.util.List;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_ccp.*;
import de.pisa.psa.dto.psa_ifc.*;
import de.pisa.psa.frm.psa_ccp.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.ifc.dia.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/** CSO_CCP_EVT_CON_STA_SCI */
public class IplDlgCsoCcpEvtConStaSci extends PsaCcpDiaSciApp
{

public IplDlgCsoCcpEvtConStaSci(String dsc) { super(dsc); }

@Override
public int fetDat() throws Exception
{
	PsaTmpBlb tmp_dto = (PsaTmpBlb)getDynDto().getDto("PSA_TMP_BLB");
	tmp_dto.setTmpBlb(null);
	// get configuration
	PscDto cnf_dto = getCnfDto();
	if (cnf_dto==null) {
		return super.fetDat();
	}
	if (Cre_Dia) {
		UsxPar par = new UsxPar(cnf_dto.getDat("PAR", 1));
		List<KeyValuePair<String, Integer>> sta_lis = getSta(par);
		// create small diagram
		byte[] dia_fil = creDia(true, sta_lis, cnf_dto);
		setBlb(true, dia_fil);
		// create large diagram
		dia_fil = creDia(false, sta_lis, cnf_dto);
		setBlb(false, dia_fil);
		setBlbTlp(cnf_dto, sta_lis);
		// open diagram
		opnDia(true);
	}
	else {
		shwHouGls(cnf_dto);
	}
	return numRec();
}

@Override
protected void iniQflVal(UsxPar par) throws Exception
{
	if (getQflFld().equals("EVT")) {
		PscSsn ssn = getSsn();
		String evt_gid = par.getPar("EVT_GID");
		setQflVal(evt_gid);
		String evt_nam = PsaDtoIpl.getFldDat(ssn, "PSA_EVT", "NAM", evt_gid);
		setEdtDat("QFL_EDT", evt_nam);
	}
	else {
		super.iniQflVal(par);
	}		
}

protected List<KeyValuePair<String, Integer>> getSta(UsxPar cnf_par) throws Exception
{
	List<KeyValuePair<String, Integer>> ret = new ArrayList<>();
	String evt_gid = getQflVal();
	PscSsn ssn = getSsn();
	if (!PscGid.isVld(evt_gid)) {
		return ret;
	}
	PscDto rel_dto = ssn.newDto("PSA_PRO_CON_EXT_REF");
	PscDbd rel_dbd = rel_dto.getDbd();
	rel_dto.setQue("FAT_GID", evt_gid);
	PscFld sta_nam_fld = rel_dbd.setSqlSel("GRP_STA_NAM", "GROUP BY STA_NAM");
	PscFld sta_cnt_fld = rel_dbd.setSqlSel("CNT_STA_NAM", "COUNT(1)");
	try {
		while (rel_dto.fetNxt()) {
			String sta_nam = rel_dto.getDat(sta_nam_fld, 1);
			int sta_cnt = PscUti.str2int(rel_dto.getDat(sta_cnt_fld, 1), 0);
			ret.add(new KeyValuePair<>(sta_nam, sta_cnt));
		}
	}
	finally {
		rel_dto.fetCls();
	}
	ret.sort(new PscKeyValPar.ValCmp<Integer>().reversed());
	return ret;
}

protected byte[] creDia(boolean sml, List<KeyValuePair<String, Integer>> sta_lis, PscDto cnf_dto) throws Exception
{
	PscSsn ssn = getSsn();
	UsxPar par = new UsxPar(cnf_dto.getDat("PAR", 1));
	StrBuf add_uni = new StrBuf();
	double max = 0;
	for (KeyValuePair<String, Integer> sta : sta_lis) {
		max = Math.max(max, sta.Val);
	}
	int div = getDiv(max, add_uni);
	DiaSiz dia_siz = getDiaSiz(cnf_dto, sml);
	CcpTit ccp_tit = new CcpTit(sml);
	ccp_tit.setTit(par.getPar("TIT"));
	boolean lgl = par.getPar(sml?"LGL_SML":"LGL_LRG", "y").equals("y");

	// setup diagram
	PsaBarDia dia = new PsaBarDia();
	setDiaOpt(dia);
	dia.setLgl(lgl);
	List<Color> clr_lis = new ArrayList<Color>();
	int bar_idx = 0;
	for (KeyValuePair<String, Integer> sta : sta_lis) {
		bar_idx++;
		String key = PscUti.isStr(sta.Key) ? sta.Key : "-";
		dia.addBar("", key, sta.Val/div);
		clr_lis.add(PsaCcpDiaSciApp.getClr(ssn, "BAR_"+bar_idx, null));
	}
	dia.setClr(clr_lis);
	ccp_tit.setDiaTit(this, dia);
	dia.setAxsTck(true, true);
	dia.setNumFmt(getDecFmt(" "+add_uni));
	dia.setNoDatMsg(getNoDatMsg());
	return savDiaBin(dia, dia_siz);
}

protected void setBlbTlp(PscDto cnf_dto, List<KeyValuePair<String, Integer>> sta_lis) throws Exception
{
	CcpTlp tlp = new CcpTlp(CcpTlp.ALIGN.RIGHT);
	sta_lis.forEach(sta->tlp.addLine(sta.Key, Integer.toString(sta.Val)));
	setTlp(tlp);
	setTagPrp("BLB", "HLP", tlp.getTlp(), false);
}

public void OPN_EVT(String par, Integer row, PscFld fld) throws Exception
{
	PscGui gui = getGui();
	PscDto cnf_dto = getCnfDto();
	if (cnf_dto==null) {
		return;
	}
	UsxPar cnf_par = new UsxPar(cnf_dto.getDat("PAR", 1));
	String evt_gid = getQflVal(cnf_par, "EVT_GID", "EVT");
	if (!PscGid.isVld(evt_gid)) {
		return;
	}
	PscFrm frm = gui.newFrm(par);
	PscDto dto = frm.getDynDto();
	dto.setQue("PSC_GID", evt_gid);
	frm.calEvt("REC_FET");
}

}