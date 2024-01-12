// Last update by user CUSTOMIZER on host dmoref at 20171213140835
import java.util.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_ifc.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

/** CSO_EVT_CRE_SAV_QUE */
public class CsoEvtCreSavQue {

	private final PscSsn Ssn;
	private final PscGui Gui;
	private final PscFrm Frm;
	private final PscDto Dto;
	private final int Row;
	private final UsxPar Par;
	private final String Lis_Nam_Fmt;
	private final String Lis_Cmt_Fmt;	
	private final String Pst_Msg_Fmt;	
	
	public CsoEvtCreSavQue(PscFrm frm, String par, Integer row)
	{
		Ssn = frm.getSsn();
		Gui = frm.getGui();
		Frm = frm;
		Dto = frm.getDynDto();
		Row = row;
		Par = new UsxPar(par);
		
		Lis_Nam_Fmt = "Generierte Leads aus der Veranstaltung \"%s\" für \"%s\"";
		Lis_Cmt_Fmt = "Status: %s";
		Pst_Msg_Fmt = "@%s Neue Ad-hoc-Liste angelegt: %s";
	}
	
	public static void RUN(PscFrm frm, String par, Integer row) throws Exception
	{
		new CsoEvtCreSavQue(frm, par, row).run();
	}
	
	public void run() throws Exception
	{
		String pro_gid = Dto.getDat("PSA_PRO.PSC_GID", Row);
		if (!PscGid.isVld(pro_gid)) {
			return;
		}
		
		// get status
		String sta_idn = null;
		if (Par.getPar("STA_SEL", true)) {
			sta_idn = chcSta();
		}
		else {
			sta_idn = getOriSta();
		}
		if (!PscUti.isStr(sta_idn) && !Par.getPar("STA_ALL", false)) {
			return;
		}

		// get contacts
		Map<String, String> cre_nam_map = new HashMap<>();
		MapToSet<String, String> cre_con_map = new MapToSet<>();
		PscDto pro_con_dto = Ssn.newDto("PSA_PRO_CON_EXT_REF");
		pro_con_dto.setQue("FAT_GID", pro_gid);
		pro_con_dto.setQue("STA_IDN", sta_idn);
		try {
			while (pro_con_dto.fetNxt()) {
				String con_gid = pro_con_dto.getDat("CHD_GID", 1);
				String cre_prs_gid = pro_con_dto.getDat("PSA_CRE_PRS.PSC_GID", 1);
				if (!PscGid.isVld(cre_prs_gid)) {
					cre_prs_gid = "";
				}
				String cre_prs_nam = pro_con_dto.getDat("PSA_CRE_PRS.CMP_NAM", 1);
				cre_nam_map.putIfAbsent(cre_prs_gid, cre_prs_nam);
				cre_con_map.add(cre_prs_gid, con_gid);
			}
		}
		finally {
			pro_con_dto.fetCls();
		}
		
		// create lists
		String sta_nam = PsaDtoIpl.getFldDat(Ssn, "PSA_PRO_CON_STA", "IDN", "'"+sta_idn+"'", "NAM");
		List<String> sav_que_lis = new ArrayList<>();
		for (Map.Entry<String, String> cre_ent : cre_nam_map.entrySet()) {
			String con_gid = cre_ent.getKey();
			Set<String> con_set = cre_con_map.get(con_gid);
			if (con_set.isEmpty()) {
				continue;
			}
			String lis[] = creLis(cre_ent.getValue(), sta_nam);
			String lis_gid = lis[0];
			String lis_idn = lis[1];
			String lis_nam = lis[2];
			sav_que_lis.add(lis_gid);
			addLisCon(lis_idn, con_set);
			// post pin board message
			pstMsg(con_gid, lis_nam);
		}
		
		// open lists
		opnLis(sav_que_lis);
	}
	
