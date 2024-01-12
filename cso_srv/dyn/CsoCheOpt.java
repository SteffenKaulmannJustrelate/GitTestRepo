// Last update by user CUSTOMIZER on host dmoref at 20190717133542
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.pisa.psa.dto.psa_scn.JobLog;
import de.pisa.psa.dto.psa_scn.JobThr;
import de.pisa.psa.ifc.MapSrt;
import de.pisa.psa.ifc.NewSsn;
import de.pisa.psa.ifc.PsaUti;
import de.pisa.psc.srv.che.CheItf;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.dto.PscFld;
import de.pisa.psc.srv.glb.PscChe;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.gui.PscGui;
import de.pisa.psc.srv.svc.PscUti;

public class CsoCheOpt {

	static final String[] Frm_Lst = {
	// Dialogs
		"PSC_WEB_POR",
		"PSA_VOC_DIC_EAC",
		"PSA_QUO_SUP_WEB_POR",
		"DMO_SVC_OPN_ESL",
		"DMO_PRO_SAL_ART_ESL",
		"PSA_WPM_EVT_REG",
		"PSA_EPT_ACT_ESC",
		"OBJ_FOL_PRC_ESC",
		"PSA_FOL_PRC_ESC",
		"PSA_CAL_ESC",
		"PSA_APM_ESC",
		"PSD_CPD_EMA_ESC",
		"PSA_CPD_LET_ESC",
		"PSA_ORG_HOC_SSC",
		"PSA_PRS_HOE_SSC",
		"PSA_RSM_HOE_ESL",
		"PSA_CCP_SSC",
		"PSA_FUL_IDX_ESL",
		"CST_ORG_EXT_ESC",
		"PSA_ORG_EXT_ESC",
		"PSA_PRS_EXT_ESC",
		"PSA_FOL_ESC",
		"PSA_OPP_ESC",
		"PSA_QUO_ESC",
		"PSA_ORD_ESC",
		"CST_SVC_TRB_ESC",
		"PSA_SVC_TRB_ESC",
		"CSO_SVC_QUO_ESC",
		"PSA_SVC_QUO_ESC",
		"CSO_SVC_ORD_ESC",
		"PSA_SVC_ORD_ESC",
		"PSA_CTR_ORD_ESC",
		"PSA_CMP_ESC",
		"PSA_NLT_ESC",
		"CSO_EVT_ESC",
		"PSA_EVT_ESC",
		"PSA_PRO_SAL_ESL",
		"PSA_PRO_SAL_ART_ESL",
		"PSA_TSK_ESC",
		"PSA_APM_ESC",
		"PSA_CAL_ESC",
		"PSA_CPD_EMA_ESC",
		"PSA_CPD_LET_ESC",
		"PSA_DOC_ESC",
		"PSA_EPT_ACT_ESC",
		"PSA_CLD_CON_ACT_SSC",
		"PSA_CLD_UCR_CHT_SSC",
		"PSA_SAV_QUE_BAS_ESC",
	};

	static final String[] Dto_Lst = {
	};

	private static final int	Pad_Num = 8; 

	private static final SimpleDateFormat	Dat_Fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final DecimalFormat		Num_Fmt = new DecimalFormat("#,###");
	private static final DecimalFormat		Dbl_Fmt = new DecimalFormat("#,###.0");

	private JobLog MbrLog;
	private PscGui MbrGui;

	public CsoCheOpt() {
		MbrLog = JobThr.getJobLog(CsoCheOpt.class);
		MbrGui = null;
	}

