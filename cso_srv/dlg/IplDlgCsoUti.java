// Last Update by user CUSTOMIZER at 20200922085406
import static de.pisa.psc.srv.svc.PscUti.isStr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.pisa.psa.com.map.AdrLoc;
import de.pisa.psa.com.map.AdrLocFnd;
import de.pisa.psa.com.map.AdrLocRes;
import de.pisa.psa.dto.PsaDto;
import de.pisa.psa.dto.PsaDtoIpl;
import de.pisa.psa.dto.UsxPar;
import de.pisa.psa.dto.psa_doc.BlbUtl;
import de.pisa.psa.dto.psa_doc.FilUtl;
import de.pisa.psa.dto.psa_scn.JobThr;
import de.pisa.psa.frm.PsaFrm;
import de.pisa.psa.ifc.NewSsn;
import de.pisa.psa.ifc.PsaGlbChe;
import de.pisa.psa.ifc.PsaUti;
import de.pisa.psa.ifc.PscGid;
import de.pisa.psa.ifc.SavPnt;
import de.pisa.psa.ifc.TriSta;
import de.pisa.psa.ssn.PsaSsn;
import de.pisa.psc.srv.dsi.PscDbi;
import de.pisa.psc.srv.dsi.PscLng;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.dto.PscFld;
import de.pisa.psc.srv.dto.PscRel;
import de.pisa.psc.srv.glb.EnuExc;
import de.pisa.psc.srv.glb.PscChe;
import de.pisa.psc.srv.glb.PscExc;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.gui.PscDlg;
import de.pisa.psc.srv.gui.PscFrm;
import de.pisa.psc.srv.gui.PscGui;
import de.pisa.psc.srv.sio.PscOut;
import de.pisa.psc.srv.svc.LamUti.Runnable_WithExceptions;
import de.pisa.psc.srv.svc.LamUti.Supplier_WithExceptions;
import de.pisa.psc.srv.svc.PscUsxPar.Ent;
import de.pisa.psc.srv.svc.PscUti;
import de.pisa.psc.srv.sys.PscDtoEnv;
import de.pisa.psc.srv.xml.PscExp;
import de.pisa.psc.srv.xml.PscImp;

/** CSO_UTI */
public interface IplDlgCsoUti {

	static final String BIG_USX_QUE_OR = "||";
	static final String NEW_LIN_STR = "\r\n";
	
	static boolean isStrAll(String... all_str) throws Exception {
		if (all_str == null) {
			return false;
		} else if (all_str.length == 0) {
			throw new UnsupportedOperationException();
		}
		for (String str: all_str) {
			if (!isStr(str)) {
				return false;
			}
		}
		return true;
	}
	
	static boolean isStrAny(String... all_str) throws Exception {
		if (all_str == null) {
			return false;
		} else if (all_str.length == 0) {
			throw new UnsupportedOperationException();
		}
		for (String str: all_str) {
			if (isStr(str)) {
				return true;
			}
		}
		return false;
	}
	
	static boolean addDtoCom(PscDto dto, String com_dsc, String rel_dsc) throws Exception {
		PscSsn ssn = dto.getSsn();
		if (hasCom(dto, com_dsc)) {
			return true;
		}
		PscImp sml_dto_imp = dto.getImp();
		boolean no_acc = sml_dto_imp == null ? false : !sml_dto_imp.getAcc();
		boolean no_lck = sml_dto_imp == null ? false : !sml_dto_imp.getLck();
		PscDto com = ssn.newDto(com_dsc, false, false, no_acc, no_lck);
		
		PscRel com_rel = com.getRel(rel_dsc);
		if (com_rel == null) {
			return false;
		}
		dto.addCom(com, com_rel);
		return hasCom(dto, com_dsc);
	}

	static boolean hasCom(PscDto dto, String com_dsc) throws Exception {
		if (dto.getCom(com_dsc) != null) {
			return true;
		}
		String lnk_com_dsc = dto.getSsn().getLnk(com_dsc, "DTO");
		if (PscUti.isStr(lnk_com_dsc) && dto.getCom(lnk_com_dsc) != null) {
			return true;
		}
		return false;
	}
    
	public static void modDatUseRfr (PscDto dto, String dsc, int row, String str) throws Exception {
		PscFld fld = dto.getFld(dsc);
		if ( fld==null ) throw new PscExc(PscOut.LEV_WRN, EnuExc.DTO_PAR_INV_FLD, dsc);
		modDatUseRfr(dto, fld, row, str);
	}
	
	public static void modDatUseRfr (PscDto dto, PscFld fld, int row, String str) throws Exception {
		dto.modDat(fld, row, str);
		// write modification into dialog
		if ( fld.getIdn()>0 ) dto.getSsn().getCon().setDat(fld, row);
	}
	
    public static Set<String> addRec(PscDto dst_dto, String que_fld_dsc, String que) throws Exception {
    	return Priv.addRec(dst_dto, que_fld_dsc, que, false);
    }
    
    public static String addSglRec(PscDto dst_dto, String que_fld_dsc, String que) throws Exception {
    	 Set<String> new_rec_set = Priv.addRec(dst_dto, que_fld_dsc, que, true);
    	 if (new_rec_set.isEmpty()) {
    		 return null;
    	 } else {
    		 return (String) new_rec_set.toArray()[0];
    	 }
    }
    
    static void setUsxQue(PscDto dto, String usx_que_str) throws Exception {
    	setUsxQue(dto, usx_que_str, false);
    }
    
    /** Set query on dto via usx params. </br>
    * The special logical OR || can be used for inter field queries. </br>
    * Example: FLD1:='que1' FLD2:=que2% || FLD2:=&que3% ...
    * @param dto
    * @param usx_que_str
    * @throws Exception 
    */
    static void setUsxQue(PscDto dto, String usx_que_str, boolean add_qte) throws Exception {
    	if (!PscUti.isStr(usx_que_str)) {
    		return;
    	}
//    	dto.delQue();
    	String[] or_que_str_lis = usx_que_str.split("\\|\\|");
    	boolean is_or = or_que_str_lis.length > 1;
    	String or_pfx = is_or ? "|" : "";
    	int and_grp_num = 0;
    	Set<PscFld> que_fld_set = new HashSet<>();
    	String fin_que_str = "";
    	for (String or_que_str: or_que_str_lis) {
    		UsxPar usx_que = new UsxPar(or_que_str);
    		boolean bui_and_grp = is_or && usx_que.numPar() > 1;
    		String and_grp_pfx = "";
    		if (bui_and_grp) {
    			and_grp_num++;
    			and_grp_pfx = "@"+and_grp_num+"@";
    		}
    		for (Ent ent: usx_que.getParLis()) {
    			String fld_nam = ent.Key;
        		PscFld fld = dto.getFld(fld_nam);
        		if (fld == null) {
        			throw new RuntimeException("Invalid field: '"+fld_nam+"'");
        		}
        		if (!que_fld_set.contains(fld)) {
        			que_fld_set.add(fld);
        		} else {
        			throw new RuntimeException("Duplicate field use not allowed! Field name: '"+fld_nam+"'");
        		}
        		String fld_que = ent.Val;
        		if (PscUti.isStr(fld_que)) {
        			if (fld_que.equals("Y") || fld_que.equals("N")) {
        				fld_que = fld_que.toLowerCase();
        			} else if (fld_que.equalsIgnoreCase("null") || fld_que.equalsIgnoreCase("!null")) {
        				fld_que = fld_que.toUpperCase();
        			}
        			fld_que = fld_que.replace("&", " & ").replace("|", " | ");
        			if (add_qte) {
            			fld_que = Priv.buiQuoQue(fld_que, true);
            		}
        			fld_que = or_pfx + and_grp_pfx + fld_que;
        		}
        		dto.setQue(fld, fld_que);
//        		if (!fin_que_str.isEmpty()) {
//        			fin_que_str += " ";
//        		}
//        		fin_que_str += fld_nam + "=" + fld_que;
    		}
    	}
//    	if (!fin_que_str.isEmpty()) {
//    		dto.getDbd().creQue(fin_que_str, 'R');
//    	}
	}
    
    /**
     * Set query on dto via usx query. </br>
     * Example: FLD1:='que1' FLD2:=que2% ...
     * @param dto
     * @param usx_str
     * @throws Exception 
     */
    @Deprecated
    static void setUsxQue(PscDto dto, UsxPar usx_que) throws Exception {
    	for (Ent ent: usx_que.getParLis()) {
    		String fld_que = ent.Val;
    		if (fld_que != null) {
    			if (fld_que.equals("Y") || fld_que.equals("N")) {
    				fld_que = fld_que.toLowerCase();
    			} else if (fld_que.equalsIgnoreCase("null")) {
    				fld_que = fld_que.toUpperCase();
    			}
    		}
    		
			dto.setQue(ent.Key, fld_que);
		}
	}
    
    
    /**
     * get query for a comma separated field</br>
     * example usage: getLisFldQue("pig", ",") for searching exact occurence of "pig" in "duck,pig,house,car", not matching "duck,piggy,house,car"
     */
    static String getLisFldQue(String elm, String sep) {
		if (!PscUti.isStr(elm) || !PscUti.isStr(sep)) {
			return null;
		}
		elm = elm.replace("'", "");
		if (elm.isEmpty() || elm.contains(sep)) {
			return null;
		}
		String que = "'elm' | 'elm,%' | '%,elm' | '%,elm,%'".replace("elm", elm);
		return que;
	}
    
    /**
     * usage example from activity ref dto (PSA_PRO_ACT_REF): IplDlgCsoUti.addRelRec(this, "PSA_ACT", true, oth_pro_gid_set);</br>
     * while 'oth_pro_gid_set'is a set of gids of other projects which activities are to add
     */
    public static Set<String> addRelRec(PscDto rel_dto, String ref_or_agg_com_dsc, boolean is_chd_add, Set<String> oth_obj_que_gid_set) throws Exception {
    	if (!rel_dto.hasSupDto("PSA_REL") || rel_dto.getDto(ref_or_agg_com_dsc) == null) {
    		throw new UnsupportedOperationException();
    	}
    	if (oth_obj_que_gid_set.isEmpty()) {
    		return new HashSet<>();
    	}
        PscSsn ssn = rel_dto.getSsn();
        PscDto drv_dto = rel_dto.getDrv();
        int max_rec = drv_dto.getMax();
        PscDto clo_dto = drv_dto.cloDto();
        PsaDtoIpl.copDtoQue( drv_dto, clo_dto);
        clo_dto.copSrt( drv_dto);
        Set<String> has_gid_set = getDatSet(rel_dto, ref_or_agg_com_dsc+".PSC_GID");
        Set<String> new_gid_set = new HashSet<>();
        String que_fld_dsc = is_chd_add ? "FAT_GID" : "CHD_GID";
        String que = buiQuoQueOr(new ArrayList<>(oth_obj_que_gid_set));
        clo_dto.setQue(que_fld_dsc, que);
        try {
            while ( clo_dto.fetNxt()) {
            	if (!clo_dto.chkAcc(1, 'R')) {
            		continue;
            	}
                String add_gid = clo_dto.getDat( ref_or_agg_com_dsc+".PSC_GID", 1);
                if (has_gid_set.contains( add_gid) || new_gid_set.contains( add_gid)) {
                    continue;
                } else {
                	new_gid_set.add( add_gid);
                }
                int new_row = drv_dto.numRec() + 1;
                if ( new_row > max_rec) {
                    ssn.wriMsg( "PSC_DBD_DTO_FET_MAX", Integer.toString( max_rec), drv_dto.getTit());
                    return new_gid_set;
                }
                drv_dto.addRow( new_row - 1, 1);
                drv_dto.cloRow( new_row, clo_dto, 1);                
            }
        } finally {
            clo_dto.fetCls();
            drv_dto.clnDat();
        }
        return new_gid_set;
    }
    
    
    
    
    
    
    public static Set<String> getDatSet(PscDto dto, String fld_dsc) throws Exception {
    	return getDatSet(dto, fld_dsc, true);
	}
    
