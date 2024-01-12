// Last Update by user CUSTOMIZER at 20220707063441
import static de.pisa.psc.srv.svc.PscUti.isStr;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;
import java.util.stream.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psa.frm.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;
import de.pisa.psc.srv.svc.PscUsxPar.*;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.model.*;
import guru.nidi.graphviz.parse.*;
import org.apache.commons.lang3.*;
import org.apache.logging.log4j.*;


/*
Call job with...
class: $CSO_INV_GLB_IPL
method: INV
parameter: INV_IPL:=CSO_GVZ_GRA.RUN
*/

/** CSO_GVZ_GRA_UTI 
 * @param <E>*/
public class IplGlbCsoGvzGraUti implements IplGlbCsoLogUtiItf {

	
	public static final String D3_DFL_REND_ENGI = "dot";
	public static final int DFL_MAX_LVL_GRA_DPH = 2;
	/** if false: try to use functional object links internally.
	 *  if true: use only normal object links.**/
	public boolean use_nrm_obj_lnk = true; 
	
	PscSsn SSN = null;
	UsxPar PAR = null;
	JobLog LOG = null;
	
	@Override
	public Level getGuiLogLvl() {
		return Level.INFO;
	}
	
	public static final String WRA_HTM_BDY_PFX = "<!DOCTYPE html>\r\n" + 
			"<html>\r\n" + 
			"<head>\r\n" + 
			"</head>\r\n" + 
			"<body>\r\n";
	public static final String WRA_HTM_BDY_SFX = "\r\n</body>\r\n" + 
			"</html>";
	
	
	
	public static final String D3_HTM_TPL = "<!DOCTYPE html>\r\n" + 
			"<html>\r\n" + 
			"<head>\r\n" + 
//			"    <meta charset=\"utf-8\">\r\n" + 
			"</head>\r\n" + 
			"<body>\r\n" + 
			"\r\n" + 
			"<LIB_REFERENCE>\r\n" + 
			"\r\n" + 
			"<div id=\"graph\" style=\"text-align: center;\"></div>\r\n" + 
			"\r\n" + 
			"<script type=\"text/javascript\" >\r\n" + 
			"var dotLines = [\r\n" + 
			"<DOT_LINES>\r\n" + 
			"];\r\n" + 
			"\r\n" + 
			"var thisDoc = document;        \r\n" + 
			"var handlerFnc = null;\r\n" + 
			"\r\n" + 
			"if ( typeof document._handlePsaLinks === 'function' ) {\r\n" + 
			"    console.log('Got a handler function!');\r\n" + 
			"	handlerFnc = document._handlePsaLinks;\r\n" + 
			"}\r\n" + 
			"var vie_wid = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);\r\n" + 
			"var vie_hgt = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);\r\n" + 
			"dot = dotLines.join('\\n');\r\n" + 
			"d3.select(\"#graph\")\r\n" + 
			"	.graphviz({useWorker: false})\r\n" + 
			"	.engine('<ENGINE>')\r\n" + 
//			"	.width(<SVG_WIDTH>)\r\n" + 
//			"	.height(<SVG_HEIGHT>)\r\n" + 
			"	.width(vie_wid)\r\n" + 
			"	.height(vie_hgt)\r\n" + 
			"	.renderDot(dot)\r\n" + 
			"	.on(\"end\", function () {\r\n" + 
			"		if ( handlerFnc ) {\r\n" + 
			"			handlerFnc(thisDoc);\r\n" + 
			"		}		\r\n" + 
			"	});\r\n" + 
			" \r\n" + 
			"</script>\r\n" + 
			"</body>\r\n" + 
			"</html>";
	
	public static final String EXT_D3_LIB_REF = "<script src=\"https://d3js.org/d3.v5.min.js\"></script>\r\n" + 
	"<script src=\"https://unpkg.com/@hpcc-js/wasm@0.3.11/dist/index.min.js\"></script>\r\n" + 
	"<script src=\"https://unpkg.com/d3-graphviz@3.0.5/build/d3-graphviz.js\"></script>";
	
	public static final String INT_D3_LIB_REF = "<script type=\"text/javascript\" src=\"psacht/d3.v5.min.js\"></script>\r\n" + 
			"<script type=\"text/javascript\" src=\"psacht/wasm.v0.3.11.min.js\"></script>\r\n" + 
			"<script type=\"text/javascript\" src=\"psacht/d3-graphviz.js\"></script>";
			

	private void run() throws Exception {
		
	}

	
	public static void CRE_LVL_GRA(PscFrm frm, String par, Integer row) throws Exception {
		PscDto dyn_dto = frm.getDynDto();
		if (!"CYCLIC_GENERIC".equals(dyn_dto.getDat("TYP", row))) {
			throw new RuntimeException("Only possible for graphs of type 'CYCLIC_GENERIC'");
		}
		PscGui gui = frm.getGui();
		UsxPar cre_par = getParForCreLevFroCycGra(gui);
		if (cre_par == null) {
			return; // aborted
		}
		int max_dph = cre_par.getPar("MAX_DPH", -1);
		if (max_dph == -1) {
			throw new RuntimeException("You need to define param max depth (MAX_DPH)");
		}
		String roo_nod_idn = cre_par.getPar("ROO_NOD");
		if (!isStr(roo_nod_idn)) {
			throw new RuntimeException("You need to define the root node (ROO_NOD)");
		}
		roo_nod_idn = roo_nod_idn.toLowerCase();
		PscSsn ssn = frm.getSsn();
		String cyc_gra_dot = dyn_dto.getDat("DEF", row);
		IplGlbCsoGvzGraUti thi = new IplGlbCsoGvzGraUti(ssn);
		CsoGvzGra lvl_gra = thi.creLvlGraFroCycGra(cyc_gra_dot, roo_nod_idn, max_dph);
		PscDto gvz_gra_dto = ssn.newDto("CSO_GVZ_GRA");
		gvz_gra_dto.insRow(1);
		String lvl_dot_dat = lvl_gra.getDotDat();
		gvz_gra_dto.setDat("DEF", 1, lvl_dot_dat);
		gvz_gra_dto.setDat("TYP", 1, "LEVELED_GENERIC");
		gvz_gra_dto.setDat("FAT_GID", 1, dyn_dto.getDat("PSC_GID", row));
		gvz_gra_dto.putDat();
		
		PscFrm eac = gui.newFrm("CSO_GVZ_GRA_EAC");
		PscDto new_dyn_dto = eac.getDynDto();
		eac.setQue(new_dyn_dto.getFld("IDN"), "'"+gvz_gra_dto.getDat("IDN", 1)+"'");
		eac.calEvt("REC_FET");
	}
	