	private String getOriSta() throws Exception
	{
		String sta_ori_idn = Par.getPar("STA_IDN");
		if (sta_ori_idn.isEmpty()) {
			return null;
		}
		String pro_gid = Dto.getDat("PSA_PRO.PSC_GID", Row);
		PscDto sta_dto = Ssn.newDto("PSA_PRO_CON_STA");
		sta_dto.setQue("PRO_GID", pro_gid);
		sta_dto.setQue("ORI_IDN", "'"+sta_ori_idn+"'");
		return sta_dto.fetFst() ? sta_dto.getDat("IDN", 1) : null;
	}
	
	private String chcSta() throws Exception
	{
		String pro_gid = Dto.getDat("PSA_PRO.PSC_GID", Row);
		PsaChcUti chc = new PsaChcUti(Gui, "PSA_PRO_CON_STA_CHC_ESC", null);
		chc.setXbm(chc_frm->{
			chc_frm.setFixQue("PRO_GID", pro_gid);
			chc_frm.fetDat();
		});
		chc.opn();
		int chc_row = chc.getSelRow();
		if (chc_row<=0) {
			return null;
		}
		PscDto chc_dto = chc.getChcDto();
		return chc_dto.getDat("IDN", chc_row);
	}

	private String[] creLis(String con_nam, String sta_nam) throws Exception
	{
		String pro_nam = Dto.getDat("PSA_PRO.NAM", Row);
		PscDto sav_que_dto = Ssn.newDto("PSA_SAV_QUE_BAS");
		String lis_nam = String.format(Lis_Nam_Fmt, pro_nam, con_nam);
		String lis_cmt = PscUti.isStr(sta_nam) ? String.format(Lis_Cmt_Fmt, sta_nam) : "";
		sav_que_dto.insRow(1);
		PsaDtoIpl.setDatMax(sav_que_dto, "NAM", 1, lis_nam);
		PsaDtoIpl.setDatMax(sav_que_dto, "CMT", 1, lis_cmt);
		sav_que_dto.putDat();
		String gid = sav_que_dto.getDat("PSC_GID", 1);
		String idn = sav_que_dto.getDat("QUE_IDN", 1);
		return new String[]{gid, idn, lis_nam};
	}
	
	private void addLisCon(String que_idn, Collection<String> con_col) throws Exception
	{
		if (con_col.isEmpty()) {
			return;
		}
		PscDto dst_dto = Ssn.newDto("PSA_SAV_QUE_CON");
		dst_dto.getFld("QUE_IDN").setDfv(que_idn);
		dst_dto.insRow(1, con_col.size());
		int row = 0;
		for (String con_gid : con_col) {
			row++;
			dst_dto.setDat("QUE_DTO_GID", row, con_gid);
		}
		dst_dto.putDat();
	}

	private void opnLis(List<String> gid_lis) throws Exception
	{
		if (gid_lis.isEmpty()) {
			return;
		}
		String gid_que = PsaUti.lis2str(gid_lis, " ");
		PscFrm frm = Gui.newFrm("PSA_SAV_QUE_BAS_ESC");
		frm.getDynDto().setQue("PSC_GID", gid_que);
		frm.fetDat();
	}
	
	private void pstMsg(String rcp_gid, String lis_nam) throws Exception
	{
		if (!PscGid.isVld(rcp_gid)) {
			return;
		}
		PsaPinBrd pin_brd = new PinBrd(Ssn);
		pin_brd.setPinBrdFrm(Frm);
		String rcp_nam = PsaUti.getCheFldVal(Ssn, "PSA_PRS_INT_XTD", "PSC_GID", rcp_gid, "PSC_USR.NAM");
		String msg = String.format(Pst_Msg_Fmt, rcp_nam, lis_nam);
		pin_brd.pstMsg(Dto, Row, "PIN_BRD", msg);
	}
	
	private static class PinBrd extends PsaPinBrd
	{

		public PinBrd(PscSsn ssn) {
			super(ssn);
		}
		
		/**
		 * {@inheritDoc}
		 * @see de.pisa.psa.dto.psa_ifc.PsaPinBrd#askAddAtPrs(de.pisa.psc.srv.dto.PscDto, int)
		 */
		@Override
		protected void askAddAtPrs(PscDto dto, int row) throws Exception {
			// Don't add persons
		}
		
	}
	
}