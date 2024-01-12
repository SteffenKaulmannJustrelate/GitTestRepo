// Last Update by user CUSTOMIZER at 20170817123651
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.rpt.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_cpd.*;
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.dto.psa_oth.*;
import de.pisa.psa.dto.psa_rpt.*;
import de.pisa.psa.ifc.*;

/** CSO_EVT_ESC */
public class IplDlgCsoEvtEsc extends de.pisa.psa.frm.psa_pro.PsaProApp
{

public IplDlgCsoEvtEsc(String dsc) throws Exception{ super(dsc); }


public void CSO_CRE_CER(String par, Integer row) throws Exception
{
	PscDto dyn_dto = getDynDto();
	PscSsn ssn = getSsn();
	PscGui gui = getGui();
	UsxPar usx_par = new UsxPar(par);
	
	if (!gui.wriBoxQue("CSO_CRE_EVT_CER")) {
		return;
	}
	
	String con_sta_idn = usx_par.getPar("CON_STA_IDN");
	String doc_sty_idn = usx_par.getPar("DOC_STY_IDN");
	String cer_rpt_idn = usx_par.getPar("CER_RPT_IDN");
	String ema_rpt_idn = usx_par.getPar("EMA_RPT_IDN");
	String pro_gid = dyn_dto.getDat("PSC_GID", row);
	
	if (!PscGid.isVld(pro_gid)) {
		return;
	}
	
	String pro_nam = dyn_dto.getDat("NAM", row);
	Map<String, String> doc_sty_nam_map = getDocStyNam(doc_sty_idn);
	Set<String> lng_set = new HashSet<String>();
	Collections.addAll(lng_set, ssn.getDbi().getLng());
	String act_cla_add_idn = null;
	if (dyn_dto.getDat("STY_IDN", row).equals("PSA_EVT_STY_TRA")) {
		act_cla_add_idn = "PSA_ACT_CLA_TRA";
	}
	
	NewFrmMulHlp frm_hlp = new NewFrmMulHlp("PSA_CPD_EMA_ESC", "PSC_GID");
	
	if (PscUti.isStr(con_sta_idn) && PscGid.isVld(pro_gid)) {
		PscDto pro_con_sta_dto = ssn.newDto("PSA_PRO_CON_STA");
		pro_con_sta_dto.setQue("PRO_GID", pro_gid);
		pro_con_sta_dto.setQue("ORI_IDN", "EVT_STA_PRT");
		if (pro_con_sta_dto.fetDat()!=0) {
			con_sta_idn = pro_con_sta_dto.getDat("IDN", 1);
		}
	}
	
	PscDto pro_con_dto = ssn.newDto("PSA_PRO_CON_EXT_REF");
	pro_con_dto.setQue("FAT_GID", pro_gid);
	pro_con_dto.setQue("STA_IDN", con_sta_idn);
	SavPnt sav_pnt = new SavPnt(ssn);
	try {
		while (pro_con_dto.fetNxt()) {
			// check if contact already has a certificate
			String con_gid = pro_con_dto.getDat("CHD_GID", 1);
			if (hasCer(pro_gid, con_gid, doc_sty_idn)) {
				continue;
			}
			
			// get language
			String com_lng = PsaLng.getComLngIdn(ssn, con_gid);
			
			// generate document name
			String doc_nam = pro_nam;
			{
				String doc_sty_nam = doc_sty_nam_map.get(com_lng);
				if (doc_sty_nam==null) {
					doc_sty_nam = doc_sty_nam_map.get("");
				}
				if (PscUti.isStr(doc_sty_nam)) {
					doc_nam = doc_sty_nam+": "+doc_nam;
				}
			}
			
			// generate report
			RptGen.RptDat rpt_dat;
			{
				RptGen rpt_cer = new RptGen(dyn_dto, new int[]{row});
				rpt_cer.addAddQue("CONTACT", "PSC_GID", con_gid);
				rpt_cer.setRetDat(true);
				rpt_cer.setRptNam(cer_rpt_idn);
				rpt_cer.setDatLng(com_lng);
				rpt_cer.setRptLng(com_lng);
				if (!rpt_cer.genRpt()) {
					continue;
				}
				rpt_dat = rpt_cer.getRptDat().get(0);
			}
			
			// create document
			String rpt_typ = FilUtl.getTyp(rpt_dat.getNam());
			String doc_gid = PsaDoc.creDoc(ssn, rpt_dat.getDat(), doc_nam+'.'+rpt_typ, doc_nam, false);
			{
				if (!PscGid.isVld(doc_gid)) {
					continue;
				}
				PsaDoc.setDocSty(ssn, doc_gid, doc_sty_idn);
				PscDto doc_dto = ssn.newDto("PSA_DOC");
				doc_dto.setQue("PSC_GID", doc_gid);
				if (doc_dto.fetDat()==0) {
					continue;
				}
				Sta sta = DocSta.getSta(doc_dto, 1);
				sta.modSta(Sta.OPR.RLS);
			}
			
			// link document
			{
				PscDto pro_doc_dto = ssn.newDto("PSA_PRO_DOC");
				pro_doc_dto.insRow(1);
				pro_doc_dto.setDat("FAT_GID", 1, pro_gid);
				pro_doc_dto.setDat("CHD_GID", 1, doc_gid);
				pro_doc_dto.putDat();
				PscDto con_doc_dto = ssn.newDto("PSA_CON_DOC");
				con_doc_dto.insRow(1);
				con_doc_dto.setDat("FAT_GID", 1, con_gid);
				con_doc_dto.setDat("CHD_GID", 1, doc_gid);
				con_doc_dto.putDat();
			}
			
			// create mail
			{
				String rpt_gid = PscRptFac.getIns(ssn, ema_rpt_idn, com_lng).getGid();
				PscDto ema_dto = ssn.newDto("PSA_CPD_EMA");
				ema_dto.insRow(1);
				ema_dto.setDat("NAM", 1, doc_nam);
				ema_dto.setDat("RPT_GID", 1, rpt_gid);
				ema_dto.setDat("PRO_GID", 1, pro_gid);
				try {
					PsaDtoIpl.setSemSetDat(ema_dto);
					ema_dto.setDat("LNG_IDN", 1, com_lng);
					PsaLng.cpyLngCol(ema_dto, 1, "");
				}
				finally {
					PsaDtoIpl.resSemSetDat(ema_dto);
				}
				if (act_cla_add_idn!=null) {
					ema_dto.setDat("CLA_ADD_IDN", 1, act_cla_add_idn);
					PsaDtoIpl.copMulLngCol(ema_dto, 1, "PSA_ACT_CLA", "CLA_ADD_IDN", "CLA_ADD_NAM");
				}
				ema_dto.putDat();
				String ema_gid = ema_dto.getDat("PSC_GID", 1);
				PscDto act_doc_dto = ssn.newDto(ema_dto, 1, "ACT_DOC");
				act_doc_dto.insRow(1);
				act_doc_dto.setDat("FAT_GID", 1, ema_gid);
				act_doc_dto.setDat("CHD_GID", 1, doc_gid);
				act_doc_dto.putDat();
				PscDto rcp_dto = ssn.newDto("PSA_CON_CPD_AGG");
				rcp_dto.insRow(1);
				rcp_dto.setDat("FAT_GID", 1, con_gid);
				rcp_dto.setDat("CHD_GID", 1, ema_gid);
				PsaDtoIpl.refCom(rcp_dto, rcp_dto.getFld("PSA_CON_XRO.PSC_GID"), 1);
				rcp_dto.putDat();
				frm_hlp.Gid_Set.add(ema_gid);
				
				PsaCpdFnc cpd_fnc = new PsaCpdFnc(ema_dto);
				cpd_fnc.genEmaBdy(1);
			}
			
			ssn.wriMsg("CSO_EVT_CER_CRE", pro_con_dto.getDat("PSA_CON.FRN_IDN", 1));
		}
		sav_pnt.end();
	}
	finally {
		pro_con_dto.fetCls();
		sav_pnt.abo();
	}
	
	newFrmMul(this, frm_hlp);
}

private boolean hasCer(String pro_gid, String con_gid, String sty_idn)
	throws Exception
{
	PscSsn ssn = getSsn();
	PscDto pro_doc_dto = ssn.newDto("PSA_PRO_DOC");
	pro_doc_dto.setQue("FAT_GID", pro_gid);
	PscDto con_doc_dto = ssn.newDto("PSA_CON_DOC_REF");
	con_doc_dto.setQue("FAT_GID", con_gid);
	con_doc_dto.setQue("PSA_DOC.STY_IDN", sty_idn);
	int con_doc_cnt = con_doc_dto.fetDat();
	for (int con_doc_row=1; con_doc_row<=con_doc_cnt; con_doc_row++) {
		String doc_gid = con_doc_dto.getDat("CHD_GID", con_doc_row);
		pro_doc_dto.setQue("CHD_GID", doc_gid);
		if (pro_doc_dto.cntDat()!=0) {
			return true;
		}
	}
	return false;
}

private Map<String, String> getDocStyNam(String sty_idn) throws Exception 
{
	if (!PscUti.isStr(sty_idn)) {
		return Collections.emptyMap();
	}
	PscSsn ssn = getSsn();
	PscDto sty_dto = ssn.newDto("PSA_DOC_STY");
	sty_dto.setQue("IDN", sty_idn);
	if (sty_dto.fetDat()==0) {
		return Collections.emptyMap();
	}
	return PsaUti.getMulLngDat(sty_dto.getFld("NAM"), 1);
}

}