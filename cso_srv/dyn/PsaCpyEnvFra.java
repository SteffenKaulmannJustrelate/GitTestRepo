// Last update by user CUSTOMIZER on host dmoref62beta at 20130524081244
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;

import de.pisa.psa.ifc.*;

/** PSA_CPY_ENV_FRA */
public class PsaCpyEnvFra {
	
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		Map<String, String> map = getMap(ssn, "GER", "FRA", "PSA_FMT_%");
		ssn.wriTxt("PSA_FMT: "+map.size());
		for (String nam : map.keySet()) {
			ssn.wriTxt(nam);
		}
		cop(ssn, map, "FRA");
		map = getMap(ssn, "ENG", "FRA", "");
		ssn.wriTxt(""+map.size());
		for (String nam : map.keySet()) {
			ssn.wriTxt(nam);
		}
		cop(ssn, map, "FRA");
	}
	
	private Map<String, String> getMap(PscSsn ssn, String lng_src, String lng_dst, String nam_que) 
		throws Exception 
	{
		Map<String, String> ret = new HashMap<String, String>();
		PscDto env_dto = ssn.newDto("PSC_ENV_GLB");
		env_dto.setQue("NAM", nam_que);
		env_dto.setQue("LNG", lng_src);
		// get german
		FetMor env_fet = new FetMor(env_dto);
		do {
			int env_cnt = env_fet.fetDat();
			for (int env_row=1; env_row<=env_cnt; env_row++) {
				String nam = env_dto.getDat("NAM", env_row);
				String gid = env_dto.getDat("PSC_GID", env_row);
				ret.put(nam, gid);
			}
		} while (env_fet.fetMor());
		// get french
		env_dto.setQue("LNG", lng_dst);
		env_fet = new FetMor(env_dto);
		do {
			int env_cnt = env_fet.fetDat();
			for (int env_row=1; env_row<=env_cnt; env_row++) {
				String nam = env_dto.getDat("NAM", env_row);
				ret.remove(nam);
			}
		} while (env_fet.fetMor());
		return ret;
	}
	
	private void cop(PscSsn ssn, Map<String, String> map, String lng) throws Exception
	{
		PscDto env_dto = ssn.newDto("PSC_ENV_GLB");
		for (String ger_gid : map.values()) {
			env_dto.setQue("PSC_GID", ger_gid);
			env_dto.fetDat();
			env_dto.insRow(2);
			env_dto.copRow(2, env_dto, 1);
			env_dto.setDat("GRP", 2, "0");
			env_dto.setDat("LNG", 2, lng);
			env_dto.putDat();
		}
	}
}