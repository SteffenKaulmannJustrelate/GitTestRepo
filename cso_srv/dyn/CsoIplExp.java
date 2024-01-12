// Last update by user CUSTOMIZER on host WSAMZN-GCNRJ0KM at 20230213125109
import java.util.*;

import de.pisa.psa.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;
import de.pisa.psc.srv.sys.*;

/** CSO_IPL_EXP */
public class CsoIplExp {

	private static final String PSC_OWN_QUE = "!0 !45 !58 !60"; // !SYSTEM !PSA !PSI !PSA_VOC
	
	public static void RUN(PscGui gui, String par) throws Exception {
		new CsoIplExp().run(gui);
	}

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		String cso_exp_nam = ssn.getEnv("PSC_CST_PFX", null);
		cso_exp_nam = cso_exp_nam.toLowerCase();
		cso_exp_nam = cso_exp_nam.concat("_srv");
		String dir = gui.selDir(null);
		if (!PscUti.isStr(dir)) {
			return;
		}

		Set<String> dir_exs = new HashSet<>();
		{ // PSC_IPL
			PscDto ipl_dto = ssn.newDto("PSC_IPL");
			ipl_dto.getDbd().setFetClb(true);
			ipl_dto.setQue("SRC", "!''");
			ipl_dto.setQue("PSC_OWN", PSC_OWN_QUE);
			try {
				while (ipl_dto.fetNxt()) {
					String nam = ipl_dto.getDat("NAM", 1);
					String src = ipl_dto.getDat("SRC", 1);
					String typ = ipl_dto.getDat("TYP", 1).toLowerCase();
					String pck = PscUsx.getClsPck(src);
					nam = PscDtoIpl.getClsNam(ipl_dto, 1) + ".java";
					String dst_dir = dir + '\\' + cso_exp_nam + '\\' +typ;
					if (!pck.isEmpty()) {
						dst_dir += '\\' + pck.replace('.', '\\');
					}
					if (!dir_exs.contains(dst_dir)) {
						gui.creDir(dst_dir);
						dir_exs.add(dst_dir);
					}
					String fil = dst_dir + '\\' + nam;
					gui.putFil(fil, "wb", src.getBytes());
				}
			}
			finally {
				ipl_dto.fetCls();
			}
		}
		{ // PSC_RUL
			PscDto ipl_dto = ssn.newDto("PSC_RUL");
			ipl_dto.getDbd().setFetClb(true);
			ipl_dto.setQue("SRC", "!''");
			ipl_dto.setQue("PSC_OWN", PSC_OWN_QUE);
			try {
				while (ipl_dto.fetNxt()) {
					String nam = ipl_dto.getDat("NAM", 1);
					String src = ipl_dto.getDat("SRC", 1);
					String typ = ipl_dto.getDat("TYP", 1).toLowerCase();
					nam = PscUsx.creNam(nam) + ".java";
					String dst_dir = dir + '\\' + typ;
					if (!dir_exs.contains(dst_dir)) {
						gui.creDir(dst_dir);
						dir_exs.add(dst_dir);
					}
					String fil = dst_dir + '\\' + nam;
					gui.putFil(fil, "wb", src.getBytes());
				}
			}
			finally {
				ipl_dto.fetCls();
			}
		}
		{ // PSA_DYN_JAV
			PscDto jav_dto = ssn.newDto("PSA_DYN_JAV");
			jav_dto.getDbd().setFetClb(true);
			jav_dto.setQue("COD", "!''");
			jav_dto.setQue("PSC_OWN", PSC_OWN_QUE);
			boolean cre_dir = true;
			try {
				while (jav_dto.fetNxt()) {
					if (cre_dir) {
						gui.creDir(dir + "\\" + cso_exp_nam + "\\dyn");
						cre_dir = false;
					}
					String nam = jav_dto.getDat("NAM", 1);
					String cod = jav_dto.getDat("COD", 1);
					String pck = PscUsx.getClsPck(cod);
					nam = PsaSysClf.creNam(nam) + ".java";
					String dst_dir = dir + '\\' + cso_exp_nam + "\\dyn";
					if (!pck.isEmpty()) {
						dst_dir += '\\' + pck.replace('.', '\\');
					}
					if (!dir_exs.contains(dst_dir)) {
						gui.creDir(dst_dir);
						dir_exs.add(dst_dir);
					}
					String fil = dst_dir + "\\" + nam;
					gui.putFil(fil, "wb", cod.getBytes());
				}
			}
			finally {
				jav_dto.fetCls();
			}
		}
	}

}