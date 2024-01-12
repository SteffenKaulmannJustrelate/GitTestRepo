// Last update by user CUSTOMIZER on host dmoref62 at 20140401121813
import de.pisa.psa.ifc.tst_fnc.*;
import de.pisa.psc.srv.gui.*;
// Last update by user CUSTOMIZER on host dmoref62 at 20140401121810
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.svc.*;
import de.pisa.psc.srv.sys.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.frm.psa_qst.*;
import de.pisa.psa.ifc.*;
import de.pisa.psa.ssn.*;

/**
 * Test function: Unused sub dialogs
 * @since 03.02.2014
 * @author weiland
 */
public class TstNonUsdSub extends PsaAppTstFnc {

	/** set with dialog names to be ignored */
	protected final Set<String> Ign_Dlg_Set;

public void run(PscGui gui) throws Exception
{
new TstNonUsdSub(gui.getSsn()).exeFnc();
}

public TstNonUsdSub() throws Exception
{
super(null);
Ign_Dlg_Set = null;
}

	public TstNonUsdSub(PscSsn ssn) throws Exception
	{
		super(ssn);
		
		Ign_Dlg_Set = new HashSet<String>();
		iniIgnDlgSet();
	}

	/**
	 * initialize dialog ignore set
	 * @throws Exception
	 */
	protected void iniIgnDlgSet() throws Exception
	{
		addIgnDlgSet("PSA_RPT_TRE");
		addIgnDlgSet("PSA_RPT_TRE_DLG");
		addIgnDlgSet("PSA_EPT_ACT_ESC");
		addIgnDlgSet("PSA_SAP_DOC_REF_ESB");
		addIgnDlgSet("PSA_SAP_DOC_DOC_REF_ESB");
		addIgnDlgSet("PSA_CLA_ELM_ACI");
		addIgnDlgSet("PSA_CLA_ELM_SCI");
	}

