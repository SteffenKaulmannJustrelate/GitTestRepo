// Last update by user CUSTOMIZER on host NB-GEGER-18 at 20200206152008
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.sys.PscUsx;
import de.pisa.psc.srv.dto.PscFld;
import de.pisa.psc.srv.glb.*;
import de.pisa.psa.dto.UsxPar;
import de.pisa.psa.ifc.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Method;

import org.apache.log4j.*;

/** CSO_INV_GLB_IPL */
public class CsoInvGlbIpl {

	private static final String CSO_INV_UTI_CLA_CHE_CTX_KEY =  "CSO_INV_UTI_CLA";
	
	
	
	public static int INV(PscSsn ssn, String par) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		
		Method run_mth = getMth(usx_par, PscSsn.class, String.class);
		
		
		String new_par_str = usx_par.genParStr(false);
		run_mth.invoke(null, ssn, new_par_str);
		return 0;
	 }
	
	public static int INV(PscGui gui, String par) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		
		Method run_mth = getMth(usx_par, PscGui.class, String.class);
		
		
		String new_par_str = usx_par.genParStr(false);
		run_mth.invoke(null, gui, new_par_str);
		return 0;
	 }
	
	public static void INV(PscFrm frm, String par) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		
		Method run_mth = getMth(usx_par, PscFrm.class, String.class);
		
		String new_par_str = usx_par.genParStr(false);
		run_mth.invoke(null, frm, new_par_str);
	}
	
	public static void INV(PscFrm frm, String par, Integer row) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		
		Method run_mth = getMth(usx_par, PscFrm.class, String.class, Integer.class);
		
		String new_par_str = usx_par.genParStr(false);
		run_mth.invoke(null, frm, new_par_str, row);
	}
	
	public static void INV(PscFrm frm, String par, Integer row, PscFld fld) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		
		Method run_mth = getMth(usx_par, PscFrm.class, String.class, Integer.class, PscFld.class);
		
		String new_par_str = usx_par.genParStr(false);
		run_mth.invoke(null, frm, new_par_str, row, fld);
	}
	
	
	public static void INV_GUI(PscSsn ssn, String par) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		String rep_cla_nam = usx_par.getPar("INV_IPL");
		
		Class<?> cla = getIplFroPsaChe(rep_cla_nam); // primary use psa cache to get
        if (cla == null) {
            cla = loaIplUseCsoChe( rep_cla_nam);
        }
        Object obj = cla.newInstance();
        Method run_mth = cla.getMethod("run", PscGui.class);

		run_mth.invoke(obj, ssn.getGui());
	}
	
	public static <T> T inv(PscSsn ssn, String rep_cla_and_jav_mth, Object... args) throws Exception{
		int dot_idx = rep_cla_and_jav_mth.indexOf('.');
		String rep_cla_nam_wit_typ = rep_cla_and_jav_mth.substring(0, dot_idx);
		String mth_nam = rep_cla_and_jav_mth.substring(dot_idx + 1);
		
		Class<?> cla = getIplFroPsaChe(rep_cla_nam_wit_typ); // primary use psa cache to get
        if (cla == null) {
            cla = loaIplUseCsoChe( rep_cla_nam_wit_typ);
        }
        if (cla == null) {
        	throw new ClassNotFoundException(rep_cla_nam_wit_typ);
        }
        Method mth = RefUti.invMthDynJav(ssn, "CSO_INV", "fndMth", cla, mth_nam, args);
        if (mth == null) {
        	throw new NoSuchMethodException(rep_cla_and_jav_mth);
        }
        return (T) mth.invoke(null, args);
	}
	
	public static Method getMth(UsxPar usx_par, Class<?>... cla_lis) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		String run_par_str = usx_par.getPar("INV_IPL");
		usx_par.delPar("INV_IPL");
		int dot_idx = run_par_str.indexOf('.');
		String rep_cla_nam_wit_typ = run_par_str.substring(0, dot_idx);
		String mth_nam = run_par_str.substring(dot_idx + 1);
		
		Class<?> cla = getIplFroPsaChe(rep_cla_nam_wit_typ); // primary use psa cache to get
        if (cla == null) {
            cla = loaIplUseCsoChe( rep_cla_nam_wit_typ);
        }
        if (cla == null) {
        	throw new ClassNotFoundException("Repository class '"+rep_cla_nam_wit_typ+"' not found");
        }
		Method run_mth = cla.getMethod(mth_nam, cla_lis);
		return run_mth;
	}


	private static Class<?> getIplFroPsaChe(String rep_cla_nam) {
		String jav_cla_nam = getJavClaNam(rep_cla_nam);
		String rep_idn = PscUsx.getNam(jav_cla_nam);
		Class<?> cla = BlbLoa.getCls(rep_idn);
		return cla;
	}

	private static Class<?> loaIplUseCsoChe(String rep_cla_nam_wit_typ) {
		String jav_cla_nam = getJavClaNam(rep_cla_nam_wit_typ);
		String rep_idn = PscUsx.getNam(jav_cla_nam);

		Class<?> cla = (Class<?>) PsaGlbChe.get(null, CSO_INV_UTI_CLA_CHE_CTX_KEY, rep_idn);
		if (cla == null) {
			String rep_cla_nam = rep_cla_nam_wit_typ;
			if (rep_cla_nam.contains(":")) {
				rep_cla_nam = rep_cla_nam.substring(rep_cla_nam.indexOf(':')+1);
			}
			if (jav_cla_nam.startsWith("IplGlb")) {
				cla = PscUsx.loaGlbIpl(rep_cla_nam);
			} else if (jav_cla_nam.startsWith("IplDlg")) {
				cla = PscUsx.loaDlgIpl(rep_cla_nam);
			} else if (jav_cla_nam.startsWith("IplDto")) {
				cla = PscUsx.loaDtoIpl(rep_cla_nam);
			} else {
				throw new RuntimeException("Invalid class: "+jav_cla_nam);
			}
			PsaGlbChe.put(null, CSO_INV_UTI_CLA_CHE_CTX_KEY, rep_idn, cla);
		}
		return cla;
	}

	private static String getJavClaNam(String typ_and_rep_cla_nam) {
		String rep_cla_nam;
		String typ;
		if (typ_and_rep_cla_nam.contains(":")) {
			rep_cla_nam = typ_and_rep_cla_nam.split(":")[1];
			typ = typ_and_rep_cla_nam.split(":")[0];
			typ = "Ipl" + typ.substring(0,1).toUpperCase() + typ.substring(1).toLowerCase();
		} else {
			rep_cla_nam = typ_and_rep_cla_nam;
			typ = "IplGlb";
		}
		String jav_cla_nam = typ + PscUsx.creNam(rep_cla_nam);
		return jav_cla_nam;
	}
	
	
	
	
//	public static int INV(PscSsn ssn, String par) throws Exception {
//		UsxPar usx_par = new UsxPar(par);
//		String run_par_str = usx_par.getPar("INV_IPL");
//		int dot_idx = run_par_str.indexOf('.');
//		String glb_ipl_rep_cla_nam = run_par_str.substring(0, dot_idx);
//		String mth_nam = run_par_str.substring(dot_idx + 1);
//
//		
//		
//		Class<?> cla = getIplFroPsaChe(glb_ipl_rep_cla_nam); // primary use psa cache to get
//        if (cla == null) {
//            cla = loaIplUseCsoChe( glb_ipl_rep_cla_nam);
//        }
//		Method run_mth = cla.getMethod(mth_nam, PscSsn.class, String.class);
//		
//		
//		String new_par_str = usx_par.genParStr(false);
//		run_mth.invoke(null, ssn, new_par_str);
//		return 0;
//	 }
}