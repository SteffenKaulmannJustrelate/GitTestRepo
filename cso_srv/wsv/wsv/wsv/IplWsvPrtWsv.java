// Last Update by user CUSTOMIZER at 20201001101249
package wsv.wsv;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.glassfish.grizzly.http.server.Request;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pisa.psa.dto.psa_cpd.PsaCpdFnc;
import de.pisa.psa.dto.psa_doc.BlbUtl;
import de.pisa.psa.dto.psa_ifc.PsaPinBrd;
import de.pisa.psa.dto.psa_scn.JobLog;
import de.pisa.psa.dto.psa_scn.JobThr;
import de.pisa.psa.ifc.PscGid;
import de.pisa.psa.ssn.PsaObjFac;
import de.pisa.psc.ipc.svc.BCrypt;
import de.pisa.psc.ipc.svc.IpcKey;
import de.pisa.psc.ipc.svc.IpcUti;
import de.pisa.psc.srv.dsi.PscDbi;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.dto.PscFld;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.glb.PscSsnPool;
import de.pisa.psc.srv.glb.PscSsnPool.Key;
import de.pisa.psc.srv.svc.PscUti;

import static de.pisa.psc.srv.svc.PscUti.getTim;
import static de.pisa.psc.srv.svc.PscUti.isStr;
import static de.pisa.psc.srv.svc.PscUti.isStrEqu;;

/**
 * Webservice for Customerportal
 * @author hoeffler
 * @since 2019
 * 
 * Productive use needs SSL-Encryption because REST is stateless (no session handling) wherefore
 * user credentials have to be passed on each request. 
 * 
 * Login handling is done in the getSsn method, which should be used on each call.
 * 
 * All access handling is done via the org gid, which you can get by getUsrOrgGid or getUsrReaOrgQue. 
 * If you need sub-organisations, change return value getUsrReaOrgSet
 * Further, more fine-grained permissions are handled in the hasPrm and getUrsPrm, from DTO PRT_USR_PRM 
 * 
 * The webserver itself usually expects JSON-Strings or stringified booleans. 
 * Two static utility methods to create JSON-Strings from DTOs (dtoToJso) and ResultSets (rslToJso) 
 * can and should be used
 * 
 * The singular requests are intentionally not very generic, for obvious security reasons. 
 * 
 * If you use getSSn() or getAdmSsn(), do not forget to return sessions back to session pool, otherwise 
 * the webservice will stop working after a dozen requests or so.
 * Alternatively, use and override helper classes basRqs and admRqs 
 * 
 * User handling is done via Administration->Tools->Web-Portal->Portal Toolbox
 * 
 * Installation: see documents in SVN/PRT/doku
 *         
 * */
@Path("prt")
public class IplWsvPrtWsv {	
	/** Cache for List of allowed request ips; is ignored if empty. On null, gets filled by DTO PRT_IP_WHT_LST */
	private ArrayList<String> IP_WHT_LST = null;
		
	/** standard dto-handling of links, userexits, access-checks and lock-handling. Ignore lock for PiSA 7.5.*/
	protected static final boolean[] DTO_STD_PAR_ARR = {false,false,false,true};
	
	/** PiSA's standard salt*/
	String STD_SLT = BCrypt.gensalt(BCrypt.GENSALT_DEFAULT_LOG2_ROUNDS, IpcKey.PSA_PSK.getKeyDat());
		
