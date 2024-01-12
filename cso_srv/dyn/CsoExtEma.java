// Last update by user CUSTOMIZER on host PC-WEILAND-12 at 20160415093637
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;

import de.pisa.psa.com.ext_ema.*;
import de.pisa.psa.com.ext_ema.PsaEmaSta.*;
import de.pisa.psa.dto.psa_pro.*;
import de.pisa.psa.ifc.*;

/** CSO_EXT_EMA */
public class CsoExtEma extends de.pisa.psa.com.ext_ema.inx.PsaInxEma {

	public CsoExtEma(PscSsn ssn) throws Exception {
		super(ssn);
	}

	@Override
	public PsaCpdRspGen getRspGen(PscDto cpd_dto, int cpd_row) throws Exception {
		return new CsoCpdRspGen(cpd_dto, cpd_row);
	}
	
	static class CsoCpdRspGen extends de.pisa.psa.com.ext_ema.PsaCpdRspGen
	{

		public CsoCpdRspGen(PscDto cpd_dto, int cpd_row) throws Exception {
			super(cpd_dto, cpd_row);
		}

		@Override
		protected void wriCpdSta(PsaEmaSta sta) throws Exception {
			super.wriCpdSta(sta);
			
			String pro_gid = Cpd_Dto.getDat("PRO_GID", Cpd_Row);
			if (sta.getSta()==EmaSta.Sent &&
				PscGid.isVld(Cpd_Gid) &&
				PscGid.isVld(pro_gid))
			{
				PscDto sta_dto = Ssn.newDto("PSA_PRO_CON_STA");
				sta_dto.setQue("PRO_GID", pro_gid);
				sta_dto.setQue("ORI_IDN", "'NLT_STA_INF_SNT'");
				if (sta_dto.fetDat()!=0) {
					String sta_idn = sta_dto.getDat("IDN", 1);
					PscDto pro_con_dto = Ssn.newDto("PSA_PRO_CON");
					pro_con_dto.setQue("FAT_GID", pro_gid);
					PscDto rcp_dto = Ssn.newDto("PSA_CON_CPD_AGG");
					rcp_dto.setQue("CHD_GID", Cpd_Gid);
					rcp_dto.setQue("SND_FLG", "y");
					try {
						while (rcp_dto.fetNxt()) {
							String con_gid = rcp_dto.getDat("FAT_GID", 1);
							pro_con_dto.setQue("CHD_GID", con_gid);
							int pro_con_cnt = pro_con_dto.fetDat();
							for (int pro_con_row=1; pro_con_row<=pro_con_cnt; pro_con_row++) {
								pro_con_dto.setDat("STA_IDN", pro_con_row, sta_idn);
								PsaProConSta.cpyProConStaCol(pro_con_dto, pro_con_row);
							}
							pro_con_dto.putDat();
						}
					}
					finally {
						rcp_dto.fetCls();
					}
				}
			}
		}
		
	}
	
}