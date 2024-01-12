// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20171122075932
import java.util.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_act.*;
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.dto.psa_pro.*;
import de.pisa.psa.dto.psa_sap.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.frm.psa_svc.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/** DMO_SVC_ORD_USX */
public class DmoSvcOrdUsx extends de.pisa.psa.dto.psa_svc.PsaSvcOrdApp {
	
	public DmoSvcOrdUsx(String dsc) throws Exception {
		super(dsc);
	}

	public int clsSvcTrb(PscDto dto, Integer row, String old_sta, String par) throws Exception
	{
		PscSsn ssn = dto.getSsn();
		PscGui gui = ssn.getGui();
		if (gui==null) {
			return 0;
		}
		String svc_gid = dto.getDat("SVC_GID", row);
		if (!PscGid.isVld(svc_gid)) {
			return 0;
		}
		PscDto svc_dto = ssn.newDto("PSA_SVC_TRB");
		svc_dto.setQue("PSC_GID", svc_gid);
		if (svc_dto.fetDat()==0 || !svc_dto.chkAcc(1, 'W')) {
			return 0;
		}
		String svc_opr_idn = PsaDtoIpl.getFldDat(ssn, "PSA_SVC_TRB", "OPR_IDN", svc_gid);
		svc_opr_idn = svc_opr_idn.substring(0, svc_opr_idn.lastIndexOf('_')) + "_CLS";
		if (!PsaOpr.chkOprExs(ssn, svc_opr_idn)) {
			return 0;
		}
		String msg = gui.getMsg("CST_CLS_SVC_CAL");
		String box_ret = PsaDtoIpl.wriBox(gui, msg, "Q", "Q", "N");
		if (!box_ret.equals("Y")) {
			return 0;
		}
		svc_dto.setDat("OPR_IDN", 1, svc_opr_idn);
		svc_dto.putDat();
		return 0;
	}
	
	public int creSvcApm(PscDto dto, Integer row, String old_sta, String par) throws Exception
	{
		if (PsaDtoIpl.getSemBasDto(dto, "creSvcApm")) {
			return 0;
		}
		
		PscSsn ssn = dto.getSsn();
		PscDto apm_dto = ssn.newDto("PSA_APM");
		apm_dto.insRow(1);
		String pro_gid = dto.getDat("PSC_GID", row);
		if (PscGid.isVld(pro_gid)) {
			apm_dto.setDat("PRO_GID", 1, pro_gid);
		}
		String con_gid = dto.getDat("CON_GID", row);
		if (PscGid.isVld(con_gid)) {
			apm_dto.setDat("CON_GID", 1, con_gid);
		}
		PsaFrmIpl.insFrmGidModSetDat(dto, row, apm_dto, 1, par);
		PsaSvcOrdApp.setSvcApmDat(dto, row, apm_dto, 1);
		apm_dto.setDat("OPR_IDN", 1, dto.getDat("OPR_IDN", row));
		apm_dto.setDat("CTT", 1, dto.getDat("DSC", row));
		String pro_pri = dto.getDat("PRI_IDN", row);
		String act_pri;
		if (pro_pri.contains("LOW")) {
			act_pri = "PSA_LOW";
		}
		else if (pro_pri.contains("HGH")) {
			act_pri = "PSA_HGH";
		}
		else {
			act_pri = "PSA_NRM";
		}
		apm_dto.setDat("PRI_IDN", 1, act_pri);
		PsaActPri.cpyActPriCol(apm_dto, 1);
		apm_dto.putDat();
		String apm_gid = apm_dto.getDat("PSC_GID", 1);
		byte blb[] = BlbUtl.getBlb(ssn, "CSO_MAP_PNT_ROU", false);
		if (blb!=null && blb.length>0) {
			String doc_gid = PsaDoc.creDoc(ssn, blb, "Routenplanung.pdf", "Routenplanung", true);
			PscDto rel_dto = ssn.newDto("PSA_ACT_DOC");
			rel_dto.insRow(1);
			rel_dto.setDat("FAT_GID", 1, apm_gid);
			rel_dto.setDat("CHD_GID", 1, doc_gid);
			rel_dto.putDat();
			PsaDoc.setDocSty(ssn, doc_gid, "PSA_ROU_PLN");
		}
		return 0;
	}

