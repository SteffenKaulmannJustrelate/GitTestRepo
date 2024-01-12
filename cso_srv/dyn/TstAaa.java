// Last update by user PSA_PRE_SAL on host vmdmoref at 20100315115225
import de.pisa.psc.srv.dsi.*;
// Last update by user PSA_PRE_SAL on host vmdmoref at 20100315112804
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.sys.*;
// Last update by user PSA_PRE_SAL on host vmdmoref at 20100315111335

import java.util.*;
import java.io.*;
import org.apache.log4j.*;

/** TST_AAA */
public class TstAaa {

     public void run(PscGui gui) throws Exception {
         PscSsn glb_ssn = PscChe.getGlb();
			PscDbi dbi = glb_ssn.getDbi();
         PscSsn ssn = gui.getSsn();
			ssn.wriTxt((dbi==null) ? "no dbi" : "dbi");
         creNewSsn(glb_ssn,"45", gui);
         ssn.wriTxt("ok");

     }

         public static PscSsn creNewSsn(PscSsn glb_ssn, String idc, PscGui gui) throws Exception {
         PscSsn new_ssn = null;
         if (idc!=null && idc.length()>0) {
             PscDto new_usr_dto = glb_ssn.newDto("PSC_USR");
             new_usr_dto.setQue("IDC", "'"+idc+"'");
             int new_usr_cnt = new_usr_dto.fetDat();
             if (new_usr_cnt==1) {
                 String new_usr_nam = new_usr_dto.getDat("NAM", 1);
                 String new_usr_pwd = new_usr_dto.getDat("PWD", 1);
                 PscUsx new_usx = new PscUsx();
                 new_ssn = new_usx.newSsn();
                 if (new_ssn==null) new_ssn = new PsaSsn();
                 new_ssn.setApp(glb_ssn.getApp());
                 new_ssn.setPor(glb_ssn.getPor());// check 
					  new_ssn.setPrp(glb_ssn.getPrp());  
                 new_ssn.setDdb(glb_ssn.getDdb());
                 new_ssn.setUdb(glb_ssn.getUdb());
                 new_ssn.setPdb(glb_ssn.getPdb());
                 new_ssn.setTsp(glb_ssn.getTsp());
                 new_ssn.setIsp(glb_ssn.getIsp());
                 new_ssn.setBsp(glb_ssn.getBsp());
						PscDbi old_dbi = glb_ssn.getDbi();
					String dbi_dsc = old_dbi.getDsc();
					
                 new_ssn.conDbs();
						PscDbi dbi = new_ssn.getDbi();
			         PscSsn ssn = gui.getSsn();
						ssn.wriTxt((dbi==null) ? "no dbi" : "dbi");
  						ssn.wriTxt(dbi_dsc);
  						ssn.wriTxt(new_ssn.getDdb());
						ssn.wriTxt(new_ssn.getUdb());
						
                 new_ssn.creUsr(new_usr_nam); //, new_usr_pwd);
             }
         }
         return new_ssn;
     }
}