	public static void OPN_GRA(PscFrm frm, String par, Integer row) throws Exception {
		if (!IplDlgCsoUti.isMaiSelRow(frm, row)) {
			return;
		}
		boolean is_pad_or_pho = CLI_ENV.isPadOrPhn(frm.getGui());
		PscDto dyn_dto = frm.getDynDto();
		PscGui gui = frm.getGui();
		PscSsn ssn = frm.getSsn();
		UsxPar cre_par = new UsxPar(par);
		if (!isStr(cre_par.getPar("MAX_DPH"))) {
			cre_par.setPar("MAX_DPH", DFL_MAX_LVL_GRA_DPH);
		}
//		if (!is_pad_or_pho || gui.getSsn().getUsr().equalsIgnoreCase("FOO2")) {
			cre_par = getInpPar(gui, cre_par);
			if (cre_par == null) {
				return; // aborted
			}
//		}
		String dlg_dsc = cre_par.getPar("DLG");
		if (!isStr(dlg_dsc)) {
			throw new RuntimeException("You need to define the dialog descriptor (DLG)");
		}
		String htm_fld = cre_par.getPar("HTM_FLD");
		if (!isStr(htm_fld)) {
			throw new RuntimeException("You need to define the html viewer field (HTM_FLD)");
		}
		Integer max_dph = cre_par.getPar("MAX_DPH", -1);
		if (max_dph == -1) {
			throw new RuntimeException("You need to define the max depth (MAX_DPH)");
		}
		String gra_idn = cre_par.getPar("GRA_IDN");
		if (!isStr(gra_idn)) {
			throw new RuntimeException("You need to define the graph IDN (GRA_IDN)");
		}
		PscDto lvl_gra_dto = IplDlgCsoUti.fetDtoOne(true, frm.getSsn(), "CSO_GVZ_GRA", "IDN", "'"+gra_idn+"'");
		String roo_nod_idn = null;
		boolean is_cyc;
		if ("LEVELED_GENERIC".equals(lvl_gra_dto.getDat("TYP", 1))) {
			is_cyc = false;
		} else if ("CYCLIC_GENERIC".equals(lvl_gra_dto.getDat("TYP", 1))) {
			is_cyc = true;
			roo_nod_idn = cre_par.getPar("ROO_NOD");
			if (!isStr(roo_nod_idn)) {
				throw new RuntimeException("Param ROO_NOD is null/empty");
			}
			roo_nod_idn = roo_nod_idn.toLowerCase();
		} else {
			throw new RuntimeException("Unsupported graph type");
		}
		String roo_obj_key_fld_dsc = cre_par.getPar("ROO_KEY", "PSC_GID");
		List<Integer> row_lis = Arrays.stream(frm.getLisRow()).boxed().collect(Collectors.toList());
		String roo_obj_key_que = IplDlgCsoUti.buiQuoQueOr(IplDlgCsoUti.getDatSet(dyn_dto, roo_obj_key_fld_dsc, true, row_lis));
		String cyc_gra_dot = lvl_gra_dto.getDat("DEF", 1);
		IplGlbCsoGvzGraUti thi = new IplGlbCsoGvzGraUti(ssn);
		CsoGvzGra nrm_gra;
		if (is_cyc) {
			nrm_gra = thi.creNrmGraFroCycGra(cyc_gra_dot, roo_nod_idn, max_dph, roo_obj_key_fld_dsc, roo_obj_key_que);
		} else {
			nrm_gra = thi.creNrmGraFroLvlGra(cyc_gra_dot, max_dph, roo_obj_key_fld_dsc, roo_obj_key_que);
		}
		setAddChcPar(cre_par, ssn, nrm_gra);
		String dot_dat = nrm_gra.getDotDat();
//		IplDlgCsoUti.savStrAsTmpFil(dot_dat, true, gui, "meinDot.txt");
//		String dot_dat = escPsaCha("digraph { a->b; b[label=\"bääm\"];}");
		String lay_eng = nrm_gra.getAtr("psa_lay_eng", D3_DFL_REND_ENGI);
		
		if (is_pad_or_pho) {
			String htm_dat = dotToD3Htm(dot_dat, lay_eng, true);
			IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, gui, "graph.html");
		} else {
			PsaFrm gra_frm = (PsaFrm) gui.newFrm(dlg_dsc);
			gra_frm.getTop().setValBuf("CSO_GVZ_DOT__"+htm_fld, dot_dat);
			gra_frm.addFrmIniOpr((_frm)->{
				if (false) {
					testLoadThomasBlob(gra_frm, htm_fld);
				} else {
					if (CLI_ENV.isWin(gui)) {
						gra_frm.setEdtDat(htm_fld, "Opened graph locally (browser) because you use the PiSA windows client");
						String htm_dat = dotToD3Htm(dot_dat, lay_eng, true);
						IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, gui, "graph.html");
					} else {
						String htm_dat = dotToD3Htm(dot_dat, lay_eng, false);
						setHtmDat(gra_frm, htm_fld, htm_dat);
						gra_frm.calEvt("TAG_CLK "+htm_fld);
					}
//					IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, gui, "meinHtml.txt");
//					IplDlgCsoUti.savStrAsTmpFil(escPsaCha(htm_dat), true, gui, "meinEscHtml.txt");
					
				}
			});
		}
	}


	private static void setAddChcPar(UsxPar cre_par, PscSsn ssn, CsoGvzGra nrm_gra) throws Exception {
		if (cre_par.hasPar("CHC_PAR_IDN_LIS")) {
			String chc_par_idn_lis = cre_par.getPar("CHC_PAR_IDN_LIS", "");
			if (isStr(chc_par_idn_lis)) {
				for (String chc_par_idn: chc_par_idn_lis.split(",")) {
					PscDto par_dto = IplDlgCsoUti.fetDtoOne(true, ssn, "CSO_GVZ_CHC_PAR", "IDN:='"+chc_par_idn+"'");
					UsxPar chc_par = new UsxPar(par_dto.getDat("PAR", 1));
					for (Ent ent: chc_par.getParLis()) {
						String key = ent.getKey();
						String val = ent.getVal();
						nrm_gra.gra.graphAttrs().add(key, val);
					}
				}
			}
		}
	}
	
	public static void setHtmDat(PsaFrm gra_frm, String htm_fld, String htm_dat) throws Exception {
		setHtmDat(gra_frm, htm_fld, htm_dat, true, true);
	}


	public static void setHtmDat(PsaFrm gra_frm, String htm_fld, String htm_dat, boolean do_rpl, boolean do_fmt) throws Exception {
		String fmt_htm_dat = do_fmt ? escPsaCha(htm_dat) : htm_dat;
		if (do_rpl) {
			gra_frm.setTagPrp(htm_fld, "TIT", "", true);
		}
		gra_frm.setTagPrp(htm_fld, "TIT", fmt_htm_dat, true);
	}


	private static String escPsaCha(String str) {
		if (str == null) {
			return null;
		}
		String ret_str = str.replace("\\n", "\\\\n")
				.replace("\\r", "\\\\r")
				.replace("\\t", "\\\\t")
				.replace("\\v", "\\\\v");
		return ret_str;
	}


