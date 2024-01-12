// Last Update by user CUSTOMIZER at 20200922085430
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psa.ifc.BlbLoa;
import de.pisa.psa.ifc.PsaGlbChe;
import de.pisa.psa.ifc.PsaUti;
import de.pisa.psa.ifc.PscGid;
import de.pisa.psa.ifc.RefUti;
import de.pisa.psa.ifc.SavPnt;
import de.pisa.psa.ifc.StrBuf;
import de.pisa.psa.ssn.PsaSsn;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.dto.PscFld;
// Last Update by user CUSTOMIZER at 20180703081848
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.PscFrm;
import de.pisa.psc.srv.gui.PscGui;
import de.pisa.psc.srv.sio.PscOut;
import de.pisa.psc.srv.svc.PscPckImp;
import de.pisa.psc.srv.svc.PscUti;
import de.pisa.psc.srv.svc.LamUti.Runnable_WithExceptions;
import de.pisa.psc.srv.svc.LamUti.Supplier_WithExceptions;
import de.pisa.psc.srv.svc.PscUsxPar.Ent;
import de.pisa.psc.srv.sys.PscDtoIpl;
import de.pisa.psc.srv.sys.PscUsx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.pisa.psc.srv.svc.PscUti.isStr;
import static de.pisa.psc.srv.svc.PscUti.isTrue;


import org.apache.log4j.Level;

import de.pisa.psa.dto.UsxPar;
import de.pisa.psa.dto.psa_ifc.PsaDynJav;

/** CSO_INS_UTI 
 * 
 * Installation utilities
 * To use after loader import of DTOs, IPLs or Dyn-Javas to create/compile them.
 * Standalone class - no dependencies.
 * 
 * @author geger
 * */

public class IplGlbCsoInsUti {

	public static final String DEL_USX_QUE_SEP = "|#SEP#|";
	public static final String DEL_USX_QUE_SEP__PAT_ESC = Pattern.quote(DEL_USX_QUE_SEP);
//	public static final String DEL_USX_QUE_SEP__RPL_ESC = Matcher.quoteReplacement(DEL_USX_QUE_SEP);

	PscSsn SSN = null;
	
	String DYN_JAV_QUE = "";
	
	String IPL_DTO_QUE = "";
	String IPL_DLG_QUE = "";
	String IPL_GLB_QUE = "";
	String IPL_WSV_QUE = "";
	String IPL_LOA_QUE = "";
	
	// order matters here!
	String DTO_DSC_QUE = "";
	
	/** USX query for deleting records. Separate multiple operations by separator |#SEP#|
	 * DTO:=PSC_TAG  QUE:=NAM=NAM | TIT_NAM  MAX:=2 |#SEP#|  DTO:=PSC_ENV_GLB  QUE:=NAM=TEST123  MAX:=50  Optional parameters: NO_ACC, NO_LCK, NO_USX, NO_LNK
	 *  */
	String DEL_USX_QUE_PAR_LIS = "";
	
	String PRE_USX_LIS = "";
	String PST_USX_LIS = "";
	
	/**
	 * Add menus, syntax: KEY->DIALOG(,KEY->DIALOG)... 
	 * example: MY_KEY->PSA_TOP_TLS - add all (template) tags with DLG=MY_KEY to the (CSO version of) menu PSA_TOP_TLS
	 */
	String ADD_MEN_VIA_KEY = "";
	
	int QUE_MAX = 150; // if more DTOs or IPLs found, throw error, maybe query was bad (like %) and found all
	
	Level GUI_LOG_LVL = Level.DEBUG;

	private Level getGuiLogLvl() {
		return GUI_LOG_LVL;
	}
	
	private IplGlbCsoInsUti(PscSsn ssn, String DTO_DSC_QUE, String DYN_JAV_QUE, String IPL_DTO_QUE, String IPL_DLG_QUE, String IPL_GLB_QUE, 
			String IPL_WSV_QUE, String IPL_LOA_QUE, String ADD_MEN_VIA_KEY, String DEL_USX_QUE_PAR_LIS, String PRE_USX_LIS, String PST_USX_LIS) {
		SSN = ssn;
		this.DTO_DSC_QUE = DTO_DSC_QUE;
		this.DYN_JAV_QUE = DYN_JAV_QUE;
		this.IPL_DTO_QUE = IPL_DTO_QUE;
		this.IPL_DLG_QUE = IPL_DLG_QUE;
		this.IPL_GLB_QUE = IPL_GLB_QUE;
		this.IPL_WSV_QUE = IPL_WSV_QUE;
		this.IPL_LOA_QUE = IPL_LOA_QUE;
		this.ADD_MEN_VIA_KEY = ADD_MEN_VIA_KEY;
		this.DEL_USX_QUE_PAR_LIS = DEL_USX_QUE_PAR_LIS;
		this.PRE_USX_LIS = PRE_USX_LIS;
		this.PST_USX_LIS = PST_USX_LIS;
	}
	
