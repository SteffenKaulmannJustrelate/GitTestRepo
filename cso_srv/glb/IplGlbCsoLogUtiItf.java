// Last Update by user CUSTOMIZER at 20220706074501
import de.pisa.psa.dto.psa_scn.*;
import de.pisa.psa.ssn.*;
import de.pisa.psc.srv.glb.*;

import org.apache.logging.log4j.*;

/**
 * CSO_LOG_UTI_ITF
 * 
 * @author geger
 */
public interface IplGlbCsoLogUtiItf {

	PscSsn getGuiSsn();
	JobLog getJobLog();

	default Level getGuiLogLvl() {
		IplGlbCsoLogUtiItf fat_log = getFatLog();
		if (fat_log != null) {
			return fat_log.getGuiLogLvl();
		}
		return Level.DEBUG;
	}

	default Level getGblMinLogLev() {
		IplGlbCsoLogUtiItf fat_log = getFatLog();
		if (fat_log != null) {
			return fat_log.getGblMinLogLev();
		}
		return Level.DEBUG;
	}

	default void logLvl(String txt, Level lev, Throwable exc) {
		IplGlbCsoLogUtiItf fat_log = getFatLog();
		if (fat_log != null) {
			fat_log.logLvl(txt, lev, exc);
			return;
		}
		if (logIsOff() || !lev.isLessSpecificThan(getGblMinLogLev())) {
			return;
		}
		JobLog job_log = getJobLog();
		if (job_log != null) {
			if (exc != null) {
				job_log.logErr(txt, exc);
			} else {
				job_log.log(txt, lev);
			}
		} else {
			logLvlToGui(txt, lev, exc);
		}
	}

	default void logLvl(String txt, Level lev) {
		logLvl(txt, lev, null);
	}

	default void logNfo(String msg) throws Exception {
		logLvl(msg, Level.INFO);
	}

	default void logWrn(String msg) throws Exception {
		logLvl(msg, Level.WARN);
	}

	default void logErr(String msg) throws Exception {
		logLvl(msg, Level.ERROR);
	}

	default void logErr(String msg, Throwable exc) throws Exception {
		logLvl(msg, Level.ERROR, exc);
	}

	default void logDbg(String msg) throws Exception {
		logLvl(msg, Level.DEBUG);
	}

	default void logLvlToGui(String msg, Level lev) {
		logLvlToGui(msg, lev, null);
	}

	default void logLvlToGui(String msg, Level lev, Throwable exc) {
		IplGlbCsoLogUtiItf fat_log = getFatLog();
		if (fat_log != null) {
			fat_log.logLvlToGui(msg, lev, exc);
			return;
		}
		if (logIsOff() || !lev.isLessSpecificThan(getGblMinLogLev())) {
			return;
		}
		if (getGuiLogLvl() == null || !lev.isLessSpecificThan(getGuiLogLvl())) {
			return;
		}
		String gui_log_pfx = getGuiLogPfx(msg, lev, exc);
		String log_str = gui_log_pfx + msg;
		if (exc != null) {
			log_str += IplDlgCsoUti.NEW_LIN_STR;
			log_str += IplDlgCsoUti.getStkTrcStr(exc);
		}
		PscSsn ssn = getGuiSsn();
		if (ssn.getCon() != null && ssn.getCon().hasAct("PSC_FRM_CRE")) {
			ssn.wriTxt(log_str);
		} else {
			PsaSsn.wriTxtImm(ssn, log_str);
		}
	}

	default String getGuiLogPfx(String msg, Level lev, Throwable exc) {
		IplGlbCsoLogUtiItf fat_log = getFatLog();
		if (fat_log != null) {
			return fat_log.getGuiLogPfx(msg, lev, exc);
		}
		return lev.equals(Level.INFO) ? "" : lev.toString() + ": ";
	}
	/**
	 * Use/override {@link IplGlbCsoLogUtiItf#getGblMinLogLev()} instead
	 */
	@Deprecated
	default boolean logIsOff() {
		IplGlbCsoLogUtiItf fat_log = getFatLog();
		if (fat_log != null) {
			return fat_log.logIsOff();
		}
		return false;
	}

	default IplGlbCsoLogUtiItf getFatLog() {
		return null;
	}

}