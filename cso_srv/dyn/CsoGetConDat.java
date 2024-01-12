// Last update by user CUSTOMIZER on host pc-weiland-07 at 20110823083736
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.com.*;
import de.pisa.psa.com.clp.*;
import de.pisa.psa.dto.*;
import de.pisa.psa.ifc.*;

/** CSO_GET_CON_DAT */
public class CsoGetConDat {

	public static void RUN(PscFrm frm, String par, Integer row) throws Exception
	{
		PscSsn ssn = frm.getSsn();
		PscDto dyn_dto = frm.getDynDto();
		
		String ctt = dyn_dto.getDat("CTT", row);
		ctt = ClbCnv.getTxt(ctt);
		PsaTxtDat dat = new PsaTxtDat(ctt, PsaTxtDat.FMT_TXT);
		if (dat.siz()==0) {
			return;
		}
		
		String sex_nam = dat.get("SEX_NAM");
		String nam = dat.get("NAM");
		String fst_nam = dat.get("FST_NAM");
		String ema = dat.get("EMA");
		String org_pos = dat.get("POS");
		String str_nam = dat.get("STR");
		String str_num = dat.get("STR_NUM");
		String zip = dat.get("ZIP");
		String cit = dat.get("CIT");
		String org = dat.get("ORG");
		String tel = dat.get("TEL");
		String cty = dat.get("CTY");
		
		Map<Integer, String> dat_map = new HashMap<Integer, String>();
		dat_map.put(FldConst.Street, str_nam+" "+str_num);
		dat_map.put(FldConst.City, cit);
		dat_map.put(FldConst.ZIP, zip);
		dat_map.put(FldConst.Country, cty);
		dat_map.put(FldConst.Surname, nam);
		dat_map.put(FldConst.Firstname, fst_nam);
		dat_map.put(FldConst.Dear, sex_nam);
		dat_map.put(FldConst.Phone, tel);
		dat_map.put(FldConst.Email, ema);
		dat_map.put(FldConst.Company, org);
		dat_map.put(FldConst.position, org_pos);
		ParMap par_map = new ParMap(dat_map);
		String clp_str = par_map.toString((char)1, false);
		
		ClpImp clp = new ClpImp();
		clp.setAquPls(PsaDtoIpl.getFldDat(ssn, "PSA_ACT_CLA", "IDN", "PSA_ACT_CLA_REQ", "NAM"));
		String prs_gid = clp.impPrsExt(ssn, clp_str);
		
		if (PscGid.isVld(prs_gid)) {
			dyn_dto.setDat("CON_GID", row, prs_gid);
			PsaDtoIpl.refCom(dyn_dto, dyn_dto.getFld("PSA_CLI_CON.FRN_IDN"), row);
			dyn_dto.putDat();
		}
	}
		
}