	private void addGenOvr(StringBuilder str) {
        Runtime rt = Runtime.getRuntime();
        long max_mem = rt.maxMemory();
        long tot_mem = rt.totalMemory();
        long fre_tot_mem = rt.freeMemory();
        long usd_mem = tot_mem-fre_tot_mem;
        long mem_usd = usd_mem/(1024*1024);
        long mem_max = max_mem/(1024*1024);
        long mem_prc = 100 * mem_usd / mem_max;
        int usr_cnt = PscChe.numSsn();
        long mem_usr = (usr_cnt>0) ? (mem_usd / usr_cnt) : 0;
        str.append(Dat_Fmt.format(new Date()));
        str.append("\r\n");
        str.append("Memory:   ");
        str.append(PsaUti.padStr(Num_Fmt.format(mem_usd), 5, ' ', false));
        str.append(" Mb / ");
        str.append(PsaUti.padStr(Num_Fmt.format(mem_max), 5, ' ', false));
        str.append(" Mb ( ");
        str.append(PsaUti.padStr(Long.toString(mem_prc), 3, ' ', false));
        str.append(" % )");
        str.append("\r\n");
        str.append("User:     ");
        str.append(PsaUti.padStr(Integer.toString(usr_cnt), 5, ' ', false));
        str.append("    / ");
        str.append(PsaUti.padStr(Num_Fmt.format(mem_usr), 5, ' ', false));
        str.append(" Mb");
        str.append("\r\n");
        str.append("\r\n");
	}
	
	private void addCheOvr(CheItf<?> che, String typ, StringBuilder str) {
		Date lst_del = che.getLstDel();
		Date old_ent = che.getOldEnt();
		str.append(typ);
		str.append(":          ");
		str.append(che.getClass().getName());
		str.append("\r\n");
		str.append("Since:        ");
		str.append(Dat_Fmt.format(lst_del));
		str.append(" (").append(getTimStr(lst_del)).append(')');
		str.append("\r\n");
		str.append("Oldest entry: ");
		if (old_ent!=null) {
			str.append(Dat_Fmt.format(old_ent));
			str.append(" (").append(getTimStr(old_ent)).append(')');
		}
		str.append("\r\n");
		str.append("Size:         ");
		str.append(PsaUti.padStr(Num_Fmt.format(che.cnt()), Pad_Num, ' ', false));
		str.append(" / ");
		str.append(PsaUti.padStr(Num_Fmt.format(che.getSiz()), Pad_Num, ' ', false));
		str.append(" ( ");
		int siz = che.getSiz();
		int cnt_prc = (siz<=0) ? 0 : (100*che.cnt()/siz);
		str.append(PsaUti.padStr(Integer.toString(cnt_prc), 3, ' ', false));
		str.append(" % )");
		str.append("\r\n");
		str.append("Hit/Miss:     ");
		str.append(PsaUti.padStr(Num_Fmt.format(che.getHitCnt()), Pad_Num, ' ', false));
		str.append(" / ");
		str.append(PsaUti.padStr(Num_Fmt.format(che.getMisCnt()), Pad_Num, ' ', false));
		str.append(" ( ");
		str.append(PsaUti.padStr(Integer.toString((int)che.getHitRat()), 3, ' ', false));
		str.append(" % )");
		str.append("\r\n");
		str.append("\r\n");
	}
	
