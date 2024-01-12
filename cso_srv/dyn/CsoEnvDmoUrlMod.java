// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20200623121213
import java.net.*;
import java.util.regex.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

/**
 * CSO_ENV_DMO_URL_MOD
 * 
 * @since 20.06.2018
 * @author weiland
 */
public class CsoEnvDmoUrlMod {

	private static final Pattern PAT_URL = Pattern.compile("(https?://)([^/:]+)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PAT_DOK_HST = Pattern.compile("psadmo([0-9]{2})", Pattern.CASE_INSENSITIVE);

	public static void run(PscSsn ssn, String par) throws Exception
	{
		String loc_hst = InetAddress.getLocalHost().getHostName();
		String srv_hst;
		String web_hst;
		String dbs_hst;
		Matcher mat = PAT_DOK_HST.matcher(loc_hst);
		if (mat.find()) {
			String num = mat.group(1);
			srv_hst = "psadmo"+num;
			web_hst = "webdmo"+num;
			dbs_hst = "oradmo"+num;
		}
		else {
			srv_hst = loc_hst;
			web_hst = loc_hst;
			dbs_hst = loc_hst;
		}
		UsxPar usx_par = new UsxPar(par);
		String srv_env = usx_par.getPar("ENV");
		run(ssn, srv_env, srv_hst);
		String web_env = usx_par.getPar("ENV_WEB");
		run(ssn, web_env, web_hst);
		String dbs_env = usx_par.getPar("ENV_DBS");
		run(ssn, dbs_env, dbs_hst);
	}
	
	private static void run(PscSsn ssn, String env, String hst) throws Exception
	{
		String env_lis[] = PsaUti.strSplit(env, " ,;");
		JobLog log = JobThr.getJobLog(CsoEnvDmoUrlMod.class);
		PscDto env_dto = ssn.newDto("PSC_ENV");
		for (String nam : env_lis) {
			StringBuilder log_str = new StringBuilder();
			log_str.append(nam).append('\n');
			env_dto.setQue("NAM", "'"+nam+"'");
			try {
				while (env_dto.fetNxt()) {
					if (env_dto.getDat("GRP", 1).isEmpty() && env_dto.getDat("USR", 1).isEmpty()) {
						continue;
					}
					String dat_old = env_dto.getDat("DAT", 1);
					log_str.append(dat_old).append(" -> ");
					Matcher mat = PAT_URL.matcher(dat_old);
					if (mat.find()) {
						String dat_new = mat.replaceFirst("$1"+hst+"$3");
						if (dat_new.equals(dat_old)) {
							log_str.append("OK");
						}
						else {
							log_str.append(dat_new);
							String env_gid = env_dto.getDat("PSC_GID", 1);
							PsaUti.updFldDat(env_dto, "DAT", env_gid, dat_new);
						}
					}
					log_str.append('\n');
				}
			}
			finally {
				env_dto.fetCls();
			}
			log.logNfo(log_str.toString());
		}
		PscChe.delEnv();
	}

}