    public static Set<String> getDatSet(PscDto dto, String fld_dsc, boolean chk_acc) throws Exception {
    	return getDatSet(dto, fld_dsc, chk_acc, null);
    }
    
    public static Set<String> getDatSet(PscDto dto, String fld_dsc, boolean chk_acc, Collection<Integer> row_whi_lis) throws Exception {
    	HashSet<String> ret_set = new HashSet<>();
    	PscFld fld = dto.getFld(fld_dsc);
    	if (row_whi_lis == null) { // take all
    		row_whi_lis = new ArrayList<>();
    		for (int row = 1; row <= dto.numRec(); row++) {
        		row_whi_lis.add(row);
        	}
    	}
		for (Integer row: row_whi_lis) {
			if (chk_acc && !dto.chkAcc(row, 'R')) {
				continue;
			}
			String dat = dto.getDat(fld, row);
			if (dat != null) {
				ret_set.add(dat);
			}
		}
		return ret_set;
    }

    /**
     * get method from stack trace using regular expression
     * @return method name
     */
    static String getClaAndMthFroStkTrc(Pattern pat) {
    	return Priv.getDscFroStkTrc(pat, StkTrcDscTyp.CLA_AND_MTH);
    }
    
	/**
     * get method from stack trace using regular expression
     * @return method name
     */
    static String getMthFroStkTrc(Pattern pat) {
    	return Priv.getDscFroStkTrc(pat, StkTrcDscTyp.MTH);
    }
    
    /**
     * get class from stack trace using regular expression
     * @return method name
     */
    static String getClaFroStkTrc(Pattern pat) {
    	return Priv.getDscFroStkTrc(pat, StkTrcDscTyp.CLA);
    }
    
    /**
     * get filename (= {class name}.java) from stack trace using regular expression
     * @return method name
     */
    static String getFilFroStkTrc(Pattern pat) {
    	return Priv.getDscFroStkTrc(pat, StkTrcDscTyp.FIL);
    }
    
    enum StkTrcDscTyp {
    	MTH, CLA, FIL, CLA_AND_MTH
    }
    
