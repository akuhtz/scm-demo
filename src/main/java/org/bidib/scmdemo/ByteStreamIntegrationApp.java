/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2018, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.bidib.scmdemo;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComInByteStream;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.SMODE;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.SerialComOutByteStream;

final class CleanUp implements ICleanUpListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUp.class);

    private SerialComManager scm;

    private SerialComOutByteStream out;

    private SerialComInByteStream in;

    private long comPortHandle;

    public CleanUp(SerialComManager scm, SerialComInByteStream in, SerialComOutByteStream out, long comPortHandle) {
        this.scm = scm;
        this.in = in;
        this.out = out;
        this.comPortHandle = comPortHandle;
    }

    @Override
    public void onAppExit() {
        try {
            in.close();
            out.close();
            scm.closeComPort(comPortHandle);
            LOGGER.info("Clean up completed !");
        }
        catch (Exception e) {
            LOGGER.warn("Cleanup failed.", e);
        }
    }
}

public final class ByteStreamIntegrationApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteStreamIntegrationApp.class);

    private SerialComManager scm;

    private SerialComOutByteStream out;

    private SerialComInByteStream in;

    private String PORT;

    private long comPortHandle;

    private CleanUp cleanup;

    private GraphPlotter plotter;

    protected void begin(String port) {
        try {
            LOGGER.info("Application started !");

            // instantiate serialpundit.
            scm = new SerialComManager();
            SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

            int osType = scp.getOSType();
            if (osType == SerialComPlatform.OS_LINUX) {
                PORT = "/dev/ttyUSB0";
            }
            else if (osType == SerialComPlatform.OS_WINDOWS) {
                PORT = port /* "COM13" */;
            }
            else if (osType == SerialComPlatform.OS_MAC_OS_X) {
                PORT = "/dev/cu.usbserial-A70362A3";
            }
            else {
            }

            LOGGER.info("Open port: {}", PORT);

            // open serial port.
            comPortHandle = scm.openComPort(PORT, true, true, true);
            scm.configureComPortData(comPortHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
            scm.configureComPortControl(comPortHandle, FLOWCONTROL.NONE, 'x', 'x', false, false);

            // create input and output byte streams.
            out =
                (SerialComOutByteStream) scm.getIOStreamInstance(SerialComManager.OutputStream, comPortHandle,
                    SMODE.BLOCKING);
            in =
                (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, comPortHandle,
                    SMODE.BLOCKING);

            // prepare class that will be used when application exits.
            cleanup = new CleanUp(scm, in, out, comPortHandle);

            // setup GUI.
            plotter = new GraphPlotter(in, out, cleanup);
        }
        catch (Exception e) {
            LOGGER.warn("Open port and start GraphPlotter failed.", e);
        }
    }

    /* Entry point to this application. */
    public static void main(String[] args) {
        String port = "COM13";
        if (args.length > 0) {
            port = args[0];
        }

        final String portToOpen = port;
        // Setup GUI in event-dispatching thread for thread-safety.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ByteStreamIntegrationApp app = new ByteStreamIntegrationApp();
                app.begin(portToOpen);
            }
        });
    }
}
