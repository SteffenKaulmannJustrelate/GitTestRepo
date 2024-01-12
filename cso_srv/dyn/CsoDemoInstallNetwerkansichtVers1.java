// Last update by user CUSTOMIZER on host WSAMZN-GCNRJ0KM at 20230213123421
import java.nio.file.*;

import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.dto.psa_ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.sio.*;
import de.pisa.psc.srv.sys.*;
import de.pisa.psc.srv.xml.*;

/** CSO_DEMO_INSTALL_NETWERKANSICHT_VERS_1 */
public class CsoDemoInstallNetwerkansichtVers1 {

	static String MAI_DIR = "P:\\PISAsales_cubes\\Utilities\\DEMO_INSTALL_NETWERKANSICHT_VERS_1";
	static String OGE_DIR = "P:\\geger\\uti";
	
	PscGui GUI = null;
	PscSsn SSN = null;
	
	public void run(PscGui gui) throws Exception {
		SSN = gui.getSsn();
		GUI = gui;
		if (SSN.getDdb().equals("jdbc:oracle:thin:@sidious:1521:pisadb") && "PSATST_DATA".equals(SSN.getDbi().getTspNam())) {
			throw new RuntimeException("This is the source system for this utlity. You can not install here.");
		}
		String int_psa_pth = "P:\\PISAsales_cubes";
		if (MAI_DIR.startsWith(int_psa_pth) && !isPthAvl(int_psa_pth)) {
			GUI.wriBox("Internal network drive 'P' seems not availible. \\n"
					+ "If you are in PiSA's internal network: Just click once on P in a file explorer. Then restart the installation. \\n"
					+ "If you are NOT in PiSA's internal network: Copy all needed files from network drive 'P' to your machine. Modify the variables 'OGE_DIR' and 'MAI_DIR' of this java class to access the needed files.", "W", "W");
			throw new PscExc(PscOut.LEV_MSG, "Aborted...");
		}
		chkPthAvl(MAI_DIR);
		chkPthAvl(OGE_DIR);
		if (isFeaIns()) {
			if (!"Y".equalsIgnoreCase(GUI.wriBox("It seems the feature was already installed. So you are going to update it. "
					+ "Please make sure that currently no real user works with the graph, because the DTO CSO_GVZ_GRA "
					+ "will be recreated. \\n"
					+ "Continue?", "Q", "Q"))) {
				throw new PscExc(PscOut.LEV_MSG, "Aborted...");
			}
		}
		String jars_pth = MAI_DIR + "\\jars";
		if (!"O".equalsIgnoreCase(GUI.wriBox("Did you already put all needed jars on classpath? \\n"
				+ "All jars from here need to be on classpath: "+jars_pth+" \\n\\n"
				+ "If not already done: Click 'Cancel', put them on classpath (they will be opened now), restart the server and restart the installation. \\n\\n"
				+ "If done: Click 'Ok'.", "Q", "E"))) {
			try {
				SSN.wriTxt(EXAMPLE_CLS_PTH_CNF);
				savStrAsTmpFil(EXAMPLE_CLS_PTH_CNF, true, gui, "example_for_serv_conf.txt");
				GUI.runCmd(jars_pth);
			} catch (Exception e) {
				// ignore
			}
			throw new PscExc(PscOut.LEV_MSG, "Aborted. Jars can be found here: "+jars_pth);
		}
		runIns();
	}
	
	private void chkPthAvl(String pth) throws PscExc {
		if (!isPthAvl(pth)) {
			throw new PscExc(PscOut.LEV_CTL, "Path in not availible: "+pth);
		}
	}

	private boolean isPthAvl(String pth) {
		return GUI.lisDir(pth, null, true) != null;
	}

	private boolean isFeaIns() throws Exception {
		PscDto dto_dto = SSN.newDto("PSC_DTO");
		dto_dto.setQue("NAM", "'CSO_GVZ_GRA'");
		return dto_dto.fetDat() > 0;
	}

	public void runIns() throws Exception {
		impLoa("PSA_LOA", Paths.get(MAI_DIR, "PsaLoa.zip").toString()); // import loader-loader
		impLoaAndRunDynJav("CSO_INS_OGE_GVZ_GRA_UTI_DEPENDANCIES", Paths.get(OGE_DIR, "CSO_INS_OGE_GVZ_GRA_UTI_DEPENDANCIES.zip").toString()); // import ipl dependencies
		impLoaAndRunDynJav("CSO_INS_OGE_GVZ_GRA_UTI_BASE_NAKED", Paths.get(MAI_DIR, "CSO_INS_OGE_GVZ_GRA_UTI_BASE_NAKED.zip").toString()); // import repository
		impLoaAndRunDynJav("CSO_INS_OGE_GVZ_GRA_UTI_DATA", Paths.get(MAI_DIR, "CSO_INS_OGE_GVZ_GRA_UTI_DATA.zip").toString()); // import graph definition data
	}
	
