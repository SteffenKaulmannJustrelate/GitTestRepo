// Last Update by user PSA_PRE_SAL at 20090721140956
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.xml.*;

import de.pisa.psa.dto.*;
import de.pisa.psa.dto.psa_adr.*;
import de.pisa.psa.frm.*;

public class IplDlgCsoLocGenSsc extends de.pisa.psa.frm.PsaFrm
{
	
public IplDlgCsoLocGenSsc(String dsc) throws Exception{ super(dsc); }

@Override
public PscFrm dlgFrmGid(String dlg_dsc, int row, PscFld fld) throws Exception {
    int fld_idx = dlg_dsc.indexOf(' ');
    String dlg_nam = ( fld_idx>0 ? dlg_dsc.substring(0,fld_idx) : dlg_dsc );
    String fld_nam = ( fld_idx>0 ? dlg_dsc.substring(fld_idx+1) : "NULL" );
    
    PscDto dyn_dto = getDynDto();
    PscDto drv_dto = dyn_dto.getDrv();
    PscFrm top_chd = (PscFrm)getTopChd();
    boolean mod = (row>0) ? drv_dto.chkMod(row) : false;
    PscGui gui = getGui();
    PscSsn ssn = getSsn();
    
    PscFrm new_frm = null;
    if (fld_nam.equals("PSA_ADR.PSC_GID")) {
        String sup_int_adr = ssn.getEnv("PSA_SFR_INT_ADR"); 
        if ( sup_int_adr!=null && sup_int_adr.equalsIgnoreCase("y") ) {
            dlg_nam = dlg_nam.replaceFirst("_\\{0\\}", "");
            dlg_nam = dlg_nam.replaceFirst("CMP", "CMI"); 
        } 
        else {
            if (dlg_nam.indexOf("{0}")>0) {
                String adr_typ = dyn_dto.getDat("ADR_TYP_IDN", row);
                String dlg_prt = adr_typ.equals("CORRESPONDENCE") ? "CPD_ORG" : "ORG";
                dlg_nam = dlg_nam.replaceFirst("\\{0\\}", dlg_prt);
            }
        }
        
        if ( row==0 ) {
            insRow();
            row = getRow();
        }

        PsaDto adr_com = (PsaDto)drv_dto.getDto("PSA_ADR");
        if ( adr_com!=null ) {
            PscDto adr_drv = adr_com.getDrv();
            boolean adr_out = PsaDtoIpl.chkOut(adr_com,row); 
            new_frm = gui.newFrm(dlg_nam);
            if ( new_frm!=null ) {
                PscDto new_dto = new_frm.getDynDto();
                new_dto.insRow(1);
                int new_row = 1;
                String adr_gid = adr_com.getDat("PSC_GID",row);
                ((PsaDto)new_dto).set_act_sem();
                if ( !adr_out ) {
                	PscImp new_imp = new PscImp(ssn);
					new_dto.setImp(new_imp);
					try {
						new_dto.copRow(new_row,adr_drv,row);
						new_dto.refRow(new_row, adr_drv.getDto("PSA_CTY"), row);
						PsaDtoIpl.setRecEdt(new_dto, new_row, 'U');
					} 
					finally {
						new_dto.setImp(null);
					}
                }
                PscFld adr_typ_fld = new_dto.getFld("TYP_IDN");
                String adr_typ = new_dto.getDat(adr_typ_fld,new_row);
                if ( !adr_typ.equals("LOCATION_ADDRESS") ) {
                    String typ_dfv = adr_typ_fld.getDfv();
                    String loc_idn = PsaAdrTyp.getIdn(ssn, "LOCATION_ADDRESS");
                    if (loc_idn.length()>0) {
                        typ_dfv = loc_idn;
                    }
                    PsaAdrTyp.copAdrTypDat(ssn, typ_dfv, new_dto, new_row);
                }
                ((PsaDto)new_dto).res_act_sem();
                if (new_frm instanceof PsaFrm) {
                    PsaFrmIpl.setOriFrm(new_frm,(top_chd instanceof PsaFrm) ? (PsaFrm)top_chd : this);
                    PsaFrmIpl.setOriFrmRow(new_frm,row);
                    PsaFrmIpl.setModFrm(new_frm,true);
                }
                if ( new_dto instanceof PsaDto ) {
                    PsaDtoIpl.setOriDto(new_dto,top_chd.getDynDto());
                    PsaDtoIpl.setOriDtoRow(new_dto,row);
                }
                gui.dlgFrm(new_frm);
                if ( new_frm.chkExiFlg() ) {
                    ((PsaDto)adr_drv).set_act_sem();
                    PscFld cmp_adr_fld = adr_com.getFld("CMP_ADR");
                    if ( cmp_adr_fld!=null ) { 
                        String adr_cmp_ori = adr_com.getDat(cmp_adr_fld,row);
                        String adr_cmp_new = new_dto.getDat("CMP_ADR",new_row);
                        if ( !adr_cmp_ori.equals(adr_cmp_new) || adr_out ) adr_com.prpDat(cmp_adr_fld,row);
                    }
                    adr_drv.copRow(row,new_dto,new_row);
                    adr_drv.refRow(row, new_dto.getDto("PSA_CTY"), new_row);
                    ((PsaDto)adr_drv).res_act_sem();
                    
                    String nul_gid = adr_com.getDat("PSC_GID",row);
                    if ( !adr_out && nul_gid.equals("NULL") ) {
                    	PsaDtoIpl.setSysDat(adr_com,"PSC_GID",row,adr_gid);
                    }
                    // new_frm.aboDat();
                }
            }
            if ( !mod ) {
                drv_dto.putDat();
            }
        }
    }
    else {
        new_frm = super.dlgFrmGid(dlg_dsc, row, fld);
    }
    
    return new_frm;
}

}