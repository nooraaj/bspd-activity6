/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.gov.bsp.utils.av;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 *
 * @author PULUMBARITDS
 */
public class ICAPServiceUtil {
	private static final Logger LOGGER = Logger.getLogger(ICAPServiceUtil.class.getSimpleName());

    private ICAPServiceUtil() {
    }

    public static void scan(ICAPModel model, String file) throws IOException, ICAPException {
        FileInputStream fis = null;
        LOGGER.info("scan1" );

        try {
            ICAP icap = new ICAP(model.getIpAddress(), model.getPort(),
                    model.getService());

            fis = new FileInputStream(file);
            icap.scanStream(fis);

            fis.close();
        } catch (IOException e) {
            throw e;
        } catch (ICAPException e) {
            throw e;
        }
    }

    public static void scan(ICAPModel model, InputStream is) throws IOException, ICAPException {
        try {
            LOGGER.info("scan2" );
        	
        	ICAP icap = new ICAP(model.getIpAddress(), model.getPort(),
                    model.getService());
            icap.scanStream(is);
        } catch (IOException e) {
            throw new IOException("File cannot be opened. Please avoid"
                    + " modification or addition of security to the file.");
        } catch (ICAPException e) {
            throw e;
        }
    }
}