	@Override
	protected PscDto crePrcObj(UsxPar usx_par, PscDto pro_dto, int pro_row) throws Exception
	{
		PscDto prc_dto =  super.crePrcObj(usx_par, pro_dto, pro_row);
		
		// set default questionnaire
		PscSsn ssn = pro_dto.getSsn();
		String qst_num = ssn.getEnv("PSA_PRC_OBJ_JSN_QST_QUE");
		if (prc_dto!=null && PscUti.isStr(qst_num)) {
			String qst_gid = PsaDtoIpl.getFldDat(ssn, "PSA_QST", "NUM", "'"+qst_num+"'", "PSC_GID", true);
			if (PscGid.isVld(qst_gid)) {
				prc_dto.setDat("QST_GID", 1, qst_gid);
				prc_dto.putDat();
			}
		}

		return prc_dto;
	}

	@Override
	protected PsaActCrePrcObj.PrcObjArtMap addPrcObjArt(UsxPar usx_par, 
														PscDto pro_dto, int pro_row, 
														PscDto prc_dto, int prc_row)
		throws Exception 
	{
		PscSsn ssn = prc_dto.getSsn();
		String pro_gid = pro_dto.getDat("PSC_GID", pro_row);
		PsaActCrePrcObj.PrcObjArtMap art_ext_key_map = new PsaActCrePrcObj.PrcObjArtMap();
		PscDto ntr_art_dto = ssn.newDto("PSA_NTR_ART");
		PscDto prj_art_dto = ssn.newDto("PSA_PRO_ART_REF");
		prj_art_dto.setQue("PRO_GID", pro_gid);
		int prj_art_cnt = prj_art_dto.fetDat();
		PscDto prc_art_dto = ssn.newDto(prc_dto, prc_row, "PRC_OBJ_ART_STR");
		for (int prj_art_row=1; prj_art_row<=prj_art_cnt; prj_art_row++) {
			// search neural product
			String ori_gid = prj_art_dto.getDat("PSA_ART.ORI_GID", prj_art_row);
			ntr_art_dto.setQue("PSC_GID", ori_gid);
			String ext_key;
			if (PscGid.isVld(ori_gid) && ntr_art_dto.fetDat()!=0) {
				ext_key = ntr_art_dto.getDat("EXT_KEY", 1);
			}
			else {
				ext_key = "###"+prj_art_row+"###";
			}
			
			String prj_art_gid = prj_art_dto.getDat("CHD_GID", prj_art_row);
			String art_str_gid = prj_art_dto.getDat("PSC_GID", prj_art_row);
			int new_row = prc_art_dto.numRec() + 1;
			prc_art_dto.insRow(new_row);
			prc_art_dto.setDat("CHD_GID", new_row, art_str_gid);
			String art[] = new String[]{art_str_gid, prj_art_gid};
			art_ext_key_map.put(new PsaActCrePrcObj.PrcObjArt(ext_key, art[0], art[1]));
		}
		if (prc_art_dto.chkMod()) {
			prc_art_dto.putDat();
		}
		return art_ext_key_map;
	}

	@Override
	protected PscDto addPrcObjHou(UsxPar usx_par, 
								  PsaActCrePrcObj.PrcObjArtMap art_ext_key_map,
								  Set<String> con_set,
								  PscDto pro_dto, int pro_row, 
								  PscDto prc_dto, int prc_row)
		throws Exception 
	{
		PscDto hou_dto = super.addPrcObjHou(usx_par, art_ext_key_map, con_set, pro_dto, pro_row, prc_dto, prc_row);
		int hou_cnt = hou_dto.numRec();
		if (hou_cnt>=1) {
			hou_dto.setDat("TYP_IDN", 1, "PSA_HOU_TYP_TRV");
			PsaDtoIpl.copMulLngCol(hou_dto, 1, "PSA_HOU_TYP", "TYP_IDN", "TYP_NAM");
		}
		if (hou_cnt>=2) {
			hou_dto.setDat("TYP_IDN", 2, "PSA_HOU_TYP_WRK");
			PsaDtoIpl.copMulLngCol(hou_dto, 2, "PSA_HOU_TYP", "TYP_IDN", "TYP_NAM");
		}
		if (hou_dto.chkMod()) {
			hou_dto.putDat();
		}
		return hou_dto;
	}

}