	/**
	 * add dialog to ignore set
	 * @param dlg_dsc dialog descriptor
	 * @throws Exception
	 */
	protected void addIgnDlgSet(String dlg_dsc) throws Exception
	{
		dlg_dsc = getDlgLnk(dlg_dsc);
		if (PscUti.isStr(dlg_dsc)) {
			Ign_Dlg_Set.add(dlg_dsc);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see de.pisa.psa.ifc.tst_fnc.PsaAppTstFnc#exeFnc()
	 */
	@Override
	public boolean exeFnc() throws Exception
	{
		boolean ret = true;
		PscSsn ssn = getSsn();

		// Fetch all dialogs
		PscDto dlg_dto = ssn.newDto("PSC_DLG");
		dlg_dto.setQue("PSC_OWN", "!0");
		try {
			while (dlg_dto.fetNxt()) {
				String dlg = null;
				try {
					dlg = dlg_dto.getDat("NAM", 1);
					if (ignDlg(dlg_dto)) {
						continue;
					}
					ret &= chkDlg(dlg_dto);
				}
				catch (Exception exc) {
					PsaSsn.wriTxtImm(ssn, dlg+": "+PsaUti.getStkTrc(exc));
				}
			}
		}
		finally {
			dlg_dto.fetCls();
		}
		return ret;
	}

	/**
	 * check dialog
	 * @param dlg_dto dialog dto
	 * @return true if no errors
	 * @throws Exception
	 */
	protected boolean chkDlg(PscDto dlg_dto) throws Exception
	{
		boolean ret = true;
		PscSsn ssn = getSsn();
		String dlg_nam = dlg_dto.getDat("NAM", 1);
		
		StringBuilder msg = new StringBuilder();
		Map<String, Boolean> tab_set = null;
		PscDto sub_dto = ssn.newDto("PSC_SUB");
		sub_dto.setQue("DLG", "'" + dlg_nam + "'");
		try {
			while (sub_dto.fetNxt()) {
				if (tab_set==null) {
					tab_set = getTabSet(dlg_dto);
				}

				String sub_nam = sub_dto.getDat("NAM", 1);
				sub_nam = getDlgLnk(sub_nam);
				if (chkSub(tab_set, sub_nam)) {
					continue;
				}

				boolean chg_vie = false;
				for (Map.Entry<String, Boolean> ent : tab_set.entrySet()) {
					if (ent.getValue()) {
						String tab_nam = ent.getKey();
						Set<String> chg_set = getChgVie(tab_nam);
						if (chg_set.contains(sub_nam)) {
							chg_vie = true;
							break;
						}
					}
				}
				if (chg_vie) {
					continue;
				}

				if (msg.length()==0) {
					msg.append(dlg_nam);
					msg.append(" : ");
				}
				if (!ret) {
					msg.append(", ");
				}
				msg.append(sub_nam);
				ret = false;
			}
		}
		finally {
			sub_dto.fetCls();
		}
		if (msg.length()>0) {
			PsaSsn.wriTxtImm(ssn, msg.toString());
		}
		return ret;
	}

	/**
	 * check if sub dialog is used
	 * @param tab_set set with tabs
	 * @param sub_nam sub dialog name to check
	 * @return true if sub dialog is used
	 * @throws Exception
	 */
	protected boolean chkSub(Map<String, Boolean> tab_set, String sub_nam)
		throws Exception
	{
		if (PscUti.isStr(sub_nam) && tab_set.containsKey(sub_nam)) {
			return true;
		}
		return false;
	}

	/**
	 * get set with tabs
	 * @param dlg_dto dialog dto
	 * @return set with sub dialog names
	 * @throws Exception
	 */
	protected Map<String, Boolean> getTabSet(PscDto dlg_dto) throws Exception
	{
		PscSsn ssn = getSsn();
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		Set<String> tab_set = new HashSet<String>();
		PscDtoTag tag_dto = (PscDtoTag)ssn.newDto(dlg_dto, 1, "DLG_TAG");
		PscFld typ_fld = tag_dto.getFld("TYP");
		PscFld nam_fld = tag_dto.getFld("NAM");
		PscFld par_fld = tag_dto.getFld("PAR");
		PscFld ref_fld = tag_dto.getFld("REF");
		PscFld fnc_fld = tag_dto.getFld("FNC");
		tag_dto.setTreFlg(true);
		tag_dto.setMax(Integer.MAX_VALUE-1);
		int tag_cnt = tag_dto.fetDat();
		for (int tag_row=1; tag_row<=tag_cnt; tag_row++) {
			String typ = tag_dto.getDat(typ_fld, tag_row);
			String par = tag_dto.getDat(par_fld, tag_row);
			String nam = tag_dto.getDat(nam_fld, tag_row);
			if (typ.equals("TAB")) {
				if (par.isEmpty()) {
					tab_set.add(nam);
				}
				else {
					ret.put(getDlgLnk(par), true);
				}
			}
			else if (typ.equals("DLG")) {
				if (!par.isEmpty()) {
					ret.put(getDlgLnk(par), true);
				}
			}
		}
		for (int tag_row=1; tag_row<=tag_cnt; tag_row++) {
			String typ = tag_dto.getDat(typ_fld, tag_row);
			String par = tag_dto.getDat(par_fld, tag_row);
			String ref = tag_dto.getDat(ref_fld, tag_row);
			String nam = tag_dto.getDat(nam_fld, tag_row);
			String fnc = tag_dto.getDat(fnc_fld, tag_row);
			if ( typ.equals("TXT") && !par.isEmpty()) {
				if (!ref.isEmpty() && tab_set.contains(ref)) {
					ret.put(getDlgLnk(par), true);
				}
				else if (nam.equals("SEL_ACT_CLA_VIE") ||
						 nam.equals("SEL_ART_VIE") ||
						 nam.equals("SEL_DOC_VIE"))
				{
					UsxPar usx_par = new UsxPar(par);
					String tre = usx_par.getPar("TRE");
					if (!tre.isEmpty()) {
						ret.put(getDlgLnk(tre), false);
					}
					String lis = usx_par.getPar("LIS");
					if (!lis.isEmpty()) {
						ret.put(getDlgLnk(lis), false);
					}
				}
			}
			if (typ.equals("TXT") || typ.equals("SEL")) {
				if (fnc.startsWith("PSA_CHG_VIE")) {
					String dlg = new UsxPar(par).getPar("DLG_DSC");
					if (!dlg.isEmpty()) {
						ret.put(getDlgLnk(dlg), false);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * gte linked dialog descriptor if linked
	 * @param dlg_dsc dialog descriptor
	 * @return dialog descriptor
	 * @throws Exception
	 */
	protected String getDlgLnk(String dlg_dsc) throws Exception
	{
		if (!PscUti.isStr(dlg_dsc)) {
			return "";
		}
		PscSsn ssn = getSsn();
		String lnk = ssn.getLnk(dlg_dsc, "DLG");
		return (lnk==null) ? dlg_dsc : lnk;
	}

	/**
	 * get PSA_CHG_VIE sub dialogs
	 * @param dlg_dsc dialog descriptor
	 * @return set with dialog descriptors
	 * @throws Exception
	 */
	protected Set<String> getChgVie(String dlg_dsc) throws Exception
	{
		PscSsn ssn = getSsn();
		dlg_dsc = getDlgLnk(dlg_dsc);
		Set<String> ret = new HashSet<String>();
		if (!PscUti.isStr(dlg_dsc)) {
			return ret;
		}
		PscDto dlg_dto = ssn.newDto("PSC_DLG");
		dlg_dto.setQue("NAM", dlg_dsc);
		if (dlg_dto.fetDat()==0) {
			return ret;
		}
		PscDtoTag tag_dto = (PscDtoTag)ssn.newDto(dlg_dto, 1, "DLG_TAG");
		PscFld typ_fld = tag_dto.getFld("TYP");
		PscFld par_fld = tag_dto.getFld("PAR");
		PscFld fnc_fld = tag_dto.getFld("FNC");
		tag_dto.setTreFlg(true);
		tag_dto.setMax(Integer.MAX_VALUE-1);
		int tag_cnt = tag_dto.fetDat();
		for (int tag_row=1; tag_row<=tag_cnt; tag_row++) {
			String typ = tag_dto.getDat(typ_fld, tag_row);
			String par = tag_dto.getDat(par_fld, tag_row);
			String fnc = tag_dto.getDat(fnc_fld, tag_row);
			if (typ.equals("TXT") || typ.equals("SEL")) {
				if (fnc.startsWith("PSA_CHG_VIE")) {
					String dlg = new UsxPar(par).getPar("DLG_DSC");
					if (!dlg.isEmpty()) {
						ret.add(getDlgLnk(dlg));
					}
				}
			}
		}
		return ret;
	}

	/**
	 * ignore dialog?
	 * @param dlg_dto dialog dto
	 * @return true if dialog should be ignored
	 * @throws Exception
	 */
	protected boolean ignDlg(PscDto dlg_dto) throws Exception
	{
		String dlg_nam = dlg_dto.getDat("NAM", 1);
		dlg_nam = getDlgLnk(dlg_nam);
		if (Ign_Dlg_Set.contains(dlg_nam)) {
			return true;
		}
		if (dlg_nam.startsWith("PSA_RPT_CFG_")) {
			return true;
		}
		if (PsaQstApp.isGenQstDlg(dlg_nam)) {
			return true;
		}
		return false;
	}

}