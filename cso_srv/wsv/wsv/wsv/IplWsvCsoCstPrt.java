// Last Update by user CUSTOMIZER at 20191107090707
package wsv.wsv;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pisa.psa.dto.psa_ifc.PsaPinBrd;
import de.pisa.psa.ifc.PscGid;
import de.pisa.psa.ssn.PsaObjFac;
import de.pisa.psc.ipc.svc.IpcUti;
import de.pisa.psc.srv.dsi.PscDbi;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.glb.PscSsnPool;
import de.pisa.psc.srv.glb.PscSsnPool.Key;
import de.pisa.psc.srv.svc.PscUti;

//TODO: only allow from certain IP


@Path("prt")
public class IplWsvCsoCstPrt {	
	
	/**
	 * returns hashed password if user exists
	 * @param usr PiSA-Username
	 * @return hashed password on success, "false" as String if user doesn't exist (needed for webserver)
	 * @category Login
	 * */
	@POST
	@Path("hasUsr")
	public String hasUsr(@FormParam("usr") String usr) throws Exception{
		PscSsn adm_ssn = null;
		try {
			adm_ssn = getAdmSsn();
			PscDto dto = adm_ssn.newDto("PSA_USR");
			dto.setQue("NAM", "'" + usr + "'");
			if(dto.fetFst()) {
				return dto.getDat("PWD", 1);
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}
		finally {
			if(adm_ssn != null) {
				try {
					PscSsnPool.put(adm_ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}
		return "false"; //for client
	}
	
	/**
	 * returns if credentials match
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @return true if credentials match database, false otherwise
	 * @category Login
	 * */
	@POST
	@Path("chkCrd")
	public boolean chkCrd(@FormParam("usr") String usr,@FormParam("pwd") String pwd) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			return ssn != null;
		}
		finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}
	}
	
	//-----------------------------------------------------------------------------------
	//------------------------------- Cockpit exclusive ---------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns contact information to user and user's org.
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @return contact information as JSON
	 * */
	@POST
	@Path("getCon")
	public String getCon(@FormParam("usr") String usr,@FormParam("pwd") String pwd) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			String usr_gid = getUsrGid(ssn, usr);
			if(PscGid.isVld(usr_gid)) {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(PscGid.isVld(org_gid)) {
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql("SELECT DISTINCT i.CMP_NAM, i.TEL_COM, i.EMA, i.ORG_DPM " + 
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
		}
		finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}
		return "";
	}
	
	
	//-----------------------------------------------------------------------------------
	//------------------------------------ Profile --------------------------------------
	//-----------------------------------------------------------------------------------
	/**
	 * Returns org and prs profile data of customer
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @return JSON of Profile Data
	 * @category Profile
	 * */
	@POST
	@Path("getPrf")
	public String getPrf(@FormParam("usr") String usr,@FormParam("pwd") String pwd) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				String usr_gid = getUsrGid(ssn, usr);
				if(PscGid.isVld(usr_gid)) {
					String org_gid = getUsrOrgGid(ssn,usr);
					if(PscGid.isVld(org_gid)) {
						//fetch by SQL-Query (taken from quick solution for demo): TODO: change to proper dto handling
						PscDbi dbi = ssn.getDbi();
						PreparedStatement prp = dbi.prpSql("SELECT prs.CMP_NAM AS PRS, org.NAM, org.FRN_IDN, adr.STR, adr.ZIP, adr.CIT, org.TEL_COM, org.FAX, org.URL " + 
															"FROM PSA_CON_STR_TAB prs  " + 
															"LEFT JOIN PSA_CON_STR_TAB org ON prs.ORG_GID = org.PSC_GID  " + 
															"LEFT JOIN PSA_ADR_TAB adr ON org.ADR_GID = adr.PSC_GID " + 
															"WHERE prs.PSC_GID = ?");
						prp.setString(1, usr_gid);
						ResultSet rsl = prp.executeQuery();						
						return rslToJso(rsl);						
					}
				}
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}		
		}		
		return "";
	}
	
	/**
	 * sets editable profile data of customer
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param prs new logged in person's compound name
	 * @param tel_com new logged in person's phone number
	 * @param fax new logged in person's fax
	 * @param url new logged in person's url
	 * @return true on success, false otherwise
	 * @category Profile
	 * */
	@POST
	@Path("setPrf")
	public boolean setPrf(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("PRS") String prs,@FormParam("TEL_COM") String tel_com,@FormParam("FAX") String fax,@FormParam("URL") String url) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				String usr_gid = getUsrGid(ssn, usr);
				if(PscGid.isVld(usr_gid)) {
					PscDto con_dto = ssn.newDto("PSA_CON");
					if(PscUti.isStr(prs)) {
						con_dto.setQue("PSC_GID", usr_gid);
						if(con_dto.fetFst()) {
							String prv = con_dto.getDat("CMP_NAM", 1);
							if(!PscUti.isStrEqu(prv, prs)) {
								con_dto.setDat("CMP_NAM", 1, prs);
								con_dto.putDat();
							}
						}
					}
					String org_gid = getUsrOrgGid(ssn,usr);
					if(PscGid.isVld(org_gid)) {
						con_dto.setQue("PSC_GID", org_gid);
						if(con_dto.fetFst()) {
							boolean chg = false;
							String[] key_arr = {"TEL_COM","FAX","URL"};
							String[] val_arr = {tel_com  ,fax  , url};
							for(int i = 0; i < key_arr.length; ++i) {
								String prv = con_dto.getDat(key_arr[i], 1);
								if(!PscUti.isStrEqu(prv, val_arr[i])) {
									chg = true;
									con_dto.setDat(key_arr[i], 1, val_arr[i]);
								}
							}
							if(chg) {
								con_dto.putDat();
							}
						}						
					}
				}
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return false;
	}
	
	
	/**
	 * change Password
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed), coming from server cache
	 * @param old_pwd previous password (unhashed), coming from user input
	 * @param new_pwd new password (unhashed)
	 * @return message to user
	 * @category Profile
	 * */
	@POST
	@Path("setPwd")
	public String setPwd(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("old") String old_pwd,@FormParam("new") String new_pwd) throws Exception{
		PscSsn ssn = null;
		PscSsn adm_ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				adm_ssn = getAdmSsn();
				PscDto usr_dto = adm_ssn.newDto("PSA_USR");
				usr_dto.setQue("NAM", "'" + usr + "'");
				if(!usr_dto.fetFst()) {
					return "";
				}				
				//check pwd
				String pwd_hsh_blo = IpcUti.hashPwd(old_pwd, true);
				String psw_sav = usr_dto.getDat("PWD", 1);
				if(!psw_sav.equals(pwd_hsh_blo)) {
					return "Altes Passwort stimmt nicht überein.";
				}			
				String pwd_hsh_new = IpcUti.hashPwd(new_pwd, true);
				usr_dto.setDat("PWD", 1, pwd_hsh_new);
				usr_dto.putDat();
				return "Passwort geändert.";
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
			if(adm_ssn != null) {
				try {
					PscSsnPool.put(adm_ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";
	}
	
	//-----------------------------------------------------------------------------------
	//--------------------------------- Service Calls -----------------------------------
	//-----------------------------------------------------------------------------------
	
	
	/**
	 * Returns service calls of customer
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @return JSON of Service Objects related to user's ORG
	 * @category Service Calls
	 * */
	@POST
	@Path("getSvc")
	public String getSvc(@FormParam("usr") String usr,@FormParam("pwd") String pwd) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(org_gid != null) {
					//fetch by SQL-Query (taken from quick solution for demo): TODO: change to proper dto handling
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql("SELECT pro.PSC_GID,pro.SRC_DAT, pro.NUM, svc.CST_REF_NUM, pro.NAM, svc.CTT, com.CMP_NAM, com.EMA , opr.NAM_GER, pro.SUC_PRB, " + 
							"                STUFF( " + 
							"                	( " + 
							"                		SELECT ', ' + NUM FROM " + 
							"                		PSA_REL_TAB svc_obj_rel " + 
							"                		LEFT JOIN PSA_SVC_ART_BAS_TAB svc_art ON svc_art.PSC_GID = svc_obj_rel.CHD_GID " + 
							"                		WHERE svc_obj_rel.FAT_DTO='PSA_PRO' AND svc_obj_rel.CHD_DTO='PSA_SVC_ART' AND svc_obj_rel.FAT_GID = pro.PSC_GID " + 
							"                		ORDER BY svc_art.NUM " + 
							"                FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS SVC_OBJ " + 
							"                FROM PSA_PRO_STR_TAB pro " + 
							"                JOIN PSA_SVC_TAB svc ON pro.PSC_GID = svc.PSC_GID " + 
							"                JOIN PSA_OPR_TAB opr ON pro.OPR_IDN = opr.IDN " + 
							"                JOIN PSA_CON_STR_TAB com ON com.PSC_GID = pro.COM_PRS_GID " + 
							"                JOIN PSA_CON_STR_TAB usr ON usr.PSC_GID = pro.CON_GID " + 
							"                WHERE pro.SAP_CLA_DTO='PSA_SVC' AND usr.PSC_GID = ?" +
							 				" ORDER BY pro.SRC_DAT DESC");
					prp.setString(1, org_gid);
					ResultSet rsl = prp.executeQuery();
					String jso = rslToJso(rsl);
					return jso;
				}				
			}	
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";
	}
	
	/**
	 * Inserts new service call
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param nam Name/Title of new Service call
	 * @param dsc Description of new Service call
	 * @return true on success, false otherwise
	 * @category Service Calls
	 * */
	@POST
	@Path("insSvc")
	public boolean insSvc(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("nam") String nam, @FormParam("rnr") String rnr, @FormParam("dsc") String dsc) throws Exception{
		//TODO: Limit number of new service calls per time / Spam protection
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(org_gid != null) {
					PscDto svc_dto = ssn.newDto("PSA_SVC");
					svc_dto.insRow(1);
					svc_dto.setDat("NAM", 1, nam);
					svc_dto.setDat("CST_REF_NUM", 1, rnr);	
					svc_dto.setDat("CTT", 1, dsc);
					svc_dto.setDat("CON_GID", 1, org_gid);
					svc_dto.setDat("SAP_CLA_DTO", 1, "PSA_SVC");
					svc_dto.setDat("OPR_IDN", 1, "PSA_SVC_CAL_ACQ");					
					svc_dto.putDat();
					return true;
				}				
			}	
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return false;
	}
	
	/**
	 * Gets Pinboard of project of given gid.
	 * Removes Html-header and body tags
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param svc_gid GID of service call
	 * @return HTML of service call sans HTML-Header and Body tag
	 * @category Service Calls
	 * */
	@POST
	@Path("getPin")
	public String getPin(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("gid") String svc_gid) throws Exception{
		PscSsn ssn = null;
		PscSsn adm_ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				adm_ssn = getAdmSsn();
				String org_gid = getUsrOrgGid(ssn,usr);
				if(PscGid.isVld(org_gid) && PscGid.isVld(svc_gid)) {
					PscDto dto = adm_ssn.newDto("PSA_PRO_STR");
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
						return htm;
					}
				}
			}
		}catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
			if(adm_ssn != null) {
				try {
					PscSsnPool.put(adm_ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";
	}
	
	/**
	 * Insert Pinboard entry
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param svc_gid gid of service call to add pinboard entry to
	 * @return true on success, false otherwise
	 * @category Service Calls
	 * */
	@POST
	@Path("insPin")
	public boolean insPin(@FormParam("usr") String usr,@FormParam("pwd") String pwd, @FormParam("gid") String svc_gid, @FormParam("val") String val) throws Exception{
		PscSsn ssn = null;
		PscSsn adm_ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				adm_ssn = getAdmSsn();
				String org_gid = getUsrOrgGid(ssn,usr);
				if(PscGid.isVld(org_gid) && PscGid.isVld(svc_gid) && PscUti.isStr(val) && val.length() < 1025) {					
					PscDto pro_dto = adm_ssn.newDto("PSA_PRO_STR");
					pro_dto.setQue("PSC_GID", svc_gid);
					pro_dto.setQue("CON_GID", org_gid);
					if(pro_dto.fetFst()) {
						PsaPinBrd pin_brd = PsaObjFac.get(ssn).newPsaPinBrd(ssn);
						//XSS verhindern
						val = Jsoup.parse(val).text();
						pin_brd.pstMsg(pro_dto, 1, "PIN_BRD", val);
						return true;						
					}
				}
			}		
		}catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
			if(adm_ssn != null) {
				try {
					PscSsnPool.put(adm_ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return false;
	}
	
	//-----------------------------------------------------------------------------------
	//------------------------------------ Projects -------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns project data.
	 * possible types are numbered for security reasons, to limit user to certain sap-types
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param typ_map binary string signifying if predefined object type should be included (see typ_arr for definitions)
	 * @return JSON of project informations
	 * @category Projects
	 * */
	@POST
	@Path("getPro")
	public String getSvc(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("typ_map") String typ_map) throws Exception{
		String[] typ_arr = {"PSA_PRO_QUO","PSA_PRO_ORD","PSA_SVC_QUO","PSA_SVC_ORD"};
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(org_gid != null) {
					ArrayList<String> sap_lst = new ArrayList<String>();
					for(int i = 0; i < typ_map.length() && i < typ_arr.length; ++i) {
						if(typ_map.substring(i, i+1).equals("1")) {
							sap_lst.add(typ_arr[i]);
						}
					}
					//fetch by SQL-Query (taken from quick solution for demo): TODO: change to proper dto handling
					String que = "SELECT pro.PSC_GID, pro.NAM,pro.NUM,pro.BEG_DAT, ROUND(pro.PRO_PRI,2) AS PRO_PRI, pro.CUR_IDN, opr.NAM_GER AS OPR_NAM, opr.SUC_PRB " + 
								" FROM PSA_CON_STR_TAB con " + 
								" JOIN PSA_PRO_STR_TAB pro ON con.PSC_GID = pro.CON_GID OR con.ORG_GID = pro.CON_GID " + 
								" JOIN PSA_OPR_TAB opr ON pro.OPR_IDN = opr.IDN " + 
								" WHERE con.PSC_GID = ? AND (";
					boolean fst = true;
					ArrayList<String> par_lst = new ArrayList<String>();
					par_lst.add(org_gid);
					for(String sap:sap_lst) {
						if(fst) {
							fst = false;
						}else {
							que += " OR ";
						}						
						que +=  " pro.SAP_IDN LIKE ?";
						par_lst.add(sap + "%");
					}
					que += ")";
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql(que);
					for(int i = 0; i < par_lst.size(); ++i) {
						prp.setString(i+1, par_lst.get(i));
					}
					ResultSet rsl = prp.executeQuery();
					return rslToJso(rsl);
				}
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";		
	}
	
	/**
	 * returns project position data, if project belongs to user's ORG 
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param pro_gid project GID
	 * @return JSON of position information
	 * @category Projects
	 * */
	@POST
	@Path("getPos")
	public String getPos(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("pro_gid") String pro_gid) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null && PscGid.isVld(pro_gid)) {
				String org_gid = getUsrOrgGid(ssn,usr);
				if(org_gid != null) {
					//fetch by SQL-Query (taken from quick solution for demo): TODO: change to proper dto handling
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql("SELECT art_str.POS, CONCAT(art_str.CNT,' ', uni.NAM_GER) AS CNT, art.NAM_GER AS ART_NAM, art.NUM AS ART_NUM  " + 
														"FROM PSA_ART_STR_TAB art_str " + 
														"JOIN PSA_PRO_STR_TAB pro ON pro.PSC_GID = art_str.PRO_GID " + 
														"JOIN PSA_CON_STR_TAB con ON pro.CON_GID = con.PSC_GID " + 
														"JOIN PSA_ART_TAB art ON art_str.CHD_GID = art.PSC_GID " + 
														"JOIN PSA_UNI_TAB uni ON uni.IDN = art.UNI_IDN " + 
														"WHERE art_str.FAT_GID = '0' AND pro.PSC_GID = ? AND con.PSC_GID = ? " + 
														"ORDER BY art_str.POS ASC");
					prp.setString(1, pro_gid);
					prp.setString(2, org_gid);
					ResultSet rsl = prp.executeQuery();
					return rslToJso(rsl);
				}
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";				
	}
	
	//-----------------------------------------------------------------------------------
	//--------------------------------------- FAQ ---------------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * returns FAQ search result data, case independent
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param srh search term. Wildcards are appended on start and end if no wildcard is given
	 * @return JSON of found FAQ-Data
	 * @category FAQ
	 * */
	@POST
	@Path("getFaq")
	public String getFaq(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("srh") String srh) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				srh = srh.toLowerCase();
				if(!srh.contains("%")) {
					srh = "%" + srh + "%";
					PscDbi dbi = ssn.getDbi();
					PreparedStatement prp = dbi.prpSql("SELECT faq.NUM, faq.NAM,faq.QUE,faq.ANS " + 
														"FROM PSA_FAQ_TAB faq " + 
															"WHERE faq.RLS_WEB = 'y' AND faq.IVD = 'n' AND QUE IS NOT NULL AND ANS IS NOT NULL " + 
														"AND ( " + 
														"LOWER(NUM) LIKE ? OR LOWER(NAM) LIKE ? OR LOWER(QUE) LIKE ? OR LOWER(ANS) LIKE ? " + 
														") " + 
														"ORDER BY NUM ASC");
					for(int i = 1; i <= 4;++i) {
						prp.setString(i, srh);
					}
					ResultSet rsl = prp.executeQuery();
					return rslToJso(rsl);
				}
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";		
	}
	
	
	
	//-----------------------------------------------------------------------------------
	//-----------------------------------  events ---------------------------------------
	//-----------------------------------------------------------------------------------	
	
	/**
	 * returns Event data 
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @return JSON of Event data of all events that end in the future
	 * @category Events
	 * */
	@POST
	@Path("getEvt")
	public String getEvt(@FormParam("usr") String usr,@FormParam("pwd") String pwd) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				PscDto evt_dto = ssn.newDto("PSA_EVT_XTD");
				evt_dto.setQue("ACT_END_DAT", ">@TODAY");
				evt_dto.srtDat(evt_dto.getFld("ACT_BEG_DAT"), true);
				String jso = dtoToJso(evt_dto, new String[] {"ACT_BEG_DAT","ACT_END_DAT","NAM","LOC","DSC","SAL_PRI","URL"});
				return jso;
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";		
	}
	//-----------------------------------------------------------------------------------
	//-------------------------------  generic search -----------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * generic search
	 * @param usr PiSA-User Name
	 * @param pwd User Password (unhashed)
	 * @param srh search term; wildcards are added at begin and end if no wildcard is included
	 * @return JSON of generic search results
	 * @category Search
	 * */
	@POST
	@Path("getSrh")
	public String getSrh(@FormParam("usr") String usr,@FormParam("pwd") String pwd,@FormParam("srh") String srh) throws Exception{
		PscSsn ssn = null;
		try {
			ssn = getSsn(usr, pwd);
			if(ssn != null) {
				String org_gid = getUsrOrgGid(ssn,usr);
				String usr_gid = getUsrGid(ssn, usr);
				if(org_gid != null && usr_gid != null) {
					PscDbi dbi = ssn.getDbi();
					srh = srh.toLowerCase();
					if(!srh.contains("%")) {
						srh = "%" + srh + "%";
					}
					PreparedStatement prp = dbi.prpSql(" SELECT * FROM ( " + 
														"	SELECT " + 
														"		CASE WHEN con.PSC_GID IS NOT NULL " +
														"			 THEN 'Kontakt' " +
														"			 ELSE " +
														"				CASE WHEN pro.OPR_IDN LIKE '%SVC%' " +
														"					 THEN 'Ticket'" + 
														"					 ELSE 'Vertriebsvorgang' " +
														"				END " +
														"		END AS TYP, " + 
														"		COALESCE(con.NUM,pro.NUM) AS NUM, " + 
														"		COALESCE(con.CMP_NAM,pro.NAM) AS NAM " + 
														"	FROM ( " + 
														"		SELECT CHD_GID, CHD_DTO FROM PSA_REL_TAB WHERE FAT_GID IN (?,?) " + 
														"		UNION " + 
														"		SELECT FAT_GID, FAT_DTO FROM PSA_REL_TAB WHERE CHD_GID IN (?,?) " + 
														"	) a " + 
														"	LEFT JOIN PSA_CON_STR_TAB con ON con.PSC_GID = a.CHD_GID " + 
														"	LEFT JOIN PSA_PRO_STR_TAB pro ON pro.PSC_GID = a.CHD_GID " + 
														"	WHERE con.PSC_GID IS NOT NULL OR pro.PSC_GID IS NOT NULL " + 
														") b WHERE LOWER(NAM) LIKE LOWER(?) OR LOWER(NUM) LIKE LOWER(?)");
					prp.setString(1, usr_gid);
					prp.setString(2, org_gid);
					prp.setString(3, usr_gid);
					prp.setString(4, org_gid);
					prp.setString(5, srh);
					prp.setString(6, srh);
					ResultSet rsl = prp.executeQuery();
					return rslToJso(rsl);
				}
			}
		}
		catch (Exception e) {
			hdlExc(e);
		}finally {
			if(ssn != null) {
				try {
					PscSsnPool.put(ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}		
		return "";		
	}
	
	//-----------------------------------------------------------------------------------
	//------------------------------- Utility methods -----------------------------------
	//-----------------------------------------------------------------------------------
	
	/**
	 * creates JSON-String from SQL-Resultset as row-array with objects
	 * @param rsl SQL-Resultset
	 * @return JSON-String
	 * @category Utility Method
	 * @throws SQLException 
	 */
	private String rslToJso(ResultSet rsl) throws Exception {
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		ResultSetMetaData rsl_mtd = rsl.getMetaData();
		int columnCount = rsl_mtd.getColumnCount();

		while (rsl.next()) {
		      Map<String, Object> row = new HashMap<String, Object>();
		      for (int i = 1; i <= columnCount; i++) {		
		           String col_nam = rsl_mtd.getColumnName(i);
		           Object colVal = rsl.getObject(i);
		           row.put(col_nam, colVal);
		      }
		      rows.add(row);
		}
		ObjectMapper obj_map = new ObjectMapper();
		return obj_map.writeValueAsString(rows);				
	}
	
	/**
	 * creates JSON-String from DTO's given fields by looping through fetNxt
	 * does fetCls at the end, so don't expect dto's fetch to be reusable  
	 * @param dto the DTO, preferably with setQue's already done. May not be null.
	 * @param fld_arr array of field names for output
	 * @return JSON-String of DTO's results
	 */
	private String dtoToJso(PscDto dto, String[] fld_arr) throws Exception {
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();		
		try {
			while(dto.fetNxt()) {
				Map<String, String> row = new HashMap<String, String>();
				for (int i = 0; i < fld_arr.length; ++i) {		
					String val = dto.getDat(fld_arr[i], 1);			           
					row.put(fld_arr[i], val);
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
	 * returns org gid of user, psc_gid if org gid is OUT%, null if none given
	 * @param ssn PiSA-Cubes Session
	 * @param usr PiSA User Name
	 * @return ORG-GID of User's Person, Person's gid if no org is attached
	 * @category Utility Method
	 * @throws Exception 
	 */
	private String getUsrOrgGid(PscSsn ssn, String usr) throws Exception {
		PscDto usr_dto = ssn.newDto("PSA_USR");
		usr_dto.setQue("NAM", "'" + usr + "'");
		if(usr_dto.fetFst()) {			
			String idc = usr_dto.getDat("IDC", 1);
			if(PscUti.isStr(idc) && !idc.equals("2")) {
				PscDto con_dto = ssn.newDto("PSA_CON_STR");
				con_dto.setQue("CIC", "'" + idc + "'");
				if(con_dto.fetFst()) {
					if("ORG".equals(con_dto.getDat("CLA_TYP", 1))){
						return con_dto.getDat("PSC_GID", 1);
					}
					String org_gid = con_dto.getDat("ORG_GID", 1);
					if(PscGid.isVld(org_gid)) {
						return org_gid;
					}
				}
			}
		}
		return null;
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
	private String getUsrGid(PscSsn ssn, String usr) throws Exception {
		PscDto usr_dto = ssn.newDto("PSA_USR");
		usr_dto.setQue("NAM", "'" + usr + "'");
		if(usr_dto.fetFst()) {			
			String idc = usr_dto.getDat("IDC", 1);
			if(PscUti.isStr(idc) && !idc.equals("2")) {
				PscDto con_dto = ssn.newDto("PSA_CON_STR");
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
	 * creates session from Session pool for given user, also checks password
	 * Returns null if user doesn't exist or password is wrong. Increments counter if wrong pwd
	 * DON'T FORGET TO PUT SESSION BACK IN POOL AFTER USAGE!
	 * @param usr PiSA-Username
	 * @param pwd Password, unhashed
	 * @return Session from session pool (<bold>put it back!</bold>)
	 * @category Utility Method
	 * */
	private PscSsn getSsn(String usr, String pwd) {
		PscSsn adm_ssn = null;
		try {
			adm_ssn = getAdmSsn();
			PscSsn ssn = null;
			try {				
				PscDto usr_dto = adm_ssn.newDto("PSA_USR");
				usr_dto.setQue("NAM", "'" + usr + "'");
				if(!usr_dto.fetFst()) {
					return null;
				}
				ssn = PscSsnPool.get(new Key().usr(usr).che(true));
				//check pwd
				String pwd_hsh_blo = IpcUti.hashPwd(pwd, true);
				String psw_sav = usr_dto.getDat("PWD", 1);
				if(!psw_sav.equals(pwd_hsh_blo)) {
					return null;
				}			
				return ssn;
			}catch (Exception e) {
				hdlExc(e);
			}			
		}
		catch (Exception e) {
			hdlExc(e);
		}
		finally {
			if(adm_ssn != null) {
				try {
					PscSsnPool.put(adm_ssn);
				} catch (Exception e) {
					hdlExc(e);
				}		
			}	
		}
		return null;
	}
	
	/**
	 * creates admin-session from Session pool 
	 * DON'T FORGET TO PUT SESSION BACK IN POOL AFTER USAGE!
	 * @return Session (System-User) from session pool (<bold>put it back!</bold>)
	 * @category Utility Method
	 * @throws Exception 
	 * */
	private PscSsn getAdmSsn() throws Exception {
		return PscSsnPool.get(new Key().usr("SYSTEM").che(true));
	}
	
	/**
	 * generic exception handling. 
	 * Put logging here
	 * */
	private static void hdlExc(Exception e) {		
		//TODO: proper logging
		System.out.println(e.toString());
	}
	
}