	private IplGlbCsoInsUti(PscSsn ssn, Object ori_dyn_jav_obj) throws Exception {
		SSN = ssn;
		List<String> map_fld_lis = Arrays.asList("DTO_DSC_QUE", "DYN_JAV_QUE", "IPL_DTO_QUE", "IPL_DLG_QUE", "IPL_GLB_QUE", "IPL_WSV_QUE", 
				"IPL_LOA_QUE", "ADD_MEN_VIA_KEY", "DEL_USX_QUE_LIS", "PRE_USX_LIS", "PST_USX_LIS");
		for (Field src_jav_fld: ori_dyn_jav_obj.getClass().getDeclaredFields()) {
			String fld_nam = src_jav_fld.getName();
			if (!map_fld_lis.contains(fld_nam)) {
				continue;
			}
			src_jav_fld.setAccessible(true);
			String val = (String) src_jav_fld.get(ori_dyn_jav_obj);
			Field wri_jav_fld = IplGlbCsoInsUti.class.getDeclaredField(fld_nam);
			wri_jav_fld.setAccessible(true);
			wri_jav_fld.set(this, val);
		}
	}

	public static void RUN(PscSsn ssn, Object ori_dyn_jav_obj) throws Exception {
		IplGlbCsoInsUti obj = new IplGlbCsoInsUti(ssn, ori_dyn_jav_obj);
		obj.run();
	}
	
	@Deprecated
	public static void RUN(PscSsn ssn, String DTO_DSC_QUE, String DYN_JAV_QUE, String IPL_DTO_QUE, String IPL_DLG_QUE, String IPL_GLB_QUE, String IPL_WSV_QUE, String IPL_LOA_QUE, String ADD_MEN_VIA_KEY) throws Exception {
		RUN(ssn, DTO_DSC_QUE, DYN_JAV_QUE, IPL_DTO_QUE, IPL_DLG_QUE, IPL_GLB_QUE, IPL_WSV_QUE, IPL_LOA_QUE, ADD_MEN_VIA_KEY, "");
	}

	@Deprecated
	public static void RUN(PscSsn ssn, String DTO_DSC_QUE, String DYN_JAV_QUE, String IPL_DTO_QUE, String IPL_DLG_QUE, String IPL_GLB_QUE, String IPL_WSV_QUE, String IPL_LOA_QUE, String ADD_MEN_VIA_KEY, String DEL_USX_QUE_LIS) throws Exception {
		IplGlbCsoInsUti obj = new IplGlbCsoInsUti(ssn, DTO_DSC_QUE, DYN_JAV_QUE, IPL_DTO_QUE, IPL_DLG_QUE, IPL_GLB_QUE, IPL_WSV_QUE, IPL_LOA_QUE, ADD_MEN_VIA_KEY, DEL_USX_QUE_LIS, "", "");
		obj.run();
	}
	
	public void run() throws Exception {
		if (SSN.getUic() != 2000) {
			throw new RuntimeException(" You need to be CUSTOMIZER to run an installation ;) ");
		}
		logNfo("Installation started");
		exePreUsxLis();
		delRec();
		cplBasIpl();
		addMenViaKey();
		cplDynJavByQue();
		cplIplByQue("GLB", IPL_GLB_QUE);
		cplIplByQue("DTO", IPL_DTO_QUE);
		cplIplByQue("DLG", IPL_DLG_QUE);
		cplIplByQue("WSV", IPL_WSV_QUE);
		cplIplByQue("LOA", IPL_LOA_QUE);
		creDtoByQue();
		logNfo("Clearing chache...");
		((PsaSsn)SSN).clrChe("PRI_MEM NO_BOX");
		exePstUsxLis();
		logNfo("Installation done");
	}
	
	private void exePreUsxLis() throws Exception {
		if (!isStr(PRE_USX_LIS)) {
			return;
		}
		logNfo("Executing installation's PRE-userexits ...");
		exeUsxLis(PRE_USX_LIS);
	}
	
	private void exePstUsxLis() throws Exception {
		if (!isStr(PST_USX_LIS)) {
			return;
		}
		logNfo("Executing installation's POST-userexits ...");
		exeUsxLis(PST_USX_LIS);
		logNfo("Clearing chache...");
		((PsaSsn)SSN).clrChe("PRI_MEM NO_BOX");
	}

	private void exeUsxLis(String usx_lis) throws Exception {
		if (!isStr(usx_lis)) {
			return;
		}
		for (String raw_usx_str: usx_lis.split(",")) {
			String usx_str = raw_usx_str.trim();
			if (!isStr(usx_str)) {
				continue;
			}
			exeUsx(usx_str);
		}
	}