//	public static  class abstract SetFrmHtmDat implements PsaFrmIniOpr {
//
//		@Override
//		public void run(PsaFrmIpl frm) throws Exception {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
	
	public static void testLoadThomasBlob(PscFrm frm, String htm_fld) throws Exception {
		final String blb_nam = "HTML_GRAPH";
		PscGui gui = frm.getGui();
		PscSsn ssn = gui.getSsn();
		if ( PscUti.isStr(blb_nam) ) {
			gui.wriMsg("$I\t$I\t$I\tTrying to load BLOB \"" + blb_nam + "\"...");
			final byte[] blob = BlbUtl.getBlb(ssn, blb_nam, false);
			if ( (blob != null) && (blob.length > 0) ) {
				final String html = new String(blob, "UTF-8");
				IplDlgCsoUti.savStrAsTmpFil(html, true, gui, "testLoadThomasBlob.txt");
				frm.setTagPrp(htm_fld, "TIT", html, true);
				gui.wriMsg("$I\t$I\t$I\tBLOB \"" + blb_nam + "\" loaded.");
				ssn.wriTxt(html);
			}
			else {
				gui.wriMsg("$I\t$W\t$W\tFailed to load BLOB data!");
			}
		}
		else {
			gui.wriMsg("$I\t$W\t$W\tMissing BLOB name!");
		}
	}
	
	public static void TST_SET_DIR(PscFrm frm, String par) throws Exception {
		testLoadThomasBlob(frm, "GRA_HTM_DAT");
	}
	
	public static void OPN_VAL_BUF_DOT_BRW(PscFrm frm, String par) throws Exception {
		PscFrm top_frm = frm.getTop();
		UsxPar cre_par = new UsxPar(par);
		String htm_fld = cre_par.getPar("HTM_FLD");
		if (!isStr(htm_fld)) {
			throw new RuntimeException("You need to define the html viewer field (HTM_FLD)");
		}
		String dot_dat = top_frm.getTop().getValBuf("CSO_GVZ_DOT__"+htm_fld);
		CsoGvzGra gra = new CsoGvzGra(dot_dat);
		String lay_eng = gra.getAtr("psa_lay_eng", D3_DFL_REND_ENGI);
		String htm_dat = dotToD3Htm(dot_dat, lay_eng, true);
		if (isStr(htm_dat)) {
			IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, frm.getGui(), "gvz_gra_htm.html");
		}
	}
	
	public static void OPN_VAL_BUF_DOT_BRW(PscFrm frm, String par, Integer row) throws Exception {
		if (!IplDlgCsoUti.isMaiSelRow(frm, row)) {
			return;
		}
		OPN_VAL_BUF_DOT_BRW(frm, par);
	}
	
	// maybe throw away method if not used
	public static void TOG_POS_TYP_FDP_VS_DOT(PscFrm _frm, String par) throws Exception {
		PsaFrm gra_frm = (PsaFrm) _frm;
		PscFrm top_frm = gra_frm.getTop();
		UsxPar cre_par = new UsxPar(par);
		String htm_fld = cre_par.getPar("HTM_FLD");
		if (!isStr(htm_fld)) {
			throw new RuntimeException("You need to define the html viewer field (HTM_FLD)");
		}
		String dot_dat = top_frm.getTop().getValBuf("CSO_GVZ_DOT__"+htm_fld);
		CsoGvzGra gra = new CsoGvzGra(dot_dat);
		String lay_eng = gra.getAtr("psa_lay_eng", D3_DFL_REND_ENGI);
		lay_eng = "dot".equalsIgnoreCase(lay_eng) ? "fdp" : "dot";
		gra.setAtr("psa_lay_eng", lay_eng);
		dot_dat = gra.getDotDat();
		top_frm.getTop().setValBuf("CSO_GVZ_DOT__"+htm_fld, dot_dat);
		PscGui gui = gra_frm.getGui();
		if (CLI_ENV.isWin(gui)) {
			gra_frm.setEdtDat(htm_fld, "Opened graph locally (browser) because you use the PiSA windows client");
			String htm_dat = dotToD3Htm(dot_dat, lay_eng, true);
			IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, gui, "graph.html");
		} else {
			String htm_dat = dotToD3Htm(dot_dat, lay_eng, false);
			setHtmDat(gra_frm, htm_fld, htm_dat);
		}
	}
	
	// maybe throw away method if not used
		public static void TOG_EDG_TYP(PscFrm _frm, String par) throws Exception {
			PsaFrm gra_frm = (PsaFrm) _frm;
			PscFrm top_frm = gra_frm.getTop();
			UsxPar cre_par = new UsxPar(par);
			String htm_fld = cre_par.getPar("HTM_FLD");
			if (!isStr(htm_fld)) {
				throw new RuntimeException("You need to define the html viewer field (HTM_FLD)");
			}
			String dot_dat = top_frm.getTop().getValBuf("CSO_GVZ_DOT__"+htm_fld);
			CsoGvzGra gra = new CsoGvzGra(dot_dat);
			String lay_eng = gra.getAtr("psa_lay_eng", D3_DFL_REND_ENGI);
			String spl_par = gra.getAtr("splines", "true");
			spl_par = "true".equalsIgnoreCase(spl_par) ? "ortho" : "true";
			gra.setAtr("splines", spl_par);
			dot_dat = gra.getDotDat();
			top_frm.getTop().setValBuf("CSO_GVZ_DOT__"+htm_fld, dot_dat);
			PscGui gui = gra_frm.getGui();
			if (CLI_ENV.isWin(gui)) {
				gra_frm.setEdtDat(htm_fld, "Opened graph locally (browser) because you use the PiSA windows client");
				String htm_dat = dotToD3Htm(dot_dat, lay_eng, true);
				IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, gui, "graph.html");
			} else {
				String htm_dat = dotToD3Htm(dot_dat, lay_eng, false);
				setHtmDat(gra_frm, htm_fld, htm_dat);
			}
		}
	
	public static String dotToD3Htm(String dot_dat, String engi, boolean for_ext) {
		String fmt_dot_dat = Arrays.stream(dot_dat.replace("\r", "").split("\n"))
				.map(lin->"'"+lin+"'")
				.collect(Collectors.joining("\r\n,"));
		String htm = D3_HTM_TPL
				.replace("<ENGINE>", engi.toLowerCase())
				.replace("<LIB_REFERENCE>", for_ext ? EXT_D3_LIB_REF : INT_D3_LIB_REF)
				.replace("<DOT_LINES>", fmt_dot_dat)
				.replace("<SVG_WIDTH>", "1920")
				.replace("<SVG_HEIGHT>", for_ext ? "1200" : "1000");
		return htm;
	}


	public static void CRE_NRM_GRA(PscFrm frm, String par, Integer row) throws Exception {
		if (!IplDlgCsoUti.isMaiSelRow(frm, row)) {
			return;
		}
		PscDto dyn_dto = frm.getDynDto();
		PscGui gui = frm.getGui();
		UsxPar cre_par = new UsxPar(par);
		Integer max_dph = cre_par.getPar("MAX_DPH", -1);
		if (max_dph == -1) {
			max_dph = null;
		}
		String gra_idn = cre_par.getPar("GRA_IDN");
		if (!isStr(gra_idn)) {
			throw new RuntimeException("You need to define the graph IDN (GRA_IDN)");
		}
		PscDto lvl_gra_dto = IplDlgCsoUti.fetDtoOne(true, frm.getSsn(), "CSO_GVZ_GRA", "IDN", "'"+gra_idn+"'");
		if (!"LEVELED_GENERIC".equals(lvl_gra_dto.getDat("TYP", 1))) {
			// TODO also support cyclic graphs with root node definition
			throw new RuntimeException("Only possible for graphs of type 'LEVELED_GENERIC'");
		}
		String roo_obj_key_fld_dsc = cre_par.getPar("ROO_KEY", "PSC_GID");
		List<Integer> row_lis = Arrays.stream(frm.getLisRow()).boxed().collect(Collectors.toList());
		String roo_obj_key_que = IplDlgCsoUti.buiQuoQueOr(IplDlgCsoUti.getDatSet(dyn_dto, roo_obj_key_fld_dsc, true, row_lis));
		PscSsn ssn = frm.getSsn();
		
		
		String cyc_gra_dot = lvl_gra_dto.getDat("DEF", 1);
		IplGlbCsoGvzGraUti thi = new IplGlbCsoGvzGraUti(ssn);
		CsoGvzGra nrm_gra = thi.creNrmGraFroLvlGra(cyc_gra_dot, max_dph, roo_obj_key_fld_dsc, roo_obj_key_que);
		PscDto nrm_gra_dto = ssn.newDto("CSO_GVZ_GRA");
		nrm_gra_dto.insRow(1);
		String lvl_dot_dat = nrm_gra.getDotDat();
		nrm_gra_dto.setDat("DEF", 1, lvl_dot_dat);
		nrm_gra_dto.setDat("TYP", 1, "NORMAL");
		nrm_gra_dto.setDat("FAT_GID", 1, lvl_gra_dto.getDat("PSC_GID", 1));
		nrm_gra_dto.putDat();
		
		PscFrm eac = gui.newFrm("CSO_GVZ_GRA_EAC");
		PscDto new_dyn_dto = eac.getDynDto();
		eac.setQue(new_dyn_dto.getFld("IDN"), "'"+nrm_gra_dto.getDat("IDN", 1)+"'");
		eac.calEvt("REC_FET");
	}
	
	private static UsxPar getParForCreLevFroCycGra(PscGui gui) throws Exception {
		return getParForCreLevFroCycGra(gui, new UsxPar());
	}
	
	private static UsxPar getParForCreLevFroCycGra(PscGui gui, UsxPar cre_par) throws Exception {
		cre_par.setPar("MAX_DPH", DFL_MAX_LVL_GRA_DPH);
		PsaFrm inp_frm = (PsaFrm) gui.newFrm("CSO_GVZ_CRE_LVL_FRO_CYC_GRA_SSF");
		setEdtFldFroUsxPar(inp_frm, cre_par);
		gui.dlgFrm(inp_frm);
		String que_ret = inp_frm.getLstSetStaPar();
		if (!PscUti.isStr(que_ret) || que_ret.toUpperCase().contains("FALSE") || que_ret.toUpperCase().contains("CAN")) {
			return null;
		}
		setUsxParFroEdtFld(cre_par, inp_frm);
		return cre_par;
	}
	
	private static UsxPar getInpPar(PscGui gui, UsxPar usx_par) throws Exception {
		String inp_dlg_dsc = "CSO_GVZ_GET_MAX_DPH_SSF";
		boolean is_foo2 = gui.getSsn().getUsr().equalsIgnoreCase("FOO2"); // for testing
		if (is_foo2) {
			inp_dlg_dsc = "CSO_GVZ_INP_PAR_SSF"; 
		}
//		PsaFrm inp_frm = (PsaFrm) gui.newFrm("CSO_GVZ_INP_PAR_SSF"); // this is the detailed version
		PsaFrm inp_frm = (PsaFrm) gui.newFrm(inp_dlg_dsc);
		setEdtFldFroUsxPar(inp_frm, usx_par);
		gui.dlgFrm(inp_frm);
		String que_ret = inp_frm.getLstSetStaPar();
		if (!PscUti.isStr(que_ret) || que_ret.toUpperCase().contains("FALSE") || que_ret.toUpperCase().contains("CAN")) {
			return null;
		}
		setUsxParFroEdtFld(usx_par, inp_frm);
		
		String add_chc_par = usx_par.getParLis()
			.stream()
			.map(ent->ent.getKey())
			.filter(key->key.startsWith("ADD_CHC_PAR"))
			.sorted()
			.map(key->usx_par.getPar(key, ""))
			.filter(val->isStr(val))
			.collect(Collectors.joining(","));
		if (isStr(add_chc_par)) {
			usx_par.setPar("CHC_PAR_IDN_LIS", add_chc_par);
		}
		
		return usx_par;
	}
	