	public void impLoaAndRunDynJav(String loa_nam, String pth) throws Exception {
		impLoa(loa_nam, pth);
		runDynJavIfAvl(loa_nam);
	}
	
	private void runDynJavIfAvl(String loa_nam) throws Exception {
		PscDto dyn_jav_dto =  SSN.newDto("PSA_DYN_JAV");
		dyn_jav_dto.setQue("NAM", "'"+loa_nam+"'");
		if (dyn_jav_dto.fetDat() != 1) {
			return;
		}
		String cal_par = "CLS:="+loa_nam;
		PsaDynJav.repCal(SSN, null, cal_par);
	}

	public void impLoa(String loa_nam, String pth) throws Exception {
		PscDtoLoa loa_dto = (PscDtoLoa) SSN.newDto("PSC_LOA");
		loa_dto.setQue("NAM", "'"+loa_nam+"'");
		if (loa_dto.fetDat() != 1) {
			throw new RuntimeException();
		}
		PscImp imp = loa_dto.creLoaImp(1);
		{
			imp.setExl(PscOut.LEV_CTL);
			imp.setRmt(true);
			imp.setFil(pth);
			imp.setZip(true);
			imp.setMsg(true);
		}
		
		try (AutoCloseable _cls_los = () -> imp.clsLog()) {
			imp.opnLog();
			for (String nam = imp.nxtXml(null); nam != null; nam = imp.nxtXml(nam)) {
				imp.creDom(nam);
				imp.impDom(nam);
			}
			imp.impCsv();
			imp.impBlb();
		}
	}
	
	private static String savStrAsTmpFil (String str, boolean opn_fil_boo, PscGui gui, String fil_nam) throws Exception {
    	byte[] rpt_byt = str.getBytes();
    	if (!fil_nam.contains(".")) {
    		fil_nam += ".txt";
    	}
		String fil_pth = genCliTmpPth(fil_nam, gui);
		gui.putFil(fil_pth, "wb", rpt_byt);
		if (opn_fil_boo) {
			gui.runCmd(fil_pth, null, false);
		}
		return fil_pth;
    }
	
	private static String genCliTmpPth(String fil_nam, PscGui gui) throws Exception {
	        String ret = gui.getEnv("CLI_TMP");
	        ret = FilUtl.concatPthNam(ret, fil_nam, false);
	        ret = BlbUtl.getUniCliFil(gui, ret);
	        return ret;
	    }
	
	private static final String EXAMPLE_CLS_PTH_CNF = "Example configuration of PiSA's java classpath on LINUX: \n"
			+ "export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/commons-exec-1.3.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/graphviz-java-0.16.0.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/jcl-over-slf4j-1.7.30.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/jsr305-3.0.2.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/jul-to-slf4j-1.7.30.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/log4j-api-2.13.0.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/log4j-core-2.13.0.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/log4j-slf4j-impl-2.13.0.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/nashorn-promise-0.1.1.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/slf4j-api-1.7.30.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/svgSalamander-1.1.3.jar\n" + 
			"export PSA_CLS_PST=${PSA_CLS_PST}:ext_graphviz/viz.js-for-graphviz-java-2.1.2.jar\n"
			+ "\n"
			+ "Example configuration of PiSA's java classpath on WINDOWS \n"
			+ "wrapper.java.classpath.5=ext_graphviz/commons-exec-1.3.jar\n" + 
			"wrapper.java.classpath.6=ext_graphviz/graphviz-java-0.16.0.jar\n" + 
			"wrapper.java.classpath.7=ext_graphviz/jcl-over-slf4j-1.7.30.jar\n" + 
			"wrapper.java.classpath.8=ext_graphviz/jsr305-3.0.2.jar\n" + 
			"wrapper.java.classpath.9=ext_graphviz/jul-to-slf4j-1.7.30.jar\n" + 
			"wrapper.java.classpath.10=ext_graphviz/log4j-api-2.13.0.jar\n" + 
			"wrapper.java.classpath.11=ext_graphviz/log4j-core-2.13.0.jar\n" + 
			"wrapper.java.classpath.12=ext_graphviz/log4j-slf4j-impl-2.13.0.jar\n" + 
			"wrapper.java.classpath.13=ext_graphviz/nashorn-promise-0.1.1.jar\n" + 
			"wrapper.java.classpath.14=ext_graphviz/slf4j-api-1.7.30.jar\n" + 
			"wrapper.java.classpath.15=ext_graphviz/svgSalamander-1.1.3.jar\n" + 
			"wrapper.java.classpath.16=ext_graphviz/viz.js-for-graphviz-java-2.1.2.jar";
}