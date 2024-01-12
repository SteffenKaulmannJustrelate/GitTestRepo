// Last Update by user CUSTOMIZER at 20200824111522
import de.pisa.psa.gui.*;
import de.pisa.psc.srv.gui.*;

public class IplGlbPsaGuiApp extends PsaGui {

public IplGlbPsaGuiApp() { super(); }

@Override
protected boolean iniGui(String par, int nfo, byte[] bin) throws Exception {
   boolean res = super.iniGui(par, nfo, bin);
   if ( res ) {
      updCliEnv(CLI_ENV.AUT.toString(), "Y");
      // getSsn().wriTxt("$I\t$I\t$I\tClient authentication set to \"Y\".");
   }
   return res;
}

@Override
protected void updCliEnv(String nam, String val) {
   super.updCliEnv(nam, val);
   // getSsn().wriTxt("$I\t$I\t$I\tClient environment \"" + nam + "\" set to \"" + val + "\".");
}

@Override
protected void iniCliEnv(String cev) throws Exception {
   super.iniCliEnv(cev);
   // getSsn().wriTxt("$I\t$I\t$I\tGot client environment \"" + cev + "\".");
}

}