    /**
     * has method in stack trace?
     * @param mth_nam
     * @return
     */
    static boolean hasMthInStkTrc(String mth_nam) {
    	StackTraceElement[] stk_trc = new Throwable().getStackTrace();
    	for (int i = 0; i< stk_trc.length; i++) {
    		StackTraceElement stk_trc_elm = stk_trc[i];
    		if (stk_trc_elm != null) {
    			String itr_mth_nam = stk_trc_elm.getMethodName();
    			if (itr_mth_nam != null && itr_mth_nam.equals(mth_nam)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    static String rplAll(String str, String rpl, String ins) {
		String old;
		do {
			old = str;
			str = str.replace(rpl, ins);
		} while (!str.equals(old));
		return str;
	}
    
    static void setAccChk(PscDlg dlg, boolean chk_acc_boo) {
    	PscDto dyn_dto = dlg.getDynDto();
    	if (dyn_dto != null) {
    		setAccChk(dlg.getDynDto(), chk_acc_boo);
    	}
    }
    
	static void setAccChk(PscDto dto, boolean chk_acc_boo) {
		PscSsn ssn = dto.getSsn();
		PscImp imp = new PscImp(ssn);
		imp.setAcc(chk_acc_boo);
		dto.setImp(imp);
	}

	
    
    
    /**
     * if: language field: get data from language field; 
     * else: perform normal getDat(..); 
     * returns null if: dto==null, bad row or fld_dsc cant be resolved; 
     * good safe use with: getLngDat(fld_dsc, IplDlgCsoUti.newDto(ssn, dto_dsc, que_fld_dsc, que), 1, lng_str); 
     * @param lng_str can be null
     * 
     */
    static String getLngDat(String fld_dsc, PscDto dto, int row, String lng_str) throws Exception {
    	if (dto == null || row < 1 || row > dto.numRec()) {
			return null;
		}
		PscFld fld = dto.getFld(fld_dsc);
		if (fld == null) {
			return null;
		}
		String dat = null;
		if (PscUti.isStr(lng_str) && fld.getLng()) {
			Map<String, String> mul_lng_dat = PsaUti.getMulLngDat(fld, row, true);
			dat = mul_lng_dat.get(lng_str);
		} else {
			dat = dto.getDat(fld, row);
		}
		return dat;
	}
    
    
	/**
	 * 
	 * @param ori_max_dto_dsc
	 *            max level dto e.g. PSA_ORD_XTD .. you try to get a better here
	 * @param flt_use_dto_dsc
	 *            the used filter dto
	 * @param alw_use_bas_boo
	 *            true = always use the dto level of the filter (ignoring maybe
	 *            missing fields)
	 * @return best level dto to optimize performance at fetch
	 * @throws Exception
	 */
	static String fndBstDtoLvl(PscSsn ssn, String ori_max_dto_dsc, String flt_use_dto_dsc, boolean alw_use_bas_boo,
			Set<String> inc_fld_set) throws Exception {
		String max_dto_dsc = ori_max_dto_dsc;
		if (PscUti.isStr(max_dto_dsc) && PscUti.isStr(flt_use_dto_dsc) && max_dto_dsc.length() > 7
				&& flt_use_dto_dsc.length() > 3) {
			if (!max_dto_dsc.substring(3).equals(flt_use_dto_dsc.substring(3))) { // assure
																					// not
																					// same
																					// level
				PscDto max_dto = ssn.newDto(max_dto_dsc);
				if (max_dto != null) {
					max_dto_dsc = max_dto.getDsc();
					PscDto min_dto = ssn.newDto(flt_use_dto_dsc);
					if (min_dto != null) {
						String min_dto_dsc = min_dto.getDsc();
						ArrayList<String> pth_dto_dsc_lis = new ArrayList<>();

						// collect candidates in super dto path
						if (max_dto_dsc.equals("CSO_ORD_OVR_XTD")) {
							pth_dto_dsc_lis.add("PSA_ORD");
						} else if (max_dto_dsc.equals("CSO_QUO_OVR_XTD")) {
							pth_dto_dsc_lis.add("PSA_QUO");
						} else if (max_dto_dsc.equals("CSO_SVC_TRB_OVR_XTD")) {
							pth_dto_dsc_lis.add("PSA_SVC");
						} else {
							// standard procedure: try to use BASE or XRO if
							// possible (instead of XTD or XRO)
							if (max_dto_dsc.endsWith("_XTD") || max_dto_dsc.endsWith("_XRO")) {
								String max_dto_inf = max_dto_dsc.substring(3, max_dto_dsc.length() - 4);
								String psa_bas_dsc = "PSA" + max_dto_inf;
								pth_dto_dsc_lis.add(psa_bas_dsc);
								String cso_bas_dsc = "CSO" + max_dto_inf;
								pth_dto_dsc_lis.add(cso_bas_dsc);
								if (max_dto_dsc.endsWith("_XTD")) {
									String psa_xro_dsc = psa_bas_dsc + "_XRO";
									pth_dto_dsc_lis.add(psa_xro_dsc);
									String cso_xro_dsc = cso_bas_dsc + "_XRO";
									pth_dto_dsc_lis.add(cso_xro_dsc);
								}
							}
						}

						// check wether candidates are ok
						for (String idx_dto_dsc : pth_dto_dsc_lis) {
							if (chkDtoLvlOk(ssn, idx_dto_dsc, max_dto, min_dto_dsc, alw_use_bas_boo, inc_fld_set)) {
								return idx_dto_dsc; // found a better level !
							}
						}

					}
				}
			}
		}
		return ori_max_dto_dsc;
	}

	/**
	 * 
	 * @param ssn
	 * @param dto_dsc check if this dto lvl is ok
	 * @param max_dto you try to optimize this dto level
	 * @param min_dto_dsc dto should have at least this dto in it's super path (or be the min_dto itself)
	 * @param alw_use_bas_boo just return min_dto_dsc if this is ok and dont look at inc_fld_set
	 * @param inc_fld_set fields which should be included at optimized dto level
	 * @return true if the level is ok
	 * @throws Exception
	 */
	static boolean chkDtoLvlOk(PscSsn ssn, String dto_dsc, PscDto max_dto, String min_dto_dsc, boolean alw_use_bas_boo,
			Set<String> inc_fld_set) throws Exception {
		boolean dto_lvl_ok = false;
		if (max_dto.hasSupDto(dto_dsc)) {
			PscDto dto = ssn.newDto(dto_dsc);
			if (dto != null && dto.hasSupDto(min_dto_dsc)) {
				if (alw_use_bas_boo) {
					dto_lvl_ok = true;
				} else {
					dto_lvl_ok = chkAllFldAvl(dto, inc_fld_set);
				}
			}
		}
		return dto_lvl_ok;
	}

	/**
	 * check if all fields are availible in dto
	 * @param dto
	 * @param inc_fld_set
	 * @return
	 */
	static boolean chkAllFldAvl(PscDto dto, Set<String> inc_fld_set) {
		if (dto == null) {
			return false;
		}
		if (inc_fld_set == null) {
			return true;
		}
		for (String fld_nam : inc_fld_set) {
			PscFld fld = dto.getFld(fld_nam);
			if (fld == null) {
				return false;
			}
		}
		return true;
	}
    
    
    
    /**
     * 
     * @param sta date in PiSA cubes format
     * @param end date in PiSA cubes format
     * @return
     */
    static Long difMil(String sta, String end) {
    	Long dif = null;
    	try {
    		if (sta != null && end != null && sta.length() == 14 && end.length() == 14) {
    			Calendar sta_cal = Calendar.getInstance();
    			sta_cal.setTime(PscUti.getTim(sta));
    			Calendar end_cal = Calendar.getInstance();
    			end_cal.setTime(PscUti.getTim(end));
    			return end_cal.getTimeInMillis() - sta_cal.getTimeInMillis();
        	}
    	} catch (Exception e) {
			// ignore
		}
    	return dif;
    }
    
	static int setBlbAsSalAdm(PscSsn ssn, String blb_nam, byte dat[], String typ, boolean cmp) throws Exception  {
		return trnGet(ssn, ()->{
			int ret = BlbUtl.setBlb(ssn, blb_nam, dat, typ, cmp);
			PscDto blb_dto = fetDtoOne(true, ssn, "PSC_BLB", true, true, "NAM", "'"+blb_nam+"'");
			PsaDto.setSysDat(blb_dto, "PSC_OWN", 1, "1000");
			blb_dto.putDat();
			return ret;
		});
//		PscSsn sal_adm_ssn = null;
//		try {
//			sal_adm_ssn = NewSsn.newSsn(ssn, "SALESADMIN");
//			return BlbUtl.setBlb(sal_adm_ssn, blb_nam, dat, typ, cmp);
//		} finally {
//			if (sal_adm_ssn != null) {
//				sal_adm_ssn.exiSsn();
//			}
//		}
    }
    
    default void newLin(StringBuilder sb, int t, String str) {
		preTab(sb, t, str);
    	newLin(sb);
	}

	default void preTab(StringBuilder sb, int n, String str) {
    	for (int i = 0; i < n; i++) {
    		sb.append("\t");
    	}
    	sb.append(str);
    }
    
    default void newLin(StringBuilder sb) {
    	sb.append(IplDlgCsoUti.NEW_LIN_STR);
    }
    
    
    default void newLin(StringBuilder sb, String str) {
    	newLin(sb, 0, str);
    }
    
    /**
     * Default for method: pause every 500 millis
     * {@link IplDlgCsoUti#jobSle(long, long, boolean)}
     */
    static boolean jobSle (long dur_mil_sec, boolean thr_exc) throws InterruptedException {
    	return jobSle(dur_mil_sec, 500, thr_exc);
    }
    
    /**
     * Default for method: take this thread as job thread
     * {@link IplDlgCsoUti#jobSle(JobThr, long, long, boolean)}
     */
    static boolean jobSle (long dur_mil_sec, long pau_cyc_mil, boolean thr_exc) throws InterruptedException {
    	JobThr job_thr = JobThr.getJobThr();
    	return jobSle(job_thr, dur_mil_sec, pau_cyc_mil, thr_exc);
    }
    
    static boolean jobSle (JobThr job_thr, long dur_mil_sec, long pau_cyc_mil, boolean thr_exc) throws InterruptedException {
    	return jobSle(job_thr, dur_mil_sec, pau_cyc_mil, null, thr_exc);
    }
    
    static Boolean jobSleHidExc (JobThr job_thr, long dur_mil_sec, long pau_cyc_mil, Long trm_tim_mil, boolean thr_exc) {
    	try {
			return jobSle(job_thr, dur_mil_sec, pau_cyc_mil, trm_tim_mil, thr_exc);
		} catch (InterruptedException e) {
			throw new RuntimeException("PiSA-job's end requested", e);
		}
    }
    
    /**
     * Thread sleeps time in millis with pauses if it is a job
     * Furthermore a job terminates at 'trm_tim_mil' (if param not null)
     * At job end it return false or throws exception if requested
     * (sleeping jobs need to be interrupted to end properly in case of shutdown (think of RuntimeHook) )
     * @return 'true' if succesfully sleeped full time or 'false' if interrupted
     * @throws InterruptedException
     */
    static boolean jobSle (JobThr job_thr, long dur_mil_sec, long pau_cyc_mil, Long trm_tim_mil, boolean thr_exc) throws InterruptedException {
    	if (job_thr == null) {
    		Thread.sleep(dur_mil_sec);
    	} else {
    		long sta = System.currentTimeMillis();
        	long end = sta + dur_mil_sec;
        	long now = sta;
        	do {
        		if (!job_thr.isRun() || (trm_tim_mil != null && now > trm_tim_mil)) {
        			if (thr_exc) {
        				throw new InterruptedException("PiSA-job's end requested");
        			} else {
        				return false;
        			}
        		}
        		long rest_sle = end - now;
        		if (rest_sle < 0) {
        			rest_sle = 0;
        		}
        		long sle_tim = pau_cyc_mil < rest_sle ?  pau_cyc_mil : rest_sle;
        		Thread.sleep(sle_tim);
        		now = System.currentTimeMillis();
        	} while (now < end);
    	}
    	return true;
    }
    
    static <T> T deSerBinOfObjCla(byte dat[], Object... obj_lis) throws IOException, ClassNotFoundException {
    	Class<?>[] cla_lis = obj_lis != null ? new Class<?>[obj_lis.length] : null;
    	if (cla_lis != null) {
    		for (int i = 0; i < cla_lis.length; i++) {
        		cla_lis[i] = obj_lis[i].getClass();
    			
    		}
    	}
    	return deSerBinOfCla(dat, cla_lis);
    }
    
    static <T> T deSerBinOfCla(byte dat[], Class<?>... cla_lis) throws IOException, ClassNotFoundException {
		if (dat == null || dat.length == 0) {
			return null;
		}
		try (ByteArrayInputStream byt_inp = new ByteArrayInputStream(dat);
				ObjectInputStream obj_inp_stream = new ObjectInputStream(byt_inp) {
					protected Class<?> resolveClass(ObjectStreamClass obj_str_cla) throws IOException ,ClassNotFoundException {
						if (cla_lis != null) {
							for (int i = 0; i < cla_lis.length; i++) {
								Class<?> cla = cla_lis[i];
								if (cla.getName().equals(obj_str_cla.getName())) {
									return cla;
								}
							}
						}
						return super.resolveClass(obj_str_cla);
					};
				};) {
			
			Object ret = obj_inp_stream.readObject();
			return (T) ret;
		}
	}
    
   
    
    static String savStrAsTmpFil (String str, boolean opn_fil_boo, PscGui gui, String fil_nam) throws Exception {
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
    
    static String getStkTrcStr(Throwable exc) {
		PrintWriter pw = null;
        try {
        	final StringWriter sw = new StringWriter();
        	pw = new PrintWriter( sw, true);
        	  exc.printStackTrace( pw);
              String err_log = "";
              if ( exc instanceof PscExc) {
                  PscExc psc_exc = ( (PscExc) exc);
                  String psc_msg = psc_exc.getMsg();
                  String psc_tx1 = psc_exc.getTx1();
                  String psc_tx2 = psc_exc.getTx2();
                  String psc_tx3 = psc_exc.getTx3();

                  err_log += "PSC: (Msg: " + psc_msg + ", Txt1: " + psc_tx1 + ", Txt2: " + psc_tx2 + ", Txt3: " + psc_tx3 + ", ErrNum: " + psc_exc.getNum() + ", ErrLev: " + psc_exc.getLev() + "), ";
              }
              err_log += sw.getBuffer().toString();
              return err_log;
        } finally {
        	if (pw != null) {
        		pw.close();
        	}
        }
    }
    
    static <T> T wraExc (T dfl_ret, Supplier_WithExceptions<T> spl_fnc) {
    	try {
    		return spl_fnc.get();
		} catch (Exception e) {
			return dfl_ret;
		}
    }
    
 //------ Caching global ENVs
    
    
    /**
	 * Wrappt implementierung {@link PsaUti#getGlbEnv(PscSsn, String)}
	 * Ergebnisse werden immer gecacht. Null-Werte bzw. das Nicht-Auffinden von Variablen wird (als
	 * null) gecacht. Cache-Key wird gebildet aus Variablenname und Sprache.
	 * 
	 * @author geger
	 */
    static String getGlbEnvUseCheSavNul (PscSsn ssn, String env_nam) throws Exception {
    	return Priv.getGlbEnvUseCheIpl(ssn, env_nam, false);
    }
    
   
    /**
     * {@link IplDlgCsoUti#getGlbEnvUseCheSavNul(PscSsn, String)}
     */
    static String getGlbEnvUseCheSavNul (PscSsn ssn, String env_nam, String def_ret) throws Exception {
    	String ret = getGlbEnvUseCheSavNul(ssn, env_nam);
    	return ret == null ? def_ret : ret;
    }
    
    /**
     * {@link IplDlgCsoUti#getGlbEnvUseCheSavNul(PscSsn, String)}
     */
    static boolean getGlbEnvUseCheSavNul (PscSsn ssn, String env_nam, boolean def_ret) throws Exception {
    	String ret = getGlbEnvUseCheSavNul(ssn, env_nam);
    	return ret == null ? def_ret : ret.equalsIgnoreCase("y");
    }
    
    /**
     * {@link IplDlgCsoUti#getGlbEnvUseCheSavNul(PscSsn, String)}
     */
    static int getGlbEnvUseCheSavNul (PscSsn ssn, String env_nam, int def_ret) throws Exception {
    	String ret = getGlbEnvUseCheSavNul(ssn, env_nam);
    	return PsaUti.str2Int( ret, def_ret);
    }
    
    
    
    
    /**
     * {@link IplDlgCsoUti#getGlbEnvUseChe(PscSsn, String)}
     */
    @Deprecated
    static String getGlbEnvUseChe (PscSsn ssn, String env_nam, String def_ret) throws Exception {
    	String ret = getGlbEnvUseChe(ssn, env_nam);
    	return ret == null ? def_ret : ret;
    }
    
    /**
     * {@link IplDlgCsoUti#getGlbEnvUseChe(PscSsn, String)}
     */
    @Deprecated
    static int getGlbEnvUseChe (PscSsn ssn, String env_nam, int def_ret) throws Exception {
        String ret = getGlbEnvUseChe(ssn, env_nam);
        return PsaUti.str2Int( ret, def_ret);
    }
    
    
    /**
     * {@link IplDlgCsoUti#getGlbEnvUseChe(PscSsn, String)}
     */
    @Deprecated
    static boolean getGlbEnvUseChe (PscSsn ssn, String env_nam, boolean def_ret) throws Exception {
    	String ret = getGlbEnvUseChe(ssn, env_nam);
    	return ret == null ? def_ret : ret.equalsIgnoreCase("y");
    }
    
    
	/**
	 * Wrappt implementierung {@link PsaUti#getGlbEnv(PscSsn, String)}
	 * Ergebnisse werden immer gecacht auch wenn es sich um nicht gruppen- oder
	 * userspezifische Variblen handelt. Null wird nicht gecacht. Cache-Key wird
	 * gebildet aus Variablenname und Sprache.
	 * 
	 * @author geger
	 */
    @Deprecated
    static String getGlbEnvUseChe (PscSsn ssn, String env_nam) throws Exception {
    	return Priv.getGlbEnvUseCheIpl(ssn, env_nam, true);
    }

    
    //------ Caching ENVs for user
    
    /**
	 * Wie {@link IplDlgCsoUti#getUsrEnvUseChe(PscSsn, String)} mit dem
	 * Unterschied: Null-Werte bzw. das Nicht-Auffinden von Variablen wird (als
	 * null) gecacht.
	 * 
	 * @author geger
	 */
    static String getUsrEnvUseCheSavNul (PscSsn ssn, String env_nam) throws Exception {
    	return Priv.getUsrEnvUseCheIpl(ssn, env_nam, false);
    }
    
    /**
     * {@link IplDlgCsoUti#getUsrEnvUseCheSavNul(PscSsn, String)}
     */
    static String getUsrEnvUseCheSavNul (PscSsn ssn, String env_nam, String def_ret) throws Exception {
    	String ret = getUsrEnvUseCheSavNul(ssn, env_nam);
    	return ret == null ? def_ret : ret;
    }
    
    /**
     * {@link IplDlgCsoUti#getUsrEnvUseCheSavNul(PscSsn, String)}
     */
    static boolean getUsrEnvUseCheSavNul (PscSsn ssn, String env_nam, boolean def_ret) throws Exception {
    	String ret = getUsrEnvUseCheSavNul(ssn, env_nam);
    	return ret == null ? def_ret : ret.equalsIgnoreCase("y");
    }
    
    /**
     * {@link IplDlgCsoUti#getUsrEnvUseCheSavNul(PscSsn, String)}
     */
    static int getUsrEnvUseCheSavNul (PscSsn ssn, String env_nam, int def_ret) throws Exception {
        String ret = getUsrEnvUseCheSavNul(ssn, env_nam);
        return PsaUti.str2Int( ret, def_ret);
    }
    
    /**
   	 * Wie {@link IplDlgCsoUti#getUsrEnvUseChe(PscSsn, String)} mit dem
   	 * Unterschied: Null-Werte bzw. das Nicht-Auffinden von Variablen wird (als
   	 * null) gecacht.
   	 * 
   	 * @author geger
   	 */
       static String getOthUsrEnvUseCheSavNul (PscSsn ssn, String oth_uic, String env_nam) throws Exception {
       	return Priv.getEnvUseCheIpl(ssn, env_nam, oth_uic, false, false);
       }
       
       /**
        * {@link IplDlgCsoUti#getUsrEnvUseCheSavNul(PscSsn, String)}
        */
       static String getOthUsrEnvUseCheSavNul (PscSsn ssn, String env_nam, String oth_uic, String def_ret) throws Exception {
       	String ret = getOthUsrEnvUseCheSavNul(ssn, oth_uic, env_nam);
       	return ret == null ? def_ret : ret;
       }
       
       /**
        * {@link IplDlgCsoUti#getUsrEnvUseCheSavNul(PscSsn, String)}
        */
       static boolean getOthUsrEnvUseCheSavNul (PscSsn ssn, String env_nam, String oth_uic, boolean def_ret) throws Exception {
       	String ret = getOthUsrEnvUseCheSavNul(ssn, oth_uic, env_nam);
       	return ret == null ? def_ret : ret.equalsIgnoreCase("y");
       }
       
       /**
        * {@link IplDlgCsoUti#getUsrEnvUseCheSavNul(PscSsn, String)}
        */
       static int getOthUsrEnvUseCheSavNul (PscSsn ssn, String env_nam, String oth_uic, int def_ret) throws Exception {
           String ret = getOthUsrEnvUseCheSavNul(ssn, oth_uic, env_nam);
           return PsaUti.str2Int( ret, def_ret);
       }
    
    
    /**
     * {@link IplDlgCsoUti#getUsrEnvUseChe(PscSsn, String)}
     */
    @Deprecated
    static int getUsrEnvUseChe (PscSsn ssn, String env_nam, int def_ret) throws Exception {
        String ret = getUsrEnvUseChe(ssn, env_nam);
        return PsaUti.str2Int( ret, def_ret);
    }
    
   
    /**
     * {@link IplDlgCsoUti#getUsrEnvUseChe(PscSsn, String)}
     */
    @Deprecated
    static boolean getUsrEnvUseChe (PscSsn ssn, String env_nam, boolean def_ret) throws Exception {
    	String ret = getUsrEnvUseChe(ssn, env_nam);
    	return ret == null ? def_ret : ret.equalsIgnoreCase("y");
    }
    
   
    
    /**
     * {@link IplDlgCsoUti#getUsrEnvUseChe(PscSsn, String)}
     */
    @Deprecated
    static String getUsrEnvUseChe (PscSsn ssn, String env_nam, String def_ret) throws Exception {
    	String ret = getUsrEnvUseChe(ssn, env_nam);
    	return ret == null ? def_ret : ret;
    }
    
	/**
	 * Wrappt implementierung {@link PsaUti#getEnv(PscSsn, String, String)}
	 * Ergebnisse werden immer gecacht auch wenn es sich um nicht gruppen- oder
	 * userspezifische Variblen handelt. Null wird nicht gecacht. Cache-Key wird
	 * gebildet aus Variablenname, User-IC und Sprache.
	 * 
	 * @author geger
	 */
    @Deprecated
    static String getUsrEnvUseChe (PscSsn ssn, String env_nam) throws Exception {
    	return Priv.getUsrEnvUseCheIpl(ssn, env_nam, true);
    }
    
	
    
    
    
    
    
   

    
    static void delUsrEnvFroCsoChe (PscSsn ssn, String env_nam) throws Exception {
    	String che_key = Priv.getUsrEnvCheKey(ssn, env_nam);
    	PsaGlbChe.clr(null, Priv.ENV_CHE_CTX, che_key);
    }
    
    
    /**
     * @param ssn
     * @param end_flg name of env var which signals with it's existence to end the procedure
     * @return true if procedure should be ended
     * @throws Exception
     */
    static boolean hasEndFlg(PscSsn ssn, String end_flg, int cyc_idx, int chk_evr_x_cyc) throws Exception {
        if (PscUti.isStr( end_flg) && cyc_idx % chk_evr_x_cyc == 0) {
            PscDto env_dto = PsaUti.newDto( ssn, "PSC_ENV_GLB", false, false, true, true);
            env_dto.setQue( "NAM", end_flg);
            if (env_dto.fetDat() == 1) {
                env_dto.delDat( 1);
                PsaSsn.wriTxtImm( ssn,  "Procedure ended due to end-flag in PSC_ENV_GLB: " + end_flg);
                return true;
            }
        }
        return false;
    }
    
    /**
     * generate a unique client temp path by file name
     * @param fil_nam file name
     * @param gui
     * @return unique client temp path
     * @throws Exception
     */
    static String genCliTmpPth(String fil_nam, PscGui gui) throws Exception {
        String ret = gui.getEnv("CLI_TMP");
        ret = FilUtl.concatPthNam(ret, fil_nam, false);
        ret = BlbUtl.getUniCliFil(gui, ret);
        return ret;
    }
    
    static void chkNnlThrExc(PscDto dto, String fld_dsc, int row) throws Exception {
    	if (fld_dsc.endsWith("GID")) {
    		chkNnlThrExcGidFld(dto, fld_dsc, row);
    	} else {
    		chkNnlThrExcNrmFld( dto, fld_dsc, row);
    	}
    }

    
    default void chkNnlThrExcNrmFld (String fld_dsc, int row) throws Exception {
        PscDto dto = Priv.getDto( this);
        chkNnlThrExcNrmFld( dto, fld_dsc, row);
    }
    
    
    static void chkNnlThrExcNrmFld (PscDto dto, String fld_dsc, int row) throws Exception {
        PscFld fld = dto.getFld( fld_dsc);
        String fld_dat = dto.getDat( fld, row);
        if ( !PscUti.isStr( fld_dat)) {
            String fld_tit = fld.getTit();
            throw new PscExc( 0, EnuExc.DTO_CHK_NNL_REC, fld_tit);
        }
    }
    
    static void chkNnlThrExcGidFld (PscDto dto, String fld_dsc, int row) throws Exception {
        PscFld fld = dto.getFld( fld_dsc);
        String fld_dat = dto.getDat( fld, row);
        if ( !PscGid.isVld( fld_dat)) {
            String fld_tit = fld.getTit();
            throw new PscExc( 0, EnuExc.DTO_CHK_NNL_REC, fld_tit + " ("+fld_dsc+")");
        }
    }
    
    default boolean chkInsOrBgdImpIns (int row) throws PscExc {
        PscDto dto = Priv.getDto( this);
        return chkInsOrBgdImpIns( dto, row);
    }
    
    static boolean chkInsOrBgdImpIns (PscDto dto, int row) throws PscExc {
        boolean nrm_ins_boo = dto.chkIns( row);
        return nrm_ins_boo || chkBgdImpIns( dto, row);
    }
    
    default boolean chkBgdImpIns (int row) {
        PscDto dto = Priv.getDto( this);
        return chkBgdImpIns( dto, row);
    }
    
    static boolean chkBgdImpIns (PscDto dto, int row) {
        if (row > 0) {
            String chk_str = "CSO_BGD_IMP_INS_MOD_FLG_AT_ROW_" + row;
            return dto.hasValBuf( chk_str);
        }
        return false;
    }
    
    /**
     *
     * {@link IplDlgCsoUti#rfrComFld(PscDto top_dto, int row, String com_dsc, String com_fld_dsc)}
     */
    default void rfrComFld(int row, String com_dsc, String com_fld_dsc) throws Exception {
        PscDto dto = Priv.getDto( this);
        rfrComFld( dto, row, com_dsc, com_fld_dsc);
    }
    
    /**
     * {@link #rfrComFld(PscDto, int, String, String, boolean)}
     */
    static void rfrComFld(PscDto top_dto, int row, String com_dsc, String com_fld_dsc) throws Exception {
    	rfrComFld(top_dto, row, com_dsc, com_fld_dsc, false);
    }
    
    /**
     * Refresh a component row (capsules right usage of PsaDtoIpl.rfrCom)
     * @param top_dto
     * @param row
     * @param com_dsc
     * @param com_fld_dsc field which is to refresh (e.g. NAM or FRN_IDN) (whole row will be refreshed anyways)
     * @param ign_mis_fld just return if field is missing? (else: throw exception)
     * @throws Exception
     * 
     * @author geger
     */
    static void rfrComFld(PscDto top_dto, int row, String com_dsc, String com_fld_dsc, boolean ign_mis_fld) throws Exception {
        String ful_com_fld_dsc = com_dsc + "." + com_fld_dsc;
		PscFld com_fld = top_dto.getFld( ful_com_fld_dsc);
        if (com_fld == null) {
        	if (ign_mis_fld) {
        		return;
        	} else {
        		throw new PscExc(PscOut.LEV_WRN, EnuExc.DTO_PAR_INV_FLD, ful_com_fld_dsc);
        	}
        }
        PscDto com_fld_dto = com_fld.getDto();
        PscDto com_drv = com_fld_dto.getDrv();
        PscDto com_dto = top_dto.getCom( com_drv.getDsc());
        if ( com_dto != null) {
            PsaDtoIpl.rfrCom( top_dto, com_fld, row);
        } else {
            PscDto reg_fat = com_drv.getFat();
            PsaDtoIpl.rfrCom( reg_fat, com_fld, row);
        }
    }
    
    interface Priv {
    	
    	String ENV_CHE_CTX = "CSO_ENV_CHE_CTX";
    	
    	static String getUsrEnvUseCheIpl (PscSsn ssn, String env_nam, boolean re_fet_on_fai_boo) throws Exception {
        	return getEnvUseCheIpl(ssn, env_nam, re_fet_on_fai_boo, false);
        }
    	
    	static String getGlbEnvUseCheIpl (PscSsn ssn, String env_nam, boolean re_fet_on_fai_boo) throws Exception {
    		return getEnvUseCheIpl(ssn, env_nam, re_fet_on_fai_boo, true);
    	}
    	
    	static String getEnvUseCheIpl (PscSsn ssn, String env_nam, boolean re_fet_on_fai_boo, boolean glb) throws Exception {
    		return getEnvUseCheIpl(ssn, env_nam, null, re_fet_on_fai_boo, glb);
    	}
    	
    	static String getEnvUseCheIpl (PscSsn ssn, String env_nam, String oth_uic, boolean re_fet_on_fai_boo, boolean glb) throws Exception {
    		if (!PscUti.isStr(env_nam)) {
        		throw new RuntimeException("!PscUti.isStr(env_nam)");
        	}
        	final String NOT_FND_STR = "_NOT_FND";
        	String oth_lng = null;
        	if (isStr(oth_uic) && !"PSC_LNG_SYS".equals(env_nam)) {
        		// recursive call for getting language of other user
        		oth_lng = getEnvUseCheIpl(ssn, "PSC_LNG_SYS", oth_uic, false, false);
        	}
        	String che_key;
        	if (glb) {
        		che_key = getGlbEnvCheKey(ssn, env_nam);
    		} else {
    			che_key = isStr(oth_uic) ? 
    					getOthUsrEnvCheKey(ssn, env_nam, oth_uic, oth_lng) : 
    						getUsrEnvCheKey(ssn, env_nam);
    		}
        	boolean is_in_che_boo = false;
        	String dat = PsaGlbChe.get(null, ENV_CHE_CTX, che_key);
        	is_in_che_boo = dat != null;
        	if (!is_in_che_boo || (re_fet_on_fai_boo && NOT_FND_STR.equals(dat))) {
        		if (glb) {
        			dat = PsaUti.getGlbEnv(ssn, env_nam);
        		} else {
        			dat = isStr(oth_uic) ? 
        					getEnvPsaUti705(ssn, env_nam, oth_uic, null, oth_lng) : 
        						PsaUti.getEnv(ssn, env_nam, null);
        		}
        	}
        	if (!is_in_che_boo) {
        		// cache 'null-string' only if it will probably be used
        		if (!re_fet_on_fai_boo && dat == null) { 
            		PsaGlbChe.put(null, ENV_CHE_CTX, che_key, NOT_FND_STR);
            	}
        		// cache valid data always
            	if (dat != null && !NOT_FND_STR.equals(dat)) {
        			PsaGlbChe.put(null, ENV_CHE_CTX, che_key, dat);
        		}
        	}
        	if (NOT_FND_STR.equals(dat)) {
    			dat = null;
    		}
        	return dat;
    	}
        
        static String getUsrEnvCheKey(PscSsn ssn, String env_nam) {
        	// language sensitiv !
        	String uic = ssn.getUic() + "";
        	String che_key = uic + "_" + env_nam;
        	return appLngToEnvCheKey(ssn, che_key);
		}
        
        static String getGlbEnvCheKey(PscSsn ssn, String env_nam) {
        	// language sensitiv !
        	String che_key = "#+#global#+#_" + env_nam;
        	return appLngToEnvCheKey(ssn, che_key);
        }
        
        static String getOthUsrEnvCheKey(PscSsn ssn, String env_nam, String uic, String oth_lng) {
        	// language sensitiv !
        	String che_key = uic + "_" + env_nam;
        	if (PscUti.isStr(oth_lng)) {
    			che_key += "_" + oth_lng;
    		}
        	return che_key;
		}

		static String appLngToEnvCheKey(PscSsn ssn, String che_key) {
			PscLng lng_obj = ssn.getLng();
        	if (lng_obj != null) {
        		String lng_str = lng_obj.getStr();
        		if (PscUti.isStr(lng_str)) {
        			che_key += "_" + lng_str;
        		}
        	}
        	return che_key;
		}
		
		/**
		 * copied PsaUti.getEnv from 7.5 - need to assure backward compatibility of this uti class to 7.1
		 */
		static String getEnvPsaUti705(PscSsn ssn, String env_nam, String uic, String gic, String lng) throws Exception
		{
			if (!PscUti.isStr(env_nam)) {
				return null;
			}
			String dat = null;
			List<String> grp_lis = null;
			PscDto env_dto = ssn.newDto("PSC_ENV_GLB");
			env_dto.setQue("NAM", env_nam);
			if (PscUti.isStr(uic)) {
				env_dto.setQue("USR", uic+" ''");
				grp_lis = PsaUti.getGrpLis(ssn, uic, null);
			}
			else if (PscUti.isStr(gic) && !gic.equals("0")) {
				grp_lis = PsaUti.getGrpLis(ssn, null, gic);
			}
			if (PscUti.isStr(lng)) {
				env_dto.setQue("LNG", lng+" ''");
			}
			env_dto.fetDat();

			// 1. Look for user environment
			if (PscUti.isStr(uic)) {
				int env_row = PscDtoEnv.getEnvRow(env_dto, uic, null);
				if (env_row>0) {
					dat = env_dto.getDat("DAT", env_row);
				}
			}

			// 2. Look for group in group list
			if (dat==null && grp_lis!=null && grp_lis.size()>0) {
				for (String grp_itm : grp_lis) {
					int env_row = PscDtoEnv.getEnvRow(env_dto, null, grp_itm);
					if (env_row>0) {
						dat = env_dto.getDat("DAT", env_row);
						break;
					}
				}
			}

			// 3. Look for World Environment
			if (dat==null) {
				int env_row = PscDtoEnv.getEnvRow(env_dto, null, "0");
				if (env_row>0) {
					dat = env_dto.getDat("DAT", env_row);
				}
			}

			// 4. Look for global environment
			if (dat==null) {
				int env_row = PscDtoEnv.getEnvRow(env_dto, "", "");
				if (env_row>0) {
					dat = env_dto.getDat("DAT", env_row);
				}
			}
			
			if (PscUti.NON_STR.equals(dat)) {
				dat = null;
			}

			return dat;
		}
        
        
        public static Set<String> addRec(PscDto dst_dto, String que_fld_dsc, String que, boolean is_sgl) throws Exception {
            PscSsn ssn = dst_dto.getSsn();
            PscDto drv_dst_dto = dst_dto.getDrv();
            int max_rec = drv_dst_dto.getMax();
            PscDto clo_dto = drv_dst_dto.cloDto();
            PsaDtoIpl.copDtoQue( drv_dst_dto, clo_dto);
            clo_dto.copSrt( drv_dst_dto);
            clo_dto.setQue(que_fld_dsc, que);
            Set<String> has_gid_set = getDatSet(dst_dto, "PSC_GID");
            Set<String> new_gid_set = new HashSet<>();
            try {
                while ( clo_dto.fetNxt()) {
                	if (!clo_dto.chkAcc(1, 'R')) {
                		continue;
                	}
                    String add_gid = clo_dto.getDat( "PSC_GID", 1);
                    if (has_gid_set.contains( add_gid) || new_gid_set.contains( add_gid)) {
                        continue;
                    } else {
                    	new_gid_set.add( add_gid);
                    }
                    int new_row = drv_dst_dto.numRec() + 1;
                    if ( new_row > max_rec) {
                        ssn.wriMsg( "PSC_DBD_DTO_FET_MAX", Integer.toString( max_rec), drv_dst_dto.getTit());
                        return new_gid_set;
                    }
                    drv_dst_dto.addRow( new_row - 1, 1);
                    drv_dst_dto.cloRow( new_row, clo_dto, 1);  
                    
                    if (is_sgl) {
                    	break;
                    }
                }
            } finally {
                clo_dto.fetCls();
                drv_dst_dto.clnDat();
            }
            return new_gid_set;
        }
        
        
        /**
         * get method from stack trace using regular expression
         * @param reg_expr
         * @return method name
         */
        static String getDscFroStkTrc(Pattern pat, StkTrcDscTyp dsc_typ) {
        	StackTraceElement[] stk_trc = new Throwable().getStackTrace();
        	for (int i = 0; i< stk_trc.length; i++) {
        		StackTraceElement stk_trc_elm = stk_trc[i];
        		if (stk_trc_elm != null) {
        			String dsc = null;
        			switch (dsc_typ) {
        			case MTH:
        				dsc = stk_trc_elm.getMethodName();
        				break;
        			case CLA:
        				dsc = stk_trc_elm.getClassName();
        				break;
        			case FIL:
        				dsc = stk_trc_elm.getFileName();
        				break;
        			case CLA_AND_MTH:
        				dsc = stk_trc_elm.getClassName()+"."+stk_trc_elm.getMethodName();
        				break;
        			}
        			if (dsc != null && pat.matcher(dsc).matches()) {
        				return dsc;
        			}
        		}
        	}
        	return null;
        }

		static PscDto getDto(IplDlgCsoUti ifc_ins) {
            PscDto dto = null;
            if (ifc_ins instanceof PscFrm) {
                return ((PscFrm) ifc_ins).getDynDto(); 
            } else if (ifc_ins instanceof PscDto) {
                return (PscDto) ifc_ins;
            }
            throw new RuntimeException("Method applicable only for dto or dlg");
        }
        
        static String buiQuoQue (String que, boolean trim_boo) {
        	
        	if (que != null && que.contains("&") || que.contains("|") || que.contains("(") || que.contains(")")) {
        		throw new UnsupportedOperationException("Quoting a query containing '(',')','&' or '|'");
        	}
        	String quo_que = null;
            if ( que == null || que.isEmpty() || que.equals( "NULL") || que.equals( "!NULL") || que.equals( "!''") || que.equals( "''") || ( que.length() >= 2 && que.endsWith( "'") && ( que.startsWith( "'") || que.startsWith( "!'")))) {
                quo_que = que;
            } else if ( que.startsWith( "!") && !que.startsWith( "!'") && !que.endsWith( "'")) { // negate
                if (trim_boo) {
                    que = PscUti.trim( que);
                }
                quo_que = "!'" + que.substring( 1) + "'";
            } else {
                if (trim_boo) {
                    que = PscUti.trim( que);
                }
                quo_que = "'" + que + "'";
            }
            return quo_que;
        }

		static boolean chkRecInDto(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que, boolean chk_uni) throws Exception {
			PscDto dto = PsaUti.newDto(ssn, dto_dsc, false, false, true, true);
			dto.setQue(que_fld_dsc, que);
			if (chk_uni) {
				return dto.cntDat() == 1;
			} else {
				return dto.hasDat();
			}
		}
		
		static String getUsxParStr(String... que_par_lis) {
			String usx_fld_que_str;
			if (que_par_lis.length == 1) {
				boolean is_psc_gid_que = !que_par_lis[0].contains("=");
				if (is_psc_gid_que) {
					usx_fld_que_str = "PSC_GID:="+que_par_lis[0];
				} else {
					usx_fld_que_str = que_par_lis[0];
				}
			} else { // >= 2 param
				UsxPar usx_par = new UsxPar();
				String las_fld_dsc = null;
				for (int i = 0; i < que_par_lis.length; i++) {
					if (i % 2 == 0) { // field dsc
						las_fld_dsc = que_par_lis[i];
					} else { // que
						String que = que_par_lis[i];
						usx_par.setPar(las_fld_dsc, que);
					}
				}
				usx_fld_que_str = usx_par.genParStr(false);
			}
			return usx_fld_que_str;
		}
    }

    
    public static boolean chkRecInDto(PscSsn ssn, String gid, String dto_dsc) throws Exception {
    	return Priv.chkRecInDto(ssn, dto_dsc, "PSC_GID", gid, true);
    }
    
    public static boolean chkRecInDto(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que) throws Exception {
    	return Priv.chkRecInDto(ssn, dto_dsc, que_fld_dsc, que, false);
    }
    
    public static boolean chkRecInDtoFndAndUni(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que) throws Exception {
    	return Priv.chkRecInDto(ssn, dto_dsc, que_fld_dsc, que, true);
	}
    
    
    
        
    /**
     * resort according to the current sorting
     * @author geger
     * @throws Exception 
     */
    default void rfrSrt() throws Exception {
    	PscDto dto = null;
    	if (this instanceof PscFrm) {
    		dto = ((PscFrm) this).getDynDto(); 
    	} else if (this instanceof PscDto) {
    		dto = (PscDto) this;
    	}
    	if (dto != null) {
    		PscFld srt[] = dto.getSrt();
			PscFld srt_fld = (srt.length==0) ? null : srt[0];
			if (srt_fld!=null) {
				boolean asc = dto.chkSrt(srt_fld)=='+';
				dto.srtDat(srt_fld, asc);
			}
    	}    				
    }
    
    default void setTagWidZer(PscFrm thi_frm, String tag_dsc) throws Exception {
        HashMap<String, String> sav_wid_map = thi_frm.getValBuf( "CSO_SAV_TAG_WID_ZER_MAP");
        if (sav_wid_map == null) {
            sav_wid_map = new HashMap<>();
            thi_frm.setValBuf( "CSO_SAV_TAG_WID_ZER_MAP", sav_wid_map);
        }
        String tag_wid_str = PsaFrm.getTagPrp( thi_frm, tag_dsc, "WID");
        if (PscUti.isStr( tag_wid_str) && !tag_wid_str.equals("0")) {
            sav_wid_map.put( tag_dsc, tag_wid_str);
            PsaFrm.setTagPrp( thi_frm, tag_dsc, "WID", "0", true);
        }        
    }
    
    default void reSetTagWid(PscFrm thi_frm, String tag_dsc) throws Exception {
        HashMap<String, String> sav_wid_map = thi_frm.getValBuf( "CSO_SAV_TAG_WID_ZER_MAP");
        if (sav_wid_map == null) {
            sav_wid_map = new HashMap<>();
            thi_frm.setValBuf( "CSO_SAV_TAG_WID_ZER_MAP", sav_wid_map);
        }
        String tag_wid_str = sav_wid_map.get( tag_dsc);
        if (PscUti.isStr( tag_wid_str)) {
            PsaFrm.setTagPrp( thi_frm, tag_dsc, "WID", tag_wid_str, true);
            sav_wid_map.remove( tag_dsc);            
        }
        
    }
    
    public static String mapDbsSid2PsaSid(String dbs_sid) {
        PscSsn fnd_ssn = mapDbsSid2PsaSsn(dbs_sid);
        String sid = null;
        if (fnd_ssn != null) {
            sid = fnd_ssn.getSid();
        }
        return sid;
    }
    
    public static PscSsn mapDbsSid2PsaSsn(String dbs_sid) {
        if (!PscUti.isStr( dbs_sid)) {
            return null;
        }
        Map<PscSsn, Object> ssn_map = PscChe.getGlb().getValBuf("CSO_SSN_POL_LST");
        ArrayList<PscSsn> ssn_lis = new ArrayList<PscSsn>(ssn_map.keySet());
        for (PscSsn idx_ssn: ssn_lis) {
            PscDbi dbi = idx_ssn.getDbi();
            String idx_dbs_sid = dbi != null ? dbi.getDbsSsnIdn() : null;
            if (PscUti.isStr( idx_dbs_sid)) {
                if ( dbs_sid.equals( idx_dbs_sid)) {
                    return idx_ssn;
                }
            }
       
        }
        return null;
    }
    
    @Deprecated
    public static PscFrm opnStdLisDlg (PscGui gui, PscDto dto) throws Exception {
        dto.creIdn();
        PscChe.delFrm( "PSA_STD_SSL", false);
        PscFrm std_ssl_frm = gui.newFrm( "PSA_STD_SSL", dto);
        std_ssl_frm.fetDat(); 
        return std_ssl_frm;
    }
    
    public static PscFrm opnStdLisDlg (PscGui gui, PscDto dto, boolean fet_dat) throws Exception {
        dto.creIdn();
        PscChe.delFrm( "PSA_STD_SSL", false);
        PscFrm std_ssl_frm = gui.newFrm( "PSA_STD_SSL", dto);
        if (fet_dat) {
        	std_ssl_frm.calEvt("REC_FET");
        } else {
        	std_ssl_frm.rfrDat();
        }
        return std_ssl_frm;
    }
    
    public static void OPN_STD_LIS_DLG(PscSsn ssn, String par) throws Exception {
    	UsxPar usx_par = new UsxPar(par);
    	String dto_dsc = usx_par.getPar("DTO");
    	PscGui gui = ssn.getGui();
    	if (!PscUti.isStr(dto_dsc) || gui == null) {
    		throw new RuntimeException("!PscUti.isStr(dto_dsc) || gui == null");
    	}
    	PscDto dto = ssn.newDto(dto_dsc);
    	opnStdLisDlg(gui, dto, false);
    }
      
  
    
    public static boolean isJobEndReq(long trm_tim_mil) {
    	return isJobEndReq() || System.currentTimeMillis() > trm_tim_mil;
    }
    
    /**
     * @return true if this is a job AND the end of the job is requested
     */
    public static boolean isJobEndReq() {
        JobThr job_thr = JobThr.getJobThr();
        if ( job_thr != null)
            return !job_thr.isRun();
        return false;
    } 
    
    
//    private static boolean isJobEndReq(PscSsn ssn) throws Exception {
//        return RefUti.invMthDynJav( ssn, "CSO_INV", "uti", "isJobEndReq");
//    }
    
    
    public static String arr2Str(String[] arr) {
        String ret = null;
        for (int i = 0; i < arr.length; i++) {
            if (ret == null) {
                ret = "[ " + arr[i];
            } else {
                ret += ", " + arr[i];
            }
        }
        if (ret == null) {
            ret = "[ ]";
        } else {
            ret += " ]";
        }
        return ret;
    }
    
    public default Integer fndRow(PscDto dto, String fld_dsc, String fnd_dat) throws Exception {
        int num_rec = dto.numRec();
        PscFld fnd_in_fld =  dto.getFld( fld_dsc);
        for (int row = 1; row <= num_rec; row++) {
            if (dto.getDat(fnd_in_fld, row).equals( fnd_dat)) {
                return row;
            }
        }
        return null;
    }
    
    public static Integer fndRow(PscDto dto, String usx_que_str) throws Exception {
        UsxPar usx_que_obj = new UsxPar(usx_que_str);
        int num_rec = dto.numRec();
        for (int row = 1; row <= num_rec; row++) {
        	boolean fnd_boo = true;
        	for (Ent que_ent: usx_que_obj.getParLis()) {
            	PscFld que_fld =  dto.getFld( que_ent.Key);
            	String que_dat = que_ent.Val;
            	String fld_dat = dto.getDat(que_fld, row);
            	if (!fld_dat.equals( que_dat)) {
            		fnd_boo = false;
            		break;
            	}
            }
        	if (fnd_boo) {
        		return row;
        	}
        }
        return null;
    }
    
    /**
     * find maximum one row, throw exception if there are more
     * @param dto
     * @param usx_par_que
     * @return THE row, null if not found
     * @throws Exception
     * @author geger
     */
    public static Integer fndMaxOneRowThrExc(PscDto dto, UsxPar usx_par_que) throws Exception {
        int num_rec = dto.numRec();
        Integer fnd_row = null;
        for (int row = 1; row <= num_rec; row++) {
        	boolean fnd_boo = true;
        	for (Ent que_ent: usx_par_que.getParLis()) {
            	PscFld que_fld =  dto.getFld( que_ent.Key);
            	String que_dat = que_ent.Val;
            	String fld_dat = dto.getDat(que_fld, row);
            	if (!fld_dat.equals( que_dat)) {
            		fnd_boo = false;
            		break;
            	}
            }
        	if (fnd_boo) {
        		if (fnd_row != null) {
        			throw new RuntimeException("Found row in fndMaxOneRowThrExc(..) not unique for the query <"+usx_par_que.genParStr(false)+">! (rows matching: "+fnd_row+", "+row+")");
        		}
        		fnd_row = row; // found !
        	}
        }
        return fnd_row;
    }
    
    /**
     * 
     * @param que_elm_lis
     * @return OR-query or null if no (not empty) elements in list
     */
    static String buiQuoQueOr(List<String> que_elm_lis) {
        StringBuilder str_bui = new StringBuilder();
        for (String que_elm: que_elm_lis) {
            String quo_que_elm = Priv.buiQuoQue( que_elm, true);
            if (PscUti.isStr( quo_que_elm)) {
                str_bui.append( " | ");
                str_bui.append(quo_que_elm);
            }
        }
        String que = str_bui.toString();
        if (!que.isEmpty()) {
            que = que.substring( 3);
            return que;
        }
        return null;
    }
    
    static String buiQuoQueOr(Set<String> que_elm_set) {
    	return buiQuoQueOr(new ArrayList<>(que_elm_set));
    }

    @Deprecated
    default void setQteQue(String fld_dsc, String que) throws Exception {
    	PscDto dto = Priv.getDto(this);
    	quoQueS(dto, fld_dsc, que);
    }
    @Deprecated
    default void setQteQue(PscFld fld, String que) throws Exception {
    	PscDto dto = Priv.getDto(this);
    	quoQueS(dto, fld, que);
    }
    
    @Deprecated
    public default void quoQue(PscDto dto, String fld_dsc, String que) throws Exception {
        quoQue( dto, fld_dsc, que, true);
    }
    
    @Deprecated
    public default void quoQue(PscDto dto, String fld_dsc, String que, boolean trim_boo) throws Exception {
        quoQueS(dto, fld_dsc, que, trim_boo);
    }
    @Deprecated
   static void quoQueS(PscDto dto, String fld_dsc, String que) throws Exception {
		quoQueS(dto, fld_dsc, que, true);
	}
    @Deprecated
   static void quoQueS(PscDto dto, PscFld fld,  String que) throws Exception {
		quoQueS(dto, fld, que, true);
	}
    @Deprecated
   static void quoQueS(PscDto dto, PscFld fld, String que, boolean trim_boo) throws Exception {
	   String quo_que = Priv.buiQuoQue( que, trim_boo);
       dto.setQue( fld, quo_que);
   }
    @Deprecated
    static void quoQueS(PscDto dto, String fld_dsc, String que, boolean trim_boo) throws Exception {
    	String quo_que = Priv.buiQuoQue( que, trim_boo);
        dto.setQue( fld_dsc, quo_que);
    }
    
    /**
     * try to set coordinates of an address - not overwriting if coordinates exist
     * @return true if success, false otherwise
     * @throws Exception
     * @author geger
     */ 
    @Deprecated
    static boolean chkSetAdrCor (PscSsn ssn, String adr_gid) throws Exception {
    	return chkSetAdrCor(ssn, adr_gid, false);
    }
    
   /**
     * try to set coordinates of an adress
     * @param ovr_flg - if true overwrite existing coordinates
     * @return true if success, false otherwise
     * @throws Exception
     * @author geger
     */ 
    @Deprecated
    static boolean chkSetAdrCor (PscSsn ssn, String adr_gid, boolean ovr_flg) throws Exception {
        if (PscGid.isVld(adr_gid)) {
        	Boolean bck_ssn_acc_chk_flg = null;
			//          PscSsn ssn =  this.getSsn();
            try {
            	// no access check !
            	bck_ssn_acc_chk_flg = ((PsaSsn)ssn).isDtoAccChk();
            	((PsaSsn)ssn).setDtoAccChk(false);
            	
                PscDto adr_dto = ssn.newDto("PSA_ADR");
                adr_dto.setQue("PSC_GID", adr_gid);
                // Don't update the address if it cannot be found or user has no write access 
                if (adr_dto.fetDat() != 1) {
                    return false;
                }
                if(!adr_dto.chkAcc(1, 'R') || !adr_dto.chkAcc(1, 'W')){
                    return false;
                }
                
                String lon = adr_dto.getDat("LON", 1);
                String lat = adr_dto.getDat("LAT", 1);          
                if (ovr_flg || lat.isEmpty() || lon.isEmpty()) {
                    try (AdrLocFnd fnd = new AdrLocFnd(ssn)) {
                    	ssn.setValBuf("CSO_UTI_$_CHK_SET_ADR_COO", true);
                    	ssn.setValBuf("CSO_BGD_ALW_MOD_ADR_COO", true);
                        AdrLocRes adr_loc_res = fnd.fndLoc(adr_dto, 1);
                        AdrLoc adr_loc = (adr_loc_res==null) ? null : adr_loc_res.getLoc();
                        if (adr_loc == null) {
                            return false;
                        }
                            lat = String.valueOf(adr_loc.getLatDeg()); 
                            lon = String.valueOf(adr_loc.getLonDeg());
                            TriSta loc_unk = adr_loc_res.getLocUnk();
        					String unk = loc_unk.isUndefined()?"":(loc_unk.isTrue()?"y":"n");
                            adr_dto.modDat("LAT", 1, lat);
                            adr_dto.modDat("LON", 1, lon);
                            adr_dto.modDat("UNK", 1, unk);
                            // SUCCESS!
                                                 
                    } finally {
                    	ssn.delValBuf("CSO_BGD_ALW_MOD_ADR_COO");
                    	ssn.delValBuf("CSO_UTI_$_CHK_SET_ADR_COO");
                    }
                }
                return true;
            } catch (Exception e) {
                PscGui gui = ssn.getGui();
                if (gui != null) {
                	ssn.wriTxt(gui.getMsg( "CSO_MAP_QUE_REQ_EXH"));
                	ssn.wriExc( e);
                }                
                return false;
            } finally {
            	((PsaSsn)ssn).setDtoAccChk(bck_ssn_acc_chk_flg);
            }           
        } else {
            return false;
        }
    }
    
   /*
    * designed new with PsaSsn.setDtoAccChk(..)
    */
//    /**
//     * try to set coordinates of an adress
//     * @param adr_gid
//     * @return true if success, false otherwise
//     * @throws Exception
//     * @author geger
//     */
//    static boolean chkSetAdrCor (PscSsn thi_ssn, String adr_gid) throws Exception {
//        if (PscGid.isVld(adr_gid)) {
////          PscSsn ssn =  this.getSsn();
//            PscSsn ssn = null;
//            try {
//                /**
//                 *  call 'fnd.fndLoc(adr_dto, 1)' as SALESADMIN or you get
//                 *  an immutable acces-check
//                 */             
//                ssn = NewSsn.newSsn( thi_ssn, "SALESADMIN");
//                PscDto adr_dto = ssn.newDto("PSA_ADR");
//                adr_dto.setQue("PSC_GID", adr_gid);
//                // Don't update the address if it cannot be found or user has no write access 
//                if (adr_dto.fetDat() != 1) {
//                    return false;
//                }
//                if(!adr_dto.chkAcc(1, 'R') || !adr_dto.chkAcc(1, 'W')){
//                    return false;
//                }
//                
//                String lon = adr_dto.getDat("LON", 1);
//                String lat = adr_dto.getDat("LAT", 1);          
//                if (lat.isEmpty() || lon.isEmpty()) {
//                    try (AdrLocFnd fnd = new AdrLocFnd(ssn)) {
//                    	ssn.setValBuf("CSO_UTI_$_CHK_SET_ADR_COO", true);
//                    	ssn.setValBuf("CSO_BGD_ALW_MOD_ADR_COO", true);
//                        AdrLocRes adr_loc_res = fnd.fndLoc(adr_dto, 1);
//                        AdrLoc adr_loc = (adr_loc_res==null) ? null : adr_loc_res.getLoc();
//                        if (adr_loc == null) {
//                            return false;
//                        }
//                            lat = String.valueOf(adr_loc.getLatDeg()); 
//                            lon = String.valueOf(adr_loc.getLonDeg());
//                            TriSta loc_unk = adr_loc_res.getLocUnk();
//        					String unk = loc_unk.isUndefined()?"":(loc_unk.isTrue()?"y":"n");
//                            adr_dto.modDat("LAT", 1, lat);
//                            adr_dto.modDat("LON", 1, lon);
//                            adr_dto.modDat("UNK", 1, unk);
//                            // SUCCESS!
//                                                 
//                    } finally {
//                    	ssn.delValBuf("CSO_BGD_ALW_MOD_ADR_COO");
//                    	ssn.delValBuf("CSO_UTI_$_CHK_SET_ADR_COO");
//                    }
//                }
//                return true;
//            } catch (Exception e) {
//                PscGui gui = thi_ssn.getGui();
//                if (gui != null) {
//                    thi_ssn.wriTxt(gui.getMsg( "CSO_MAP_QUE_REQ_EXH"));
//                    thi_ssn.wriExc( e);
//                }                
//                return false;
//            } finally {
//                if (ssn != null) {
//                    ssn.exiSsn();
//                }
//            }           
//        } else {
//            return false;
//        }
//    }
    
    public default String tst_empty() {
        return "OK!";
    }
    
    public static String tst_sta (PscDto dto, String str) {
        dto.getSsn().wriTxt("in CSO_UTI :" +  str);
        return str;
    }
    
    public default String tst_not_sta (PscDto dto, String str) {
        dto.getSsn().wriTxt("in CSO_UTI :" +  str);
        return str;
    }

    public static PscDto newDto(PscSsn ssn, String dto_dsc, String gid) throws Exception {
    	return newDto(ssn, dto_dsc, gid, false, false);
    }
    
    public static PscDto newDto(PscSsn ssn, String dto_dsc, String gid, boolean no_acc, boolean no_lck) throws Exception {
        if (PscGid.isVld( gid)) {
            PscDto dto = PsaUti.newDto( ssn, dto_dsc, false, false, no_acc, no_lck);
            if (dto != null) {
                dto.setQue( "PSC_GID", gid);
                if (dto.fetDat() == 1) {
                    return dto;
                }
            }
        }       
        return null;
    }
    
    public static PscDto newDto(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que) throws Exception {
    	return newDto(ssn, dto_dsc, que_fld_dsc, que, false, false);
    }
    
	public static PscDto newDto(PscSsn ssn, String dto_dsc, String que_fld_dsc, String que, boolean no_acc,
			boolean no_lck) throws Exception {
		if (!PscUti.isStr(que)) {
			throw new Exception("Using an empty query in IplDlgCsoUti.newDto() not allowed !");
		}
		PscDto dto = PsaUti.newDto(ssn, dto_dsc, false, false, no_acc, no_lck);
		dto.setMax(10000);
		dto.setQue(que_fld_dsc, que);
		if (dto.fetDat() > 0) {
			return dto;
		}
		return null;
	}
	
	
	
	public static PscDto fetDtoOne(boolean thr_exc, PscSsn ssn, String dto_dsc, String... que_par_lis) throws Exception {
		return fetDtoOne(thr_exc, ssn, dto_dsc, false, false, que_par_lis);
	}
	
	public static PscDto fetDtoOne(boolean thr_exc, PscSsn ssn, String dto_dsc, boolean no_acc, boolean no_lck, String... que_par_lis) throws Exception {
		boolean thr_not_fnd_exc = thr_exc;
		boolean thr_not_uni_exc = thr_exc;
		return fetDtoOne(thr_not_fnd_exc, thr_not_uni_exc, ssn, dto_dsc, no_acc, no_lck, que_par_lis);
	}
	
	public static PscDto fetDtoOne(boolean thr_not_fnd_exc, boolean thr_not_uni_exc, PscSsn ssn, String dto_dsc, String... que_par_lis) throws Exception {
		return fetDtoOne(thr_not_fnd_exc, thr_not_uni_exc, ssn, dto_dsc, false, false, que_par_lis);
	}
	
	public static PscDto fetDtoOne(boolean thr_not_fnd_exc, boolean thr_not_uni_exc, PscSsn ssn, String dto_dsc, boolean no_acc, boolean no_lck, String... que_par_lis) throws Exception {
		PscDto dto = PsaUti.newDto(ssn, dto_dsc, false, false, no_acc, no_lck);
		return fetDtoOne(thr_not_fnd_exc, thr_not_uni_exc, dto, que_par_lis);
	}
	
	public static PscDto fetDtoOne(boolean thr_exc, PscDto dto, String... que_par_lis) throws Exception {
		boolean thr_not_fnd_exc = thr_exc;
		boolean thr_not_uni_exc = thr_exc;
		return fetDtoOne(thr_not_fnd_exc, thr_not_uni_exc, dto, que_par_lis);
	}
	
	public static PscDto fetDtoOne(boolean thr_not_fnd_exc, boolean thr_not_uni_exc, PscDto dto, String... que_par_lis) throws Exception {
		String dto_dsc = dto.getDsc();
		boolean is_uni = true;
		try {
			dto = fetDto(dto, 1, que_par_lis);
			if (dto != null && dto.numRec() > 1) {
				is_uni = false;
			}
		} catch (PscExc psc_exc) {
			if (psc_exc.getNum() != EnuExc.DBD_DTO_FET_MAX) {
				throw psc_exc;
			} else {
				is_uni = false;
			}
		}
		boolean has_que_par = que_par_lis != null && que_par_lis.length > 0;
		if (!is_uni) {
			if (thr_not_uni_exc) {
				String msg_sfx = has_que_par ? " for query: "+Priv.getUsxParStr(que_par_lis) : "";
				throw new RuntimeException("Record in "+dto_dsc+" not UNIQUE"+msg_sfx);
			} else {
				return null;
			}
		} else if (dto == null) { // 0 records found
			if (thr_not_fnd_exc) {
				String msg_sfx = has_que_par ? " for query: "+Priv.getUsxParStr(que_par_lis) : "";
				throw new RuntimeException("Record in "+dto_dsc+" not FOUND"+msg_sfx);
			} else {
				return null;
			}
		}
		return dto;
	}

	public static PscDto fetDto(PscSsn ssn, String dto_dsc, String... que_par_lis) throws Exception {
		return fetDto(ssn, dto_dsc, false, false, que_par_lis);
	}
	
	public static PscDto fetDto(PscSsn ssn, String dto_dsc, boolean no_acc, boolean no_lck, String... que_par_lis) throws Exception {
		return fetDto(ssn, dto_dsc, no_acc, no_lck, null, que_par_lis);
	}

	/**
	 * 
	 * @param ssn
	 * @param dto_dsc
	 * @param no_acc
	 * @param no_lck
	 * @param que_par_lis single gid OR Array having {FLD1,QUE1,FLD2,QUE2...} OR single usx query string (see {@link IplDlgCsoUti#setUsxQue(PscDto, UsxPar)}) 
	 * @throws Exception
	 */
	public static PscDto fetDto(PscSsn ssn, String dto_dsc, boolean no_acc, boolean no_lck, Integer max_rec, String... que_par_lis) throws Exception {
		PscDto dto = PsaUti.newDto(ssn, dto_dsc, false, false, no_acc, no_lck);
		return fetDto(dto, max_rec, que_par_lis);
	}
	
	public static PscDto fetDto(PscDto dto, String... que_par_lis) throws Exception {
		return fetDto(dto, null, que_par_lis);
	}
	
	public static PscDto fetDto(PscDto dto, Integer max_rec, String... que_par_lis) throws Exception {
		boolean has_que_par = que_par_lis != null && que_par_lis.length > 0;
		boolean is_bad_que = false;
		if (has_que_par) {
			is_bad_que = (que_par_lis.length >= 2 && que_par_lis.length % 2 != 0) || // uneven param count
					Arrays.stream(que_par_lis).anyMatch((str)->!PscUti.isStr(str)) ; // empty field/query
		}
		if (is_bad_que) {
			throw new Exception("Query in IplDlgCsoUti.fetDto() empty/invalid!");
		}
		if (max_rec != null && max_rec == 0) {
			throw new RuntimeException("max_rec == 0");
		}
		dto.setMax(max_rec != null ? max_rec : 10000);
		if (has_que_par) {
			if (que_par_lis.length == 1) {
				boolean is_psc_gid_que = !que_par_lis[0].contains(":=");
				if (is_psc_gid_que) {
					dto.setQue("PSC_GID", que_par_lis[0]);
				} else {
					setUsxQue(dto, que_par_lis[0]);
				}
			} else { // >= 2 param
				String las_fld_dsc = null;
				for (int i = 0; i < que_par_lis.length; i++) {
					if (i % 2 == 0) { // field dsc
						las_fld_dsc = que_par_lis[i];
					} else { // que
						String que = que_par_lis[i];
						dto.setQue(las_fld_dsc, que);
					}
				}
			}
		}
		if (dto.fetDat() > 0) {
			return dto;
		}
		return null;
	}

	/**
	 * 
	 * @param rel_dto_dsc
	 * @param fat_dto
	 * @param fat_row
	 * @param chd_dto
	 * @param chd_row
	 * @return false if objects are already linked
	 * @throws Exception 
	 */
	static boolean lnkIfNot(String rel_dto_dsc, PscDto fat_dto, Integer fat_row, PscDto chd_dto, Integer chd_row) throws Exception {
		String fat_gid = fat_dto.getDat("PSC_GID", fat_row);
		String chd_gid = chd_dto.getDat("PSC_GID", chd_row);
		if (!PscGid.isVld(fat_gid) && !PscGid.isOut(fat_gid)) {
			fat_gid = fat_dto.getDbi().creGid();
			PsaDto.setSysDat(fat_dto, "PSC_GID", fat_row, fat_gid);
			fat_dto.putDat();
		}
		if (!PscGid.isVld(chd_gid) && !PscGid.isOut(chd_gid)) {
			chd_gid = chd_dto.getDbi().creGid();
			PsaDto.setSysDat(chd_dto, "PSC_GID", chd_row, chd_gid);
			chd_dto.putDat();
		}
		return lnkIfNot(fat_dto.getSsn(), rel_dto_dsc, fat_gid, chd_gid);
	}


	static boolean lnkIfNot(PscSsn ssn, String rel_dto_dsc, String fat_gid, String chd_gid) throws Exception {
		if (!PscGid.isVld(fat_gid) || !PscGid.isVld(chd_gid)) {
			throw new RuntimeException("!PscGid.isVld(fat_gid) || !PscGid.isVld(chd_gid)");
		}
		PscDto rel_dto = PsaUti.newDto(ssn, rel_dto_dsc, false, false, true, true);
		rel_dto.setQue("FAT_GID", fat_gid);
		rel_dto.setQue("CHD_GID", chd_gid);
		if (rel_dto.fetDat() == 0) {
			rel_dto.insRow(1);
			rel_dto.setDat("FAT_GID", 1, fat_gid);
			rel_dto.setDat("CHD_GID", 1, chd_gid);
			rel_dto.putDat();
			return true;
		} else {
			return false;
		}
	}

	static boolean isMaiSelRow(PscFrm frm, Integer row) {
		if (frm != null && row != null && row > 0 && row <= frm.numRec()) {
			int[] lis_row = frm.getLisRow();
			if (lis_row != null && lis_row.length > 0 && lis_row[0] == row) {
				return true;
			}
		}
		return false;
	}
	
	public static void NEW_FRM_IGN_ACC(PscSsn ssn, String par) throws Exception {
    	newFrmIgnAcc(ssn.getGui(), new UsxPar(par).getPar("DSC"));
    }
    
    static void newFrmIgnAcc(PscGui gui, String frm_dsc) throws Exception {
    	if (!PscUti.isStr(frm_dsc) || gui == null) {
    		throw new RuntimeException("!PscUti.isStr(frm_dsc) || gui == null");
    	}
    	PscSsn ssn = gui.getSsn();
    	String dto_dsc = PsaDto.getFldDat(ssn, "PSC_DLG", "NAM", frm_dsc, "DTO", false);
    	if (!PscUti.isStr(dto_dsc)) {
    		throw new RuntimeException("!PscUti.isStr(dto_dsc)");
    	}
    	PscDto dto = PsaUti.newDto(gui.getSsn(), dto_dsc, false, false, true, false);
    	dto.creIdn();
    	gui.newFrm(frm_dsc, dto);
    }
    
    /**
	 * Get data immediate from a field without checking access
	 * @param dto the dto to use
	 * @param fld_dsc the field to get
	 * @param row the row to use
	 * @throws Exception
	 */
    public static String getDatIgnAcc(PscDto dto, String fld_dsc, int row) throws Exception
    {
    	PscFld fld = dto.getFld(fld_dsc);
    	if (dto.chkAcc('R') && dto.chkAcc(row, 'R') && dto.chkAcc(fld, 'R')) {
    		return dto.getDat(fld, row);
    	} else {
    		 PscSsn ssn = dto.getSsn();
    		 PscImp old_imp = dto.getImp();
    		 if (old_imp != null) {
    			 boolean old_acc = old_imp.getAcc();
    			 try {
    				old_imp.setAcc(false);
    				return dto.getDat(fld, row);
				} finally {
					old_imp.setAcc(old_acc);
				}
    		 } else {
    			 PscImp new_imp = new PscImp(ssn);
    			 new_imp.setAcc(false);
    			 try {
    				 dto.setImp(new_imp);
    				 return dto.getDat(fld, row);
    			 } finally {
    				 dto.setImp(null);
    			 }
    		 }
    	}
    }

    /**
     * dialog function - put itself in value buffer of dyn_dto
     */
    public static void PUT_DLG_IN_DYN_DTO_VAL_BUF(PscFrm frm, String par, Integer row) throws Exception {
    	PUT_DLG_IN_DYN_DTO_VAL_BUF(frm);
    }
    
    public static void PUT_DLG_IN_DYN_DTO_VAL_BUF(PscFrm frm, String par) throws Exception {
    	PUT_DLG_IN_DYN_DTO_VAL_BUF(frm);
    }
    
    public static void PUT_DLG_IN_DYN_DTO_VAL_BUF(PscFrm frm) throws Exception {
    	PscDto dyn_dto = frm.getDynDto();
    	if (dyn_dto != null) {
    		dyn_dto.setValBuf("DLG_OF_DTO", frm);
    	}
    }

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
	
	static <T> T trnGet(PscSsn ssn, Supplier_WithExceptions<T> get_mth) throws Exception {
		SavPnt sav = null;
		try {
			sav = new SavPnt(ssn);
			T ret = (T) get_mth.get();
			sav.end();
			return ret;
		} finally {
			if (sav != null) {
				sav.abo();
			}
		}
	}
	
	static void creExp(PscSsn ssn, String blb_nam, String dto_dsc, String usx_que) throws Exception {
		 PscExp exp = new PscExp(ssn);
        exp.setRmt(false);
        String dsc = "TMP_LOA__"+blb_nam;
        exp.setDsc(dsc);
        exp.setZip(true);
        String fil_pth = System.getProperty("java.io.tmpdir") + "/" +dsc +".zip";
        exp.setFil(fil_pth);
        exp.setMsg(true);
        exp.setAcc(true);
        exp.setUsx(true);
        exp.setBlb(true);
        exp.setLnk(true);
        exp.setStp(1000);
        exp.setMax(10000);
        exp.setExl(PscOut.LEV_WRN);
        exp.setPgb(0);

        PscDto src_dto = exp.creDto( dto_dsc);
        IplDlgCsoUti.setUsxQue(src_dto, usx_que);
        exp.addDto(src_dto, null, null, false, false);
        exp.creDom();
        exp.expDom();
	}


	static void expLoaAsBlb(PscSsn ssn, String blb_nam, String dto_dsc, String usx_que, Boolean set_cst_as_blb_own)
			throws Exception {
		PscExp exp = new PscExp(ssn);
		exp.setRmt(false);
		String dsc = "TMP_LOA__" + blb_nam;
		exp.setDsc(dsc);
		exp.setZip(true);
		String fil_pth = System.getProperty("java.io.tmpdir") + "/" + dsc + ".zip";
		exp.setFil(fil_pth);
		exp.setMsg(true);
		exp.setAcc(true);
		exp.setUsx(false);
		exp.setBlb(true);
		exp.setLnk(true);
		exp.setStp(1000);
		exp.setMax(10000);
		exp.setExl(PscOut.LEV_WRN);
		exp.setPgb(1000);
		exp.setStp(1000);

		PscDto src_dto = exp.creDto(dto_dsc);
		IplDlgCsoUti.setUsxQue(src_dto, usx_que);
		exp.addDto(src_dto, null, null, false, false);

		exp.creDom();
		exp.expDom();

		byte[] byt = Files.readAllBytes(Paths.get(fil_pth));
		PscSsn blb_sav_ssn = ssn;
		try {
			if (set_cst_as_blb_own != null) {
				if (set_cst_as_blb_own && ssn.getUic() != 2000) {
					blb_sav_ssn = NewSsn.newSsn(ssn, "CUSTOMIZER");
				} else if (!set_cst_as_blb_own && ssn.getUic() == 2000) {
					blb_sav_ssn = NewSsn.newSsn(ssn, "SALESADMIN");
				}
			}
			BlbUtl.setBlb(blb_sav_ssn, blb_nam, byt, "zip", true);
		} finally {
			if (blb_sav_ssn != null && ssn != blb_sav_ssn) {
				blb_sav_ssn.exiSsn();
			}
		}
	}
	
	static void impLoaFroBlb(PscSsn ssn, String blb_nam, boolean use_trn) throws Exception {
		PscImp imp = getLoaFroBlb(ssn, blb_nam);
		impLoa(imp, use_trn);
	}

	static PscImp getLoaFroBlb(PscSsn ssn, String blb_nam) throws Exception {
		byte[] byt = BlbUtl.getBlb(ssn, blb_nam, true);
		if (byt == null || byt.length == 0) {
			throw new RuntimeException("Blob '"+blb_nam+"' not found or empty");
		}
		String loa_dsc = "TMP_LOA__"+blb_nam;
		String fil_pth = System.getProperty("java.io.tmpdir") + "/" +loa_dsc +".zip";
		Files.write(Paths.get(fil_pth), byt);
		PscImp  imp = new PscImp(ssn);
        imp.setRmt(false);
        imp.setDsc( loa_dsc);
        imp.setFil( fil_pth);
        imp.setZip( true);
        imp.setMsg(true);
        imp.setAcc( true);
        imp.setLck( false);
        imp.setChk( false);
        imp.setUpd( false);
        imp.setDel( false);
        imp.setUsx( false);
        imp.setBlb( true);
        imp.setDcp( false);
        imp.setLnk( true);
        imp.setExl(PscOut.LEV_CTL);
        imp.setStp( 1000);
        imp.setPgb( 1000);
        imp.setMod('O');
        return imp;
	}

	static void impLoa(PscImp imp, boolean use_trn) throws Exception {
		PscSsn ssn = imp.getSsn();
		Runnable_WithExceptions run_obj = () -> {
			try (AutoCloseable _cls_los = () -> imp.clsLog()) {
				imp.opnLog();
				for (String nam = imp.nxtXml(null); nam != null; nam = imp.nxtXml(nam)) {
					imp.creDom(nam);
					imp.impDom(nam);
				}
				imp.impCsv();
				imp.impBlb();
			}
		};
		if (use_trn) {
			trn(ssn, run_obj);
		} else {
			run_obj.accept();
		}
	}
	
	
	static boolean creIfNot(PscSsn ssn, String dto_dsc, String que_and_wri_usx) throws Exception {
		return creIfNot(ssn.newDto(dto_dsc), que_and_wri_usx);
	}
	
	static boolean creIfNot(PscDto dto, String que_and_wri_usx_str) throws Exception {
		setUsxQue(dto, que_and_wri_usx_str);
		boolean cre_new = dto.fetDat() == 0;
		if (cre_new) {
			dto.insRow(1);
			for (Ent ent: new UsxPar(que_and_wri_usx_str)) {
				String val = ent.Val;
				if (val == null || val.equals("null")) {
					val = "";
				}
				dto.setDat(ent.Key, 1, val);
			}
			dto.putDat();
		}
		return cre_new;
	}
}