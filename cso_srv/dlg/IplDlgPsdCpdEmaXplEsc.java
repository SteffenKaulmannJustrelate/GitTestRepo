// Last Update by user CUSTOMIZER at 20171211122107
import java.util.*;

import de.pisa.psa.dto.psa_blb.*;
import de.pisa.psa.dto.psa_cpd.*;
import de.pisa.psa.dto.psa_doc.*;
import de.pisa.psa.dto.psa_pro.*;
import de.pisa.psa.ema.*;
import de.pisa.psa.ifc.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.rpt.*;

/** PSD_CPD_EMA_XPL_ESC */
public class IplDlgPsdCpdEmaXplEsc extends de.pisa.psa.frm.psa_cpd.PsaCpdApp
{
	
public IplDlgPsdCpdEmaXplEsc(String dsc) throws Exception{ super(dsc); }

@Override
public void EMA_SND(String par, Integer row) throws Exception
{
	PscGui gui = getGui();
	PscSsn ssn = getSsn();
	PsaCpd cpdDto = chkDsp("EML", row);
	cpdDto.chkWriAcc(row, true, true, false, true);

	String snd_msg = gui.getMsg("PSA_SND_EML");
	String box_ret = cpdDto.wriBox(snd_msg, "Q", "Q", "Y");
	if (!box_ret.equals("Y")) {
		return;
	}

	Sta sta = CpdSta.getSta(cpdDto, row);
	if (sta.modSta(Sta.OPR.SND)) {
		// write success message
		ssn.wriMsg("PSA_CPD_SND");
		
		// set template body
		setTplMsg(row);
		cpdDto.getTop().putDat();
		
		// set milestone
		PsaCpdFnc cpd_fnd = new PsaCpdFnc(cpdDto);
		String mil_stn = cpd_fnd.getMilStn(row);
		String pro_gid = cpdDto.getDat("PRO_GID", row);
		String cpd_gid = cpdDto.getDat("PSC_GID", row);
		if (mil_stn!=null && PscGid.isVld(pro_gid) && PscGid.isVld(cpd_gid)) {
			PscDto pro_con_dto = ssn.newDto("PSA_PRO_CON");
			pro_con_dto.setQue("FAT_GID", pro_gid);
			PscDto rcv_dto = ssn.newDto("PSA_CON_CPD_AGG");
			rcv_dto.setQue("CHD_GID", cpd_gid);
			rcv_dto.setMax(Integer.MAX_VALUE - 1);
			int rcv_cnt = rcv_dto.fetDat();
			for (int rcv_row=1; rcv_row<=rcv_cnt; rcv_row++) {
				String con_gid = rcv_dto.getDat("FAT_GID", rcv_row);
				if (!PscGid.isVld(con_gid)) {
					continue;
				}
				pro_con_dto.setQue("CHD_GID", con_gid);
				if (pro_con_dto.fetDat()==0) {
					continue;
				}
				pro_con_dto.setDat("STA_IDN", 1, mil_stn);
				PsaProConSta.cpyProConStaCol(pro_con_dto, 1);
				pro_con_dto.putDat();
			}
		}
	}
	else {
		if (sta.isSta(CpdSta.STA_EDT)) {
			String msg1 = gui.getMsg("PSA_SND_NOT_ALW");
			String msg2 = gui.getMsg("PSA_CPD_IN_EDT");
			cpdDto.wriBox(msg1+" "+msg2, "W", "W", "O");
		}
		else {
			ssn.wriMsg("PSA_SND_NOT_ALW");
		}
	}
}

private void setTplMsg(int row)throws Exception
{
	PscSsn ssn = getSsn();
	PscDto dyn_dto = getDynDto();
	PsaCpd cpd_dto = (PsaCpd)dyn_dto.getSupDto("PSA_CPD");
	String cpd_gid = cpd_dto.getDat("PSC_GID", row);
	PscDto rcv_dto = ssn.newDto("PSA_CON_CPD_AGG");
	rcv_dto.setQue("CHD_GID", cpd_gid);
	String con_gid = null;
	try {
		if (rcv_dto.fetNxt()) {
			con_gid = rcv_dto.getDat("FAT_GID", 1);
		}
	}
	finally {
		rcv_dto.fetCls();
	}
	if (!PscGid.isVld(con_gid)) {
		return;
	}
	Tpl tpl = creTplMsg(row, con_gid);
	if (tpl==null) {
		return;
	}
	String mailFil = "mail." + PscRptBas.getTypEnd(tpl.Typ);
	String msg = tpl.Msg;
	String tpl_img[] = tpl.Img;
	Set<String> emb_fil = new HashSet<String>();
	msg = addEmbBlbImg(msg, null, emb_fil);
	if (tpl_img!=null && tpl_img.length>0) {
		Map<String, String> img_blb = PsaCpdBlb.setBlb(ssn, cpd_gid, tpl_img, false);
		msg = EmaUti.rplImgByBlb(msg, img_blb);
	}
	if (emb_fil.size()>0) {
		String blb_lst[] = emb_fil.toArray(new String[emb_fil.size()]);
		Map<String, String> img_blb = PsaCpdBlb.addBlb(ssn, cpd_gid, blb_lst);
		msg = EmaUti.rplImgByBlb(msg, img_blb);
	}
	BlbNfo nfo = PsaCpdFnc.checkIn(cpd_dto, row, mailFil, new StrBin(msg));
	if (nfo.doSet()) {
		nfo.setDat(cpd_dto, row);
	}
	cpd_dto.setFilBlbPic(row, false);
}

private Tpl creTplMsg(int row, String con_gid) throws Exception
{
    Tpl ret;
    PscSsn ssn = getSsn();
    PscDto dyn_dto = getDynDto();
    PsaCpd cpd_dto = (PsaCpd)dyn_dto.getSupDto("PSA_CPD");
    String cpd_gid = cpd_dto.getDat("PSC_GID", row);
    BodyHlp hlp = new BodyHlp(ssn);
    hlp.setConGid(con_gid);
    hlp.setCopImgCli(false);
    hlp.setCreTpl(true);
    try {
        ret = new Tpl();
        ret.Msg = hlp.getMailBody(cpd_dto, row, new SmtpEmaSupTyp());
        ret.Img = hlp.getImgLst();
        ret.Typ = hlp.getBodyTyp();
        ret.Msg = EmaUti.addGid(ssn, ret.Msg, ret.Typ, cpd_gid, null);
    }
    catch (Exception exc) {
        ret = null;
    }
    return ret;
}

private String addEmbBlbImg(String txt, PsaSmtp mail, Set<String> emb_blb_img)
	throws Exception
{
	PscSsn ssn = getSsn();
	PsaRplHtmImg htm_img = new PsaRplHtmImg(ssn, false);
	String ret = htm_img.rpl(txt, null);
	PsaBlbGet blb_get = new PsaBlbGet(ssn);
	for (String emb_img_nam : htm_img.getBlbImg()) {
		String blb_nam = emb_img_nam;
		int dot_idx = blb_nam.lastIndexOf('.');
		if (dot_idx!=-1) {
			blb_nam = blb_nam.substring(0, dot_idx);
		}
		String cid = ssn.getDbi().creGid();
		byte img_dat[] = blb_get.getBlb(blb_nam);
		String blb_typ = blb_get.getTyp();
		if (mail!=null) {
			mail.addEmbeddedFile(img_dat, blb_nam+'.'+blb_typ, cid);
		}
		if (emb_blb_img!=null) {
			emb_blb_img.add(emb_img_nam);
		}
	}
	return ret;
}

/** template */
private static class Tpl {
    /** message body */
    String Msg = null;
    /** message images */
    String Img[] = null;
    /** message type */
    int Typ = 0;
    /** constructor */
    protected Tpl() {}
}

}