	private String getTimStr(Date dat) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime dat_tim = LocalDateTime.ofInstant(dat.toInstant(), ZoneId.systemDefault());
		Duration dur = Duration.between(dat_tim, now);
		long days = dur.toDays();
		long hours = dur.toHours();
		long minutes = dur.toMinutes();
		if (days>0) {
			double day_dbl = hours/24D;
			return Dbl_Fmt.format(day_dbl) + " days";
		}
		if (hours>0) {
			return hours + " hour"+(hours==1?"":"s");
		}
		return minutes + " minute" + (minutes==1?"":"s");
	}

	private void addCheLis(CheItf<?> che, String typ, StringBuilder str) {
		int max_len = 0;
		Map<String, Long> map = che.getTim();
		map = MapSrt.srtVal(map, true);
		for (String key : map.keySet()) {
			max_len = Math.max(max_len, key.length());
		}
		str.append(typ);
		str.append(" (");
		str.append(che.cnt());
		str.append(" / ");
		str.append(che.getSiz());
		str.append("): \r\n");
		str.append(PsaUti.padStr("", max_len+22, '-'));
		str.append("\r\n");
		for (Map.Entry<String, Long> ent : map.entrySet()) {
			str.append(PsaUti.padStr(ent.getKey(), max_len, ' '));
			str.append(" - ");
			Long val = ent.getValue();
			if (val!=null && val>0) {
				str.append(Dat_Fmt.format(new Date(val)));
			}
			str.append("\r\n");
		}
		str.append("\r\n");
	}

	private void logWrn(String txt) {
		MbrLog.logWrn(txt);
		if ( MbrGui!=null ) {
			MbrGui.getSsn().wriTxt(txt);
		}
	}

	private void logErr(String txt, Throwable exc) {
		MbrLog.logErr(txt, exc);
		if ( MbrGui!=null ) {
			MbrGui.getSsn().wriExc(exc);
			MbrGui.getSsn().wriTxt(txt);
		}
	}

	private void wriCheNfo() {
		StringBuilder str = new StringBuilder();
		
		addGenOvr(str);
		
		addCheOvr(PscChe.lisDto(), "Dto", str);
		addCheOvr(PscChe.lisFrm(), "Frm", str);
// 		addCheOvr(PscChe.lisSel(), "Sel", str);
// 		addCheOvr(PscChe.lisMen(), "Men", str);
 		addCheLis(PscChe.lisDto(), "Dto", str);
 		addCheLis(PscChe.lisFrm(), "Frm", str);
// 		addCheLis(PscChe.lisSel(), "Sel", str);
// 		addCheLis(PscChe.lisMen(), "Men", str);
	}

	private void iniOptObj(PscDto dto, String typ) throws Exception {
		String [] lng_lst = dto.getSsn().getDbi().getLng();
		dto.delQue();
		dto.setQue("TYP", typ);
		int cnt = dto.cntDat();
		if ( cnt==0 ) {
			String[] lst=null;
			if ( "FRM".equals(typ) ) {
				lst = Frm_Lst;
			} else if ( "DTO".equals(typ) ) {
				lst = Dto_Lst;
			} else {
				return;
			}
			for (String frm : lst) {
				if ( !"PSC_TRE_DTO".equals(frm) && !frm.endsWith("ESB") ) {
					dto.insRow(1);
					dto.setDat("NAM", 1, frm);
					dto.setDat("TYP", 1, typ);
					for (String lng : lng_lst) {
						dto.setDat(String.format("LNG_%s", lng), 1, "y");
					}
					dto.putDat();
					dto.clrDat();
				}
			}
		}
	}

	private void cheFrm(PscDto dto) throws Exception {
		PscSsn ssn = dto.getSsn();
		logWrn("Start caching standard forms");
		dto.delQue();
		dto.setQue("TYP", "FRM");
		String[] lng_lst = ssn.getDbi().getLng();
		PscSsn tmp_ssn = NewSsn.newSsn(ssn);
		try {
			PscGui gui = NewSsn.newGui(tmp_ssn);
			PscFld[] fld_lst = new PscFld[lng_lst.length];
			int i=-1;
			for(String lng:lng_lst) {
				fld_lst[++i] = dto.getFld(String.format("LNG_%s", lng));
			}
			while ( dto.fetNxt() ) {
				String nam = dto.getDat("NAM", 1);
				for (PscFld fld : fld_lst) {
					String lng = dto.getDat(fld, 1);
					if ( PscUti.isTrue(lng) ) {
						String dsc = fld.getDsc();
						tmp_ssn.setLng(lng);
						try {
							logWrn(String.format("Caching form %s %s sub forms included",nam,dsc.substring(4)));
							gui.newFrm(nam).creSubFrm();
						} catch(Exception exc) {
							logErr(String.format("Caching form %s %s failed by %s",nam,dsc.substring(4),exc.getMessage()),exc);
						}
					}
				}
			}
		} finally {
			dto.fetCls();
			tmp_ssn.exiSsn();
		}
		logWrn("Finish caching standard forms");
	}

	private void cheDto(PscDto dto) throws Exception {
		PscSsn ssn = dto.getSsn();
		logWrn("Start caching standard database objects");
		dto.delQue();
		dto.setQue("TYP", "DTO");
		try {
			while( dto.fetNxt() ) {
				String nam = dto.getDat("NAM", 1);
				String lng = dto.getDat("LNG_GER", 1);
				if ( PscUti.isTrue(lng) ) {
					logWrn(String.format("Caching database object %s", nam));
					ssn.newDto(nam);
				}
			}
		} finally {
			dto.fetCls();
		}
		logWrn("Finish caching standard databas objects");
	}

	public void run(PscSsn ssn) throws Exception {
//		int		cor = Runtime.getRuntime().availableProcessors();
		long	mxv = Runtime.getRuntime().maxMemory();
		if ( mxv>=6442450944L ) {	// minimum configuration 4GB uses form preload
			PscDto dto = ssn.newDto("CSO_CHE_OPT");
			iniOptObj(dto, "DTO");
			iniOptObj(dto, "FRM");
			cheFrm(dto);
			cheDto(dto);
		}
/*
		if ( Sta_Dto_Che==null ) {
			Set<String> set = PscChe.lisDto().getKey();
			Sta_Dto_Che = set.toArray(new String[set.size()]);
		}
		if ( Sta_Frm_Che==null ) {
			Set<String> set = PscChe.lisFrm().getKey();
			Sta_Frm_Che = set.toArray(new String[set.size()]);
		}
*/
	}

	private void difDtoChe(PscSsn ssn) throws Exception  {
		PscDto dto = ssn.newDto("CSO_CHE_OPT");
		String[] lng_lst = ssn.getDbi().getLng();
		Set<String> lng_set = new HashSet<>(Arrays.asList(lng_lst));
		for (String nam : PscChe.lisDto().getKey()) {
			String val = nam.replaceFirst("[#]", "");
			String lng = val.substring(val.length()-3);
			String str=null;
			if ( lng_set.contains(lng) ) {
				str = val.substring(0, val.length()-4);
			} else {
				str = val;
			}
			dto.setQue("NAM", str);
			dto.setQue("TYP", "DTO");
			int cnt = dto.fetDat();
			if ( cnt==0 ) {
				dto.insRow(1);
				dto.setDat("NAM", 1, str);
				dto.setDat("TYP", 1, "DTO");
				for (String l : lng_lst) {
					dto.setDat(String.format("LNG_%s", l), 1, "n");
				}
				dto.putDat();
			}
		}
	}

	private void difFrmChe(PscSsn ssn) throws Exception  {
		PscDto dto = ssn.newDto("CSO_CHE_OPT");
		String[] lng_lst = ssn.getDbi().getLng();
		Set<String> lng_set = new HashSet<>(Arrays.asList(lng_lst));
		for (String nam : PscChe.lisFrm().getKey()) {
			String val = nam.replaceFirst("[#]", "");
			String lng = val.substring(val.length()-3);
			String str=null;
			if ( lng_set.contains(lng) ) {
				str = val.substring(0, val.length()-4);
			} else {
				str = val;
			}
			dto.setQue("NAM", str);
			dto.setQue("TYP", "FRM");
			int cnt = dto.fetDat();
			if ( cnt==0 ) {
				dto.insRow(1);
				dto.setDat("NAM", 1, str);
				dto.setDat("TYP", 1, "FRM");
				for (String l : lng_lst) {
					dto.setDat(String.format("LNG_%s", l), 1, "n");
				}
				dto.putDat();
			}
		}
	}

	public void shwAllCheDif(PscSsn ssn) throws Exception {
		difDtoChe(ssn);
		difFrmChe(ssn);
		/*
		MbrLog.logWrn("Cache difference");
		for (String lng:ssn.getDbi().getLng()) {
			shwCheDif("Dto", lng);
		}
		for (String lng:ssn.getDbi().getLng()) {
			shwCheDif("Frm", lng);
		}
		*/
//		wriCheNfo();
	}

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		MbrGui = gui;
		run(ssn);
	}

	public static void run(PscSsn ssn, String par) throws Exception {
		CsoCheOpt opt = new CsoCheOpt();
		try {
			opt.run(ssn);
		} catch(Exception exc) {}
	}

	public static void difChe(PscSsn ssn, String par) throws Exception {
		CsoCheOpt opt = new CsoCheOpt();
		try {
			opt.wriCheNfo();
			opt.shwAllCheDif(ssn);
		} catch(Exception exc) {}
	}

}