	private void exeUsx(String usx_str) throws Exception {
		try {
			String ori_usx_str = usx_str;
			logNfo("Executing installation userexit '"+ori_usx_str+"' ...");
			boolean is_glb_ipl = false;
			if (usx_str.startsWith("GLB:")) {
				is_glb_ipl = true;
				usx_str = usx_str.substring("GLB:".length());
			}
			String[] par_pai = usx_str.split("\\.");
			final String err_msg = "A valid user exit definition for this feature must have the format "
					+ "'MY_REPOSITORY_CLASS.MY_METHOD_NAME'. It can have a prefix 'GLB:' to address global IPLs instead of DynJavas."
					+ " This is not a valid format: "+ori_usx_str;
			if (par_pai.length != 2) {
				throw new RuntimeException(err_msg);
			}
			String cla_nam = par_pai[0].trim();
			String mth_nam = par_pai[1].trim();
			if (!isStr(cla_nam) || !isStr(mth_nam)) {
				throw new RuntimeException(err_msg);
			}
			if (is_glb_ipl) {
				String jav_cla_nam = "IplGlb" + PscUsx.creNam(cla_nam);
				Class<?> cla = PscUsx.loaGlbIpl(cla_nam);
				if (cla == null) {
		        	throw new ClassNotFoundException("Class "+jav_cla_nam+" could not be loaded");
		        }
				Method run_mth = cla.getMethod(mth_nam, PscSsn.class, String.class);
				run_mth.invoke(null, SSN, "");
			} else { // DynJav
				String cal_par = "CLS:="+cla_nam+"  MTH:="+mth_nam;
				PsaDynJav.repCal(SSN, PscSsn.class, SSN, cal_par, cal_par);
			}
		} catch (Exception exc) {
			logErr("Eror while executing installation user exit '"+usx_str+"'.");
			throw exc;
		}
	}
	

	private void delRec() throws Exception {
		try {
			trn(SSN, ()->
				delRecNoTrn());
		} catch (Exception exc) {
			logNfo("Deletes have been rollbacked due to error ...");
			throw exc;
		}
	}

	private void delRecNoTrn() throws Exception {
		if (DEL_USX_QUE_PAR_LIS == null || DEL_USX_QUE_PAR_LIS.trim().isEmpty()) {
			logNfo("No delete operations defined.");
			return;
		}
		logNfo("Deleting records defined by userexit query parameter...");
		int cnt_all_del = 0;
		String del_usx_que_par_lis = DEL_USX_QUE_PAR_LIS;
		del_usx_que_par_lis = del_usx_que_par_lis.replace(DEL_USX_QUE_SEP.toLowerCase(), DEL_USX_QUE_SEP);
		for (String del_usx_que_par: DEL_USX_QUE_PAR_LIS.split(DEL_USX_QUE_SEP__PAT_ESC)) {
			UsxPar usx_par = new UsxPar(del_usx_que_par);
			String dto_dsc = usx_par.getPar("DTO");
			String que = usx_par.getPar("QUE");
			boolean no_acc = usx_par.getPar("NO_ACC", false);
			boolean no_lck = usx_par.getPar("NO_LCK", false);
			boolean no_usx = usx_par.getPar("NO_USX", false);
			boolean no_lnk = usx_par.getPar("NO_LNK", false);
			if (!isStr(dto_dsc) || !isStr(que)) {
				throw new RuntimeException("In the delete userexit query: The parameters DTO and QUE are mandatory! Original parameter string: "+del_usx_que_par);
			}
			int max_del = usx_par.getPar("MAX", Integer.MAX_VALUE);
			if (max_del == Integer.MAX_VALUE) {
				throw new RuntimeException("In the delete userexit query: For safety reasons the parameter MAX is mandatory! Original parameter string: "+del_usx_que_par);
			}
			PscDto dto = SSN.newDto(dto_dsc, no_lnk, no_usx, no_acc, no_lck);
			que = que.replace("=", ":=");
			setUsxQue(dto, que);
			int num_rec = 0;
			try {
				while (dto.fetNxt()) {
					num_rec++;
					if (num_rec > max_del) { // double checking
						throw new RuntimeException("In the delete userexit query: Delete-query was going to delete more records than speciafied by MAX parameter! Original parameter string: "+del_usx_que_par);
					}
					dto.delDat(1);
					cnt_all_del++;
				}
			} finally {
				dto.fetCls();
			}
			logNfo(num_rec+ " records deleted by query parameters: "+del_usx_que_par);
		}
		logNfo("Deleting done. "+cnt_all_del+ " records deleted it total by all query parameters: "+DEL_USX_QUE_PAR_LIS);
	}

