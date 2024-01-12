// Last update by user SALESADMIN on host dmoref at 20180322082224
import java.util.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_cpd.*;
import de.pisa.psa.dto.psa_oth.*;
import de.pisa.psa.frm.psa_qst.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.svc.*;

/** CSO_QST_SAV_USX_EMA_NFO_DOC */
public class CsoQstSavUsxEmaNfoDoc extends PsaQstSavUsxBas 
{

	private PscSsn Ssn;
	private UsxPar Par;

	@Override
	public void sav(PscSsn ssn, String par, String qst_gid, String pro_gid, String con_gid, String act_gid) throws Exception
	{
		if (!PscGid.isVld(con_gid)) {
			return;
		}
		Ssn = ssn;
		Par = new UsxPar(par);
		Set<String> doc_set = getDoc();
		if (doc_set.isEmpty()) {
			return;
		}
		creEma(con_gid, pro_gid, doc_set);
	}

	private Set<String> getDoc() throws Exception
	{
		Set<String> doc_set = new HashSet<>();
		String que_idn = Par.getPar("QUE_IDN");
		PscDto res_dto = getResRec(Ssn);
		int res_row = fndAns(res_dto, que_idn);
		if (res_row<=0) {
			return doc_set;
		}
		String ans_idn = res_dto.getDat("ANS_IDN", res_row);
		for (String doc_idn : new PscStrTok(ans_idn, ',')) {
			String doc_num = Par.getPar("DOC_"+doc_idn);
			if (doc_num.isEmpty()) {
				continue;
			}
			String doc_gid = PsaDtoIpl.getFldDat(Ssn, "PSA_DOC", "NUM", "'"+doc_num+"'", "PSC_GID");
			if (PscGid.isVld(doc_gid)) {
				doc_set.add(doc_gid);
			}				
		}
		return doc_set;
	}
	
	private void creEma(String con_gid, String pro_gid, Set<String> doc_set) throws Exception
	{
		String ema_rpt = Par.getPar("EMA_RPT");
		if (ema_rpt.isEmpty()) {
			return;
		}
		String pro_nam = PsaDtoIpl.getFldDat(Ssn, "PSA_PRO", "NAM", pro_gid);
		if (pro_nam==null) {
			pro_nam = "";
		}
		String com_lng = PsaLng.getComLngIdn(Ssn, con_gid);
		if (!PscChe.getSln().contains(com_lng)) {
			com_lng = null;
		}
		String msg_nam = Par.getPar("MSG", "CSO_QST_EVT_EMA_DOC_NAM");
		String ema_nam = PsaUti.getMsg(msg_nam, com_lng, Ssn, pro_nam);
		
		String snd_num = Par.getPar("SND_NUM");
		String snd_gid = PsaDtoIpl.getFldDat(Ssn, "PSA_CON", "NUM", "'"+snd_num+"'", "PSC_GID");
		
		PscDto ema_dto = Ssn.newDto("PSA_CPD_EMA");
		PsaDtoIpl.getSemBasDto(ema_dto, "PSA_CPD_NOT_CHK_RPT_LNG");
		PsaEnvSet env = new PsaEnvSet(Ssn, "PSA_RPT_EML", ema_rpt);
		try {
			ema_dto.insRow(1);
			ema_dto.setDat("NAM", 1, ema_nam);
			ema_dto.setDat("LNG_IDN", 1, com_lng);
			if (PscGid.isVld(pro_gid)) {
				ema_dto.setDat("PRO_GID", 1, pro_gid);
			}
			if (PscGid.isVld(snd_gid)) {
				ema_dto.setDat("SND_PRS_GID", 1, snd_gid);
			}
			ema_dto.putDat();
		}
		finally {
			env.rst();
		}
		String ema_gid = ema_dto.getDat("PSC_GID", 1);
		
		// add recipient
		PscDto con_act_dto = Ssn.newDto("PSA_CON_EMA_TO_AGG");
		con_act_dto.insRow(1);
		con_act_dto.setDat("FAT_GID", 1, con_gid);
		con_act_dto.setDat("CHD_GID", 1, ema_gid);
		PsaDtoIpl.refCom(con_act_dto, con_act_dto.getFld("PSA_CON_XRO.PSC_GID"), 1);
		con_act_dto.putDat();
		
		// add documents
		PscDto act_doc_dto = Ssn.newDto("PSA_ACT_DOC");
		act_doc_dto.getFld("FAT_GID").setDfv(ema_gid);
		for (String doc_gid : doc_set) {
			int row = act_doc_dto.numRec() + 1;
			act_doc_dto.insRow(row);
			act_doc_dto.setDat("CHD_GID", row, doc_gid);
		}
		act_doc_dto.putDat();
		
		// generate body
		PsaCpdFnc cpd_fnc = PsaObjFac.get(Ssn).newPsaCpdFnc(ema_dto);
		cpd_fnc.genEmaBdy(1);
	}

}