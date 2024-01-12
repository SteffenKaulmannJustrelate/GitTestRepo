// Last update by user CUSTOMIZER on host NB-GEGER-18 at 20200922085356
import org.apache.commons.lang3.reflect.MethodUtils;

import de.pisa.psc.srv.glb.EnuExc;
import de.pisa.psc.srv.glb.PscExc;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.gui.PscGui;
import de.pisa.psc.srv.sio.PscOut;
import de.pisa.psc.srv.svc.PscUti;
import de.pisa.psc.srv.sys.PscDtoIpl;
import de.pisa.psc.srv.sys.PscUsx;

/** CSO_INS_OGE_GVZ_GRA_UTI_BASE_NAKED */
public class CsoInsOgeGvzGraUtiBaseNaked {

	String DYN_JAV_QUE = "";
	
	String IPL_DTO_QUE = "'CSO_GVZ_GRA'";
	String IPL_DLG_QUE = "'CSO_GVZ_GRA_EAO'";
	String IPL_GLB_QUE = "'CSO_GVZ_GRA_UTI'";
	String IPL_WSV_QUE = "";
	String IPL_LOA_QUE = "";
	
	// order matters here!
	String DTO_DSC_QUE = "'CSO_GVZ_CHC_PAR' | 'CSO_GVZ_CHC_PAR_EDG_SPL' | 'CSO_GVZ_CHC_PAR_POS_ENG' | 'CSO_GVZ_GRA' | 'CSO_GVZ_GRA_FAT' | 'CSO_GVZ_GRA_XTD' | 'CSO_MEN_GVZ_MAX_DPH'";
	
	/** USX query for deleting records. Separate multiple operations by separator |#SEP#|
	 * DTO:=PSC_TAG  QUE:=NAM=NAM | TIT_NAM  MAX:=2 |#SEP#|  DTO:=PSC_ENV_GLB  QUE:=NAM=TEST123  MAX:=50  Optional parameters: NO_ACC, NO_LCK, NO_USX, NO_LNK
	 *  */
	String DEL_USX_QUE_PAR_LIS = "";
	
	// Userexits to execute BEFORE installation, example: CSO_MY_CLA.myMth, CSO_MY_OTH_CLA.RUN, GLB:MY_GLB_IPL.RUN
	String PRE_USX_LIS = "";
	
	// Userexits to execute AFTER installation, example: CSO_MY_CLA.myMth, CSO_MY_OTH_CLA.RUN, GLB:MY_GLB_IPL.RUN
	String PST_USX_LIS = "";
	
	/** Add menus, syntax: KEY->DIALOG(,KEY->DIALOG)... 
	 * example: MY_KEY->PSA_TOP_TLS - add all (template) tags with DLG=MY_KEY to the (CSO version of) menu PSA_TOP_TLS
	 */
	String ADD_MEN_VIA_KEY = "TEMPL_KEY_GVZ_V1_ORG_EXT_MEN->PSA_ORG_EXT_MEN,TEMPL_KEY_GVZ_V1_PRO_OBJ_MEN->PSA_PRO_OBJ_MEN,TEMPL_KEY_GVZ_V1_PRS_EXT_MEN->PSA_PRS_EXT_MEN,TEMPL_KEY_GVZ_V1_OPP_MEN->PSA_OPP_MEN,TEMPL_KEY_GVZ_V1_OPP_PME->PSA_OPP_PME,TEMPL_KEY_GVZ_V1_ORD_MEN->PSA_ORD_MEN,TEMPL_KEY_GVZ_V1_ORD_PME->PSA_ORD_PME,TEMPL_KEY_GVZ_V1_ORG_EXT_PME->PSA_ORG_EXT_PME,TEMPL_KEY_GVZ_V1_PRO_OBJ_PME->PSA_PRO_OBJ_PME,TEMPL_KEY_GVZ_V1_PRO_SAL_MEN->PSA_PRO_SAL_MEN,TEMPL_KEY_GVZ_V1_PRO_SAL_PME->PSA_PRO_SAL_PME,TEMPL_KEY_GVZ_V1_PRS_EXT_PME->PSA_PRS_EXT_PME,TEMPL_KEY_GVZ_V1_QUO_MEN->PSA_QUO_MEN,TEMPL_KEY_GVZ_V1_QUO_PME->PSA_QUO_PME";
	
	public void run(PscGui gui) throws Exception {
		PscSsn ssn = gui.getSsn();
		// compile main installation class and run it
		cplIplSrc(ssn, "CSO_INS_UTI", "GLB");
		Class<?> clz = PscUsx.loaGlbIpl("CSO_INS_UTI");
		if ( clz == null) {
			throw new RuntimeException("Main intstallation class 'CSO_INS_UTI' not found!");
		}
		MethodUtils.invokeStaticMethod(clz, "RUN", ssn, this);
	}
	
	
	// needed only for compilation of main installation class..
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
}