	private void addMenViaKey() throws Exception {
		if (!PscUti.isStr(ADD_MEN_VIA_KEY)) {
			logNfo("No menu added");
			return;
		}
		logNfo("Adding menus...");
		SavPnt sav = null;
		try {
			sav = new SavPnt(SSN);
			
			for (String men_def: ADD_MEN_VIA_KEY.split(",")) {
				if (!PscUti.isStr(men_def) || !men_def.contains("->") || men_def.split("->").length != 2) {
					throw new RuntimeException("Invalid key-menu-pair: " + men_def);
				}
				String key = men_def.split("->")[0].replace(" ", "");
				String dlg =  men_def.split("->")[1].replace(" ", "");
				if (!PscUti.isStr(key) || !PscUti.isStr(dlg)) {
					throw new RuntimeException("Invalid key-menu-pair: " + men_def);
				}
				logNfo("Handling key-menu-pair: " + men_def);
				PscDto dlg_dto = newDto(SSN, "PSC_DLG", "NAM", "'"+dlg+"'");
				if (dlg_dto == null || dlg_dto.numRec() != 1) {
					throw new RuntimeException("Invalid dialog: " + dlg);
				}
				boolean is_cso = dlg_dto.getDat("PSC_OWN", 1).equals("2000");
				if (!is_cso) {
					PscDto lnk_dto = SSN.newDto("PSC_ENV_LNK", false, false, true, false);
					lnk_dto.setQue("LNG", "'DLG'");
					lnk_dto.setQue("NAM", "'"+dlg+"'");
					String cso_dlg;
					if (lnk_dto.fetDat() == 1) {
						cso_dlg = lnk_dto.getDat("DAT", 1);
					} else {
						cso_dlg = creCsoDlgAndLnk(dlg_dto);
					}
//					String cso_dlg = PsaDto.getFldDat(SSN, "PSC_ENV_LNK", "NAM", "'"+dlg+"'", "DAT");
//					if (!PscUti.isStr(cso_dlg)) {
//						throw new RuntimeException("Please overload dialog '"+dlg+"'. Otherwise can't add menus there...");
//					}
					dlg = cso_dlg;
					dlg_dto = newDto(SSN, "PSC_DLG", "NAM", "'"+dlg+"'");
					if (dlg_dto == null || dlg_dto.numRec() != 1) {
						throw new RuntimeException("Missing customized dialog: " + dlg);
					}
				}
				PscDto tag_dto = SSN.newDto("PSC_TAG");
				tag_dto.setMax(100);
				tag_dto.setQue("DLG", "'"+key+"'");
				tag_dto.fetDat();
				for (int i = 1; i <= tag_dto.numRec(); i++) {
					if (!chkTagInDlg(tag_dto.getDat("NAM", i), dlg)) {
						tag_dto.setDat("DLG", i, dlg);
					}
				}
				tag_dto.putDat();
				logNfo("Added "+tag_dto.numRec()+" menu tags to " + dlg);
			}
			
			sav.end();
		} finally {
			if (sav != null) {
				sav.abo();
			}
		}
		
	}

	private String creCsoDlgAndLnk(PscDto psa_dlg_dto) throws Exception { PscGui gui = SSN.getGui();
		String dlg = psa_dlg_dto.getDat("NAM", 1);
		if (!"Y".equalsIgnoreCase(gui.wriBox("Dialog '"+dlg+"' does not belong to customizer. Overload and link it?", "Q", "Q"))) {
			throw new RuntimeException("ABORTED by user --> rollback (OK)");
		}
		PscDto lnk_dto = SSN.newDto("PSC_ENV_LNK", false, false, true, false);
		String cso_dlg = creCsoDlgNam(dlg);
		
		PscDto cso_dlg_dto = SSN.newDto("PSC_DLG", false, false, true, false);
		cso_dlg_dto.setQue("NAM", "'"+cso_dlg+"'");
		boolean has_cso_dlg = cso_dlg_dto.fetDat() == 1;
		if (has_cso_dlg) {
			if (!"Y".equalsIgnoreCase(gui.wriBox("CST-Dialog '"+cso_dlg+"' for PSA-Dialog '"+dlg+"' already exists. Link it?", "Q", "Q"))) {
				throw new RuntimeException("ABORTED by user --> rollback (OK)");
			}
		} else {
			cso_dlg_dto.insRow(1);
			cso_dlg_dto.copRow(1, psa_dlg_dto, 1);
//			PsaDto.setSysDat(cso_dlg_dto, "PSC_OWN", 1, "2000");
//			PsaDto.setSysDat(cso_dlg_dto, "PSC_ACC", 1, "D,R0,D43");
			
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_GID"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_CRE"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_UPD"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_MOD"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_VER"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_CLS"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_OWN"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_ACC"), 1, true);
			cso_dlg_dto.clrDrt(cso_dlg_dto.getFld("PSC_LCK"), 1, true);
			
