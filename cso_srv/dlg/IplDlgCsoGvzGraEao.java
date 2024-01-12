// Last Update by user CUSTOMIZER at 20200922085427

// Last Update by user CUSTOMIZER at 20200526145043
import java.io.ByteArrayOutputStream;

import de.pisa.psa.dto.UsxPar;
import de.pisa.psa.dto.psa_scn.JobLog;
import de.pisa.psa.frm.PsaFrm;
import de.pisa.psc.srv.dto.PscDto;
import de.pisa.psc.srv.glb.PscSsn;
import de.pisa.psc.srv.gui.*;
import static de.pisa.psa.ifc.PscGid.isVld;
import static de.pisa.psc.srv.svc.PscUti.isStr;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

/** CSO_GVZ_GRA_EAO */
public class IplDlgCsoGvzGraEao extends de.pisa.psa.frm.PsaFrm implements IplGlbCsoLogUtiItf {

	public IplDlgCsoGvzGraEao(String dsc) {
		super(dsc);
	}

	public void GEN_GRA_VIE(String par, Integer row) throws Exception {
		setGraHtm(row, new UsxPar(par).getPar("WEB_BRW", false));
	}

//@Override
//public void setRow(int row) throws Exception {
//	
//	getSsn().wriTxt("ddd");
//	super.setRow(row);
//	setGraHtm(row);
//}

	private void setGraHtm(int row, boolean shw_in_web_bro) throws Exception {
		PscDto dyn_dto = getDynDto();
		if (row <= 0 || row > dyn_dto.numRec()) {
			return;
		}
		PscFrm top_frm = getTop();
		PsaFrm htm_sub = (PsaFrm)top_frm.getSub("CSO_GVZ_GRA_HTM_SAO");
		String def = dyn_dto.getDat("DEF", row);
		if (def.trim().isEmpty()) {
			htm_sub.setEdtDat("GRA_HTM_DAT", "");
			return;
		}
		
		
		PscSsn ssn = getSsn();
		PscGui gui = ssn.getGui();
		String idn = dyn_dto.getDat("IDN", row);
		try {
			boolean err_whi_mod_lbl = false;
			boolean is_generic = dyn_dto.getDat("TYP", row).contains("GENERIC");
			for (int i = 0; i < 2; i++) {
				try {
					try (ByteArrayOutputStream byt_arr_out_stre = new ByteArrayOutputStream()) {
						MutableGraph g = new Parser().read(def);
						if (is_generic && !err_whi_mod_lbl) {
							for (MutableNode nod : g.rootNodes()) {
								Label lbl_obj = (Label)nod.get("label");
								String lbl = lbl_obj.value();
								String nod_idn = nod.name().value();
								String dto_dsc = (String) nod.get("psa_dto");
								String lbl_pfx = nod_idn;
								if (isStr(dto_dsc)) {
									String dto_tit = "";
									try {
										PscDto nod_dto = ssn.newDto(dto_dsc);
										dto_tit = nod_dto.getTit();
									} catch (Exception e) {
										// ignore
									}
									lbl_pfx += " ("+dto_dsc;
									if (isStr(dto_tit)) {
										lbl_pfx += " - "+dto_tit;
									}
									lbl_pfx += ")";
								}
								
								if (lbl_obj.isHtml()) {
									if (lbl.contains("</table>")) {
//										lbl = lbl.replace("</table>",  "<tr><tc><td>"+lbl_pfx+"</td></tc></tr>"+"</table>");
										String tr_tag = "<tr>";
										int fst_tr_idx = lbl.indexOf(tr_tag);
										lbl = lbl.substring(0,fst_tr_idx)+"<tr><td>"+lbl_pfx+"</td></tr>" + lbl.substring(fst_tr_idx);
									} else {
										lbl = lbl_pfx+"<br/>" +lbl;
									}
									lbl = "<"+lbl+">";
								} else {
									lbl = lbl_pfx+"\\n" + lbl;
								}
								lbl = lbl.replace("@PSA_FLD:", "::");
								nod.add(Attributes.attr("label", Label.raw(lbl)));
							}
						}
						IplGlbCsoGvzGraUti.CsoGvzGra psa_gra = new IplGlbCsoGvzGraUti.CsoGvzGra(g);
						String dot_dat = psa_gra.getDotDat();
						if (!shw_in_web_bro) {
							if (CLI_ENV.isWin(gui)) {
								shw_in_web_bro = true;
								htm_sub.setEdtDat("GRA_HTM_DAT", "Opened graph '"+idn+"' locally (browser) because you use the PiSA windows client");
							}
						}
						String lay_eng = psa_gra.getAtr("psa_lay_eng", IplGlbCsoGvzGraUti.D3_DFL_REND_ENGI);
						String htm_dat = IplGlbCsoGvzGraUti.dotToD3Htm(dot_dat, lay_eng, shw_in_web_bro);
						if (shw_in_web_bro) {
							IplDlgCsoUti.savStrAsTmpFil(htm_dat, true, gui, "show_gvz_graph.html");
						} else {
//							htm_sub.setEdtDat("GRA_HTM_DAT", htm_dat);
							IplGlbCsoGvzGraUti.setHtmDat(htm_sub, "GRA_HTM_DAT", htm_dat);
						}
						
//						Graphviz gv = Graphviz.fromGraph(g);
//						gv = gv.width(1000);
////						gv = gv.engine(is_generic? Engine.DOT : Engine.CIRCO);
//						gv = gv.engine( Engine.DOT);
//						gv.render(Format.SVG).toOutputStream(byt_arr_out_stre);
//						byte[] byt_arr = byt_arr_out_stre.toByteArray();
//						String svg_dat = new String(byt_arr);
//						String htm_dat = IplGlbCsoGvzGraUti.WRA_HTM_BDY_PFX + svg_dat + IplGlbCsoGvzGraUti.WRA_HTM_BDY_SFX;
						
						
					}
					break; // break on success
				} catch (Exception e) {
					err_whi_mod_lbl = true;
					if (i == 1) {
						throw e;
					} else {
						logErr("Exception", e);
						logWrn("Probably error while modifying labels to inject additional meta data. Retrying without modification...");
					}
				}
			}
		} catch (Exception e) {
			String exc_txt = IplDlgCsoUti.getStkTrcStr(e);
			htm_sub.setEdtDat("GRA_HTM_DAT", exc_txt);
			logErr("Exception while generating graph '" + idn + "'", e);
			logErr("For graph '"+idn+"' with input: "+def);
		}
	}

	@Override
	public PscSsn getGuiSsn() {
		return getSsn();
	}

	@Override
	public JobLog getJobLog() {
		return null;
	}

}