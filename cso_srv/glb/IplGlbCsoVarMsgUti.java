// Last Update by user CUSTOMIZER at 20200922085408

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.pisa.psa.dto.psa_prs.PsaPrsInt;
import de.pisa.psa.ifc.PsaUti;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.dto.PscFld;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.svc.LamUti;
import de.pisa.psc.srv.svc.PscUti;
import de.pisa.psc.srv.sys.PscDtoEnv;

/** CSO_VAR_MSG_UTI 
 * 
 * @author geger
 * */

public interface IplGlbCsoVarMsgUti {

	static final Pattern PAT = Pattern.compile("(<<#([^<<#]*?))?<<#(.*?)#>>(([^#>>]*?)#>>)?");
	
	

	static String getVarMsg(PscSsn ssn, String msg_nam, String prs_gid, String dto_dsc, String obj_gid) {
		try {
			PscDto dto = IplDlgCsoUti.newDto(ssn, dto_dsc, obj_gid);
			return getVarMsg(ssn, msg_nam, prs_gid, dto, 1);
		} catch (Exception e) {
			return null;
		}
	}
	
	static String getVarMsg (PscSsn ssn, String msg_nam, String prs_gid, PscDto dto, int row) throws Exception {
		if (dto == null || row < 1 || row > dto.numRec()) {
			return null;
		}
//		String raw_msg = getEnvMsgRaw(ssn, msg_nam, prs_gid);
		String lng_str = PsaPrsInt.getLng(ssn, prs_gid);
		if (!PscUti.isStr(lng_str)) {
			lng_str = "GER";
		}
		String raw_msg = Priv.getMsgRaw(ssn, msg_nam, prs_gid);
		String msg = rplMsgVar(raw_msg, dto, row, lng_str);
		return msg;
	}
	
	static String rplMsgVar(String raw_msg, PscDto dto, int row, String lng_str) throws Exception {
		return rplMsgVar(raw_msg, dto, row, lng_str, null, null);
	}
	
	static String rplMsgVar(String raw_msg, PscDto dto, int row, String lng_str, String beg_mar, String end_mar) throws Exception {
		return rplMsgVar(raw_msg, dto, row, lng_str, beg_mar, end_mar, null);
	}
	
	static String rplMsgVar(String raw_msg, PscDto dto, int row, String lng_str, String beg_mar, String end_mar, LamUti.Function_WithExceptions<String, String> map_fnc) throws Exception {
		if (!PscUti.isStr(raw_msg)) {
			return null;
		}
		if (dto == null || row < 1 || row > dto.numRec()) {
			return null;
		}
		// convert custom begin and end marker if available
		if (PscUti.isStr(beg_mar) && PscUti.isStr(end_mar)) {
			raw_msg = raw_msg.replace(beg_mar, "<<#");
			raw_msg = raw_msg.replace(end_mar, "#>>");
		}
		Matcher mat = PAT.matcher(raw_msg);
		StringBuffer sb = new StringBuffer();
		while (mat.find()) {
			String mid_str = Priv.evlMidStr(mat.group(3), dto, row, lng_str);
			String pre_str = mid_str == null || mat.group(2) == null ? "" : mat.group(2);
			String pst_str = mid_str == null || mat.group(5) == null ? "" : mat.group(5);
			String fin_rpl_str = mid_str == null ? "" : pre_str + mid_str + pst_str;
			if (fin_rpl_str.indexOf('$') >= 0) {
				return null;
			}
			if (map_fnc != null) {
				fin_rpl_str = map_fnc.apply(fin_rpl_str);
			}
			mat.appendReplacement(sb, fin_rpl_str);
		}
		mat.appendTail(sb);
		String msg = sb.toString();
		// convert back
		if (PscUti.isStr(beg_mar) && PscUti.isStr(end_mar)) {
			msg = msg.replace("<<#", beg_mar);
			msg = msg.replace("#>>", end_mar);
		}
		return msg;
	}

	
	
	interface Priv {
		enum MsgVarMac {
			DTO_CLS_TIT
		}
		
		static String getMsgRaw(PscSsn ssn, String msg_nam, String prs_gid) {
			String msg = PsaUti.getLngMsg(ssn, prs_gid, msg_nam);
			if (!PscUti.isStr(msg)) {
				return null;
			}
			return msg;
		}
		
		static String evlMidStr(String mid_str, PscDto dto, int row, String lng_str) throws Exception {
			String ret = null;
			if (!PscUti.isStr(mid_str)) {
				return null;
			}
			if (mid_str.startsWith("@")) {
				ret = evlMac(mid_str.substring(1), dto, row, lng_str);
			} else {
				PscDto use_dto_lvl = mid_str.contains(".") ? dto : dto.getBas();
				ret = IplDlgCsoUti.getLngDat(mid_str, use_dto_lvl, row, lng_str);
			}
			return ret;
		}

		

		static String evlMac(String mac_str, PscDto dto, int row, String lng_str) throws Exception {
			PscSsn ssn = dto.getSsn();
			MsgVarMac mac = MsgVarMac.valueOf(mac_str);
			String ret = null;
			switch (mac) {
			case DTO_CLS_TIT:
				PscFld cls_fld = dto.getFld("PSC_CLS");
				if (cls_fld == null) {
					return null;
				}
				String cls_nam = dto.getDat(cls_fld, row);
				if (!PscUti.isStr(cls_nam)) {
					return null;
				}
				String cls_tit = PsaUti.getCheFldVal(ssn, "PSC_DTO", "NAM", "'"+cls_nam+"'", "TIT_" + lng_str);
				if (!PscUti.isStr(cls_tit)) {
					return null;
				}
				if (cls_tit.startsWith("$")) {
					String tit_env_nam = cls_tit.substring(1);
					if (!PscUti.isStr(tit_env_nam)) {
						return null;
					}
					cls_tit = PsaUti.getEnvForLng(ssn, tit_env_nam, lng_str);
				}
				ret = cls_tit;
				break;
				
			}
			return ret;
		}
		
		

		/**
		 * maybe will be used later -> dont delete
		 */
		@Deprecated
		static String getEnvMsgRaw (PscSsn ssn, String nam, String prs_gid) throws Exception {
			String msg_key = IplDlgCsoUti.getUsrEnvUseCheSavNul(ssn, nam);
			if (!PscUti.isStr(msg_key)) {
				return null;
			}
			String msg = PsaUti.getLngMsg(ssn, prs_gid, msg_key);
			if (!PscUti.isStr(msg)) {
				return null;
			}
			// ......
			return msg;
		}
	}
	
}