//	private static UsxPar getInpMaxDph(PscGui gui, UsxPar usx_par) throws Exception {
//		usx_par.setPar("MAX_DPH", DFL_MAX_LVL_GRA_DPH);
//		PsaFrm inp_frm = (PsaFrm) gui.newFrm("CSO_GVZ_GET_MAX_DPH_SSF");
//		setEdtFldFroUsxPar(inp_frm, usx_par);
//		gui.dlgFrm(inp_frm);
//		String que_ret = inp_frm.getLstSetStaPar();
//		if (!PscUti.isStr(que_ret) || que_ret.toUpperCase().contains("FALSE") || que_ret.toUpperCase().contains("CAN")) {
//			return null;
//		}
//		setUsxParFroEdtFld(usx_par, inp_frm);
//		return usx_par;
//	}
	
	private static void setEdtFldFroUsxPar(PscFrm inp_frm, UsxPar usx_par) throws Exception {
		PscDto tag_dto = inp_frm.getTagDto();
		for (Ent ent : usx_par.getParLis()) {
			int pos = inp_frm.getTagPos(ent.Key);
			if (pos <= 0) {
				continue;
			}
			String tag_typ = tag_dto.getDat("TYP", pos);
			if (!tag_typ.equals("EDT")) {
				continue;
			}
			String txt = (ent.Val == null ? "" : ent.Val)+"";
			if (txt.equalsIgnoreCase("y")) {
				txt = "y";
			}
			if (txt.equalsIgnoreCase("n")) {
				txt = "n";
			}
			tag_dto.modDat("TIT", pos, txt);
		}
	}
	
	private static void setUsxParFroEdtFld(UsxPar usx_par, PscFrm inp_frm) throws Exception {
		PscDto tag_dto = inp_frm.getTagDto();
		for (int tag_row = 1; tag_row <= tag_dto.numRec(); tag_row++) {
			String tag_typ = inp_frm.getTagDto().getDat("TYP", tag_row);
			if (!tag_typ.equals("EDT")) {
				continue;
			}
			String key = tag_dto.getDat("NAM", tag_row);
			String val = tag_dto.getDat("TIT", tag_row);
			usx_par.setPar(key, val);
		}
	}
	
	public static void MOD_SVG_SIZ(PscFrm frm, String par, Integer row) throws Exception {
		UsxPar cre_par = new UsxPar(par);
		String htm_fld_dsc = cre_par.getPar("HTM_FLD");
		boolean is_edt_fld = cre_par.getPar("IS_EDT_FLD",true);
		int add_val_prc_int = Integer.parseInt(cre_par.getPar("ADD_VAL_PRC"));
		PscDto dyn_dto = frm.getDynDto();
		String htm_dat = is_edt_fld ? ((PsaFrm)frm).getEdtDat(htm_fld_dsc) : dyn_dto.getDat(htm_fld_dsc, row);
		Pattern wid_pat = Pattern.compile("width\\=\"(\\d+)px\"", Pattern.CASE_INSENSITIVE);
		Pattern hgt_pat = Pattern.compile("height\\=\"(\\d+)px\"", Pattern.CASE_INSENSITIVE);
		double add_val_prc_dou = add_val_prc_int/100.0;
		{
			Matcher mat = wid_pat.matcher(htm_dat);
			StringBuffer sb = new StringBuffer();
			if (mat.find()) {
				int ori_val = Integer.parseInt(mat.group(1));
				int new_val = (int) Math.round(ori_val*(1.0+add_val_prc_dou));
				String rpl_str = "width=\""+new_val+"px\"";
				mat.appendReplacement(sb, rpl_str);
			}
			mat.appendTail(sb);
			htm_dat = sb.toString();
		}
		{
			Matcher mat = hgt_pat.matcher(htm_dat);
			StringBuffer sb = new StringBuffer();
			if (mat.find()) {
				int ori_val = Integer.parseInt(mat.group(1));
				int new_val = (int) Math.round(ori_val*(1.0+add_val_prc_dou));
				String rpl_str = "height=\""+new_val+"px\"";
				mat.appendReplacement(sb, rpl_str);
			}
			mat.appendTail(sb);
			htm_dat = sb.toString();
		}
		if (is_edt_fld) {
			((PsaFrm)frm).setEdtDat(htm_fld_dsc, htm_dat);
		} else {
			IplDlgCsoUti.modDatUseRfr(dyn_dto, htm_fld_dsc, row, htm_dat);
		}
	}
	
	public CsoGvzGra creNrmGraFroCycGra (String cyc_gra_dot, String roo_nod_idn, int max_dph, String roo_obj_key_fld_dsc, String roo_obj_key_que) throws Exception {
		CsoGvzGra lvl_gra = creLvlGraFroCycGra(cyc_gra_dot, roo_nod_idn, max_dph);
		CsoGvzGra nrm_gra = creNrmGraFroLvlGra(lvl_gra.getDotDat(), max_dph, roo_obj_key_fld_dsc, roo_obj_key_que);
		return nrm_gra;
	}
	
	public static void TST_ANI_D3_GVZ(PscFrm frm, String par, Integer row) throws Exception {
		if (!IplDlgCsoUti.isMaiSelRow(frm, row)) {
			return;
		}
		PscDto dyn_dto = frm.getDynDto();
		PscGui gui = frm.getGui();
		UsxPar cre_par = new UsxPar(par);
		cre_par.setPar("MAX_DPH", "5");
		cre_par.setPar("ROO_NOD", cre_par.getPar("ROO_NOD", "").toLowerCase());
		cre_par = getParForCreLevFroCycGra(gui, cre_par);
		if (cre_par == null) {
			return; // aborted
		}
		int max_dph = cre_par.getPar("MAX_DPH", -1);
		if (max_dph == -1) {
			throw new RuntimeException("You need to define param max depth (MAX_DPH)");
		}
		String roo_nod_idn = cre_par.getPar("ROO_NOD");
		if (!isStr(roo_nod_idn)) {
			throw new RuntimeException("You need to define the root node (ROO_NOD)");
		}
		roo_nod_idn = roo_nod_idn.toLowerCase();
		PscSsn ssn = frm.getSsn();
		String cyc_gra_dot = IplDlgCsoUti.fetDtoOne(true, ssn, "CSO_GVZ_GRA", "IDN", "'"+cre_par.getPar("GRA_IDN")+"'").getDat("DEF", 1);
		IplGlbCsoGvzGraUti thi = new IplGlbCsoGvzGraUti(ssn);
		String roo_obj_key_fld_dsc = cre_par.getPar("ROO_KEY", "PSC_GID");
		List<Integer> row_lis = Arrays.stream(frm.getLisRow()).boxed().collect(Collectors.toList());
		String roo_obj_key_que = IplDlgCsoUti.buiQuoQueOr(IplDlgCsoUti.getDatSet(dyn_dto, roo_obj_key_fld_dsc, true, row_lis));
		thi.tstAniD3Gvz(cyc_gra_dot, roo_nod_idn, max_dph, roo_obj_key_fld_dsc, roo_obj_key_que);
	}
	
	public void tstAniD3Gvz (String cyc_gra_dot, String roo_nod_idn, int max_dph, String roo_obj_key_fld_dsc, String roo_obj_key_que) throws Exception {
		List<String> dot_lis = new ArrayList<String>();
		for (int i = 0; i <= max_dph; i++) {
			CsoGvzGra gra = creNrmGraFroCycGra(cyc_gra_dot, roo_nod_idn, i, roo_obj_key_fld_dsc, roo_obj_key_que);
			String dot_dat = gra.getDotDat();
			dot_dat = Arrays.stream(dot_dat.replace("\r", "").split("\n"))
					.map(lin->"'"+lin+"'")
					.collect(Collectors.joining(","));
			dot_lis.add(dot_dat);
		}
		String dot_arr_str = "["+String.join("],[", dot_lis)+"]";
		String htm_str = D3_ANI_HTM_PFX + dot_arr_str + D3_ANI_HTM_SFX;
		IplDlgCsoUti.savStrAsTmpFil(htm_str, true, SSN.getGui(), "d3_gvz_ani.html");
	}
	
	private final static String D3_ANI_HTM_PFX = "<!DOCTYPE html>\r\n" + 
			"<html>\r\n" + 
			"<head>\r\n" + 
			"<meta charset=\"utf-8\">\r\n" + 
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<script src=\"https://d3js.org/d3.v5.min.js\"></script>\r\n" + 
			"<script src=\"https://unpkg.com/@hpcc-js/wasm@0.3.11/dist/index.min.js\"></script>\r\n" + 
			"<script src=\"https://unpkg.com/d3-graphviz@3.0.5/build/d3-graphviz.js\"></script>\r\n" + 
			"<div id=\"graph\" style=\"text-align: center;\"></div>\r\n" + 
			"<script>\r\n" + 
			"\r\n" + 
			"var dotIndex = 0;\r\n" + 
			"var graphviz = d3.select(\"#graph\").graphviz()\r\n" + 
			"    .transition(function () {\r\n" + 
			"        return d3.transition(\"main\")\r\n" + 
			"            .ease(d3.easeLinear)\r\n" + 
			"            .delay(500)\r\n" + 
			"            .duration(1500);\r\n" + 
			"    })\r\n" + 
			"    .logEvents(true)\r\n" + 
			"    .on(\"initEnd\", render);\r\n" + 
			"\r\n" + 
			"function render() {\r\n" + 
			"    var dotLines = dots[dotIndex];\r\n" + 
			"    var dot = dotLines.join('');\r\n" + 
			"    graphviz\r\n" + 
			"        .renderDot(dot)\r\n" + 
			"        .on(\"end\", function () {\r\n" + 
			"            dotIndex = (dotIndex + 1) % dots.length;\r\n" + 
			"            render();\r\n" + 
			"        });\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"var dots = [";
	private final static String D3_ANI_HTM_SFX = "];\r\n" + 
			"\r\n" + 
			"</script>\r\n" + 
			"</body>\r\n" + 
			"</html>";
	
	public CsoGvzGra creNrmGraFroLvlGra (String lvl_gra_dot, Integer max_dph, String roo_obj_key_fld_dsc, String roo_obj_key_que) throws Exception {
		if (max_dph == null) {
			max_dph = Integer.MAX_VALUE;
		}
		CsoGvzGra lvl_gra = new CsoGvzGra(lvl_gra_dot);
		
		CsoGvzGra nrm_gra = lvl_gra.cpyWitOutNodAndEdg();
		MutableNode lvl_roo_nod = lvl_gra.getRooNod();
		String roo_dto_dsc = getAtr(lvl_roo_nod, "psa_dto");
//		PscDto roo_dto = SSN.newDto(roo_dto_dsc);
//		roo_dto.setQue("PSC_GID", "'"+roo_obj_gid+"'");
		PscDto roo_dto = IplDlgCsoUti.fetDto(SSN, roo_dto_dsc, roo_obj_key_fld_dsc, roo_obj_key_que);
		if (roo_dto == null) {
			throw new RuntimeException("No data found for query "+roo_obj_key_fld_dsc+":="+roo_obj_key_que+" on dto "+roo_dto_dsc);
		}
//		PscDto roo_dto = IplDlgCsoUti.fetDtoOne(true, SSN, roo_dto_dsc, "PSC_GID", "'"+roo_obj_gid+"'");
		for (int row = 1; row <= roo_dto.numRec(); row++) {
			MutableNode new_roo_nod = Factory.mutNode(genDatNodIdn(roo_dto, row));
			new_roo_nod.add(lvl_roo_nod.attrs());
			setAtr(new_roo_nod, "psa_lvl_nod_idn", getNodIdn(lvl_roo_nod));
			setAtr(new_roo_nod, "penwidth", 4);
			setAtr(new_roo_nod, "color", "red");
			DtoRec roo_rec = new DtoRec(roo_dto, row);
			nrm_gra.setNodRec(new_roo_nod, roo_rec);
			iniDatNod(roo_rec, new_roo_nod, nrm_gra);
			nrm_gra.gra.add(new_roo_nod);
		}
		for (int lvl = 0; lvl<max_dph; lvl++) {
			Set<MutableNode> nrm_nod_set = nrm_gra.getNodsByAtrVal("psa_dph_lvl", lvl);
			for (MutableNode nrm_nod: nrm_nod_set) {
				String lvl_nod_idn = getAtr(nrm_nod, "psa_lvl_nod_idn");
				MutableNode lvl_nod = lvl_gra.getNodByIdn(lvl_nod_idn);
				DtoRec rec = nrm_gra.getNodRec(nrm_nod);
				for (Link lvl_lnk: lvl_nod.links()) {
					String rel_dsc = getAtr(lvl_lnk, "psa_rel");
					PscRel rel_obj = rec.dto.getRel(rel_dsc);
					if (rel_obj == null) {
						throw new RuntimeException("Relation '"+rel_dsc+"' not found in dto '"+rec.dto.getDsc()+"'");
					}
					String son_dsc = rel_obj.getDss();
					String rel_dto_dsc = getAtr(lvl_lnk, "psa_rel_dto");
					PscDto rel_dto = null;
					
					if (isStr(rel_dto_dsc)) {
						rel_dto = SSN.newDto(rel_dto_dsc);
					} else {
						PscDto son_dto = SSN.newDto(son_dsc);
						if (son_dto.hasSupDto("PSA_REL")) {
							rel_dto = son_dto;
						}
					}
					/** if true: relation cardinality is n-m and not n-1 */
					boolean is_NxM_rel = rel_dto != null; 
					if (rel_dto != null && rel_dto.getDto(son_dsc) == null) {
						logDbg("Warning: Relation dto '"+rel_dto.getDsc()+"' does not have super or component dto '"+son_dsc+"'");
					}
					if (rel_obj.getKyf().length > 1) {
						throw new UnsupportedOperationException("Relations with more than one key are not supported in graphs yet (dto and relation: "+rec.dto.getDsc()+" and "+rel_dsc+")");
					}
					MutableNode lvl_tgt = getTgtNod(lvl_lnk);
					String tgt_dto_dsc = getAtr(lvl_tgt, "psa_dto");
					PscDto tgt_que_dto = rel_dto != null ? rel_dto : SSN.newDto(tgt_dto_dsc);
					for (int i = 0; i < rel_obj.getKyf().length; i++) {
						String key_fat = rel_obj.getKyf()[i];
						String key_son = rel_obj.getKys()[i];
						String key_fat_dat = rec.dto.getDat(key_fat, rec.row);
						tgt_que_dto.setQue(key_son, "'"+key_fat_dat+"'");
					}
//					List<String[]> chd_key_lis = new ArrayList<String[]>();
					// TODO add support for multi-key-relations, code from here
					String rel_dto_tgt_son_key_dsc[];
					if (is_NxM_rel && rel_dto.hasSupDto("PSA_REL")) {
						rel_dto_tgt_son_key_dsc = new String[1];
						if (rel_obj.getKys().length != 1) {
							throw new RuntimeException("Invalid relation for PSA_REL, only CHD_GID<->FAT_GID supported");
						}
						if (rel_obj.getKys()[0].equals("FAT_GID")) {
							rel_dto_tgt_son_key_dsc[0] = "CHD_GID";
						} else if (rel_obj.getKys()[0].equals("CHD_GID")) {
							rel_dto_tgt_son_key_dsc[0] = "FAT_GID";
						} else {
							throw new RuntimeException("Invalid relation for PSA_REL, only CHD_GID<->FAT_GID supported");
						}
					} else {
						rel_dto_tgt_son_key_dsc = new String[1];
						rel_dto_tgt_son_key_dsc[0] = "PSC_GID";
					}
					String rel_dto_tgt_fat_key_dsc[] = {"PSC_GID"};
					
					new IplGlbCsoDtoItr()
					.itr(tgt_que_dto)
					.exe((_dto)->{
						if (!tgt_que_dto.chkAcc(1, 'R')) {
							return; // ignore node
						}
						PscDto tgt_dto;
						if (!is_NxM_rel) { // n-1 relation cardinality
							tgt_dto = tgt_que_dto.cloDto();
							tgt_dto.addRow( 0, 1);
							tgt_dto.cloRow( 1, tgt_que_dto, 1);  
							tgt_dto.clnDat();
						} else { // n-m relation cardinality
							String key_dat[] = new String[rel_dto_tgt_son_key_dsc.length];
							for (int i = 0; i < rel_dto_tgt_son_key_dsc.length; i++) {
								String key_son = rel_dto_tgt_son_key_dsc[i];
								String key_son_dat = _dto.getDat(key_son, 1);
								key_dat[i] = key_son_dat;
							}
//							chd_key_lis.add(key_dat);
							// TODO do not open new dto for every record (use '|' if single key relation)
							// TODO de-de-comment this and do it well
//							PscDto tgt_dto = SSN.newDto(tgt_dto_dsc);
//							for (int i = 0; i < rel_dto_tgt_fat_key_dsc.length; i++) {
//								tgt_dto.setQue(rel_dto_tgt_fat_key_dsc[i], chd_key_dat[0]);
//							}
							// TODO remove this, this is bad
							tgt_dto = IplDlgCsoUti.fetDtoOne(false, true, SSN, tgt_dto_dsc, rel_dto_tgt_fat_key_dsc[0], "'"+key_dat[0]+"'");
						}
						if (tgt_dto == null) {
							return;
						}
						if (!tgt_dto.chkAcc(1, 'R')) {
							return; // ignore node
						}
						DtoRec tgt_rec = new DtoRec(tgt_dto, 1);
						String lvl_tgt_idn = getNodIdn(lvl_tgt);
						String nrm_tgt_idn = genDatNodIdn(tgt_rec.dto, tgt_rec.row);
						MutableNode nrm_tgt = nrm_gra.getNodByIdn(nrm_tgt_idn);
						boolean is_tgt_avl = nrm_tgt != null;
						if (!is_tgt_avl) {
							// node is not yet availible, create it, initialize it and add to graph
							nrm_tgt = Factory.mutNode(nrm_tgt_idn);
							nrm_tgt.add(lvl_tgt.attrs());
							setAtr(nrm_tgt, "psa_lvl_nod_idn", lvl_tgt_idn);
							nrm_gra.setNodRec(nrm_tgt, tgt_rec);
							iniDatNod(tgt_rec, nrm_tgt, nrm_gra);
							nrm_gra.gra.add(nrm_tgt);
						}
						// TODO use DtoRec as link-records also
						boolean cre_new_lnk = true;
						String rel_idn = getAtr(lvl_lnk, "psa_uni_rel_idn");
						if (is_tgt_avl && isStr(rel_idn)) {
							cre_new_lnk = !hasLnkWitRelIdn(nrm_nod, nrm_tgt, rel_idn);
						}
						if (cre_new_lnk) {
							Link new_lnk = creLnk(nrm_nod, nrm_tgt, lvl_lnk.attrs());
							iniDatLnkAtr(new_lnk, tgt_que_dto, 1);
							
//							Link new_lnk = creLnk(nrm_nod, nrm_tgt, lvl_lnk.attrs());
//							iniDatLnkAtr(new_lnk,tgt_que_dto,1);
							
//							MutableAttributed<Link, ForLink> add_atr = lvl_lnk.attrs();
//							Link new_lnk = nrm_nod.linkTo(nrm_tgt);
//							if (add_atr != null) {
//								new_lnk.attrs().add(add_atr);
//							}
//							
//							MutableAttributed<Link, ForLink> dat_atr = new_lnk.attrs();
//							String ori_lbl = (String)new_lnk.get("label");
//							if (!isStr(ori_lbl)) {
//								return;
//							}
//							String new_lbl = rplFldTpl(tgt_que_dto, 1, ori_lbl);
//							dat_atr.add(Attributes.attr("label", new_lbl));
//							nrm_nod.links().add(new_lnk);
//							boolean b = false;
						}
					});
				}
			}
		}
		if (false) {
			opnDotFil(SSN, lvl_gra, false);
			opnDotFil(SSN, nrm_gra, false);
			opnDotFil(SSN, lvl_gra, true);
			opnDotFil(SSN, nrm_gra, true);
		}
		return nrm_gra;
	}

	private boolean hasLnkWitRelIdn(MutableNode src_nod, MutableNode tgt_nod, String rel_idn) {
		if (!isStr(rel_idn)) {
			throw new RuntimeException("!isStr(rel_idn)");
		}
		for (Link src_lnk: src_nod.links()) {
			String rel_idn_i = getAtr(src_lnk, "psa_uni_rel_idn");
			if (isStr(rel_idn_i) && rel_idn.equals(rel_idn_i) && getTgtNod(src_lnk) == tgt_nod) {
				return true;
			}
		}
		for (Link tgt_lnk: tgt_nod.links()) {
			String rel_idn_i = getAtr(tgt_lnk, "psa_uni_rel_idn");
			if (isStr(rel_idn_i) && rel_idn.equals(rel_idn_i) && getTgtNod(tgt_lnk) == src_nod) {
				return true;
			}
		}
		return false;
	}

	private void iniDatLnkAtr(Link dat_lnk, PscDto rel_dto, int row) throws Exception {
//	private MutableAttributed<Link, ForLink> iniDatLnkAtr(Link lvl_lnk, PscDto rel_dto, int row) throws Exception {
		MutableAttributed<Link, ForLink> dat_atr = dat_lnk.attrs();
		String ori_lbl = (String)dat_lnk.get("label");
		if (!isStr(ori_lbl)) {
			return;
		}
		String new_lbl = rplFldTpl(rel_dto, row, ori_lbl);
		dat_atr.add(Attributes.attr("label", new_lbl));
		return;
	}

	private static final String IDN_PTH_PAT_STR = "[_\\.\\w]+";
	private static final String FLD_TPL_PAT_STR = "@PSA_FLD:("+IDN_PTH_PAT_STR+")";
	private static final Pattern FLD_TPL_PAT = Pattern.compile(FLD_TPL_PAT_STR);
	public static final String RUN_TRI_KEY = "CSO_GVZ_UTI__RUN_TRI_KEY";
	
	public void iniDatNod(DtoRec nod_rec, MutableNode nod, CsoGvzGra gra) throws Exception {
		boolean is_html_lbl = getNodLbl(nod).isHtml();
		String ori_lbl = getNodLblAsStr(nod);
		String new_lbl = ori_lbl;
		new_lbl = hdlPsaRplMac(nod_rec, nod, gra, new_lbl);
		new_lbl = rplFldTpl(nod_rec.dto, nod_rec.row, new_lbl);
		if (new_lbl.contains("@PSA_CLZ")) {
			String clz_tit = getDtoRecClzTit(nod_rec);
			if (clz_tit != null) {
				clz_tit = esc(clz_tit);
				new_lbl = new_lbl.replace("@PSA_CLZ", clz_tit);
			}
		}
		setNodLbl(nod, new_lbl, is_html_lbl);
		hdlPsaSetMac(nod_rec, nod, gra);
		try {
			String href;
			if (use_nrm_obj_lnk) {
				href = PsaObjLnk.getIns().creHREF(nod_rec.dto, nod_rec.dto.getFld("PSC_GID"), nod_rec.row);
			} else {
				href = creFncObjLnk();
			}
			if (isStr(href)) {
				setAtr(nod, "href", href);
			}
		} catch (Exception e) {
//			new IplGlbCsoGvzGraUti(nod_rec.dto.getSsn())
			logErr("While trying to generate hyperlink for node "+getNodIdn(nod), e);
		}
	}

	private String hdlPsaRplMac(DtoRec nod_rec, MutableNode nod, CsoGvzGra gra, String ful_str) throws Exception {
		StringBuffer sb = new StringBuffer();
		Pattern pat = Pattern.compile("PSA_RPL\\=\"(.+?)\"", Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(ful_str);
		while (mat.find()) {
			String mac = mat.group(1);
			String val = getPsaRplMacVal(nod_rec, gra, mac);
			if (val == null) {
				val = "";
			}
			 // always replace whole string
			mat.appendReplacement(sb, val);
		}
		mat.appendTail(sb);
		return sb.toString();
	}

	private void hdlPsaSetMac(DtoRec nod_rec, MutableNode nod, CsoGvzGra gra) throws Exception {
		for (Entry<String, Object> atr: nod.attrs()) {
			String key = atr.getKey();
			if (isStr(key) && key.startsWith("psa_set_")) {
				key = key.substring("psa_set_".length());
				String mac_val = (String) atr.getValue();
				String val = getPsaRplMacVal(nod_rec, gra, mac_val);
				if (val != null) {
					setAtr(nod, key, val);
				}
			}
		}
	}


	private String getPsaRplMacVal(DtoRec nod_rec, CsoGvzGra gra, String mac) throws Exception {
		if (mac.startsWith("$")) {
			String var_idn = mac.substring(1);
			mac = gra.getAtr(var_idn);
		}
		Pattern pat = Pattern.compile("^<if>(.+?)<then>(.*?)((<elseif>.+?<then>.*?)*)(<else>(.*?))?$", Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(mac);
		if (!mat.find()) {
			throw new RuntimeException("Invalid 'psa_set_'-macro: '"+mac+"'");
		}
		LinkedHashMap<String, String> cnd_val_map = new LinkedHashMap<String, String>();
		cnd_val_map.put(mat.group(1), mat.group(2));
		String elif_lis = mat.group(3);
		if (isStr(elif_lis)) {
			for (String elif: elif_lis.split("<elseif>")) {
				String[] spl = elif.split("<then>");
				String cnd = spl[0];
				if (!isStr(cnd)) {
					continue;
				}
				String val = "";
				if (spl.length == 2) {
					val = spl[1];
				}
				cnd_val_map.put(cnd, val);
			}
		}
		String ret = null;
		for (Entry<String, String> ent: cnd_val_map.entrySet()) {
			String cnd = ent.getKey().trim()
					.replace("\r", "")
					.replace("\n", "")
					.replace("\\n", "")
					.replace("\t", "");
			if (!isStr(cnd)) {
				continue;
			}
			if (isMat(nod_rec.dto, nod_rec.row, cnd)) {
				String val = ent.getValue();
				if (val == null) {
					val = "";
				}
				ret = val;
			}
			
		}
		String else_val = mat.group(6);
		if (ret == null && else_val != null) {
			ret = else_val;
		}
		if (ret != null) {
			ret = ret.replace("\\n", "\n")
					.replace("<qte>", "\"");
			if (ret.startsWith("$")) {
				ret = getPsaRplMacVal(nod_rec, gra, ret);
			}
			return ret;
		}
		return null;
	}
	
	private static boolean isMat(PscDto dto, int row, String usx_mat_str) throws Exception {
        UsxPar usx_mat_obj = new UsxPar(usx_mat_str);
    	boolean fnd_boo = true;
    	for (Ent que_ent: usx_mat_obj.getParLis()) {
        	String que_dat = que_ent.Val;
        	if (que_dat == null) {
        		que_dat = "";
        	}	
        	String fld_dat = dto.getDat(que_ent.Key, row);
        	if (!fld_dat.equals( que_dat)) {
        		fnd_boo = false;
        		break;
        	}
        }
    	return fnd_boo;
    }


	private String creFncObjLnk() {
		return null;
	}


	private static String rplFldTpl(PscDto dto, int row, String ori_lbl) throws Exception {
		Matcher mat = FLD_TPL_PAT.matcher(ori_lbl);
		StringBuffer sb = new StringBuffer();
		while (mat.find()) {
			String rpl_str = "<<#"+mat.group(1)+"#>>";
			mat.appendReplacement(sb, rpl_str);
		}
		mat.appendTail(sb);
		String new_lbl = sb.toString();
		new_lbl = IplGlbCsoVarMsgUti.rplMsgVar(new_lbl, dto, row, null, null, null, IplGlbCsoGvzGraUti::esc);
		return new_lbl;
	}
	
	private static String esc(String str) {
		return StringEscapeUtils.escapeXml(str);
	}

	private static String getDtoRecClzTit(DtoRec rec) throws Exception {
		if (rec.dto.hasSupDto("PSA_ORG_EXT")) {
			return rec.dto.getDto("PSA_ORG_EXT").getTit();
		} else if (rec.dto.hasSupDto("PSA_PRS_EXT")) {
			return rec.dto.getDto("PSA_PRS_EXT").getTit();
		} else if (rec.dto.hasSupDto("PSA_PRS_INT")) {
			return rec.dto.getDto("PSA_PRS_INT").getTit();
		} else if (rec.dto.hasSupDto("PSA_PRO_OBJ")) {
			return rec.dto.getDto("PSA_PRO_OBJ").getTit();
		} else if (rec.dto.hasSupDto("PSA_PRO_SAL")) {
			return rec.dto.getDto("PSA_PRO_SAL").getTit();
		} else {
			String dto_tit = rec.dto.getTit();
			if (isStr(dto_tit)) {
				return dto_tit;
			}
		}
		return null;
	}


	public static String getNodLblAsStr(MutableNode nod) {
		Label lbl_obj = getNodLbl(nod);
		return lbl_obj.value();
	}
	
	public static Label getNodLbl(MutableNode nod) {
		return (Label)nod.get("label");
	}

	public static void setNodLbl(MutableNode nod, String lbl, boolean is_html) {
		if (is_html) {
			lbl = "<"+lbl+">";
		}
		nod.add(Attributes.attr("label", Label.raw(lbl)));
	}

	public static String genDatNodIdn(PscDto roo_dto, int row) throws Exception {
		return roo_dto.getBas().getDsc()+"__"+roo_dto.getDat("PSC_GID", row);
	}


	public CsoGvzGra creLvlGraFroCycGra (String cyc_gra_dot, String roo_nod_idn, int max_dph) throws Exception {
		
		CsoGvzGra cyc_gra = new CsoGvzGra(cyc_gra_dot);
		for (MutableNode nod: cyc_gra.gra.nodes()) {
			String nod_idn = getNodIdn(nod);
			if (!nod_idn.equals(nod_idn.toLowerCase())) {
				throw new RuntimeException("Use only lower case letters for node IDNs in cyclic graphs. Bad IDN found: "+ nod_idn);
			}
		}
		CsoGvzGra lvl_gra = cyc_gra.cpyWitOutNodAndEdg();
		MutableNode roo_nod = cyc_gra.getNodByIdn(roo_nod_idn);
		
//		MutableNode src_nod = roo_nod;
		MutableNode new_roo_nod = Factory.mutNode(roo_nod_idn+"_LV0");
		new_roo_nod.add(roo_nod.attrs());
		setAtr(new_roo_nod, "psa_cyc_nod_idn", roo_nod_idn);
		setAtr(new_roo_nod, "psa_dph_lvl", 0);
		lvl_gra.gra.add(new_roo_nod);
		for (int lvl = 0; lvl<max_dph; lvl++) {
			Set<MutableNode> lvl_nod_set = lvl_gra.getNodsByAtrVal("psa_dph_lvl", lvl);
			for (MutableNode lvl_nod: lvl_nod_set) {
				String cyc_nod_idn = getAtr(lvl_nod, "psa_cyc_nod_idn");
				MutableNode cyc_nod = cyc_gra.getNodByIdn(cyc_nod_idn);
				for (Link cyc_lnk: cyc_nod.links()) {
					MutableNode cyc_tgt = getTgtNod(cyc_lnk);
					String cyc_tgt_idn = getNodIdn(cyc_tgt);
					MutableNode lvl_tgt = Factory.mutNode(cyc_tgt_idn+"_LV"+(lvl+1));
					lvl_tgt.add(cyc_tgt.attrs());
					setAtr(lvl_tgt, "psa_cyc_nod_idn", cyc_tgt_idn);
					setAtr(lvl_tgt, "psa_dph_lvl", lvl+1);
					lvl_gra.gra.add(lvl_tgt);
					creLnk(lvl_nod, lvl_tgt, cyc_lnk.attrs());
				}
			}
		}
		if (false) {
			opnDotFil(SSN, cyc_gra, false);
			opnDotFil(SSN, lvl_gra, false);
			opnDotFil(SSN, cyc_gra, true);
			opnDotFil(SSN, lvl_gra, true);
		}
		return lvl_gra;
	}

	private <T> T getAtr(MutableGraph gra, String key) {
		return (T) gra.graphAttrs().get(key);
	}
	
	private <T> T getAtr(MutableNode nod, String key) {
		return (T) nod.attrs().get(key);
	}

	private void setAtr(MutableNode nod, String key, Object val) {
		nod.attrs().add(Attributes.attr(key, val));
	}
	
	private <T> T getAtr(Link lnk, String key) {
		return (T) lnk.attrs().get(key);
	}

//	public static Link creLnk(MutableNode src_nod, MutableNode tgt_nod) {
//		return creLnk(src_nod, tgt_nod, null);
//	}
	
	public static Link creLnk(MutableNode src_nod, MutableNode tgt_nod, MutableAttributed<Link, ForLink> add_atr) {
//	public static Link creLnk(MutableNode src_nod, MutableNode tgt_nod, MutableAttributed<Link, ForLink> mut_lnk_atr) {
		Link new_lnk = src_nod.linkTo(tgt_nod);
		if (add_atr != null) {
			new_lnk.add(add_atr);
		}
		src_nod.links().add(new_lnk);
		 // the one which is added is not this
		new_lnk = src_nod.links().get(src_nod.links().size() - 1);
		return new_lnk;
	}
	
	public static void opnDotFil(PscSsn ssn, CsoGvzGra gra, boolean srt_lin_for_cmp) throws Exception {
		String dot_dat = gra.getDotDat(srt_lin_for_cmp);
		String fil_nam = "graph_defin.dot";
		IplDlgCsoUti.savStrAsTmpFil(dot_dat, true, ssn.getGui(), fil_nam);
	}

	public static MutableNode getTgtNod(Link lnk) {
		return (MutableNode)lnk.to().asLinkSource();
	}


	public static String getNodIdn(MutableNode nod) {
		return nod.name().value();
	}
	
	public static void setNodIdn(MutableNode nod, String nod_idn) {
		nod.setName(nod_idn);
	}
		
	public static class CsoGvzGra {
		public final MutableGraph gra;
		private Map<String, DtoRec> nod_idn_2_dto_map = new HashMap<>();
		public CsoGvzGra (MutableGraph mut_gra) {
			this.gra = mut_gra;
		}
		
		public CsoGvzGra(String dot_dat) throws IOException {
			this(new Parser().read(dot_dat));
		}

		public DtoRec getNodRec(MutableNode nod) {
			return getNodRec(getNodIdn(nod));
		}
		
		public DtoRec getNodRec(String nod_idn) {
			return nod_idn_2_dto_map.get(nod_idn);
		}

		public void setNodRec(MutableNode nod, DtoRec rec) {
			setNodRec(getNodIdn(nod), rec);
		}

		public void setNodRec(String nod_idn, DtoRec rec) {
			nod_idn_2_dto_map.put(nod_idn, rec);
		}

		public MutableNode getRooNod() {
			Set<MutableNode> roo_nod_lst = getRooNodSet();
			if (roo_nod_lst.isEmpty()) {
				throw new RuntimeException("No root node found");
			}
			if (roo_nod_lst.size()>1) {
				throw new RuntimeException("Root node not unique: "+roo_nod_lst);
			}
			return new ArrayList<>(roo_nod_lst).get(0);
		}

		private Set<MutableNode> getRooNodSet() {
			Set<MutableNode> not_roo_nod_lst = new HashSet<MutableNode>();
			for (MutableNode nod: gra.nodes()) {
				for (Link lnk: nod.links()) {
					not_roo_nod_lst.add(getTgtNod(lnk));
				}
			}
			Set<MutableNode> roo_nod_lst = new HashSet<MutableNode>();
			roo_nod_lst.addAll(gra.nodes());
			roo_nod_lst.removeAll(not_roo_nod_lst);
			return roo_nod_lst;
		}

		public Set<MutableNode> getNodsByAtrVal(String atr_key, Object atr_val) {
			// TODO Index
			HashSet<MutableNode> ret_nod_set = new HashSet<MutableNode>();
			for (MutableNode nod : gra.nodes()) {
				Object nod_atr_val = nod.attrs().get(atr_key);
				if (atr_val == null) {
					if (nod_atr_val == null) {
						ret_nod_set.add(nod); // both null = match
					}
				} else {
					if (nod_atr_val == null) {
						continue;
					}
					// both not null
					if (nod_atr_val instanceof String || atr_val instanceof String) {
						// at least one string, the other will become a string too
						if (!(nod_atr_val instanceof String)) {
							nod_atr_val = ""+nod_atr_val;
						}
						if (!(atr_val instanceof String)) {
							atr_val = ""+atr_val;
						}
					}
					if (atr_val.equals(nod_atr_val)) {
						ret_nod_set.add(nod);
					}
				}
			}
			return ret_nod_set;
		}

		// TODO case sensitivity klären (wegen Eintrag in PSC_TAG.PAR)
		public MutableNode getNodByIdn(String nod_idn) {
			// TODO index graph for faster traversion
			for (MutableNode nod : gra.nodes()) {
				String nod_idn_i = nod.name().value();
				if (nod_idn.equals(nod_idn_i)) {
					return nod;
				}
			}
			return null;
		}

		public CsoGvzGra cpyWitOutNodAndEdg() {
			MutableGraph new_gra = Factory.mutGraph(gra.name().value());
			new_gra.setDirected(gra.isDirected());
			new_gra.setStrict(gra.isStrict());
			new_gra.setCluster(gra.isCluster());
			new_gra.nodeAttrs().add(gra.nodeAttrs());
			new_gra.linkAttrs().add(gra.linkAttrs());
			new_gra.graphAttrs().add(gra.graphAttrs());
			return new CsoGvzGra(new_gra);
		}
		
		public String getDotDat() throws IOException {
			return getDotDat(false);
		}
		
		public String getDotDat(boolean srt_lin_for_cmp) throws IOException {
			return gra.toString();
		}
			
		public <T> T getAtr(String key) {
			return (T) gra.graphAttrs().get(key);
		}
		
		public <T> T getAtr(String key, T dfl) {
			T ret = getAtr(key);
			if (ret == null) {
				return dfl;
			} else {
				return ret;
			}
		}
		
		public void setAtr(String key, Object val) {
			gra.graphAttrs().add(key, val);
		}
	}
	
	public static class DtoRec {
		public final PscDto dto;
		public final int row;
		public DtoRec (PscDto dto, int row) {
			this.dto = dto;
			this.row = row;
		}
	}

	/* ____________________________ GENERATED CODE ____________________________ */

	public IplGlbCsoGvzGraUti(PscSsn ssn) {
		this(ssn, "");
	}

	public IplGlbCsoGvzGraUti(PscSsn ssn, String par) {
		SSN = ssn;
		PAR = new UsxPar(par);
		LOG = JobThr.getJobThrPar() != null ? JobThr.getJobThrPar().getLog() : null;
	}

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		SSN = ssn;
		RUN(ssn, "");
	}

	@Override
	public PscSsn getGuiSsn() {
		return SSN;
	}

	@Override
	public JobLog getJobLog() {
		return LOG;
	}

	public static int RUN(PscSsn ssn, String par) throws Exception {
		IplGlbCsoGvzGraUti inst = new IplGlbCsoGvzGraUti(ssn, par);
		inst.run();
		return IplDlgCsoUti.isJobEndReq() ? -1 : 0;
	}


	/* ________________________ END GENERATED CODE ____________________________ */
}