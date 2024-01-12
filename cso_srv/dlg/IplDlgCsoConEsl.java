// Last Update by user CUSTOMIZER at 20110824094703
import java.util.*;

import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.svc.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_rel.*;
import de.pisa.psa.ifc.*;

/** CSO_CON_ESL */
public class IplDlgCsoConEsl extends de.pisa.psa.frm.psa_con.PsaConApp
{

public IplDlgCsoConEsl(String dsc) throws Exception{ super(dsc); }

public void CSO_FUP_APM(String par, Integer row) throws Exception
{
	int rows[] = getLisRow();
	if (rows==null || rows.length==0 || rows[0]!=row) {
		return;
	}
	
	PscDto dyn_dto = getDynDto();
	PscGui gui = getGui();
	PscSsn ssn = getSsn();

	PscFrm new_frm = null;
	SavPnt sav_pnt = new SavPnt(ssn);
	try {
		new_frm = opnFrmGid(par + " NULL", 0, null, false);
		if (new_frm!=null) {
			PsaDto new_dto = (PsaDto)new_frm.getDynDto();
			PsaDtoIpl.setSemBasDto(new_dto, "NO_INT_NOT_DLG_ACT");

			new_frm.rfrDat();
			new_frm.insRow();

			int new_row = new_frm.getRow();

			String new_gid = new_dto.getDat("PSC_GID", new_row);
			if (new_gid.isEmpty()) {
				PscDbd dbd = new_dto.getDbd();
				new_gid = dbd.getGid();
				PsaDtoIpl.setSysDat(new_dto, "PSC_GID", new_row, new_gid);
			}

			gui.dlgFrm(new_frm);

			if ( new_frm.chkExiFlg() ) {
				sav_pnt.end();
				Set<String> doc_set = new HashSet<String>();
				if (PscGid.isVld(new_gid)) {
					PscDto act_doc_dto = ssn.newDto("PSA_ACT_DOC");
					act_doc_dto.setQue("FAT_GID", new_gid);
					try {
						while (act_doc_dto.fetNxt()) {
							doc_set.add(act_doc_dto.getDat("CHD_GID", 1));
						}
					}
					finally {
						act_doc_dto.fetCls();
					}
				}

				//Relationship to contact
				PscDto con_act_cli_dto = ssn.newDto("PSA_CON_ACT_CLI_AGG");
				for (int row_idx=0; row_idx<rows.length; row_idx++) {
					String con_gid = dyn_dto.getDat("PSC_GID", rows[row_idx]);
					if (row_idx>0) {
						new_dto.insRow(1);
						new_dto.copRow(1, new_dto, 2);
						
						// copy series definition
						String ser_gid = new_dto.getDat("SER_GID", 1);
						if (PscUti.isStr(ser_gid)) {
							PscDto ser_dto = PsaUti.newDto(ssn, "PSA_ACT_SER", true, true, true, true);
							ser_dto.setQue("PSC_GID", ser_gid);
							if (ser_dto.fetDat()!=0) {
								ser_dto.insRow(2);
								ser_dto.copRow(2, ser_dto, 1);
								ser_dto.putDat();
								ser_gid = ser_dto.getDat("PSC_GID", 2);
							}
							else {
								ser_gid = "";
							}
							new_dto.setDat("SER_GID", 1, ser_gid);
						}
					}
					new_dto.setDat("CON_GID", 1, con_gid);
					new_dto.set_act_sem();
					try {
						new_dto.putDat();
					}
					finally {
						new_dto.res_act_sem();
					}
					String act_gid = new_dto.getDat("PSC_GID", 1);
					int con_act_cli_cnt = PsaConActCli.getConActCliCnt(ssn,null,con_gid,act_gid,null);
					if (con_act_cli_cnt==0) {
						con_act_cli_dto.insRow(1);
						con_act_cli_dto.setDat("FAT_GID", 1, con_gid);
						con_act_cli_dto.setDat("CHD_GID", 1, act_gid);
						// suppress insert of new contact
						PscFld con_fld = con_act_cli_dto.getFld("PSA_CON_XRO.PSC_GID");
						PsaDtoIpl.refCom(con_act_cli_dto, con_fld, 1);
					}

					// copy documents
					if (row_idx>0 && !doc_set.isEmpty()) {
						PscDto act_doc_dto = ssn.newDto("PSA_ACT_DOC");
						int act_doc_row = 1;
						act_doc_dto.insRow(act_doc_row, doc_set.size());
						for (String doc_gid : doc_set) {
							act_doc_dto.setDat("FAT_GID", act_doc_row, act_gid);
							act_doc_dto.setDat("CHD_GID", act_doc_row, doc_gid);
							act_doc_row++;
						}
						act_doc_dto.putDat();
					}
				}
				con_act_cli_dto.putDat();
			}
		}
	} 
	finally {
		sav_pnt.abo();
	}
}

}