	/** Array of known file extensions for blob upload */
	protected static final String[] KNW_FIL_TYP_ARR =     {"PNG"            ,"JPG"            ,"RTF"            ,"DOC"            ,"HTML"           ,"HTM"            ,"XLSX"            ,"DOCX"};
	/** corresponding array of APP_IDNs for KNW_FIL_TYP_ARR */
	protected static final String[] KNW_FIL_APP_ARR =     {"APP_PNG"        ,"APP_JPG"        ,"APP_RTF"        ,"APP_DOC"        ,"APP_HTML"       ,"APP_HTM"        ,"APP_XSLX"        ,"APP_DOCX"};
	/** corresponding array of icons for KNW_FIL_TYP_ARR */
	protected static final String[] KNW_FIL_TYP_ICO_ARR = {"PSA_APP_ICO_PNG","PSA_APP_ICO_JPG","PSA_APP_ICO_DOC","PSA_APP_ICO_DOC","PSA_APP_ICO_HTM","PSA_APP_ICO_HTM","PSA_APP_ICO_XLSX","PSA_APP_ICO_DOCX"};
	
	
	/** current server's dbi engine, set on constructor */
	protected static int dbs = -1;

	
	public IplWsvPrtWsv() {
		super();
		if(dbs == -1) {
			PscSsn adm_ssn = null;
			try {
				adm_ssn = getAdmSsn();
				dbs = adm_ssn.getDbi().getDbs();
			}
			catch (Exception e) {
				hdlExc(e);
			}finally {
				dspSsn(adm_ssn);					
			}	
		}
	}
	
			
	/**
	 * returns language identifier for user
	 * @param usr PiSA-Username
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param ip client IP of end user (not of requesting portal server)
	 * @param req Jersey/Grizzly-Request
	 * @return language identifier, GER if none set
	 * @category Login
	 * */
	@POST
	@Path("getLng")
	public String getLng(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {				
				return getPrsLng(ssn, usr_gid);													
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * sets language for user
	 * @param usr PiSA-Username
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param ip client IP of end user (not of requesting portal server)
	 * @param lng new language identifier
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Login
	 * */
	@POST
	@Path("setLng")
	public String setLng(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("ip") String ip,@FormParam("lng") String lng,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid)) {
					return "false";	
				}
				PscDto con_dto = getWsvDto(ssn,"PSA_CON_STR");
				con_dto.setQue("PSC_GID", usr_gid);
				if(!con_dto.fetFst()) {
					return "false";						
				}				
				PscDto lng_dto = getWsvDto(ssn,"PSA_LNG");
				lng_dto.setQue("IDN", "'" + lng + "'");
				if(lng_dto.fetFst()) {
					con_dto.setDat("COM_LNG_IDN", 1, lng);
					con_dto.setDat("COM_LNG_NAM_GER", 1, lng_dto.getDat("NAM_GER", 1));
					con_dto.setDat("COM_LNG_NAM_ENG", 1, lng_dto.getDat("NAM_ENG", 1));
					con_dto.putDat();	
				}					
				return "true";				
			};
		};	
		return bas_req.go();
	}
	
	/**
	 * new user request; creates task on pisa
	 * @param nam Name of Person
	 * @param org Name of Company
	 * @param ema Email of Person
	 * @param ip IP of request client
	 * @param req Jersey/Grizzly-Request
	 * @return user message in german, since we don't know user language at this point
	 * @category Registry
	 * */
	@POST
	@Path("rqsUsr")
	public String rqsUsr(@FormParam("nam") String nam,@FormParam("org") String org,@FormParam("ema") String ema,@FormParam("ip") String ip, @Context Request req) throws Exception{				
		AdmRqs adm_req = new AdmRqs(req) {
			public String hdlRqu(PscSsn adm_ssn, String usr_gid) throws Exception {
				if(!isStr(nam) || !isStr(org) || !isStr(ema)) {					
					return adm_ssn.getEnv("PRT_STR_FIL_ALL");
				}
				//get gid of responsible user
				String rsp_usr = adm_ssn.getEnv("PRT_NEW_USR_ADM");
				if(rsp_usr == null) {
					throw new Exception("PRT_NEW_USR_ADM not set!");
				}
				
				PscDto con_int_dto = getWsvDto(adm_ssn,"PSA_CON_INT");
				con_int_dto.setQue("CIC", "'" + rsp_usr + "'");
				if(!con_int_dto.fetFst()) {
					return "";
				}
				String agn_prs_gid = con_int_dto.getDat("PSC_GID", 1);				
				
				//create task
				PscDto tsk_dto = getWsvDto(adm_ssn,"PSA_TSK");
				tsk_dto.setQue("AGN_PRS_GID", "'" + agn_prs_gid + "'");								
				tsk_dto.setQue("PSC_CRE", ">" + getPsaTimSpmPrt(adm_ssn));
				if(tsk_dto.cntDat() >= getEnvAsInt(adm_ssn, "PRT_MAX_REQ_PER_TIM_CNT")) {
					return adm_ssn.getEnv("PRT_STR_TMI");
				}			
				tsk_dto.insRow(1);
				tsk_dto.setDat("NAM_GER", 1, "Customerportal - Loginanfrage: " + nam);
				tsk_dto.setDat("NAM_ENG", 1, "Customer Portal Login Request: " + nam);
				tsk_dto.setDat("CTT", 1, "Name: " + nam + "\nFirma: " + org + "\nE-Mail: " + ema + "\nIP: " + ip  + "\n\nzu tun:\n   - Identität prüfen\n   - Login und Passwort erstellen\n   - Person in Kenntnis setzen");
				tsk_dto.setDat("AGN_PRS_GID", 1, agn_prs_gid);			
				tsk_dto.putDat();
				return adm_ssn.getEnv("PRT_STR_NEW_ACO");																	
			};
		};
		return adm_req.go();
	}	
	
	
	//-----------------------------------------------------------------------------------
	//----------------------------------- Cockpit ---------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns user's cockpit configuration; creates from standard if none exist
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param ip client IP of end user (not of requesting portal server)
	 * @param req Jersey/Grizzly-Request
	 * @category Cockpit
	 * @return contact information as JSON
	 * */
	@POST
	@Path("getCcpCnf")
	public String getCcpCnf(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.CCP) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				updCcpAlw(ssn,ssn.getUic());
				PscDto ccp_dto = getWsvDto(ssn,"PRT_CCP_CNF");
				ccp_dto.setQue("UIC", "'" + ssn.getUic() + "'");
				if(ccp_dto.cntDat() == 0) { //create from standard
					PscDto ccp_std_dto = getWsvDto(ssn,"PRT_CCP_CNF");
					ccp_std_dto.setQue("UIC", "'STD'");
					try {
						int num_rec = ccp_std_dto.fetDat();					
						ccp_dto.insRow(1, num_rec);
						for(int i = 0; i < num_rec; ++i) {						
							ccp_dto.copRow(i+1, ccp_std_dto, i+1);
							ccp_dto.setDat("UIC", i+1, ssn.getUic());
						}					
						ccp_dto.putDat();
					}finally {
						ccp_std_dto.fetCls();
					}
				}
				ccp_dto.setQue("ALW", "'y'");
				return dtoToJso(ccp_dto, "CCP_ELM_IDN", "LEF", "TPP", "WID", "HEI","HTM_COL","HID");												
			};
		};
		return bas_req.go();
	}
	
	/**
	 * sets user's cockpit configuration
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param ip client IP of end user (not of requesting portal server)
	 * @param idn Element-IDN to set
	 * @param left left-coordinate
	 * @param top top-coordinate
	 * @param w width
	 * @param h height
	 * @param col background color
	 * @param hid is element hidden (y => true, n => false) 
	 * @param req Jersey/Grizzly-Request
	 * @category Cockpit
	 * @return stringified true on success, false otherwise
	 * */
	@POST
	@Path("setCcpCnf")
	public String setCcpCnf(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("idn") String idn,@FormParam("left") String left,@FormParam("top") String top,@FormParam("w") String w,@FormParam("h") String h, @FormParam("col") String col,@FormParam("hid") String hid,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.CCP) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto ccp_dto = ssn.newDto("PRT_CCP_CNF",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				ccp_dto.setQue("UIC", "'" + ssn.getUic() + "'");
				ccp_dto.setQue("CCP_ELM_IDN", "'" + idn + "'");
				if(!ccp_dto.fetFst()) {
					return "false";
				}
				ccp_dto.setDat("LEF", 1, left);
				ccp_dto.setDat("TPP", 1, top);
				ccp_dto.setDat("WID", 1, w);
				ccp_dto.setDat("HEI", 1, h);
				ccp_dto.setDat("HTM_COL", 1, col);
				ccp_dto.setDat("HID", 1, hid);
				ccp_dto.putDat();			
				return "true";													
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * returns contact information to user and user's org.
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @category Cockpit
	 * @return contact information as JSON
	 * */
	@POST
	@Path("getCon")
	public String getCon(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.CCP) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(PscGid.isVld(usr_gid)) {
					String org_gid = getUsrOrgGid(ssn,usr);
					if(PscGid.isVld(org_gid)) {
						PscDbi dbi = ssn.getDbi();
						PreparedStatement prp = dbi.prpSql("SELECT DISTINCT i.CMP_NAM, i.TEL_COM, i.EMA, i.ORG_DPM, COALESCE(i.THU_NAI,i.VIS_CRD) AS BLB " + 
								"FROM PSA_CON_STR_TAB con " + 
								"JOIN PSA_CON_STR_TAB i ON (con.CRE_PRS_GID = i.PSC_GID OR con.SPT_GID = i.PSC_GID ) AND i.CLA_TYP = 'PRS' " + 
								"JOIN PSA_ADR_TAB adr ON i.ADR_GID = adr.PSC_GID " + 
								"WHERE con.PSC_GID IN  (?,?) AND i.CMP_NAM IS NOT NULL");
						prp.setString(1, usr_gid);
						prp.setString(2, org_gid);
						ResultSet rsl = prp.executeQuery();
						return rslToJso(rsl);
					}
				}	
				return "false";
			};
		};
		return bas_req.go();		
	}
	
	
	//-----------------------------------------------------------------------------------
	//------------------------------------ Profile --------------------------------------
	//-----------------------------------------------------------------------------------
	/**
	 * Returns org and prs profile data of customer
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of Profile Data
	 * @category Profile
	 * */
	@POST
	@Path("getPrf")
	public String getPrf(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {				
				if(PscGid.isVld(usr_gid)) {
					String org_gid = getUsrOrgGid(ssn,usr);
					if(PscGid.isVld(org_gid)) {					
						PscDbi dbi = ssn.getDbi();
						PreparedStatement prp = dbi.prpSql("SELECT " + String.join(",",getPrfQueStrRea()) +  
															" FROM PSA_CON_STR_TAB prs  " + 
															"LEFT JOIN PSA_CON_STR_TAB org ON prs.ORG_GID = org.PSC_GID  " + 
															"LEFT JOIN PSA_ADR_TAB adr ON org.ADR_GID = adr.PSC_GID " + 
															"WHERE prs.PSC_GID = ?");
						prp.setString(1, usr_gid);
						ResultSet rsl = prp.executeQuery();						
						return rslToJso(rsl);						
					}
				}
				return "false";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * returns profile field definitions as CSV 
	 * @param req Jersey/Grizzly-Request
	 * */
	@POST
	@Path("getPrfFldDef")
	public String getPrfFldDef(@Context Request req) throws Exception{
		if(!reqAlw(req)) {
			return "";
		}
		String ret = "";
		String[] rea_fld_arr = getPrfQueStrRea();
		String[] wri_fld_arr = getPrfQueStrWri();
		for(String rea_fld:rea_fld_arr) {
			String[] fld_prt_arr = rea_fld.split("\\.");
			if(fld_prt_arr.length != 2) {
				continue;
			}
			boolean wri = false;
			for(String wri_fld:wri_fld_arr) {
				if(wri_fld.equals(rea_fld)) {
					wri = true;
					break;
				}
			}
			if(!"".equals(ret)) {
				ret += "|";
			}
			ret += (wri ? "1" : "0") + "|" + fld_prt_arr[1];
		}		
		return ret;
	}
	
	/**
	 * returns String of SELECT section for field names readable for profile section.
	 * Prefix with prs. for person fields, org. for company fields, and adr. for person's address fields.
	 * Customize this if you need more/different fields.
	 * For titles, change string table with value PRF_TAB_NAM on portal server
	 * */
	public String[] getPrfQueStrRea() {		 
		return new String[] {"prs.CMP_NAM", "org.NAM", "org.FRN_IDN", "adr.STR", "adr.ZIP", "adr.CIT", "prs.TEL_COM", "org.FAX", "org.URL"};
	}
	
	/**
	 * returns String of SELECT section for field names writable for profile section.
	 * Prefix with prs. for person fields, and org. for company fields
	 * Customize this if you need more/different fields
	 * */
	public String[] getPrfQueStrWri() {		 
		return new String[] {"prs.CMP_NAM", "prs.TEL_COM", "org.FAX", "org.URL"};
	}
	
	/**
	 * sets editable profile data of customer
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param jso JSON of Data to save
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true if no error, false otherwise
	 * @category Profile
	 * */
	@POST
	@Path("setPrf")
	public String setPrf(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("jso") String jso,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid)) {
					return "false";
				}
				PscDto con_dto = getWsvDto(ssn,"PSA_CON");
				PscDto org_dto = null;
				con_dto.setQue("PSC_GID", usr_gid);
				if(!con_dto.fetFst()) {
					return "false";
				}
				ObjectMapper jso_mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String, String> map = jso_mapper.readValue(jso, Map.class);
				if(map == null || !isStr(map.get("CMP_NAM"))){
					return "false";
				}
				String[] wri_fld_arr = getPrfQueStrWri();
				for(String fld:wri_fld_arr) {					
					String[] fld_prt_arr = fld.split("\\.");
					if(fld_prt_arr.length != 2) {
						continue;
					}
					String val = map.get(fld_prt_arr[1]);
					if(val == null) {
						continue;
					}
					if("prs".equals(fld_prt_arr[0])) {
						con_dto.setDat(fld_prt_arr[1], 1, val);
					}else if("org".equals(fld_prt_arr[0])) {
						if(org_dto == null) {
							String org_gid = con_dto.getDat("ORG_GID", 1);
							if(PscGid.isVld(org_gid)) {
								org_dto = getWsvDto(ssn,"PSA_ORG");
								org_dto.setQue("PSC_GID", org_gid);
								if(!org_dto.fetFst()) {
									org_dto = null;
								}
							}
						}
						if(org_dto != null) {
							org_dto.setDat(fld_prt_arr[1], 1, val);
						}
					}
					con_dto.putDat();
					if(org_dto != null) {
						org_dto.putDat();
					}
					
				}
				return "true";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * returns newsletter data of mailing lists user is subscribed to
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with newsletter data
	 * @category Profile
	 * */
	@POST
	@Path("getNwsLet")
	public String getNwsLet(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid)) {
					return "";
				}
				PscDbi dbi = ssn.getDbi();
				PreparedStatement prp = dbi.prpSql("SELECT nws.PSC_GID AS NWS_GID, nws.NAM, CASE WHEN rel.CHD_GID IS NOT NULL THEN 'y' ELSE 'n' END AS SUB " + 
						"FROM PSA_CON_STR_TAB nws " + 
						"JOIN PRT_NWS_LET_TAB prt_nws ON prt_nws.GRP_DIS_GID = nws.PSC_GID " +
						"LEFT JOIN PSA_REL_TAB rel ON rel.FAT_DTO='PSA_CON' AND rel.CHD_DTO='PSA_CON' AND rel.CTX = 'GRP' AND rel.FAT_GID = nws.PSC_GID AND rel.CHD_GID = ? " + 
						"WHERE nws.TYP_GRP = 'y' AND nws.CLA_TYP='PRS' AND nws.EXT = 'y' AND nws.PSC_GID NOT LIKE 'OUT%' AND prt_nws.WEB_PUB = 'y' " + 
						"ORDER BY nws.NAM ASC ");
				prp.setString(1, usr_gid);
				ResultSet rsl = prp.executeQuery();
				return rslToJso(rsl);												
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * subscribes or unsubscribes user to newsletter
	 * subscription needs active DSGVO permission
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param gid Newsletter gid
	 * @param sub 'y' to subscribe, otherwise to unsibscribe
	 * @param req Jersey/Grizzly-Request
	 * @return "true" on success, "nodsgvo" on missing dsgvo, "false" otherwise
	 * @category Profile
	 * */
	@POST
	@Path("setNwsLet")
	public String setNwsLet(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("gid") String gid,@FormParam("sub") String sub,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid) || !PscGid.isVld(gid)) {
					return "false";
				}
				//--- check if exists ---
				PscDto nws_dto = getWsvDto(ssn,"PRT_NWS_LET");
				nws_dto.setQue("GRP_DIS_GID", gid);
				nws_dto.setQue("WEB_PUB", "'y'");
				if(!nws_dto.fetFst()) {
					return "false";
				}
				PscDto ref_dto = ssn.newDto("PSA_GRP_CON_REF",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				ref_dto.setQue("FAT_GID", gid);
				ref_dto.setQue("CHD_GID", usr_gid);
				boolean exs = ref_dto.fetFst();
				boolean new_exs = "y".equals(sub);				
				if(exs == new_exs) { //no change, goodbye
					return "false";
				}
				if(new_exs) { //check DSGVO
					PscDto dsg_dto = ssn.newDto("PSA_DPR_PRP_USE",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
					dsg_dto.setQue("CON_GID", usr_gid);
					dsg_dto.setQue("IDN", "'PSA_DPR_PRP_USE_SND_NTL'");
					dsg_dto.setQue("ACT", "'y'");
					if(!dsg_dto.fetFst()) {
						return "nodsgvo";
					}
				}
				
				if(!new_exs) {
					ref_dto.delDat(1);
				}
				else {
					ref_dto.insRow(1);
					ref_dto.setDat("FAT_GID", 1, gid);
					ref_dto.setDat("CHD_GID", 1, usr_gid);
					String cmt = ssn.getEnv("PRT_STR_WEB_NWS_LET_DSC");
					if(cmt == null) {
						cmt = "Webportalanmeldung";
					}
					ref_dto.setDat("CMT", 1, cmt);
					ref_dto.putDat();
				}
				return "true";													
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * subscribes user for newsletter DSGVO 
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param ip client IP Adress
	 * @param lgl_txt exact text the user agreed to
	 * @param req Jersey/Grizzly-Request
	 * @return true on success, false otherwise
	 * @category Profile
	 * */
	@POST
	@Path("subNwsDsg")
	public String subNwsDsg(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("lgl_txt") String lgl_txt,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid)) {
					return "false";
				}
				//--- check if exists ---
				PscDto dsg_dto = getWsvDto(ssn,"PSA_DPR_PRP_USE");
				dsg_dto.setQue("CON_GID", usr_gid);
				dsg_dto.setQue("IDN", "'PSA_DPR_PRP_USE_SND_NTL'");	
				if(!dsg_dto.fetFst()) {
					dsg_dto.insRow(1);
					dsg_dto.setDat("IDN", 1, "PSA_DPR_PRP_USE_SND_NTL");
					dsg_dto.setDat("CON_GID", 1, usr_gid);
				}
				dsg_dto.setDat("ACT", 1, "y");
				dsg_dto.setDat("EXT_KEY", 1, ip);
				dsg_dto.setDat("LGL_BAS_IDN", 1, "PSA_DPR_LGL_BAS_CNS");
				dsg_dto.setDat("LGL_BAS_NAM_GER", 1, "Einwilligung");
				dsg_dto.setDat("LGL_BAS_NAM_ENG", 1, "Consent");
				dsg_dto.setDat("SRC_IDN", 1, "PSA_DPR_SRC_CUS_POR");
				dsg_dto.setDat("SRC_NAM_GER", 1, "Kundenportal");
				dsg_dto.setDat("SRC_NAM_ENG", 1, "Customer portal");
				dsg_dto.setDat("NAM_GER", 1, "Newsletterversand");
				dsg_dto.setDat("NAM_ENG", 1, "Newsletter");
				dsg_dto.setDat("DSC_GER", 1, lgl_txt);
				dsg_dto.setDat("DSC_ENG", 1, lgl_txt);				
				dsg_dto.putDat();
				return "true";													
			};
		};
		return bas_req.go();
	}
	
	/**
	 * returns active DSGVO-entries of user
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of active DSGVO-entries
	 * @category Profile
	 * */
	@POST
	@Path("getDsg")
	public String getDsg(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {				
				if(!PscGid.isVld(usr_gid)) {
					return "false";
				}
				PscDto dsg_dto = getWsvDto(ssn,"PSA_DPR_PRP_USE");
				dsg_dto.setQue("CON_GID", usr_gid);
				dsg_dto.setQue("ACT", "'y'");
				dsg_dto.addSrt("NAM", true);
				return dtoToJso(dsg_dto, "PSC_GID","NAM","LGL_BAS_NAM","DSC");												
			};
		};
		return bas_req.go();
	}
			
	/**
	 * revokes given DSGVO entry; if entry was for newsletter, also removes all newsletter subscriptions 
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param gid gid of DSVGO-Entry
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Profile
	 * */
	@POST
	@Path("rvkDsg")
	public String rvkDsg(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("gid") String gid,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid) || !PscGid.isVld(gid)) {
					return "false";
				}
				PscDto dsg_dto = ssn.newDto("PSA_DPR_PRP_USE",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				dsg_dto.setQue("CON_GID", usr_gid);
				dsg_dto.setQue("PSC_GID", gid);
				if(!dsg_dto.fetFst()) {
					return "false";
				}
				dsg_dto.setDat("ACT", 1, "n");
				dsg_dto.putDat();
				//remove subscriptions for newsletters if necessary
				if("PSA_DPR_PRP_USE_SND_NTL".equals(dsg_dto.getDat("IDN", 1))) {
					PscDto nws_let_dto = getWsvDto(ssn,"PRT_NWS_LET");
					PscDto ref_dto = getWsvDto(ssn,"PSA_GRP_CON_REF");									
					try {
						while(nws_let_dto.fetNxt()) {
							String nws_let_gid = nws_let_dto.getDat("GRP_DIS_GID", 1);
							ref_dto.setQue("CHD_GID", usr_gid);
							ref_dto.setQue("FAT_GID", nws_let_gid);
							if(ref_dto.fetFst()) {
								ref_dto.delDat(1);
							}
						}
					}finally {
						nws_let_dto.fetCls();
					}
				}				
				return "true";													
			};
		};
		return bas_req.go();
	}
	
	/**
	 * change Password
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param old_pwd previous password , coming from user input
	 * @param new_pwd new password 
	 * @param req Jersey/Grizzly-Request
	 * @return message to user
	 * @category Profile
	 * */
	@POST
	@Path("setPwd")
	public String setPwd(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("old") String old_pwd,@FormParam("new") String new_pwd,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRF) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscSsn adm_ssn = null;
				try {
					adm_ssn = getAdmSsn();
					PscDto usr_dto = getWsvDto(adm_ssn,"PSA_USR");
					usr_dto.setQue("NAM", "'" + usr + "'");
					if(!usr_dto.fetFst()) {
						return "";
					}				
					//check pwd
					String pwd_hsh_blo = IpcUti.hashPwd(old_pwd, true);
					String psw_sav = usr_dto.getDat("PWD", 1);
					if(!psw_sav.equals(pwd_hsh_blo)) {
						return ssn.getEnv("PRT_STR_PWD_EQU");
					}			
					String pwd_hsh_new = IpcUti.hashPwd(new_pwd, true);
					usr_dto.setDat("PWD", 1, pwd_hsh_new);
					usr_dto.putDat();
					return "OK";
				}finally {
					dspSsn(adm_ssn);					
				}
			};
		};
		return bas_req.go();		
	}
	
	//-----------------------------------------------------------------------------------
	//--------------------------------- Service Calls -----------------------------------
	//-----------------------------------------------------------------------------------
	
	
	/**
	 * Returns service calls of customer
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of service calls related to user's ORG
	 * @category Service Calls
	 * */
	@POST
	@Path("getSvc")
	public String getSvc(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SVC) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				HashSet<String> org_set = getUsrReaOrgSet(ssn, usr);
				if(org_set != null && org_set.size() > 0) {
					//fetch by SQL-Query for performance reasons
					PscDbi dbi = ssn.getDbi();					
					String svc_obj_sub_que = "''";					
					String fil_upl_sub_que = "''";
					//--- handling of group concat fields ---
					if(dbs == 1) { //Oracle
						svc_obj_sub_que = "(SELECT LISTAGG(svc_art.NUM,', ') WITHIN GROUP (ORDER BY svc_art.NUM) FROM PSA_REL_TAB svc_obj_rel LEFT JOIN PSA_SVC_ART_BAS_TAB svc_art ON svc_art.PSC_GID = svc_obj_rel.CHD_GID WHERE svc_obj_rel.FAT_DTO='PSA_PRO' AND svc_obj_rel.CHD_DTO='PSA_SVC_ART' AND svc_obj_rel.FAT_GID = pro.PSC_GID)";
						fil_upl_sub_que = "(SELECT LISTAGG(svc_doc.fil_nam || '|Z|' || fil_blb,'|Y|') WITHIN GROUP (ORDER BY svc_doc.FIL_NAM) FROM PSA_REL_TAB svc_obj_rel LEFT JOIN PSA_DOC_TAB svc_doc ON svc_doc.PSC_GID = svc_obj_rel.CHD_GID WHERE (svc_doc.WEB_PUB = 'y' OR svc_doc.PSC_OWN = '"+ssn.getUic()+"') AND svc_obj_rel.FAT_DTO='PSA_PRO' AND svc_obj_rel.CHD_DTO='PSA_DOC' AND svc_obj_rel.FAT_GID = pro.PSC_GID)";
					}
					if(dbs == 2) { //MSSQL
						svc_obj_sub_que = "STUFF((SELECT ', ' + NUM FROM PSA_REL_TAB svc_obj_rel LEFT JOIN PSA_SVC_ART_BAS_TAB svc_art ON svc_art.PSC_GID = svc_obj_rel.CHD_GID WHERE svc_obj_rel.FAT_DTO='PSA_PRO' AND svc_obj_rel.CHD_DTO='PSA_SVC_ART' AND svc_obj_rel.FAT_GID = pro.PSC_GID ORDER BY svc_art.NUM FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 1, '')";
						fil_upl_sub_que = "STUFF((SELECT '|Y|' + CONCAT(svc_doc.FIL_NAM,'|Z|',FIL_BLB) FROM PSA_REL_TAB svc_obj_rel LEFT JOIN PSA_DOC_TAB svc_doc ON svc_doc.PSC_GID = svc_obj_rel.CHD_GID WHERE (svc_doc.WEB_PUB = 'y' OR svc_doc.PSC_OWN = '"+ssn.getUic()+"') AND svc_obj_rel.FAT_DTO='PSA_PRO' AND svc_obj_rel.CHD_DTO='PSA_DOC' AND svc_obj_rel.FAT_GID = pro.PSC_GID ORDER BY svc_doc.FIL_NAM FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 1, '')";
					}
					//---
					String que = "SELECT pro.PSC_GID,pro.SRC_DAT, pro.NUM, svc.CST_REF_NUM, pro.NAM, svc.CTT, com.CMP_NAM, com.EMA , opr.NAM_GER, opr.NAM_ENG,pro.SUC_PRB, pro.PRI_TXT_GER, pro.PRI_TXT_ENG, " + 
							" "+ svc_obj_sub_que +" AS SVC_OBJ, " + 
							" "+ fil_upl_sub_que +" AS UPL_FIL " +
							" FROM PSA_PRO_STR_TAB pro " + 
							" JOIN PSA_SVC_TAB svc ON pro.PSC_GID = svc.PSC_GID " + 
							" JOIN PSA_OPR_TAB opr ON pro.OPR_IDN = opr.IDN " + 
							" JOIN PSA_CON_STR_TAB com ON com.PSC_GID = pro.COM_PRS_GID " + 
							" JOIN PSA_CON_STR_TAB usr ON usr.PSC_GID = pro.CON_GID " + 
							" WHERE pro.SAP_CLA_DTO='PSA_SVC' AND usr.PSC_GID IN ( " + getSqlPrmRpt(org_set.size()) +
							") ORDER BY pro.SRC_DAT DESC";
					
					PreparedStatement prp = dbi.prpSql(que);
					int n = 1;
					for(String org_gid:org_set) {
						prp.setString(n++, org_gid);
					}
					ResultSet rsl = prp.executeQuery();
					String jso = rslToJso(rsl);
					return jso;										
				}			
				return "";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * Returns service objects of customer
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of Service Objects related to user's ORG
	 * @category Service Calls
	 * */
	@POST
	@Path("getSvcObj")
	public String getSvcObj(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SVC) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				String org_gid = getUsrReaOrgQue(ssn,usr);
				if(PscGid.isVld(org_gid)) {
					PscDto svc_art_dto = getWsvDto(ssn,"PSA_SVC_ART");
					svc_art_dto.setQue("CST_CON_GID", org_gid);
					svc_art_dto.addSrt("NAM", true);
					return dtoToJso(svc_art_dto, "NAM", "PSC_GID");
				}
				return "";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * Inserts new service call
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param nam Name/Title of new Service call
	 * @param rnr customer reference number
	 * @param dsc Description of new Service call
	 * @param svc_obj gid of service object, ignored if not a valid gid
	 * @param req Jersey/Grizzly-Request
	 * @return gid of newly inserted ticket, "tmt" if too many tickets were created in the last time period, "false" (as String) otherwise
	 * @category Service Calls
	 * */
	@POST
	@Path("insSvc")
	public String insSvc(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("ip") String ip,@FormParam("nam") String nam, @FormParam("rnr") String rnr, @FormParam("dsc") String dsc,@FormParam("opr") String opr,@FormParam("svcObj") String svc_obj,@Context Request req) throws Exception{		
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SVC) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(org_gid != null) {
					//spam protection
					PscDto svc_dto = getWsvDto(ssn,"PSA_SVC");
					svc_dto.setQue("PSC_OWN", "'" + ssn.getUic() + "'");				
					svc_dto.setQue("PSC_CRE", ">" + getPsaTimSpmPrt(ssn));
					if(svc_dto.cntDat() >= getEnvAsInt(ssn, "PRT_MAX_REQ_PER_TIM_CNT")) {
						return "tmt";//too many tickets
					}
					//user name
					String src = "";
					PscDto prs_dto = getWsvDto(ssn, "PSA_CON_STR");
					prs_dto.setQue("PSC_GID", usr_gid);
					if(prs_dto.fetFst()) {
						src = prs_dto.getDat("CMP_NAM", 1);
					}
					
					svc_dto.delQue();
					svc_dto.insRow(1);
					svc_dto.setDat("NAM", 1, nam);
					svc_dto.setDat("CST_REF_NUM", 1, rnr);	
					svc_dto.setDat("CTT", 1, dsc);
					svc_dto.setDat("CON_GID", 1, org_gid);
					svc_dto.setDat("SAP_CLA_DTO", 1, "PSA_SVC");
					svc_dto.setDat("OPR_IDN", 1, opr);			
					svc_dto.setDat("PRF_COM_TYP_IDN", 1, "PSA_SVC_ORI_TYP_WEB");
					svc_dto.setDat("PRF_COM_TYP_NAM_GER", 1, "Web");
					svc_dto.setDat("PRF_COM_TYP_NAM_ENG", 1, "Web");
					svc_dto.setDat("SRC", 1, src);
					svc_dto.putDat();
					String svc_gid = svc_dto.getDat("PSC_GID", 1);
					if(PscGid.isVld(svc_obj)) {
						PscDto ref_dto = getWsvDto(ssn,"PSA_PRO_SVC_ART");
						ref_dto.insRow(1);
						ref_dto.setDat("FAT_GID", 1, svc_gid);
						ref_dto.setDat("CHD_GID", 1, svc_obj);						
						ref_dto.putDat();
					}
					//add user with role and recipient flag
					PscDto rel_dto = getWsvDto(ssn, "PSA_PRO_CON"); 
					rel_dto.insRow(1);
					rel_dto.setDat("FAT_GID", 1, svc_dto.getDat("PSC_GID", 1));
					rel_dto.setDat("CHD_GID", 1, usr_gid);
					rel_dto.setDat("RUL_IDN", 1, "SVC_CST_PRS");
					rel_dto.setDat("RCP", 1, "y");
					rel_dto.putDat();
					
					return svc_gid;
				}	
				return "false";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * Gets Pinboard of project of given gid.
	 * Removes Html-header and body tags
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param svc_gid GID of service call
	 * @param req Jersey/Grizzly-Request
	 * @return HTML of service call sans HTML-Header and Body tag
	 * @category Service Calls
	 * */
	@POST
	@Path("getPin")
	public String getPin(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("ip") String ip, @FormParam("gid") String svc_gid,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SVC) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscSsn adm_ssn = null;
				try {
					adm_ssn = getAdmSsn();
					String org_gid = getUsrReaOrgQue(ssn,usr);
					if(PscGid.isVld(org_gid) && PscGid.isVld(svc_gid)) {
						PscDto dto = getWsvDto(adm_ssn,"PSA_PRO_STR");
						dto.setQue("CON_GID", org_gid);
						dto.setQue("PSC_GID", svc_gid);
						if(dto.fetFst()) {
							String htm = dto.getDat("PIN_BRD", 1);
							if(htm.contains("<body>")) {
								htm = htm.substring(htm.indexOf("<body>") + 6);
							}
							if(htm.contains("</body>")) {
								htm = htm.substring(0,htm.indexOf("</body>"));
							}						
							setPinLstSen(ssn,ssn.getUic(),svc_gid);//set "last seen" timestamp for future notifications
							return htm;
						}
					}
				}finally {
					dspSsn(adm_ssn);					
				}
				return "";
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * Insert Pinboard entry
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param svc_gid gid of service call to add pinboard entry to
	 * @param val string to insert. HTML-/XML-Tags are removed to prevent cross-site-scripting 
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Service Calls
	 * */
	@POST
	@Path("insPin")
	public String insPin(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("ip") String ip,@FormParam("gid") String svc_gid, @FormParam("val") String val,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SVC) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscSsn adm_ssn = null;	
				try {
					adm_ssn = getAdmSsn();
					String org_gid = getUsrReaOrgQue(ssn,usr);
					if(PscGid.isVld(org_gid) && PscGid.isVld(svc_gid) && isStr(val) && val.length() < 1025) {					
						PscDto pro_dto = getWsvDto(adm_ssn,"PSA_PRO_STR");
						pro_dto.setQue("PSC_GID", svc_gid);
						pro_dto.setQue("CON_GID", org_gid);
						if(pro_dto.fetFst()) {
							PsaPinBrd pin_brd = PsaObjFac.get(ssn).newPsaPinBrd(ssn);						
							String val_esc = Jsoup.parse(val).text();//prevent XSS
							pin_brd.pstMsg(pro_dto, 1, "PIN_BRD", val_esc);
							setPinLstSen(ssn,ssn.getUic(),svc_gid);//set "last seen" timestamp for future notifications
							return "true";						
						}
					}					
				}finally {
					dspSsn(adm_ssn);
				}
				return "false";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * sets "last seen" timestamp for pinboard for future notifications
	 * @param ssn PiSA-Cubes Ssn
	 * @category Service Calls
	 * */
	protected void setPinLstSen(PscSsn ssn, int uic, String gid){
		try {
			PscDto lst_sen_dto = getWsvDto(ssn,"PRT_PIN_LST_SEN");
			lst_sen_dto.setQue("UIC", "'" + uic + "'");
			lst_sen_dto.setQue("OBJ_GID", "'" + gid + "'");
			if(!lst_sen_dto.fetFst()) {
				lst_sen_dto.insRow(1);
				lst_sen_dto.setDat("UIC", 1, Integer.toString(uic));
				lst_sen_dto.setDat("OBJ_GID", 1, gid);			
			}
			lst_sen_dto.setDat("LST_SEN", 1, PscUti.getTim());
			lst_sen_dto.putDat();
		}catch (Exception e) {
			hdlExc(e); 
		}
	}
	
	/**
	 * Get List of Service Calls with new Messages
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of new message project data; stringified false in case of error
	 * @category Service Calls
	 * */
	@POST
	@Path("getMsg")
	public String getMsg(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SVC) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto lst_sen_dto = getWsvDto(ssn,"PRT_PIN_LST_SEN_XRO");
				lst_sen_dto.setQue("UIC", "'" + ssn.getUic() + "'");
				lst_sen_dto.addSrt("PRT_PRO_STR_PIN_BRD.PIN_BRD_MOD_DAT", false);
				return dtoToJso(lst_sen_dto, "PRT_PRO_STR_PIN_BRD.PSC_GID=>PSC_GID","PRT_PRO_STR_PIN_BRD.NUM=>NUM");													
			};
		};
		return bas_req.go();		
	}
	
	
	//-----------------------------------------------------------------------------------
	//------------------------------------ Projects -------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns project data.
	 * possible types are numbered for security reasons, to limit user to certain sap-types
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param typ_map binary string signifying if predefined object type should be included (see typ_arr for definitions)
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of project informations
	 * @category Projects
	 * */
	@POST
	@Path("getPro")
	public String getPro(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("typ_map") String typ_map,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRO) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				String[] typ_arr = {"PSA_PRO_QUO","PSA_PRO_ORD","PSA_SVC_QUO","PSA_SVC_ORD"};
				String org_gid = getUsrReaOrgQue(ssn,usr);
				if(!PscGid.isVld(org_gid)) {
					return "false";
				}
				String sap_que = "";
				for(int i = 0; i < typ_map.length() && i < typ_arr.length; ++i) {
					if(typ_map.substring(i, i+1).equals("1")) {
						if(!sap_que.equals("")) {
							sap_que += " | ";
						}
						sap_que += "'"+typ_arr[i]+"%'";							
					}
				}
				PscDto pro_dto = getWsvDto(ssn,"PSA_PRO_XTD");
				pro_dto.addSrt("BEG_DAT", false);
				pro_dto.setQue("CON_GID", org_gid);
				pro_dto.setQue("SAP_IDN", sap_que);
				return dtoToJso(pro_dto, "PSC_GID","NAM","NUM","BEG_DAT","PRO_PRI","CUR_IDN","PSA_OPR_XTD.NAM=>OPR_NAM","PSA_OPR_XTD.SUC_PRB=>SUC_PRB","OPR_IDN");													
			};
		};
		return bas_req.go();	
	}
	
	/**
	 * inserts task to create new quotation for customer
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param nam title of request
	 * @param dsc description of request
	 * @return gid of created task, "false" (as string) otherwise
	 * @category Projects
	 * */
	@POST
	@Path("insPro")
	public String insPro(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("nam") String nam,@FormParam("dsc") String dsc,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRO) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(PscGid.isVld(org_gid)) {					
					PscDto tsk_dto = getWsvDto(ssn,"PSA_TSK");
					tsk_dto.setQue("PSC_OWN", "'" + ssn.getUic() + "'");
					tsk_dto.setQue("PSC_CRE", ">" + getPsaTimSpmPrt(ssn));
					if(tsk_dto.cntDat() >= getEnvAsInt(ssn, "PRT_MAX_REQ_PER_TIM_CNT")) {
						return "tmt";//too many requests
					}
					
					tsk_dto.insRow(1);
					tsk_dto.setDat("NAM_GER", 1, "Customerportalanfrage: " + nam);
					tsk_dto.setDat("NAM_ENG", 1, "Customer Portal Request: " + nam);
					tsk_dto.setDat("CTT", 1, dsc);					
					tsk_dto.setDat("CON_GID", 1, org_gid);
					//set account manager of org as agn_prs
					PscDto org_dto = getWsvDto(ssn,"PSA_ORG");
					org_dto.setQue("PSC_GID", org_gid);
					if(org_dto.fetFst()) {
						String cre_prs_gid = org_dto.getDat("CRE_PRS_GID", 1);
						if(PscGid.isVld(cre_prs_gid)) {
							tsk_dto.setDat("AGN_PRS_GID", 1, cre_prs_gid);
						}
					}
					tsk_dto.putDat();
					return tsk_dto.getDat("PSC_GID", 1);					
				}	
				return "false";
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * returns project position data, if project belongs to user's ORG 
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param pro_gid project GID
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of position information
	 * @category Projects
	 * */
	@POST
	@Path("getPos")
	public String getPos(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("pro_gid") String pro_gid,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.PRO) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				HashSet<String> org_set = getUsrReaOrgSet(ssn, usr);
				if(org_set != null && org_set.size() > 0 && PscGid.isVld(pro_gid)) {
					//--- check if project really belongs to user's company ---
					PscDto pro_dto = getWsvDto(ssn,"PSA_PRO_STR");
					pro_dto.setQue("PSC_GID", pro_gid);
					if(pro_dto.fetFst()) {
						String con_gid = pro_dto.getDat("CON_GID", 1);
						if(org_set.contains(con_gid)){						
							PscDto art_ref_dto = getWsvDto(ssn,"PSA_PRO_ART_REF");
							art_ref_dto.setQue("PRO_GID", pro_gid);
							art_ref_dto.addSrt(art_ref_dto.getFld("POS"), true);
							try {
								return dtoToJso(art_ref_dto, "POS","CNT","PSA_ART_XRO.NAM=>ART_NAM","PSA_ART_XRO.NUM=>ART_NUM");
							}finally {
								art_ref_dto.fetCls();
							}
						}
					}								
				}
				return "";
			};
		};
		return bas_req.go();			
	}
	
	//-----------------------------------------------------------------------------------
	//--------------------------------------- FAQ ---------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns FAQ search result data, case independent
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param srh search term. Wildcards are appended on start and end if no wildcard is given
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of found FAQ-Data
	 * @category FAQ
	 * */
	@POST
	@Path("getFaq")
	public String getFaq(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("srh") String srh,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.FAQ) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				String srh_esc = srh.toLowerCase();
				if(!srh_esc.contains("%")) {
					srh_esc = "%" + srh_esc + "%";
				}
				PscDbi dbi = ssn.getDbi();
				PreparedStatement prp = dbi.prpSql("SELECT faq.NUM, faq.NAM,faq.QUE,faq.ANS " + 
													"FROM PSA_FAQ_TAB faq " + 
														"WHERE faq.RLS_WEB = 'y' AND faq.IVD = 'n' AND QUE IS NOT NULL AND ANS IS NOT NULL " + 
													"AND ( " + 
													"LOWER(NUM) LIKE ? OR LOWER(NAM) LIKE ? OR LOWER(QUE) LIKE ? OR LOWER(ANS) LIKE ? " + 
													") " + 
													"ORDER BY NUM ASC");
				for(int i = 1; i <= 4;++i) {
					prp.setString(i, srh_esc);
				}
				ResultSet rsl = prp.executeQuery();
				return rslToJso(rsl);													
			};
		};
		return bas_req.go();
	}	
	
	
	//-----------------------------------------------------------------------------------
	//-----------------------------------  events ---------------------------------------
	//-----------------------------------------------------------------------------------	
	
	/**
	 * returns Event data 
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param req Jersey/Grizzly-Request
	 * @return JSON of Event data of all events that end in the future
	 * @category Events
	 * */
	@POST
	@Path("getEvt")
	public String getEvt(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.EVT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDbi dbi = ssn.getDbi();
				String sql_cur_dat_fnc = "getdate()";
				String sql_dat_sel = "format(evt.ACT_BEG_DAT,'yyyyMMddHHmmssffff') AS ACT_BEG_DAT, format(evt.ACT_END_DAT,'yyyyMMddHHmmssffff') AS ACT_END_DAT";
				String sql_doc_dat = "DOC_DAT = STUFF((SELECT '|Y|' + doc.FIL_BLB + '|X|' + doc.NAM_GER + '|X|' + doc.NAM_ENG FROM PSA_DOC_TAB doc JOIN PSA_REL_TAB rel ON rel.FAT_DTO='PSA_PRO' AND rel.CHD_DTO='PSA_DOC' AND rel.CHD_GID = doc.PSC_GID WHERE doc.PSC_GID NOT LIKE 'OUT%' AND doc.WEB_PUB = 'y' AND rel.FAT_GID = evt.PSC_GID FOR XML PATH ('')) , 1, 1, '')";
				if(dbs == 1) { //oracle
					sql_cur_dat_fnc = "CURRENT_DATE";
					sql_dat_sel = "to_char(evt.ACT_BEG_DAT,'YYYYmmddHHMISS') AS ACT_BEG_DAT, to_char(evt.ACT_END_DAT,'YYYYmmddHHMISS') AS ACT_END_DAT";
					sql_doc_dat = "(SELECT LISTAGG(doc.FIL_BLB || '|X|' || doc.NAM_GER || '|X|' || doc.NAM_ENG,'|Y|') WITHIN GROUP (ORDER BY doc.NAM_GER) FROM PSA_REL_TAB evt_doc_del LEFT JOIN PSA_DOC_TAB doc ON doc.PSC_GID = evt_doc_del.CHD_GID WHERE evt_doc_del.FAT_DTO='PSA_PRO' AND evt_doc_del.CHD_DTO='PSA_DOC' AND doc.PSC_GID NOT LIKE 'OUT%' AND doc.WEB_PUB = 'y' AND evt_doc_del.FAT_GID = evt.PSC_GID) AS DOC_DAT";
				}
				
				String que = "SELECT evt.PSC_GID AS EVT_GID, "+sql_dat_sel+" , evt.NAM, evt.LOC, evt.DSC_ENG, "
						+ "evt.DSC_GER, evt.SAL_PRI, evt.URL, COALESCE(fre_plc.FRE_PLC,evt.VAL_MAX,-1) AS FRE_PLC,  " + 
						"	CASE WHEN rel.CHD_GID IS NULL THEN 'n' ELSE 'y' END AS SUB ," + sql_doc_dat +  
						" FROM PSA_PRO_STR_TAB evt " +
						" LEFT JOIN ( " + 
							" SELECT evt.PSC_GID, evt.VAL_MAX - COUNT(rel.CHD_GID) AS FRE_PLC FROM PSA_PRO_STR_TAB evt " + 
							" LEFT JOIN PSA_REL_TAB rel ON rel.FAT_DTO='PSA_PRO' AND rel.CHD_DTO='PSA_CON' AND rel.FAT_GID = evt.PSC_GID " + 
							" LEFT JOIN PSA_CON_STR_TAB con ON rel.CHD_GID = con.PSC_GID " + 
							" WHERE evt.SAP_IDN='PSA_PRO_EVT' AND evt.PSC_GID NOT LIKE 'OUT%' AND con.EXT = 'y' " + 
							" GROUP BY evt.PSC_GID, evt.VAL_MAX" +
						") fre_plc ON fre_plc.PSC_GID = evt.PSC_GID " + 
						"LEFT JOIN PSA_REL_TAB rel ON rel.FAT_DTO='PSA_PRO' AND rel.CHD_DTO='PSA_CON' AND rel.CTX IS NULL AND rel.FAT_GID = evt.PSC_GID " + 
						"	AND rel.CHD_GID = ? " + 
						"WHERE evt.SAP_SUB='PSA_PRO_MKT' AND evt.SAP_IDN='PSA_PRO_EVT' AND evt.PSC_GID NOT LIKE 'OUT%' AND evt.ACT_BEG_DAT >  " + sql_cur_dat_fnc + 
						" ORDER BY evt.ACT_BEG_DAT ASC"; 
				PreparedStatement prp = dbi.prpSql(que);
				prp.setString(1, usr_gid);
				ResultSet rsl = prp.executeQuery();
				return rslToJso(rsl);													
			};
		};
		return bas_req.go();
	}
	
	/**
	 * subscribes or unsubscribes user from event
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param gid event gid
	 * @param sub 'y' to subscribe, otherwise to unsibscribe
	 * @param req Jersey/Grizzly-Request
	 * @return number of free slots as string on success, "nms" if no free slots, "false" on error, "true" on infinite free slots
	 * @category Profile
	 * */
	@POST
	@Path("setEvt")
	public String setEvt(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("gid") String gid,@FormParam("sub") String sub,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.EVT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!PscGid.isVld(usr_gid) || !PscGid.isVld(gid)) {
					return "false";
				}
				//--- check if exists ---
				PscDto evt_dto = getWsvDto(ssn,"PSA_EVT");
				evt_dto.setQue("PSC_GID", gid);
				if(!evt_dto.fetFst()) {
					return "false";
				}
				//--- check for free slots ---
				int fre_slt = -1;
				String val_max = evt_dto.getDat("VAL_MAX", 1);
				if(isStr(val_max)) {// check if free slots exist
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql("SELECT ? - COUNT(1) FROM PSA_REL_TAB rel " + 
							"JOIN PSA_CON_STR_TAB con ON rel.CHD_GID = con.PSC_GID " + 
							"WHERE rel.FAT_DTO='PSA_PRO' AND rel.CHD_DTO='PSA_CON' AND con.EXT = 'y' AND rel.FAT_GID = ?");
					prp.setInt(1, Integer.parseInt(val_max));
					prp.setString(2, gid);
					ResultSet rsl = prp.executeQuery();
					if(!rsl.next()) {
						return "false";
					}
					fre_slt = rsl.getInt(1);
					prp.close();	
					if("y".equals(sub) && fre_slt <= 0) {
						return "nms";
					}
				}
				
				PscDto ref_dto = ssn.newDto("PSA_PRO_CON_CLI_REF",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				ref_dto.setQue("FAT_GID", gid);
				ref_dto.setQue("CHD_GID", usr_gid);
				boolean exs = ref_dto.fetFst();
				boolean new_exs = "y".equals(sub);				
				if(exs == new_exs) { //no change, goodbye
					return "false";
				}
				if(!new_exs) {
					ref_dto.delDat(1);
				}
				else {
					ref_dto.insRow(1);
					ref_dto.setDat("FAT_GID", 1, gid);
					ref_dto.setDat("CHD_GID", 1, usr_gid);
					ref_dto.setDat("CMT", 1, "Webportalanmeldung");
					ref_dto.putDat();
				}
				if(fre_slt == -1) {
					return "true";
				}
				if("y".equals(sub)) {
					return Integer.toString(fre_slt - 1);
				}
				return Integer.toString(fre_slt + 1);													
			};
		};
		return bas_req.go();		
	}
	
	//-----------------------------------------------------------------------------------
	//-------------------------------- Media Library ------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * Returns JSON with public documents of media library
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with public document metadata of media library
	 * @category Media Library
	 * */
	@POST
	@Path("getMdiThk")
	public String getMdiThk(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.MDI) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {							
				PscDto mdi_thk_dto = ssn.newDto("PRT_MDI_THK_DOC_XRO",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				mdi_thk_dto.setQue("MDI_THK_PUB", "y");
				mdi_thk_dto.setQue("LNG", "'*' | '%" + ssn.lngUsr() + "%'");
				mdi_thk_dto.clrSrt();
				mdi_thk_dto.addSrt("PRT_MDI_THK_CAT.SRT", true);
				mdi_thk_dto.addSrt("HTM_TAG_TYP", true);				
				return dtoToJso(mdi_thk_dto, "HTM_TAG_TYP","PSA_DOC_XRO.NAM=>NAM","PSA_DOC_XRO.NUM=>NUM","PSA_DOC_XRO.CMT=>CMT","PRT_MDI_THK_CAT.NAM=>CAT_NAM","PSA_DOC_XRO.FIL_BLB=>FIL_BLB","PSA_DOC_XRO.FIL_NAM=>FIL_NAM","PSA_DOC_XRO.THU_NAI=>THU_NAI");													
			};
		};
		return bas_req.go();
	}
	
	//-----------------------------------------------------------------------------------
	//---------------------------- sales partner menu -----------------------------------
	//-----------------------------------------------------------------------------------
	/**
	 * Returns JSON with Country IDNs and Names
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with Country IDNs and Names
	 * @category Salespartner
	 * */
	@POST
	@Path("getCtyDef")
	public String getCtyDef(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto cty_dto = ssn.newDto("PSA_CTY",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				cty_dto.addSrt("NAM", true);
				return dtoToJso(cty_dto, "IDN", "NAM"); 														
			};
		};
		return bas_req.go();
	}
	
	/**
	 * Returns JSON with OPR IDNs and Names
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param sap_idn SAP-IDN to search for
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with OPR_IDNs and Names
	 * @category Salespartner
	 * */
	@POST
	@Path("getOpr")
	public String getOpr(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("sapIdn") String sap_idn,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto opr_dto = getWsvDto(ssn,"PSA_OPR");
				opr_dto.setQue("SAP_IDN", "'" + sap_idn + "'");
				opr_dto.addSrt("NAM", true);
				return dtoToJso(opr_dto, "IDN", "NAM"); 													
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * Returns JSON with OPR IDNs and SAP-Names
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param cla_dto CLA_DTO to search for
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with first OPR_IDN and SAP_NAM
	 * @category Salespartner
	 * */
	@POST
	@Path("getSap")
	public String getSap(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("claDto") String cla_dto,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDbi dbi = ssn.getDbi();
				String lan = getPrsLng(ssn, usr_gid);
				String que = "SELECT opr.IDN, sap.NAM_"+lan+" AS NAM FROM PSA_SAP_TAB sap " + 
						"JOIN PSA_OPR_TAB opr ON opr.SAP_IDN = sap.IDN " + 
						"JOIN ( " + 
						"    SELECT SAP_IDN, MIN(POS) AS minpos FROM PSA_OPR_TAB " + 
						"    WHERE ACT = 'y' " + 
						"    GROUP BY SAP_IDN " + 
						") min_opr ON opr.SAP_IDN = min_opr.SAP_IDN AND opr.POS = min_opr.minpos  " + 
						"WHERE sap.ACT = 'y' AND CLA_DTO = ? " +
						"ORDER BY NAM ASC";
				PreparedStatement prp = dbi.prpSql(que);
				prp.setString(1, cla_dto);
				return rslToJso(prp.executeQuery());
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * Returns JSON with external contacts
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param typ "1" for persons, everything else for orgs
	 * @param shr search term
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with public document metadata of media library
	 * @category Salespartner
	 * */
	@POST
	@Path("getConExt")
	public String getConExt(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("typ") String typ,@FormParam("srh") String srh,@Context Request req) throws Exception{		
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				String dto_nam = "1".equals(typ) ? "PSA_PRS_EXT_XTD" : "PSA_ORG_EXT_XRO"; //Standard ORG_XRO has Adress, PRS_XRO has not :(
				PscDto con_dto = ssn.newDto(dto_nam,DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				con_dto.addSrt("CMP_NAM", true);
				con_dto.setQue("CMP_NAM", "%" + srh + "%");
				return dtoToJso(con_dto, "PSC_GID","CMP_NAM","PSA_ADR_XTD.STR=>STR","PSA_ADR_XTD.ZIP=>ZIP","PSA_ADR_XTD.CIT=>CIT","TEL_COM","EMA","PSA_ADR_XTD.CTY_IDN=>CTY_IDN","ORG_GID");													
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * saves changes to external contact
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param typ "1" for persons, everything else for orgs
	 * @param jso JSON of field names to values
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Salespartner
	 * */
	@POST
	@Path("setConExt")
	public String setConExt(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("typ") String typ,@FormParam("jso") String jso,@Context Request req) throws Exception{		
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				ObjectMapper jso_mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String, String> map = jso_mapper.readValue(jso, Map.class);
				if(map == null || !isStr(map.get("CMP_NAM"))){
					return "false";
				}
				String dto_nam = "1".equals(typ) ? "PSA_PRS_EXT_XTD" : "PSA_ORG_EXT_XRO"; //Standard ORG_XRO has Adress, PRS_XRO has not :(
				PscDto con_dto = ssn.newDto(dto_nam,DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				String con_gid = map.get("PSC_GID");
				if(PscGid.isVld(con_gid)) {
					con_dto.setQue("PSC_GID", "'" + con_gid + "'");
					if(!con_dto.fetFst()) {
						return "false";
					}
				}
				else {
					con_dto.insRow(1);
				}
				for (Map.Entry<String, String> ent : map.entrySet()) {
					String key = ent.getKey();
					if(key.startsWith("PSC")) {
						continue;
					}
					if(!isStr(ent.getValue())) {
						continue;
					}
					PscFld fld = con_dto.getFld(key);
					if(fld == null) {
						fld = con_dto.getFld("PSA_ADR_XTD." + key);
						if(fld == null) {
							con_dto.aboDat();
							throw new Exception("missing field " + key);
						}
					}
					con_dto.setDat(fld, 1, ent.getValue());
				}				
				con_dto.putDat();			
				return "true"; 														
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * Returns JSON with business opportunity data of given con_gid
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param con_gid GID of contact
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with business opportunity data
	 * @category Salespartner
	 * */
	@POST
	@Path("getOpp")
	public String getOpp(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("ip") String ip,@FormParam("conGid") String con_gid,@Context Request req) throws Exception{		
		if(!PscGid.isVld(con_gid)) {
			return "false";
		}
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto opp_dto = ssn.newDto("PSA_OPP",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				opp_dto.setQue("CON_GID", con_gid);
				opp_dto.addSrt("NAM", true);				
				return dtoToJso(opp_dto, "PSC_GID","NAM","NUM","DSC","GO_PRB","SUC_PRB","TRN_OVR_BAS","OPR_IDN");													
			};
		};	
		return bas_req.go();
	}	
	
	/**
	 * saves changes to business opportunity
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param con_gid GID of contact to attach opp to
	 * @param jso JSON of field names to values
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Salespartner
	 * */
	@POST
	@Path("setOpp")
	public String setOpp(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("conGid") String con_gid,@FormParam("jso") String jso,@Context Request req) throws Exception{		
		if(!PscGid.isVld(con_gid)) {
			return "false";
		}
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				ObjectMapper jso_mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String, String> map = jso_mapper.readValue(jso, Map.class);
				if(map == null || !isStr(map.get("NAM"))){
					return "false";
				}				
				PscDto opp_dto = ssn.newDto("PSA_OPP",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				String psc_gid = map.get("PSC_GID");
				boolean is_ins = PscGid.isVld(psc_gid);
				if(is_ins) {
					opp_dto.setQue("PSC_GID", "'" + psc_gid + "'");
					if(!opp_dto.fetFst()) {
						return "false";
					}					
				}
				else {
					opp_dto.insRow(1);
				}
				opp_dto.setDat("CON_GID", 1, con_gid);
				for (Map.Entry<String, String> ent : map.entrySet()) {
					if(!isStr(ent.getValue())) {
						continue;
					}
					String key = ent.getKey();
					if(key.startsWith("PSC")) {
						continue;
					}
					String val = ent.getValue();
					if(key.equals("NUM")) {//check if number is valid; create automatically if not
						PscDto pro_dto = getWsvDto(ssn,"PSA_OPP");
						pro_dto.setQue("NUM", "'" + val + "'");
						if(is_ins) {
							pro_dto.setQue("PSC_GID", psc_gid);
						}
						if(!isStr(val) || pro_dto.fetFst()) {
							val = ssn.getSeq("PRO_NUM");
						}
					}
					PscFld fld = opp_dto.getFld(key);
					if(fld == null) {
						opp_dto.aboDat();
						throw new Exception("missing field " + key);
					}
					opp_dto.setDat(fld, 1, val);
				}				
				String cur_spt_gid = opp_dto.getDat("SPT_GID", 1);
				if(!PscGid.isVld(cur_spt_gid)) {
					opp_dto.setDat("SPT_GID", 1, getUsrGid(ssn, usr));
				}
				opp_dto.putDat();			
				return "true"; 															
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * Returns JSON with compound names and gid of internal persons
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with names and gids
	 * @category Salespartner
	 * */
	@POST
	@Path("getPrsInt")
	public String getPrsInt(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{		
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto opp_dto = ssn.newDto("PSA_PRS_INT",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				opp_dto.addSrt("NAM", true);				
				return dtoToJso(opp_dto, "PSC_GID","CMP_NAM"); 												
			};
		};
		return bas_req.go();
	}
	
	/**
	 * Returns JSON with activity data of given con_gid
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param con_gid GID of contact
	 * @param typ "1" for tasks, everything else for appointments
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with activity data
	 * @category Salespartner
	 * */
	@POST
	@Path("getAct")
	public String getAct(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("conGid") String con_gid,@FormParam("typ") String typ,@Context Request req) throws Exception{		
		if(!PscGid.isVld(con_gid)) {
			return "false";
		}
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto act_dto = ssn.newDto("PSA_CON_ACT_CLI_REF",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				act_dto.setQue("FAT_GID", con_gid);
				if("1".equals(typ)) {
					act_dto.setQue("PSA_ACT_XRO.STY_TYP_IDN", "'TASK'");
				}else {
					act_dto.setQue("PSA_ACT_XRO.STY_TYP_IDN", "'APPOINTMENT'");
				}
				act_dto.addSrt("NAM", true);				
				return dtoToJso(act_dto, "CHD_GID=>PSC_GID","PSA_ACT_XRO.NAM=>NAM","PSA_ACT_XRO.END_DAT=>END_DAT","PSA_ACT_XRO.AGN_PRS_GID=>AGN_PRS_GID","PSA_ACT_XRO.CTT=>CTT");													
			};
		};
		return bas_req.go();		
	}	
	
	/**
	 * saves changes to activity
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param con_gid GID of contact to attach opp to; can be null
	 * @param pro_gid GID of project to attach opp to; can be null
	 * @param typ "1" for tasks, everything else for appointments
	 * @param jso JSON of field names to values
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Salespartner
	 * */
	@POST
	@Path("setAct")
	public String setAct(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("conGid") String con_gid,@FormParam("proGid") String pro_gid,@FormParam("typ") String typ,@FormParam("jso") String jso,@Context Request req) throws Exception{		
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				ObjectMapper jso_mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String, String> map = jso_mapper.readValue(jso, Map.class);
				if(map == null || !isStr(map.get("NAM"))){
					return "false";
				}
				//--- insert / edit activity ---
				String agn_prs_gid_to_set = null; //due to awkward handling in standard, AGN_PRS_GID has to be set by system user
				String dto_dsc = "1".equals(typ) ? "PSA_TSK" : "PSA_APM";	
				PscDto act_dto = ssn.newDto(dto_dsc,DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				String psc_gid = map.get("PSC_GID");
				if(PscGid.isVld(psc_gid)) {
					act_dto.setQue("PSC_GID", "'" + psc_gid + "'");
					if(!act_dto.fetFst()) {
						return "false";
					}				
				}
				else {
					act_dto.insRow(1);
				}
				if(PscGid.isVld(con_gid)){
					act_dto.setDat("CON_GID", 1, con_gid);
				}
				if(PscGid.isVld(pro_gid)){
					act_dto.setDat("PRO_GID", 1, pro_gid);
				}
				for (Map.Entry<String, String> ent : map.entrySet()) {
					String val = ent.getValue();
					if(!isStr(val)) {
						continue;
					}
					String key = ent.getKey();
					if(key.startsWith("PSC")) {
						continue;
					}
					if(key.equals("AGN_PRS_GID")) {
						String cur_agn = act_dto.getDat("AGN_PRS_GID", 1);
						if(!PscUti.isStrEqu(cur_agn, val)) {
							agn_prs_gid_to_set = val;
							continue;
						}
					}
					PscFld fld = act_dto.getFld(key);
					if(fld == null) {
						act_dto.aboDat();
						throw new Exception("missing field " + key);
					}
					act_dto.setDat(fld, 1, val);
				}				
				act_dto.putDat();
				String act_gid = act_dto.getDat("PSC_GID", 1);
				//--- due to awkward handling in standard, AGN_PRS_GID has to be set by system user ---
				if(PscGid.isVld(agn_prs_gid_to_set)) {
					PscSsn adm_ssn = null;
					try {
						adm_ssn = getAdmSsn();
						PscDto act_adm_dto = adm_ssn.newDto(dto_dsc,true,true,true,true);
						act_adm_dto.setQue("PSC_GID", act_gid);
						if(act_adm_dto.fetFst()) {
							act_adm_dto.setDat("AGN_PRS_GID", 1, agn_prs_gid_to_set);
							act_adm_dto.putDat();
						}
					}finally {
						if(adm_ssn != null) {
							dspSsn(adm_ssn);
						}	
					}					
				}			
				return "true"; 																	
			};
		};
		return bas_req.go();		
	}
	
	/**
	 * Returns JSON with active questionnaire data
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param req Jersey/Grizzly-Request
	 * @return JSON with activity data
	 * @category Salespartner
	 * */
	@POST
	@Path("getQst")
	public String getQst(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{		
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, PRT_PRM_ENU.SPT) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				PscDto qst_dto = ssn.newDto("PSA_QST",DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],true,DTO_STD_PAR_ARR[3]);
				qst_dto.setQue("OPR_IDN", "'PSA_QST_ACT'");
				qst_dto.addSrt("NAM", true);
				return dtoToJso(qst_dto, "PSC_GID","NAM");
				//TODO: hiermit weiterarbeiten, extras/fragebögen/Link erzeugen
			};
		};
		return bas_req.go();
	}	
	
	//-----------------------------------------------------------------------------------
	//------------------------ blob- and document handling ------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * Returns JSON of document names and their blob names of all documents directly attached to logged in user
	 * and his org(s)
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param req Jersey/Grizzly-Request
	 * @category Documents
	 * */
	@POST
	@Path("getDoc")
	public String getDoc(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {				
				if(!PscGid.isVld(usr_gid)) {
					return "";
				}
				PscDto doc_ref_dto = getWsvDto(ssn,"PSA_CON_DOC_REF");
				String con_que = "'" + usr_gid + "'";
				String org_que = getUsrReaOrgQue(ssn, usr);
				con_que += " | " + org_que;				
				doc_ref_dto.setQue("FAT_GID", con_que);
				doc_ref_dto.setQue("PSA_DOC_XRO.FIL_BLB", "!''");				
				doc_ref_dto.addSrt("PSA_DOC_XRO.FIL_NAM", true);				
				return dtoToJso(doc_ref_dto, "PSA_DOC_XRO.FIL_NAM", new String[] {"PSA_DOC_XRO.FIL_NAM=>FIL_NAM","PSA_DOC_XRO.FIL_BLB=>FIL_BLB"});													
			};
		};
		return bas_req.go();			
	}
	
	
	/**
	 * returns base64-encoded String of given Blob, if user has access.
	 * only Blobs starting with "PSA", "PSC" or "CSO" are allowed, 
	 * as well as pictures and thumbnails of internal persons, 
	 * and documents with web_pub_flag on events, 
	 * and documents in media library PRT_MDI_THK_DOC
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt 
	 * @param blb Name of Blob
	 * @param req Jersey/Grizzly-Request
	 * @return base64-encoded String of given Blob, or empty String (needed for Webserver) if blob nonexistent or user is not allowed to see it
	 * @category Blob Handling
	 * */
	@POST
	@Path("getBlb")
	public String getBlb(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("blb") String blb,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				boolean alw = blb.startsWith("PSC") || blb.startsWith("PSA") || blb.startsWith("CSO");
				if(!alw) { //check for thumbnails on internal person
					PscDto prs_int_dto = getWsvDto(ssn,"PSA_PRS_INT");
					prs_int_dto.setQue("VIS_CRD", "'" + blb + "'");
					alw = prs_int_dto.fetFst();	
					if(!alw && blb.length() > 32) { //sometimes it's a thumbnail
						prs_int_dto.delQue();
						prs_int_dto.setQue("THU_NAI", "'" + blb + "'");
						alw = prs_int_dto.fetFst();
					}
				}
				if(!alw) { //check for documents with web_pub-flag on events
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql("SELECT doc.PSC_GID FROM PSA_DOC_TAB doc " + 
							"JOIN PSA_REL_TAB rel ON rel.FAT_DTO='PSA_PRO' AND rel.CHD_DTO='PSA_DOC' AND rel.CHD_GID = doc.PSC_GID " + 
							"WHERE doc.WEB_PUB = 'y' AND doc.FIL_BLB = ?");
					prp.setString(1, blb);
					ResultSet rsl = prp.executeQuery();
					alw = rsl.next();
					prp.close();
				}
				if(!alw) { //check for documents in mediathek
					PscDto mdi_thk_dto = getWsvDto(ssn,"PRT_MDI_THK_DOC_XRO");
					mdi_thk_dto.setQue("MDI_THK_PUB", "y");
					mdi_thk_dto.setQue("PSA_DOC_XRO.FIL_BLB", "'" + blb + "'");
					alw = mdi_thk_dto.fetFst();
					if(!alw) {
						mdi_thk_dto.delQue();
						mdi_thk_dto.setQue("PSA_DOC_XRO.THU_NAI", "'" + blb + "'");
						alw = mdi_thk_dto.fetFst();
					}
				}
				if(alw) {
					byte[] byt_arr = BlbUtl.getBlb(ssn, blb, false);				
					if(byt_arr != null && byt_arr.length > 0) {
						byte[] bas64_byt_arr = Base64.getEncoder().encode(byt_arr);
						return new String(bas64_byt_arr);
					}
				}	
				return "";
			};
		};
		return bas_req.go();		
	}
	
	
	/**
	 * returns restricted blob as base64-encoded String of given Blob
	 * only Blobs attached to user or his org are allowed. This method is similar to getBlb, but no 
	 * generic handling exists to prevent mistakes resulting in public caching of private blobs,
	 * calling this should result in a direct stream without public cache
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param blb Name of Blob
	 * @param req Jersey/Grizzly-Request
	 * @return base64-encoded String of given Blob, or empty String (needed for Webserver) if blob nonexistent or user is not allowed to see it
	 * @category Blob Handling
	 * */
	@POST
	@Path("getRtrBlb")
	public String getRtrBlb(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("blb") String blb,@Context Request req) throws Exception{
		if(!isStr(blb)) {
			return "";
		}
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				if(!isBlbAlw(ssn, blb)) {
					return "";
				}
				byte[] byt_arr = BlbUtl.getBlb(ssn, blb, false);				
				if(byt_arr != null && byt_arr.length > 0) {
					byte[] bas64_byt_arr = Base64.getEncoder().encode(byt_arr);
					return new String(bas64_byt_arr);
				}	
				return "";
			};
		};
		return bas_req.go();
	}
	
	/**
	 * uploading of blobs. Saves given blob, creates document, and creates relation to user, org and project
	 * @param usr username
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param fil_nam original filename
	 * @param svc_gid gid of project, usually service call
	 * @param dat64 base64-encoded String of file data
	 * @param req Jersey/Grizzly-Request
	 * @category Blob Handling
	 * @return stringified true on success, false otherwise
	 * */
	@POST
	@Path("uplBlb")
	public String uplBlb(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@FormParam("fil_nam") String fil_nam,@FormParam("svc_gid") String svc_gid,@FormParam("dat") String dat64,@Context Request req) throws Exception{
		if(!isStr(dat64)) {
			return "false";
		}
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				//create blob
				String blb_gid = ssn.getDbi().creGid();				
				BlbUtl.setBlb(ssn, blb_gid, Base64.getDecoder().decode(dat64), "PRT_FILE", false);
				//create document
				PscDto doc_dto = getWsvDto(ssn,"PSA_DOC");
				doc_dto.insRow(1);
				doc_dto.setDat("NAM_GER", 1, fil_nam);
				doc_dto.setDat("NAM_ENG", 1, fil_nam);
				doc_dto.setDat("CMT", 1, "Fileupload customerportal");
				doc_dto.setDat("FIL_NAM", 1, fil_nam);
				doc_dto.setDat("FIL_BLB", 1, blb_gid);								
				String fil_nam_upc = fil_nam.toUpperCase(); 
				for(int i = 0; i < KNW_FIL_TYP_ARR.length; ++i) {
					if(fil_nam_upc.endsWith("." + KNW_FIL_TYP_ARR[i])) {
						doc_dto.setDat("APP_IDN", 1, KNW_FIL_APP_ARR[i]);
						doc_dto.setDat("APP_PIC", 1, KNW_FIL_TYP_ICO_ARR[i]);
						break;
					}
				}				
				doc_dto.putDat();
				String doc_gid = doc_dto.getDat("PSC_GID", 1);
				//relate to person
				PscDto doc_con_rel_dto = getWsvDto(ssn,"PSA_CON_DOC");
				String org_gid = getUsrOrgGid(ssn, usr);
				if(PscGid.isVld(usr_gid)) {
					doc_con_rel_dto.insRow(1);
					doc_con_rel_dto.setDat("FAT_GID", 1, usr_gid);
					doc_con_rel_dto.setDat("CHD_GID", 1, doc_gid);
					doc_con_rel_dto.putDat();
					//relate to org
					if(PscGid.isVld(org_gid) && !isStrEqu(usr_gid, org_gid)){
						doc_con_rel_dto.insRow(1);
						doc_con_rel_dto.setDat("FAT_GID", 1, org_gid);
						doc_con_rel_dto.setDat("CHD_GID", 1, doc_gid);
						doc_con_rel_dto.putDat();
					}					
				}	
				//relate to service call
				if(PscGid.isVld(svc_gid)) {
					//check if service call actually belongs to user / org
					PscDto svc_dto = getWsvDto(ssn,"PSA_SVC_TRB");
					svc_dto.setQue("PSC_GID", svc_gid);
					if(svc_dto.fetFst()) {
						String con_gid = svc_dto.getDat("CON_GID", 1);
						if(isStrEqu(con_gid, usr_gid) || isStrEqu(con_gid, org_gid)) {
							PscDto svc_rel_dto = getWsvDto(ssn,"PSA_PRO_DOC");
							svc_rel_dto.insRow(1);
							svc_rel_dto.setDat("FAT_GID", 1, svc_gid);
							svc_rel_dto.setDat("CHD_GID", 1, doc_gid);
							svc_rel_dto.putDat();
						}
					}
				}
				return "true";
			};
		};
		return bas_req.go();		
	}
	
	//-----------------------------------------------------------------------------------
	//------------------------------- Utility methods -----------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns new dto with standard parameter values from static array DTO_STD_PAR_ARR
	 * @param ssn PiSA-Cubes Session
	 * @param dto_dsc Name of DTO
	 * @category Utility Method
	 * @throws Exception 
	 * */	
	protected PscDto getWsvDto(PscSsn ssn, String dto_dsc) throws Exception {
		return ssn.newDto(dto_dsc,DTO_STD_PAR_ARR[0],DTO_STD_PAR_ARR[1],DTO_STD_PAR_ARR[2],DTO_STD_PAR_ARR[3]);		
	}
	
	
	/**
	 * returns environment variable as integer
	 * @param ssn PiSA-Cubes Session
	 * @param nam name of environment variable
	 * @category Utility Method
	 * @return integer representation of env
	 * @throws NumberFormatException, Exception
	 * */
	protected int getEnvAsInt(PscSsn ssn, String nam) throws Exception{
		return Integer.parseInt(ssn.getEnv(nam));
	}
	
	/**
	 * returns time in PiSA-Cubes format that's to be used for Spam-Protection
	 * @param ssn PiSA-Cubes Session
	 * @return PiSA timestamp
	 * @category Utility Method
	 * @throws Exception 
	 * */
	protected String getPsaTimSpmPrt(PscSsn ssn) throws Exception {
		long sta_tim_stp = System.currentTimeMillis() - getEnvAsInt(ssn, "PRT_MAX_REQ_PER_TIM_TIM") * 1000L;
		Date min_dat = new Date(sta_tim_stp);
		return getTim(min_dat);
	}
	
	/**
	 * returns ?-Parameter-String for SQL-Query;
	 * example: cnt=5 => ?,?,?,?,?
	 * */
	protected String getSqlPrmRpt(int cnt) {
		String ret = "";
		for(int i = 0; i < cnt; ++i) {
			if(i > 0) {
				ret += ",";
			}
			ret += "?";
		}
		return ret;
	}
	
	/**
	 * creates JSON-String from SQL-Resultset as row-array with objects
	 * @param rsl SQL-Resultset
	 * @return JSON-String
	 * @category Utility Method
	 * @throws SQLException 
	 */
	public String rslToJso(ResultSet rsl) throws Exception {
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		ResultSetMetaData rsl_mtd = rsl.getMetaData();
		int columnCount = rsl_mtd.getColumnCount();
		try {
			while (rsl.next()) {
			      Map<String, Object> row = new HashMap<String, Object>();
			      for (int i = 1; i <= columnCount; i++) {		
			           String col_nam = rsl_mtd.getColumnName(i);			           
			           Object colVal = rsl.getObject(i);
			           String colString = rsl.getString(i);			           
			           if(colVal instanceof Date) { //for oracle
			        	   Date d = (Date) colVal;
			        	   colString = Long.toString(d.getTime());
			           }
			           row.put(col_nam, colString);
			      }
			      rows.add(row);
			}
			ObjectMapper obj_map = new ObjectMapper();
			return obj_map.writeValueAsString(rows);
		}finally {
			rsl.close();
		}
	}
	
	/**
	 * creates JSON-String from DTO's given fields by looping through fetNxt
	 * does fetCls at the end, so don't expect dto's fetch to be reusable  
	 * @param dto the DTO, preferably with setQue's already done. Can not be null.
	 * @param fld_arr array of field names for output; use <field name>=><display name> Syntax to rename output field 
	 * @return JSON-String of DTO's results
	 * @category Utility Method
	 */
	public String dtoToJso(PscDto dto, String... fld_arr) throws Exception {
		return dtoToJso(dto,null,fld_arr);
	}
	
		
	/**
	 * creates JSON-String from DTO's given fields by looping through fetNxt
	 * does fetCls at the end, so don't expect dto's fetch to be reusable  
	 * @param dto the DTO, preferably with setQue's already done. Can not be null.
	 * @param unq_fld if not null, output rows will be unique by given field. Should usually be null
	 * @param fld_arr array of field names for output; use <field name>=><display name> Syntax to rename output field 
	 * @return JSON-String of DTO's results
	 * @category Utility Method
	 */
	public String dtoToJso(PscDto dto, String unq_fld, String[] fld_arr) throws Exception {
		HashSet<String> don_ent = null;
		if(unq_fld != null) {
			don_ent = new HashSet<String>();
		}
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();		
		try {
			while(dto.fetNxt()) {				
				if(unq_fld != null) {
					String unq_val = dto.getDat(unq_fld, 1);
					if(don_ent.contains(unq_val)) {
						continue;
					}
					don_ent.add(unq_val);
				}				
				Map<String, String> row = new HashMap<String, String>();
				for (int i = 0; i < fld_arr.length; ++i) {		
					String fld_nam = fld_arr[i];
					String dsp_nam = fld_arr[i];
					if(fld_nam.contains("=>")) {
						String[] nam_prt_arr = fld_nam.split("=>");
						fld_nam = nam_prt_arr[0];
						dsp_nam = nam_prt_arr[1];
					}					
					String val = dto.getDat(fld_nam, 1);			           
					row.put(dsp_nam, val);
				}
				rows.add(row);
			}
		}	
		finally {
			dto.fetCls();
		}
		ObjectMapper obj_map = new ObjectMapper();
		return obj_map.writeValueAsString(rows);		
	}

	/**
	 * returns if blob is allowed for user of given session.
	 * Blobs are allowed if they're attached to a document that's allowed.
	 * @param ssn PiSA-Cubes session
	 * @param blb_nam name of blob
	 * @category Access Control
	 * @throws Exception 
	 * */
	public boolean isBlbAlw(PscSsn ssn, String blb_nam) throws Exception {
		//to be allowed, blob has to be attached to an allowed document
		PscDto doc_dto = getWsvDto(ssn,"PSA_DOC");
		doc_dto.setQue("FIL_BLB", "'" + blb_nam + "'");
		try {
			while(doc_dto.fetNxt()) {
				String doc_gid = doc_dto.getDat("PSC_GID", 1);
				if(isDocAlw(ssn, doc_gid)) {
					return true;
				}
			}
			return false;
		}finally {
			doc_dto.fetCls();
		}
	}
	
	/**
	 * returns if document is allowed for user of given session.
	 * Customize this for different ruleset.
	 * Normal rules:<ul>
	 * <li>documents that logged in portal-user uploaded are allowed</li>
	 * <li>documents that colleagues of logged in portal user uploaded are allowed</li>
	 * <li>documents that have WEB_PUB-Flag AND are connected to user, user's org, or one of their projects are allowed</li>
	 * <li>rest is forbidden.</li></ul>
	 * @category Access Control
	 * @throws Exception 
	 * */
	public boolean isDocAlw(PscSsn ssn, String doc_gid) throws Exception {
		if(!PscGid.isVld(doc_gid)) {
			return false;
		}
		PscDto doc_dto = getWsvDto(ssn,"PSA_DOC");
		doc_dto.setQue("PSC_GID", doc_gid);
		if(!doc_dto.fetFst()) {
			return false;
		}
		//--- 1. a document is allowed if it was created (uploaded) by logged in user ---
		String own = doc_dto.getDat("PSC_OWN", 1);
		if(own.equals(Integer.toString(ssn.getUic()))){
			return true;
		}
		//--- 1.1 a document is allowed if it was created (uploaded) by a user of the same company as logged in user ---
		if(PscUti.isStr(own) && !"2".equals(own)) {
			PscDto con_dto = getWsvDto(ssn,"PSA_PRS");
			con_dto.setQue("CIC", "'"+own+"'");
			if(con_dto.fetFst()) {
				String upl_org = con_dto.getDat("ORG_GID", 1);
				if(PscGid.isVld(upl_org)) {
					HashSet<String> org_set = getUsrReaOrgSet(ssn, ssn.getUsr());
					for(String org:org_set) {
						if(upl_org.equals(org)) {
							return true;
						}
					}
				}
			}
		}
		
		//--- 2. a document is allowed if WEB_PUB-flag is set AND it's attached to user, org, or a project of one of those ---
		String web_flg = doc_dto.getDat("WEB_PUB", 1);
		if(!"y".equals(web_flg)) {
			return false;
		}
		//--- 2.1. check if doc is attached to org or prs ---
		String usr_gid = getUsrGid(ssn, ssn.getUsr());
		if(!PscGid.isVld(usr_gid)) {
			return false;
		}	
		String con_que = "'" + usr_gid + "'";		
		PscDto con_doc_dto = getWsvDto(ssn,"PSA_CON_DOC");
		con_doc_dto.setQue("CHD_GID", doc_gid);
		con_doc_dto.setQue("FAT_GID", con_que);
		if(con_doc_dto.cntDat() > 0) {
			return true;
		}
		//--- 2.2. otherwise, check if doc is attached to project of org or prs. We do this by SQL because performance over dto in this is abysmal ---
		HashSet<String> org_set = getUsrReaOrgSet(ssn, ssn.getUsr());
		if(org_set != null && org_set.size() > 0) {
			PscDbi dbi = ssn.getDbi();
			PreparedStatement prp = dbi.prpSql("SELECT pro_doc.FAT_GID  " + 
					"FROM PSA_REL_TAB pro_doc " + 
					"LEFT JOIN PSA_REL_TAB pro_con ON pro_con.FAT_DTO='PSA_PRO' AND pro_con.CHD_DTO='PSA_CON' AND pro_con.FAT_GID = pro_doc.FAT_GID " + 
					"LEFT JOIN PSA_PRO_STR_TAB pro ON pro.PSC_GID = pro_doc.FAT_GID " + 
					"LEFT JOIN PSA_PRO_STR_TAB con ON con.PSC_GID = pro.CON_GID " + 
					"WHERE pro_doc.FAT_DTO='PSA_PRO' AND pro_doc.CHD_DTO='PSA_DOC' AND pro_doc.CHD_GID = ? " + 
					"AND (? IN (pro_con.CHD_GID,con.PSC_GID) "
					+    "OR pro_con.CHD_GID IN ("+getSqlPrmRpt(org_set.size())+") "
					+    "OR con.PSC_GID IN ("+getSqlPrmRpt(org_set.size())+") )");
			prp.setString(1, doc_gid);
			prp.setString(2, usr_gid);
			int n = 3;
			for(String org_gid:org_set) {
				prp.setString(n, org_gid);
				prp.setString(n + org_set.size(), org_gid);
				++n;
			}			
			try {
				return prp.executeQuery().next();
			}finally {
				prp.close();
			}
		}
		return false;
	}
	
	/**
	 * returns if user has permission
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @param prm permission (enum)
	 * @throws Exception 
	 * @category permissions
	 * */	
	protected boolean hasPrm(PscSsn ssn, String usr, PRT_PRM_ENU prm) throws Exception {
		return hasPrm(ssn,usr,PRT_PRM_ENU.getInt(prm));
	}
	
	/**
	 * returns if user has numbered permission
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @param prm_num permission number
	 * @throws Exception 
	 * @category permissions
	 * */	
	protected boolean hasPrm(PscSsn ssn, String usr, int prm_num) throws Exception {
		boolean[] prm_arr = getUsrPrm(ssn, usr);
		if(prm_arr == null || prm_num < 0 || prm_num >= prm_arr.length ) {
			return false;
		}
		return prm_arr[prm_num];
	}
	
	/**
	 * returns boolean array of permissions for user
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @return boolean array, null in case of invalid user
	 * @throws Exception 
	 * @category permissions
	 * */
	protected boolean[] getUsrPrm(PscSsn ssn, String usr) throws Exception {
		PscDto usr_dto = getWsvDto(ssn,"PSA_USR");
		usr_dto.setQue("NAM", "'" + usr + "'");
		if(!usr_dto.fetFst()) {
			return null;
		}
		String idc = usr_dto.getDat("IDC", 1);
		if(!isStr(idc) || idc.equals("2")) {
			return null;
		}
		PscDto prm_dto = getWsvDto(ssn,"PRT_USR_PRM");
		prm_dto.setQue("UIC", "'" + idc + "'");
		if(!prm_dto.fetFst()) {
			prm_dto.insRow(1);
			prm_dto.setDat("UIC", 1, idc);
			prm_dto.putDat();
		}
		boolean[] ret = new boolean[PRT_PRM_ENU.getNumPrm()];
		int prm_num = 0;
		PscFld fld = null;
		while((fld = prm_dto.getFld("PRM_" + prm_num)) != null) {
			ret[prm_num] = "y".equals(prm_dto.getDat(fld,1));
			++prm_num;
		}
		return ret;
	}
	
	/**
	 * updates cockpit permissions from user permissions to prevent empty cockpit elements
	 * @param ssn PiSA-Cubes session
	 * @param uic UIC to check for
	 * @category Utility Method
	 * @throws Exception 
	 * */
	protected void updCcpAlw(PscSsn ssn, int uic) throws Exception {
		PscDto std_dto = getWsvDto(ssn,"PRT_CCP_STD_CNF");		
		std_dto.setQue("PRM_FLD", "!''");
		
		PscDto usr_cnf_dto = ssn.newDto("PRT_CCP_CNF",false, false, true, true); 	
		usr_cnf_dto.setQue("UIC", "'" + uic + "'");
		usr_cnf_dto.setQue("ALW", "'y'");
		
		PscDto usr_prm_dto = getWsvDto(ssn,"PRT_USR_PRM");
		usr_prm_dto.setQue("UIC", "'" + uic + "'");
		if(!usr_prm_dto.fetFst()) {
			return;
		}
		try {			
			while(std_dto.fetNxt()) {
				String ccp_elm_idn = std_dto.getDat("CCP_ELM_IDN", 1);
				String prm_fld_nam = std_dto.getDat("PRM_FLD", 1);
				if(usr_prm_dto.hasFld(prm_fld_nam) && "n".equals(usr_prm_dto.getDat(prm_fld_nam, 1))) {
					usr_cnf_dto.setQue("CCP_ELM_IDN", "'"+ccp_elm_idn+"'");					
					if(usr_cnf_dto.fetFst()) {
						usr_cnf_dto.setDat("ALW", 1, "n");
						usr_cnf_dto.putDat();
					}
				}
			}
		}finally {
			std_dto.fetCls();
		}
	}
	
	/**
	 * returns org gid of user, psc_gid if org gid is OUT%, null if none given
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @return ORG-GID of User's Person, Person's gid if no org is attached
	 * @category Utility Method
	 * @throws Exception 
	 */
	protected String getUsrOrgGid(PscSsn ssn, String usr) throws Exception {
		PscDto usr_dto = getWsvDto(ssn,"PSA_USR");
		usr_dto.setQue("NAM", "'" + usr + "'");
		if(usr_dto.fetFst()) {			
			String idc = usr_dto.getDat("IDC", 1);
			if(isStr(idc) && !idc.equals("2")) {
				PscDto con_dto = getWsvDto(ssn,"PSA_CON_STR");
				con_dto.setQue("CIC", "'" + idc + "'");
				if(con_dto.fetFst()) {
					if("ORG".equals(con_dto.getDat("CLA_TYP", 1))){
						return con_dto.getDat("PSC_GID", 1);
					}
					String org_gid = con_dto.getDat("ORG_GID", 1);
					if(PscGid.isVld(org_gid)) {
						return org_gid;
					}
					return con_dto.getDat("PSC_GID", 1);
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns PiSA-Query for all organisations that user has read access to
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @throws Exception 
	 * */
	protected String getUsrReaOrgQue(PscSsn ssn, String usr) throws Exception{		
		HashSet<String> org_set = getUsrReaOrgSet(ssn, usr);
		StringBuilder ret_bui = new StringBuilder();
		boolean fst = true;
		for(String org:org_set) {
			if(fst) {
				fst = false;
			}else {
				ret_bui.append(" | ");
			}
			ret_bui.append("'" + org + "'");
		}
		return ret_bui.toString();
	}
	
	/**
	 * Returns set of organisation gids that user has read access to
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @throws Exception 
	 * */
	protected HashSet<String> getUsrReaOrgSet(PscSsn ssn, String usr) throws Exception{
		int mod = getUsrMod(ssn, usr);
		return getAllUsrOrgGidSet(ssn, usr, mod);
	}
	
	/**
	 * returns permission level regarding user's organisations; 0 = just user's company; 1 = user's company and subcompanies; 2 = user's company and subcompanies recursively; other values reserved for customizing)
	 * in standard, always returns zero. Use this in customizing to allow more fine grained company permissions.
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * */
	protected int getUsrMod(PscSsn ssn, String usr) throws Exception {
		return 0;
	}
	
	/**
	 * returns set of GIDs of all of user's organisations (either by ORG_GID, or by company structure, depending of mode
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @param mod (0 just for user's company, 1 to include subcompanies as well, 2 to include subcompanies recursively, other values reserved for customizing)  
	 * @throws Exception 
	 * */
	protected HashSet<String> getAllUsrOrgGidSet(PscSsn ssn, String usr, int mod) throws Exception {
		HashSet<String> ret = new HashSet<String>();
		String pri_gid = getUsrOrgGid(ssn, usr); 
		if(PscGid.isVld(pri_gid)) {
			ret.add(pri_gid);
			if(mod == 1 || mod == 2) {
				ret = getSubOrgRec(ssn, pri_gid, mod == 1 ? 0 : 9);
			}			
		}
		return ret;
	}
	
	/**
	 * returs set of given org gid and all sub organisations recursively
	 * @param ssn PiSA-Cubes Session
	 * @param org_gid gid of company to start from
	 * @param rec_dep maximum recursion depth
	 * @throws Exception 
	 * */
	protected HashSet<String> getSubOrgRec(PscSsn ssn, String org_gid, int rec_dep) throws Exception{
		HashSet<String> ret = new HashSet<String>();
		if(PscGid.isVld(org_gid)) {
			ret.add(org_gid);
			PscDto org_dto = getWsvDto(ssn,"PSA_ORG_EXT");
			org_dto.setQue("FAT_GID", org_gid);
			try {
				while(org_dto.fetNxt()) {
					String chd_gid = org_dto.getDat("PSC_GID", 1);
					ret.add(chd_gid);
					if(rec_dep > 0) {
						HashSet<String> gnd_chd_set = getSubOrgRec(ssn, chd_gid, rec_dep - 1);
						for(String gnd_chd:gnd_chd_set) {
							ret.add(gnd_chd);
						}
					}							
				}
			}finally {
				org_dto.fetCls();
			}
		}
		return ret;
	}	
	
	
	/**
	 * returns user person gid (from PSA_CON_STR, not PSC_USR)
	 * null if none found
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @return GID of user's person
	 * @category Utility Method
	 * @throws Exception 
	 * */
	protected String getUsrGid(PscSsn ssn, String usr) throws Exception {
		PscDto usr_dto = getWsvDto(ssn,"PSA_USR");
		usr_dto.setQue("NAM", "'" + usr + "'");
		if(usr_dto.fetFst()) {			
			String idc = usr_dto.getDat("IDC", 1);
			if(isStr(idc) && !idc.equals("2")) {
				PscDto con_dto = getWsvDto(ssn,"PSA_CON_STR");
				con_dto.setQue("CIC", "'" + idc + "'");
				if(con_dto.fetFst()) {					
					String prs_gid = con_dto.getDat("PSC_GID", 1);
					if(PscGid.isVld(prs_gid)) {
						return prs_gid;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * returns language identifier of person with given gid
	 * @param ssn PiSA-Cubes session
	 * @param gid PSA_PRS_GID
	 * @return language identifier, null in case of invalid gid or gid not found
	 * @throws Exception
	 * @category Utility Method 
	 * */
	protected String getPrsLng(PscSsn ssn, String gid) throws Exception {
		if(PscGid.isVld(gid)) {
			PscDto con_dto = getWsvDto(ssn,"PSA_CON_STR");
			con_dto.setQue("PSC_GID", gid);
			if(con_dto.fetFst()) {					
				return con_dto.getDat("COM_LNG_IDN", 1);
			}				
		}
		return null;
	}
	
	//-----------------------------------------------------------------------------------
	//------------------------------- Login handling ------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns true if user exists
	 * @param usr PiSA-Username
	 * @param req Jersey/Grizzly-Request
	 * @return stringified "true" if user exists, "false" otherwise
	 * @category Login
	 * */
	@POST
	@Path("hasUsr")
	public String hasUsr(@FormParam("usr") String usr,@Context Request req) throws Exception{
		AdmRqs adm_req = new AdmRqs(req) {
			public String hdlRqu(PscSsn adm_ssn, String usr_gid) throws Exception {
				PscDto dto = getWsvDto(adm_ssn,"PSA_USR");
				dto.setQue("NAM", "'" + usr + "'");
				return Boolean.toString(dto.fetFst());											
			};
		};
		return adm_req.go();
	}
	
	/**
	 * creates and stores additional salt for given user, returns that salt and standard pisa salt
	 * @param usr PiSA-Username
	 * @param req Jersey/Grizzly-Request
	 * @return standard PiSA Blowfish salt, separator $$, new Blowfish Salt, separator $$, bitmap of permissions
	 * @category Login
	 * */
	@POST
	@Path("getSlt")
	public String getSlt(@FormParam("usr") String usr,@FormParam("ip") String ip,@Context Request req) throws Exception{
		AdmRqs adm_req = new AdmRqs(req) {
			public String hdlRqu(PscSsn adm_ssn, String usr_gid) throws Exception {
				PscDto dto = getWsvDto(adm_ssn,"PSA_USR");
				dto.setQue("NAM", "'" + usr.toUpperCase() + "'");
				if(dto.fetFst()) {
					String uic = dto.getDat("IDC", 1);
					if(isStr(uic) && !"2".equals(uic)) {
						PscDto slt_dto = getWsvDto(adm_ssn,"PRT_SLT");
						slt_dto.setQue("UIC", "'" + uic + "'");
						slt_dto.setQue("IP_ADR", "'" + ip + "'");
						if(!slt_dto.fetFst()) {
							slt_dto.insRow(1);
							slt_dto.setDat("UIC", 1, uic);
							slt_dto.setDat("IP_ADR", 1, ip);
						}
						String slt = BCrypt.gensalt();
						slt_dto.setDat("SLT", 1, slt);
						slt_dto.putDat();						
						
						String ret = STD_SLT + "$$" + slt + "$$";
						boolean[] prm_arr = getUsrPrm(adm_ssn, usr);
						for(boolean prm:prm_arr) {
							ret += prm ? "1" : "0";
						}
						return ret;
					}
				}
				return "false";
			};
		};
		return adm_req.go();
	}
	
	/**
	 * returns if credentials match.
	 * If credentials match, but 2-factor-auth is enabled and user's IP not validated,
	 * creates and sends E-Mail to user
	 * @param usr PiSA-User Name
	 * @param pwd Password, hashed both with standard and additional salt
	 * @param ip user's client IP for 2-factor-auth
	 * @param req Jersey/Grizzly-Request
	 * @return "true" if credentials match database, "noauth" if 2-factor-auth is enabled and IP is not authenticated, "false" otherwise
	 * @category Login
	 * */
	@POST
	@Path("chkCrd")
	public String chkCrd(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("ip") String ip,@Context Request req) throws Exception{
		BasRqs bas_req = new BasRqs(usr, pwd, ip, req, null) {
			public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception {
				//"false" is automatically returned in BasRqs.go() if credentials don't match
				//we still check it here in case some careless customising might change that behaviour
				if(ssn == null) {
					return "false";
				}
				if(!isAut(ssn, ip, ssn.getUic())) {
					try {
						creAutEma(ssn, ip, ssn.getUic(), true);
					}catch (Exception e) {
						hdlExc(e);
					}
					return "noauth";
				}
				return "true"; 														
			};
		};
		return bas_req.go();
	}
	
	/**
	 * sets 2-factor-auth-Address to validated
	 * @param gid GID of PRT_IP_AUT
	 * @param req Jersey/Grizzly-Request
	 * @return stringified true on success, false otherwise
	 * @category Login
	 * */
	@POST
	@Path("autIp")
	public String autIp(@FormParam("gid") String gid,@Context Request req) throws Exception{
		AdmRqs adm_req = new AdmRqs(req) {
			public String hdlRqu(PscSsn adm_ssn, String usr_gid) throws Exception {
				if(PscGid.isVld(gid)) {				
					PscDto aut_dto = adm_ssn.newDto("PRT_IP_AUT",false,false,true,true);
					aut_dto.setQue("PSC_GID", gid);
					if(aut_dto.fetFst()) {
						aut_dto.setDat("AUT", 1, "y");
						aut_dto.putDat();
						return "true";
					}
				}
				return "false";																	
			};
		};
		return adm_req.go();		
	}	

	
	/**
	 * creates session from Session pool for given user, also checks password
	 * Returns null if user doesn't exist or password is wrong. Increments counter if wrong pwd
	 * DON'T FORGET TO PUT SESSION BACK IN POOL AFTER USAGE!
	 * @param usr PiSA-Username
	 * @param pwd Password, hashed both with standard and additional salt
	 * @return Session from session pool (<bold>put it back!</bold>)
	 * @throws Exception 
	 * @category Utility Method
	 * */
	protected PscSsn getSsn(String usr, String pwd, String ip) throws Exception {
		usr = usr.toUpperCase();
		PscSsn adm_ssn = null;
		PscSsn ssn = null;
		try {
			adm_ssn = getAdmSsn();			
				
			PscDto usr_dto = getWsvDto(adm_ssn,"PSA_USR");				
			usr_dto.setQue("NAM", "'" + usr + "'");
			usr_dto.setQue("TRY", "'' | <3");
			if(!usr_dto.fetFst()) {
				return null;
			}
			ssn = PscSsnPool.get(new Key().usr(usr).che(true));
			String uic = usr_dto.getDat("IDC", 1);
			if(!isStr(uic) || "2".equals(uic)) {
				dspSsn(ssn);
				return null;
			}
			
			//check pwd
			String pwd_sav = usr_dto.getDat("PWD", 1);
			if(pwd_sav.startsWith("$")) {
				pwd_sav = pwd_sav.substring(1);
			}
			//additional hash with stored salt			
			PscDto slt_dto = getWsvDto(adm_ssn,"PRT_SLT");
			slt_dto.setQue("UIC", "'" + uic + "'");
			slt_dto.setQue("IP_ADR", "'" + ip + "'");
			if(!slt_dto.fetFst()) {
				dspSsn(ssn);
				return null;
			}
			String add_slt = slt_dto.getDat("SLT", 1);
			if(!isStr(add_slt)) {
				dspSsn(ssn);
				return null;
			}
			//save timestamp for logged in user list
			slt_dto.setDat("LST_ACT_TIM", 1, PscUti.getTim());
			slt_dto.putDat();
			
			String sto_pwd_hsh = BCrypt.hashpw(pwd_sav, add_slt);
			sto_pwd_hsh = sto_pwd_hsh.substring(add_slt.length());
						
			if(!sto_pwd_hsh.equals(pwd)) {
				//update try-counter
			/*	if(!usr.equals("CUSTOMIZER") && !usr.equals("SALESADMIN")) {
					int new_try = 1;
					String try_str = usr_dto.getDat("TRY", 1);
					if(isStr(try_str)) {
						try {
							int try_int = Integer.parseInt(try_str);
							new_try = try_int + 1;
						}catch (NumberFormatException e) {} //can be disregarded; setting 1							
					}
					usr_dto.setDat("TRY", 1, Integer.toString(new_try));
					usr_dto.putDat();
				} TODO: überdenken*/
				dspSsn(ssn);
				return null;
			}				
			ssn.setLng(getPrsLng(ssn, getUsrGid(ssn, usr)));
			return ssn;
		}
		finally {
			dspSsn(adm_ssn);
		}
	}
	
	/**
	 * dispatches session back into the pool, if not null
	 * @category Utility Method
	 * */
	protected void dspSsn(PscSsn ssn) {
		if(ssn != null) {				
			try {
				PscSsnPool.put(ssn);
			} catch (Exception e) {
				hdlExc(e);
			}						
		}	
	}
	
	/**
	 * returns true if user is authenticated or if 2-factor-authentication is disabled.
	 * @param ssn PiSA-Cubes Session
	 * @param ip IP-Adress
	 * @param uic user identifier
	 * @throws Exception 
	 * @category 2-factor-authentification
	 * */
	protected boolean isAut(PscSsn ssn, String ip, int uic) throws Exception {
		if(!"y".equals(ssn.getEnv("PRT_TWO_FAC_AUT"))) {
			return true;
		}
		PscDto aut_dto = ssn.newDto("PRT_IP_AUT",false,false,true,true);
		aut_dto.setQue("UIC", "'" + uic + "'");
		aut_dto.setQue("IP", "'" + ip + "'");
		aut_dto.setQue("AUT", "'y'");
		return aut_dto.fetFst();
	}
	
	/**
	 * creates 2-factor-auth E-Mail for given user
	 * @param ssn PiSA-Cubes Session
	 * @param ip IP-Adress
	 * @param uic user identifier
	 * @param snd send E-Mail immediately?
	 * @return GID of created E-Mail 
	 * @throws Exception 
	 * @category 2-factor-authentification
	 * */
	protected String creAutEma(PscSsn ssn, String ip, int uic, boolean snd) throws Exception {
		PscDto aut_dto = ssn.newDto("PRT_IP_AUT",false,false,true,true);
		aut_dto.setQue("UIC", "'" + uic + "'");
		aut_dto.setQue("IP", "'" + ip + "'");
		if(aut_dto.fetFst()) {
			return null;
		}
		aut_dto.insRow(1);
		aut_dto.setDat("UIC", 1, Integer.toString(uic));
		aut_dto.setDat("IP", 1, ip);
		aut_dto.putDat();
		String aut_gid = aut_dto.getDat("PSC_GID", 1);
		String ema_gid = ssn.getEnv("PRT_TWO_FAC_AUT_EMA_GID");
		if(!PscGid.isVld(ema_gid)) {
			throw new Exception("no valid EMA-GID set in ENV PRT_TWO_FAC_AUT_EMA_GID");
		}
		PscDto ema_dto = ssn.newDto("PSA_CPD_EMA",false,false,true,true);
		ema_dto.setQue("PSC_GID", "'" +ema_gid + "'");		
		if(!ema_dto.fetFst()) {
			throw new Exception("EMA-GID set in ENV PRT_TWO_FAC_AUT_EMA_GID not pointing to valid mail");
		}
		//--- get contents, create link ---
		String ema_nam = ema_dto.getDat("NAM", 1);
		String ema_cnt = ema_dto.getDat("CTT", 1);
		String lnk = ssn.getEnv("PRT_TWO_FAC_AUT_ROU");
		if(lnk == null) {
			throw new Exception("env PRT_TWO_FAC_AUT_ROU not set");
		}
		lnk += aut_gid;
		lnk = "<a href=\""+lnk+"\">" + lnk + "</a>";
		ema_cnt = ema_cnt.replace("{LINK}", lnk);		
		//--- get sender (acc manager of person) ---
		PscDto con_dto = getWsvDto(ssn,"PSA_CON_STR");
		con_dto.setQue("CIC", "'" + uic + "'");
		if(!con_dto.fetFst()) {
			throw new Exception("no user for uic " + uic);
		}
		String snd_gid = con_dto.getDat("CRE_PRS_GID", 1);
		//--- get receiver ---
		String rcp_gid = con_dto.getDat("PSC_GID", 1);
		String foa = con_dto.getDat("CPD_FOA", 1);
		ema_cnt = ema_cnt.replace("{ANREDE}", foa);
		
		//--- create mail ---
		ema_dto.fetCls();
		ema_dto.insRow(1);
		ema_dto.setDat("SND_PRS_GID", 1, snd_gid);
		ema_dto.setDat("NAM", 1, ema_nam);
		ema_dto.setDat("CTT", 1, ema_cnt);					
		ema_dto.putDat();
		//--- set recipient ---
		String new_ema_gid = ema_dto.getDat("PSC_GID", 1);
		PscDto rcp_ref_dto = ssn.newDto("PSA_CON_ACT_CLI_REF",false,false,true,true);
		rcp_ref_dto.insRow(1);
		rcp_ref_dto.setDat("RCP_TYP_IDN", 1, "TO");
		rcp_ref_dto.setDat("CLI", 1, "y");
		rcp_ref_dto.setDat("FAT_GID", 1, rcp_gid);
		rcp_ref_dto.setDat("CHD_GID", 1, new_ema_gid);						
		rcp_ref_dto.putDat();
		
		//--- send if parameter given and prod ---
		if(snd && PscSsn.getSrvIDN().contains("PROD")) {						
	        PsaObjFac cpd_fac = PsaObjFac.get(ema_dto);
	        PsaCpdFnc cpd_fnc = cpd_fac.newPsaCpdFnc(ema_dto);				        
	        cpd_fnc.sndEma(1,false);				        
		}
		return new_ema_gid;
	}
	
	/**
	 * creates admin-session from Session pool 
	 * DON'T FORGET TO PUT SESSION BACK IN POOL AFTER USAGE!
	 * @return Session (System-User) from session pool (<bold>put it back!</bold>)
	 * @category Utility Method
	 * @throws Exception 
	 * */
	protected PscSsn getAdmSsn() throws Exception {
		return PscSsnPool.get(new Key().usr("SYSTEM").che(true));
	}
	
	/**
	 * generic exception handling. 
	 * Put custom logging here
	 * @category Logging
	 * */
	protected void hdlExc(Exception e) {		
		JobLog log = JobThr.getJobLog(IplWsvPrtWsv.class);
        log.logErr("Exception in customer portal", e);     
	}
	
	/**
	 * helper class to simplify general session and request handling, fixes
	 * session pool handling and request validation for you.
	 * override hdlRqu to do things. Run go() to fire
	 * */
	class BasRqs{
		String rqs_usr = null;
		String rqs_pwd = null;
		String rqs_ip = null;
		Request rqs_req;
		PRT_PRM_ENU rqs_prm = null;;
		
		public BasRqs(String usr, String pwd, String ip, Request req,PRT_PRM_ENU prm) {
			this.rqs_usr = usr;
			this.rqs_pwd = pwd;
			this.rqs_req = req;
			this.rqs_prm = prm;
			this.rqs_ip = ip;
		}
		
		public BasRqs(Request req) {
			this.rqs_req = req;
		}
		
		public String go() {
			PscSsn ssn = null;
			try {
				ssn = befRqs();
				if(ssn == null) {
					return "false";
				}
				String usr_gid = getUsrGid(ssn, rqs_usr);
				return hdlRqu(ssn,usr_gid);
			}catch (Exception e) {
				hdlExc(e);
				return "false";			
			}finally {
				dspSsn(ssn);				
			}		
		}
		
		public PscSsn befRqs() throws Exception {
			if(!reqAlw(rqs_req)) {
				return null;
			}			
			PscSsn ssn = getSsn(rqs_usr, rqs_pwd, rqs_ip);
			if(ssn == null) {
				return null;
			}
			if(rqs_prm != null && !hasPrm(ssn, rqs_usr, rqs_prm)) {
				PscSsnPool.put(ssn);
				return null;
			}
			return ssn;
		}
		
		public String hdlRqu(PscSsn ssn, String usr_gid) throws Exception{
			return "";
		}
	}	
	
	/**
	 * helper class to simplify general session and request handling, fixes
	 * session pool handling and request validation for you. Creates a SYSTEM-Session.
	 * override hdlRqu to do things. Run go() to fire.
	 * Warning: the usr_gid will be null on hdlRql
	 * */
	class AdmRqs extends BasRqs{		

		public AdmRqs(Request req) {
			super(req);
		}
		
		@Override
		public PscSsn befRqs() throws Exception {
			if(!reqAlw(rqs_req)) {
				return null;
			}	
			return getAdmSsn();
		}
	}
	
	/**
	 * returns if the given request is allowed; usually by comparing IP with whitelist.
	 * Customize here for other handling
	 * @throws Exception 
	 * */
	protected boolean reqAlw(Request req) throws Exception {
		String ip = req.getRemoteAddr();
		if(IP_WHT_LST == null) { //fill from dto
			IP_WHT_LST = new ArrayList<String>();
			PscSsn adm_ssn = null;
			try {
				adm_ssn = getAdmSsn();
				PscDto wht_lst_dto = getWsvDto(adm_ssn,"PRT_IP_WHT_LST");
				try {
					while(wht_lst_dto.fetNxt()) {
						String dto_ip = wht_lst_dto.getDat("IP", 1);
						if(isStr(dto_ip)) {
							IP_WHT_LST.add(dto_ip);
						}
					}
				}finally {
					wht_lst_dto.fetCls();
				}
			}finally {
				dspSsn(adm_ssn);					
			}
		}		
		if(IP_WHT_LST.size() == 0) { //ignore if unset (for testing and customizing purposes)
			return true;
		}
		for(String wht_ip:IP_WHT_LST) {
			if(wht_ip.equals(ip)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * enumeration of portal permissions
	 * */
	public enum PRT_PRM_ENU{
		CCP,NWS,PRF,SVC,EVT,PRO,MDI,FAQ,SPT;
		
		public static int getInt(PRT_PRM_ENU prm) {
			switch(prm){
				case CCP: return 0;	//Cockpit
				case NWS: return 1; //News
				case PRF: return 2; //Profile
				case SVC: return 3; //Service
				case EVT: return 4; //Events
				case PRO: return 5; //Projects
				case MDI: return 6; //Media Library
				case FAQ: return 7; //FAQ
				case SPT: return 8; //Salespartner
			}
			return -1;
		}
		
		public static int getNumPrm() {
			return PRT_PRM_ENU.values().length;
		}
	}
}