			cso_dlg_dto.setDat("NAM", 1, cso_dlg);
			cso_dlg_dto.setDat("SUP", 1, dlg);
			cso_dlg_dto.setDat("TIH", 1, "y");
			
//			cso_dlg_dto.setDat("NAM", 1, cso_dlg);
//			cso_dlg_dto.setDat("TYP", 1, psa_dlg_dto.getDat("TYP", 1));
			
			
			cso_dlg_dto.setMod(true);
			cso_dlg_dto.putDat();
		}
		lnk_dto.insRow(1);
		lnk_dto.setDat("LNG", 1, "DLG");
		lnk_dto.setDat("NAM", 1, dlg);
		lnk_dto.setDat("DAT", 1, cso_dlg);
		lnk_dto.putDat();
		
		logNfo("Dialog '"+dlg+"' was overloaded and linked to dialog '"+cso_dlg+"'");
		
		return cso_dlg;
	}

	private String creCsoDlgNam(String dlg) throws Exception {
		if (!dlg.startsWith("PSA_")) {
			throw new RuntimeException("!dlg.startsWith(PSA_)");
		}
		String cst_pfx = SSN.getEnv("PSC_CST_PFX");
		if (!PscUti.isStr(cst_pfx)) {
			throw new RuntimeException("!PscUti.isStr(cst_pfx)");
		}
		String cso_dlg = cst_pfx + dlg.substring(3);
		return cso_dlg;
	}

	private boolean chkTagInDlg(String nam, String dlg) throws Exception {
		PscDto tag_dto = SSN.newDto("PSC_TAG");
		tag_dto.setQue("DLG", "'"+dlg+"'");
		tag_dto.setQue("NAM", "'"+nam+"'");
		return tag_dto.fetDat() > 0;
	}

	private void cplBasIpl() throws Exception {
		logNfo("Compiling basic IPLs (if they exist)");
		Level old_log_lvl = GUI_LOG_LVL;
		GUI_LOG_LVL = Level.ERROR;
		
		cplIplByQue("DLG", "CSO_UTI", false);
		cplIplByQue("GLB", "CSO_LOG_UTI_ITF", false);
		cplIplByQue("GLB", "CSO_DTO_ITR", false);
		cplDynJavByQue("CSO_INV | CSO_INV_GLB_IPL", false);
		
		GUI_LOG_LVL = old_log_lvl;
		logNfo("Compiling basic IPLs done");
	}

	private void creDtoByQue() throws Exception {
		if (!PscUti.isStr(DTO_DSC_QUE)) {
			logNfo("No database objects created");
			return;
		}
		PscDto dto_dto = SSN.newDto("PSC_DTO");
		dto_dto.setMax(QUE_MAX);
		dto_dto.setQue("NAM", DTO_DSC_QUE);
		int num_rec = dto_dto.fetDat();
		if (num_rec > 0) {
			logNfo(num_rec +" DTOs found");
		} else {
			throw new RuntimeException("No DTOs found - query cant be not empty than");
		}
		List<Integer> row_lis = new ArrayList<>();
		for (int i = 1; i <= num_rec; i++) {
			row_lis.add(i);
		}
		
		List<Integer> ord_row_lis = new ArrayList<>();
		String ord_str = "";
		if (DTO_DSC_QUE.contains("&") || DTO_DSC_QUE.contains("?")) {
			ord_str = "No order will be applied creating DTOs because query contaians a '&' or a '?'";
			ord_row_lis = row_lis;
		} else {
			String ord_que = DTO_DSC_QUE;
			ord_que = ord_que.replace("'", " ");
			ord_que = ord_que.replace("|", " ");
			ord_que = rplAll(ord_que, "  ", " ");
			String dsc_lis[] = ord_que.split(" ");
			
			for (String dsc: dsc_lis) {
				Integer fnd_row = fndRow(dto_dto, "NAM", dsc);
				if (fnd_row != null) {
					ord_row_lis.add(fnd_row);
					ord_str += "->" + dto_dto.getDat("NAM", fnd_row);
				}
			}
			if (ord_str.isEmpty()) {
				ord_str = "No order will be applied creating DTOs";
			} else {
				ord_str = "DTOs will be created in following order: " + ord_str;
				int no_ord_num = row_lis.size()-ord_row_lis.size();
				if (no_ord_num > 0) {
					ord_str += " + <"+no_ord_num +" without order>";
				}
			}
			for (Integer i: row_lis) {
				if (!ord_row_lis.contains(i)) {
					ord_row_lis.add(i);
				}
			}
		}
		logNfo(ord_str);
		for (Integer i: ord_row_lis) {
			creDbo(SSN, dto_dto.getDat("NAM", i));
		}
	}
	
	private Integer fndRow(PscDto dto, String fld_dsc, String fnd_dat) throws Exception {
        int num_rec = dto.numRec();
        PscFld fnd_in_fld =  dto.getFld( fld_dsc);
        for (int row = 1; row <= num_rec; row++) {
            if (dto.getDat(fnd_in_fld, row).equals( fnd_dat)) {
                return row;
            }
        }
        return null;
    }
	
	private static String rplAll(String str, String rpl, String ins) {
		String old;
		do {
			old = str;
			str = str.replace(rpl, ins);
		} while (!str.equals(old));
		return str;
	}
	
	private void cplIplByQue(String typ, String que) throws Exception {
		cplIplByQue(typ, que, true);
	}

	private void cplIplByQue(String typ, String que, boolean exc_bad_que) throws Exception {
		if (!PscUti.isStr(que)) {
			logNfo("No IPLs of type "+typ+" compiled");
			return;
		}
		PscDto ipl_dto = SSN.newDto("PSC_IPL");
		ipl_dto.setMax(QUE_MAX);
		ipl_dto.setQue("TYP", "'"+typ+"'");
		ipl_dto.setQue("NAM", que);
		int num_rec = ipl_dto.fetDat();
		if (num_rec > 0) {
			logNfo(num_rec +" IPLs of type "+typ+" found");
		} else {
			if (exc_bad_que) {
				throw new RuntimeException("No IPLs of type "+typ+" found - query cant be not empty than");
			}
		}
		for (int i = 1; i <= num_rec; i++) {
			cplIplSrc(SSN, ipl_dto.getDat("NAM", i), typ);
		}
	}

	private void cplDynJavByQue() throws Exception {
		cplDynJavByQue(DYN_JAV_QUE, true);
	}
	
	private void cplDynJavByQue(String que, boolean exc_bad_que) throws Exception {
		if (!PscUti.isStr(que)) {
			logNfo("No dyn java compiled");
			return;
		}
		PscDto dyn_jav_dto = SSN.newDto("PSA_DYN_JAV");
		dyn_jav_dto.setMax(QUE_MAX);
		dyn_jav_dto.setQue("NAM", que);
		int num_rec = dyn_jav_dto.fetDat();
		if (num_rec > 0) {
			logNfo(num_rec +" dyn javas found");
		} else {
			if (exc_bad_que) {
				throw new RuntimeException("No dyn javas found - query cant be not empty than");
			}
		}
		for (int i = 1; i <= num_rec; i++) {
			cplDynJavSrc(SSN, dyn_jav_dto.getDat("NAM", i));
		}
	}
	
	/** Copied from IplDlgCsoUti (Geger) */
	static void trn(PscSsn ssn, Runnable_WithExceptions run_obj) throws Exception {
		SavPnt sav = null;
		try {
			sav = new SavPnt(ssn);
			run_obj.accept();
			sav.end();
		} finally {
			if (sav != null) {
				sav.abo();
			}
		}
	}
	
	/**
	 * Copied from IplDlgCsoUti (Geger)
	 * 
	 * Set query on dto via usx params. </br>
	 * The special logical OR || can be used for inter field queries. </br>
	 * Example: FLD1:='que1' FLD2:=que2% || FLD2:=&que3% ...
	 * 
	 * @param dto
	 * @param usx_que_str
	 * @throws Exception
	 */
	private static void setUsxQue(PscDto dto, String usx_que_str) throws Exception {
		if (!PscUti.isStr(usx_que_str)) {
			return;
		}
//	    	dto.delQue();
		String[] or_que_str_lis = usx_que_str.split("\\|\\|");
		boolean is_or = or_que_str_lis.length > 1;
		String or_pfx = is_or ? "|" : "";
		int and_grp_num = 0;
		Set<PscFld> que_fld_set = new HashSet<>();
		for (String or_que_str : or_que_str_lis) {
			UsxPar usx_que = new UsxPar(or_que_str);
			boolean bui_and_grp = is_or && usx_que.numPar() > 1;
			String and_grp_pfx = "";
			if (bui_and_grp) {
				and_grp_num++;
				and_grp_pfx = "@" + and_grp_num + "@";
			}
			for (Ent ent : usx_que.getParLis()) {
				String fld_nam = ent.Key;
				PscFld fld = dto.getFld(fld_nam);
				if (fld == null) {
					throw new RuntimeException("Invalid field: '" + fld_nam + "'");
				}
				if (!que_fld_set.contains(fld)) {
					que_fld_set.add(fld);
				} else {
					throw new RuntimeException("Duplicate field use not allowed! Field name: '" + fld_nam + "'");
				}
				String fld_que = ent.Val;
				if (PscUti.isStr(fld_que)) {
					if (fld_que.equals("Y") || fld_que.equals("N")) {
						fld_que = fld_que.toLowerCase();
					} else if (fld_que.equalsIgnoreCase("null") || fld_que.equalsIgnoreCase("!null")) {
						fld_que = fld_que.toUpperCase();
					}
					fld_que = fld_que.replace("&", " & ").replace("|", " | ");
					fld_que = or_pfx + and_grp_pfx + fld_que;
				}
				dto.setQue(fld, fld_que);
			}
		}
	}

	public static void creDbo(PscSsn ssn, String dsc) throws Exception {
		if ( dsc.startsWith("PSC_") && ssn.getAcc().chkSys()==false ) return;
		PscDto dto = null;
		boolean che = PscChe.getChe();
		try {
			PscChe.setChe(false);
			dto = ssn.newDto(dsc, null, false);
		}
		catch (Exception exc) {
    		SQLException sql_exc = ( exc instanceof SQLException ? (SQLException)exc : null);
			if ( sql_exc!=null && sql_exc.getErrorCode()==17002 ) {
				String sql_msg = sql_exc.getMessage();
				ssn.wriTxt(sql_msg);
				return;
			}
			throw exc;
		}
		finally {
			PscChe.setChe(che);
		}

		dto.creDbo();
		ssn.wriMsg("PSC_SYS_DTO_TAB_CRE", dsc);
		ssn.getCon().sndAct();
	}
	
	
	public static void cplIplSrc(PscSsn ssn, String nam, String typ) throws Exception {
		if (!PscUti.isStr(nam) || !PscUti.isStr(typ)) {
			throw new RuntimeException("!PscUti.isStr(nam) || !PscUti.isStr(typ)");
		}
		PscDtoIpl dyn_dto = (PscDtoIpl) ssn.newDto("PSC_IPL");
		dyn_dto.setQue("NAM", "'"+nam+"'");
		dyn_dto.setQue("TYP", "'"+typ+"'");
		if (dyn_dto.fetDat()!=1) {
			throw new RuntimeException("Ipl '"+nam+"' of type '"+typ+"' not found!");
		}
		if (!dyn_dto.creClsDat(1)) {
			if (dyn_dto.chkMod(1) ) {
				dyn_dto.aboDat();
			}
			throw new PscExc(PscOut.LEV_CTL, EnuExc.SYS_IPL_ERR_CPL);
		}
		if (dyn_dto.chkMod(1)) {
			dyn_dto.putDat();
		}
		ssn.getCon().sndAct();
	}
	
	public static void cplDynJavSrc(PscSsn ssn, String nam) throws Exception {
		if (!PscUti.isStr(nam)) {
			throw new RuntimeException("!PscUti.isStr(nam)");
		}
		PscDto dyn_jav_dto = ssn.newDto("PSA_DYN_JAV");
		dyn_jav_dto.setQue("NAM", "'"+nam+"'");
		if (dyn_jav_dto.fetDat()!=1) {
			throw new RuntimeException("Dyn. Java '"+nam+"' not found!");
		}
		String cod = dyn_jav_dto.getDat("COD", 1);
		StrBuf err = new StrBuf();
		Set<String> mis_cls = new HashSet<String>();
		Class<?> cls = BlbLoa.getCls(ssn, nam, cod, true, err, mis_cls);
		boolean suc;
		if (cls!=null) {
			ssn.wriMsg("PSC_SYS_IPL_CLS_CRE", nam);
			suc = true;
		}
		else {
			String err_str = err.getStr();
			boolean err_msg = true;
			suc = hdlComErr(dyn_jav_dto, err_str, mis_cls, 1, err_msg);
		}
		if (!suc) {
			throw new RuntimeException("Can't compile dyn java '"+nam+"'");
		}
		ssn.getCon().sndAct();
	}
	
	 public static PscFrm creJobForGlbIpl(PscGui gui, String glb_ipl_nam) throws Exception {
	    	PscFrm job_frm = gui.newFrm("PSA_JOB_ESC");
			PscDto job_dto = job_frm.getDynDto();
			job_dto.insRow(1);
			job_dto.setDat("USX_CLA", 1, "$CSO_INV_GLB_IPL");
			job_dto.setDat("USX_FNC", 1, "INV");
			job_dto.setDat("USX_PAR", 1, "INV_IPL:="+glb_ipl_nam+".RUN");
			job_dto.setDat("USR", 1, "SALESADMIN");
			job_dto.setDat("LOG_LEV", 1, "INFO");
			job_dto.setDat("IDN", 1, glb_ipl_nam);
			return job_frm;
	    }

	
	/**
	 * handle compile error
	 * @param err_str error string
	 * @param mis_cls missing classes
	 * @param row current row
	 * @param err_msg show error message
	 * @throws Exception 
	 */
	private static boolean hdlComErr(PscDto dyn_jav_dto, String err_str, Set<String> mis_cls, int row, boolean err_msg) 
		throws Exception 
	{
		PscSsn ssn = dyn_jav_dto.getSsn();
		if (mis_cls!=null && !mis_cls.isEmpty()) {
			String cod = dyn_jav_dto.getDat("COD", row);
			String nam = dyn_jav_dto.getDat("NAM", row);
			String imp_stm = PscPckImp.getImpStm(mis_cls);
			if (imp_stm.length()!=0) {
				String cod_tok[] = PscPckImp.splSrcPck(cod);
				cod = cod_tok[0]+"\n"+cod_tok[1]+"\n"+imp_stm+cod_tok[2];
				cod = cod.trim();
				StrBuf err = new StrBuf();
				Class<?> cls = BlbLoa.getCls(ssn, nam, cod, true, err, null);
				err_str = err.getStr();
				if (cls!=null) {
					ssn.wriMsg("PSA_DON");
					dyn_jav_dto.setDat("COD", row, cod);
					dyn_jav_dto.putDat();
					return true;
				}
			}
		}
		if (err_str!=null && err_msg) {
			ssn.wriTxt(err_str);
		}
		return false;
	}
	
	// log utilities ---------------------------------------
	
	
	private void logLvl(String txt, Level lev, Throwable exc) {
		logLvlToGui(txt, lev, exc);
	}

	private void logLvl(String txt, Level lev) {
		logLvl(txt, lev, null);
	}

	private void logNfo(String msg) throws Exception {
		logLvl(msg, Level.INFO);
	}

	private void logWrn(String msg) throws Exception {
		logLvl(msg, Level.WARN);
	}

	private void logErr(String msg) throws Exception {
		logLvl(msg, Level.ERROR);
	}

	private void logErr(String msg, Throwable exc) throws Exception {
		logLvl(msg, Level.ERROR, exc);
	}

	private void logDbg(String msg) throws Exception {
		logLvl(msg, Level.DEBUG);
	}

	private void logLvlToGui(String msg, Level lev) {
		logLvlToGui(msg, lev, null);
	}

	private void logLvlToGui(String msg, Level lev, Throwable exc) {
		if (getGuiLogLvl() == null || !lev.isGreaterOrEqual(getGuiLogLvl())) {
			return;
		}
		String log_str = lev.toString() + ": " + msg;
		if (exc != null) {
			log_str += "\r\n";
			log_str += getStkTrcStr(exc);
		}
		PsaSsn.wriTxtImm(SSN, log_str);
	}

	private static String getStkTrcStr(Throwable exc) {
		PrintWriter pw = null;
		try {
			final StringWriter sw = new StringWriter();
			pw = new PrintWriter(sw, true);
			exc.printStackTrace(pw);
			String err_log = "";
			if (exc instanceof PscExc) {
				PscExc psc_exc = ((PscExc) exc);
				String psc_msg = psc_exc.getMsg();
				String psc_tx1 = psc_exc.getTx1();
				String psc_tx2 = psc_exc.getTx2();
				String psc_tx3 = psc_exc.getTx3();

				err_log += "PSC: (Msg: " + psc_msg + ", Txt1: " + psc_tx1 + ", Txt2: " + psc_tx2 + ", Txt3: " + psc_tx3
						+ ", ErrNum: " + psc_exc.getNum() + ", ErrLev: " + psc_exc.getLev() + "), ";
			}
			err_log += sw.getBuffer().toString();
			return err_log;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	private static PscDto newDto(PscSsn ssn, String dto_dsc, String gid) throws Exception {
		return newDto(ssn, dto_dsc, gid, false, false);
	}

	private static PscDto newDto(PscSsn ssn, String dto_dsc, String gid, boolean no_acc, boolean no_lck)
			throws Exception {
		if (PscGid.isVld(gid)) {
			PscDto dto = PsaUti.newDto(ssn, dto_dsc, false, false, no_acc, no_lck);
			if (dto != null) {
				dto.setQue("PSC_GID", gid);
				if (dto.fetDat() == 1) {
					return dto;
				}
			}
		}
		return null;
	}

	private static PscDto newDto(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que) throws Exception {
		return newDto(ssn, dto_dsc, que_fld_dsc, que, false, false);
	}

	private static PscDto newDto(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que, boolean no_acc,
			boolean no_lck) throws Exception {
		if (!PscUti.isStr(que)) {
			throw new Exception("Using an empty query in newDto() not allowed !");
		}
		PscDto dto = PsaUti.newDto(ssn, dto_dsc, false, false, no_acc, no_lck);
		dto.setQue(que_fld_dsc, que);
		if (dto.fetDat() > 0) {
			return dto;
		}
		return null;
	}

}