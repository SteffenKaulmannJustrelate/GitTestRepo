// Last Update by user CUSTOMIZER at 20220719221718
import de.pisa.psc.srv.glb.*;
// Last Update by user CUSTOMIZER at 20220718222327

import de.pisa.psc.srv.gui.*;
import de.pisa.psc.srv.dto.*;
import de.pisa.psc.srv.glb.*;
import de.pisa.psa.dto.psa_blb.PsaBlbSet;
import de.pisa.psa.ifc.*;
import java.io.*;

import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;

/** CSO_TCH_CFG_LNK_QRC_SSF */
public class IplDlgCsoTchCfgLnkQrcSsf extends de.pisa.psa.frm.PsaFrm
{

   public IplDlgCsoTchCfgLnkQrcSsf(String dsc) { super(dsc); }

	private class QRgen {

		private final String text;
		private String imageType = "PNG";
		private int width = 250;
		private int height = 250;

		public QRgen(String text) {
			this.text = text;
		}

		/**
		 * returns a {@link ByteArrayOutputStream} representation of the QR code
		 * @return qrcode as stream
		 */
		public byte[] get() throws IOException, WriterException {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			writeTo(stream);
			return stream.toByteArray();
		}

		/**
		 * writes a representation of the QR code to the supplied  {@link OutputStream}
		 * @param stream the {@link OutputStream} to write QR Code to
		 */
		public void writeTo(OutputStream stream) throws IOException, WriterException {
			MatrixToImageWriter.writeToStream(createMatrix(), imageType, stream);
		}

		private BitMatrix createMatrix() throws WriterException {
			return new QRCodeWriter().encode(text, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
		}

	}

	private void rstQrc() throws Exception {
	   PscSsn ssn = this.getSsn();
      String mcd = ssn.getEnv("CSO_TCH_CFG_LNK");
		QRgen code = new QRgen(mcd);
		byte[] dat = code.get();
		PsaBlbSet blb = new PsaBlbSet(ssn);
		blb.setChe(TriSta.FALSE);
		blb.setBlb("CSO_TCH_CFG_LNK_QRC", dat, "PNG");
		setTagPrp("TIT_TCH_CFG", "TIT", mcd);
	}

   @Override
   public void creDlg(PscGui gui, PscFrm frm) throws Exception {
      super.creDlg(gui, frm);
      rstQrc();
   }
}