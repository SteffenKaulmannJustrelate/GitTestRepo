// Last update by user CUSTOMIZER on host NB-GEGER-18 at 20190121194651
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.PscUti;
import de.pisa.psc.srv.sys.PscUsx;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psa.dto.UsxPar;
import de.pisa.psa.ifc.*;
import java.util.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.log4j.*;


/** CSO_INV */
public class CsoInv {
    
    private static final String CSO_INV_UTI_OBJ_CHE_CTX_KEY =  "CSO_INV_UTI_OBJ";
    private static final String CSO_INV_UTI_CLA_CHE_CTX_KEY =  "CSO_INV_UTI_CLA";

	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
	
	}
	
	public static <T> T invOn(Object inst, String mth_nam) throws Exception{
		return invOn(inst, mth_nam, null);
	}
	
	public static <T> T invOn(Object inst, String mth_nam, Object... args) throws Exception{
		if (inst == null) {
			throw new UnsupportedOperationException("The instance can't be null here. Use utility 'CsoInvGlbIpl.inv(..)' to call a static mehtod if you have no instance.");
		}
		Class<?> cla = inst.getClass();
		Method mth = fndMth(cla, mth_nam, args, true);
		if (mth == null) {
			throw new NoSuchMethodException("Method " + mth_nam + " having given signature not found for class "+cla.getName());
		}
		return (T) mth.invoke(inst, args);
	}
	
	public static void UTI(PscSsn ssn, String par) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		String mth_dsc = usx_par.getPar("UTI_MTH");
		usx_par.delPar("UTI_MTH");
		String new_par_str = usx_par.genParStr(false);
		uti(mth_dsc, ssn, new_par_str);
	}
	
	public static void UTI(PscFrm frm, String par) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		String mth_dsc = usx_par.getPar("UTI_MTH");
		usx_par.delPar("UTI_MTH");
		String new_par_str = usx_par.genParStr(false);
		uti(mth_dsc, frm, new_par_str);
	}
	
	public static void UTI(PscFrm frm, String par, Integer row) throws Exception {
		UsxPar usx_par = new UsxPar(par);
		String mth_dsc = usx_par.getPar("UTI_MTH");
		usx_par.delPar("UTI_MTH");
		String new_par_str = usx_par.genParStr(false);
		uti(mth_dsc, frm, new_par_str, row);
	}
	
	public static Object getUtiObj(PscSsn ssn, Class<?> uti_ifc_cla) {
        Object uti_obj_obj = Proxy.newProxyInstance( uti_ifc_cla.getClassLoader(), new Class<?>[]{uti_ifc_cla}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String mth_nam = method.getName();
                return RefUti.invMthDynJav( ssn, "CSO_INV", "uti", mth_nam, args);
            }
        });
        return uti_obj_obj;
	}
	
	public static <T> T uti(String mth_nam) throws Exception{
	    return uti( mth_nam, null);
	}
	
	public static <T> T uti(String mth_nam, Object... args) throws Exception{
	    String rep_cla_nam = "CSO_UTI";
	    boolean dlg_boo = true;
	    return inv( dlg_boo, rep_cla_nam, mth_nam, args);
	}
	
	public static <T> T inv(String rep_cla_nam, String mth_nam) throws Exception{
	   return inv( rep_cla_nam, mth_nam, null);
	}
	
	public static <T> T inv(String rep_cla_nam, String mth_nam, Object... args) throws Exception{
	    boolean dlg_boo;
	    if (rep_cla_nam.endsWith( "FII")) {
	        dlg_boo = true;
	    } else if (rep_cla_nam.endsWith( "DII")) {
	        dlg_boo = false;
	    } else {
	        throw new Exception("Not specified being a dlg or dto ipl!");
	    }
	    return inv( dlg_boo, rep_cla_nam, mth_nam, args);
	}
	
	public static <T> T inv(boolean dlg_boo, String rep_cla_nam, String mth_nam) throws Exception{
	    return inv( dlg_boo, rep_cla_nam, mth_nam, null);
	}
	
	public static <T> T inv(boolean dlg_boo, String rep_cla_nam, String mth_nam, Object... args) throws Exception{
	    boolean obj_in_che_boo = false;
	    String jav_cla_nam = getJavClaNam( dlg_boo, rep_cla_nam);
        Object cla_obj = PsaGlbChe.get( null, CSO_INV_UTI_OBJ_CHE_CTX_KEY, jav_cla_nam);
        Class<?> cla;        
	    if (cla_obj != null) {
	        obj_in_che_boo = true;
	        cla = cla_obj.getClass();
	    } else {	        
	        cla = getIplFroPsaChe(dlg_boo, rep_cla_nam); // primary use psa cacce to get
            if (cla == null) {
                cla = loaIplUseCsoChe( dlg_boo, rep_cla_nam);
            }
	    }	    
	    Method mth = fndMth(cla, mth_nam, args, true);
	    mth.setAccessible( true);
	    boolean mth_is_sta =  Modifier.isStatic( mth.getModifiers());	  	    
	    if (!mth_is_sta && cla_obj == null) {  
	        if (cla.isInterface()) {
	            final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
	            if (!constructor.isAccessible()) {
	                constructor.setAccessible(true);
	            }
	           
	            cla_obj = Proxy.newProxyInstance( cla.getClassLoader(), new Class<?>[]{cla}, new InvocationHandler() {
                    
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.isDefault())
                        {
                            final Class<?> declaringClass = method.getDeclaringClass();
                            return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
                                    .unreflectSpecial(method, declaringClass)
                                    .bindTo(proxy)
                                    .invokeWithArguments(args);
                        }
                     
                        // proxy impl of not defaults methods
                        return null;
                    }
                });
	        } else {
	            cla_obj = cla.newInstance();
	        }
	        /*
	         * NEVER cache proxy objects (case cla.isInterface()=true)
	         * there are cached per default by jvm.
	         * caching them manually can lead to errors!
	         */
	        if (!obj_in_che_boo && !cla.isInterface()) {
	            PsaGlbChe.put( null, CSO_INV_UTI_OBJ_CHE_CTX_KEY, jav_cla_nam, cla_obj);
	        }	       
	        
	    }
	    if (mth_is_sta) {
            cla_obj = null;
        }
	    T ret_obj = (T) mth.invoke( cla_obj, args);
	    return ret_obj;
	}

	public static Method fndMth(Class<?> cla, String mth_nam, Object[] args) {
		return fndMth(cla, mth_nam, args, false);
	}
	
    public static Method fndMth(Class<?> cla, String mth_nam, Object[] args, boolean inc_sup_cla) {
        if (args == null) {
            args = new Object[]{};
        }
        Method ret_mth = null;
        Method mth_lis[] = inc_sup_cla ? getAllMth(cla) : cla.getDeclaredMethods();
        for (int i = 0; i < mth_lis.length; i++) {
            Method mth_i = mth_lis[i];
            String mth_i_nam = mth_i.getName();
            if (mth_i_nam.equals( mth_nam)) {
                Class<?> par_cla_lis[] = mth_i.getParameterTypes();
                if (par_cla_lis.length == args.length) {
                    int par_idx = 0;
                    ret_mth = mth_i;
                    for (Class<?> par_cla: par_cla_lis) {
                    	
                    	Object arg = args[par_idx];
                        if (arg != null && !chkAssFrom( par_cla, arg.getClass())) {
                              ret_mth = null;
                              break;
                        }
                        par_idx++;
                    }
                    if (ret_mth != null) {
                        return ret_mth;
                    }
                }
            }
        }
        return null;
    }
    

    private static Method[] getAllMth(Class<?> cla) {
		Method[] dcl_mth = cla.getDeclaredMethods();
		Method[] oth_mth = cla.getMethods();
		
		Set<Method> mth_chk_set = new HashSet<>(Arrays.asList(dcl_mth));
		List<Method> mth_lnk_lis = new LinkedList<>(Arrays.asList(dcl_mth));
		
		for (int i = 0; i < oth_mth.length; i++) {
			Method mth_i = oth_mth[i];
			if (!mth_chk_set.contains(mth_i)) {
				mth_chk_set.add(mth_i);
				mth_lnk_lis.add(mth_i);
			}
		}
		Method[] ret_mth_arr = mth_lnk_lis.toArray(new Method[mth_lnk_lis.size()]);
		return ret_mth_arr;
	}

	public static boolean chkAssFrom(Class<?> par_cla, Class<?> arg_cla) {
        if ( par_cla.isAssignableFrom( arg_cla)) {
            return true;
        } else {
            /*
             * autoboxing (one side: wrapper -> primitive) and conversion to Number-primitives
             */
            if (par_cla.isPrimitive()) {            
                if ( par_cla == int.class) {
                    if ( Arrays.asList( new Class<?>[] { Integer.class }).contains( arg_cla)) {
                        return true;
                    }
                } else if ( par_cla == double.class) {
                    if ( Arrays.asList( new Class<?>[] { Double.class, Integer.class, int.class, Long.class, long.class }).contains( arg_cla)) {
                        return true;
                    }
                } else if ( par_cla == long.class) {
                    if ( Arrays.asList( new Class<?>[] { Long.class, Integer.class, int.class }).contains( arg_cla)) {
                        return true;
                    }
                } else if ( par_cla == boolean.class) {
                    if ( Arrays.asList( new Class<?>[] { Boolean.class }).contains( arg_cla)) {
                        return true;
                    }
                }
            } else {
                /*
                 * other side of autoboxing (primitive -> wrapper)
                 */
                if ( par_cla == Integer.class) {
                    if ( Arrays.asList( new Class<?>[] { int.class }).contains( arg_cla)) {
                        return true;
                    }
                } else if ( par_cla == Double.class) {
                    if ( Arrays.asList( new Class<?>[] { double.class }).contains( arg_cla)) {
                        return true;
                    }
                } else if ( par_cla == Long.class) {
                    if ( Arrays.asList( new Class<?>[] { long.class }).contains( arg_cla)) {
                        return true;
                    }
                } else if ( par_cla == Boolean.class) {
                    if ( Arrays.asList( new Class<?>[] { boolean.class }).contains( arg_cla)) {
                        return true;
                    }
                }
            }
        }        
        return false;
    }

    private static Class<?> getIplFroPsaChe(boolean dlg_boo, String rep_cla_nam) {
        String jav_cla_nam = getJavClaNam( dlg_boo, rep_cla_nam);
        String rep_idn = PscUsx.getNam(jav_cla_nam);
        Class<?> cla = BlbLoa.getCls(rep_idn);
        return cla;
    }
    
    private static Class<?> loaIplUseCsoChe(boolean dlg_boo, String rep_cla_nam) {
        String jav_cla_nam = getJavClaNam( dlg_boo, rep_cla_nam);
        String rep_idn = PscUsx.getNam(jav_cla_nam);
        
        Class<?> cla = (Class<?>)PsaGlbChe.get(null, CSO_INV_UTI_CLA_CHE_CTX_KEY, rep_idn);
        if  (cla == null) {
        	cla = dlg_boo ? PscUsx.loaDlgIpl(rep_cla_nam) : PscUsx.loaDtoIpl(rep_cla_nam);
        	PsaGlbChe.put(null, CSO_INV_UTI_CLA_CHE_CTX_KEY, rep_idn, cla);
        }
        return cla;
    }
    
    private static String getJavClaNam(boolean dlg_boo, String rep_cla_nam) {
        String jav_cla_nam = "Ipl" + (dlg_boo ? "Dlg" : "Dto") + PscUsx.creNam( rep_cla_nam);
        return jav_cla_nam;
    }
	
}