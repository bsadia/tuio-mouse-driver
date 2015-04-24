/*
 * Copyright 2015 ArtisTech, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artistech.tuio.mouse;

import TUIO.TuioBlob;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MouseDriver implements TuioListener {

    private Robot robot = null;

    private final ArrayList<MutableTriple<Long, Integer, Integer>> curs = new ArrayList<MutableTriple<Long, Integer, Integer>>();

    private static final Log logger = LogFactory.getLog(MouseDriver.class);

    /**
     * Add Object Event.
     *
     * @param tobj
     */
    public void addTuioObject(TuioObject tobj) {
        logger.debug(MessageFormat.format("add tuio object symbol id: {0}", tobj.getSymbolID()));
    }

    /**
     * Update Object Event.
     *
     * @param tobj
     */
    public void updateTuioObject(TuioObject tobj) {
        logger.debug(MessageFormat.format("update tuio object symbol id: {0}", tobj.getSymbolID()));
    }

    /**
     * Remove Object Event.
     *
     * @param tobj
     */
    public void removeTuioObject(TuioObject tobj) {
        logger.debug(MessageFormat.format("remove tuio object symbol id: {0}", tobj.getSymbolID()));
    }

    /**
     * Refresh Event.
     *
     * @param bundleTime
     */
    public void refresh(TuioTime bundleTime) {
        logger.debug(MessageFormat.format("refresh frame id: {0}", bundleTime.getFrameID()));
    }

    /**
     * Add Cursor Event.
     *
     * @param tcur
     */
    public void addTuioCursor(TuioCursor tcur) {
        logger.trace(MessageFormat.format("add tuio cursor id: {0}", tcur.getCursorID()));

        int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        boolean found = false;
        for (MutableTriple<Long, Integer, Integer> trip : curs) {
            if (trip.getLeft() == tcur.getSessionID()) {
                found = true;
                break;
            }
        }
        if (!found) {
            curs.add(new MutableTriple<Long, Integer, Integer>(tcur.getSessionID(), tcur.getScreenX(width), tcur.getScreenY(height)));
        }

        if (curs.size() == 1) {
            logger.debug(MessageFormat.format("add mouse move: ({0}, {1})", new Object[]{tcur.getScreenX(width), tcur.getScreenY(height)}));
            robot.mouseMove(tcur.getScreenX(width), tcur.getScreenY(height));
        } else {
            logger.debug(MessageFormat.format("add mouse press: {0}", tcur.getCursorID()));
            if (curs.size() == 2) {
                robot.mousePress(InputEvent.BUTTON1_MASK);
            } else {
                robot.mousePress(InputEvent.BUTTON3_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_MASK);
            }
        }
    }

    /**
     * Update Cursor Event.
     *
     * @param tcur
     */
    public void updateTuioCursor(TuioCursor tcur) {
        logger.trace(MessageFormat.format("update tuio cursor id: {0}", tcur.getCursorID()));

        int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        if (!curs.isEmpty() && curs.get(0).getLeft() == tcur.getSessionID()) {
            logger.debug(MessageFormat.format("update mouse move: ({0}, {1})", new Object[]{tcur.getScreenX(width), tcur.getScreenY(height)}));
            robot.mouseMove(tcur.getScreenX(width), tcur.getScreenY(height));
        }
        for (MutableTriple<Long, Integer, Integer> trip : curs) {
            if (trip.getLeft() == tcur.getSessionID()) {
                trip.setMiddle(tcur.getScreenX(width));
                trip.setRight(tcur.getScreenY(height));
                break;
            }
        }
    }

    /**
     * Remove Cursor Event.
     *
     * @param tcur
     */
    public void removeTuioCursor(TuioCursor tcur) {
        logger.trace(MessageFormat.format("remove tuio cursor id: {0}", tcur.getCursorID()));

        if (!curs.isEmpty() && curs.get(0).getLeft() == tcur.getSessionID()) {
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        } else if (curs.size() == 2) {
            logger.debug(MessageFormat.format("remove mouse release: {0}", tcur.getCursorID()));
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }
        MutableTriple t = null;
        for (MutableTriple<Long, Integer, Integer> trip : curs) {
            if (trip.getLeft() == tcur.getSessionID()) {
                t = trip;
                break;
            }
        }
        if (t != null) {
            curs.remove(t);
        }
        if (!curs.isEmpty()) {
            robot.mouseMove(curs.get(0).getMiddle(), curs.get(0).getRight());
        }
    }

    /**
     * Constructor.
     *
     * @throws AWTException
     */
    public MouseDriver() throws AWTException {
        robot = new Robot();
    }

    /**
     * Add Blob Event.
     *
     * @param tblb
     */
    @Override
    public void addTuioBlob(TuioBlob tblb) {
        logger.debug(MessageFormat.format("Added Blob: {0}", tblb.getBlobID()));
    }

    /**
     * Update Blob Event.
     *
     * @param tblb
     */
    @Override
    public void updateTuioBlob(TuioBlob tblb) {
        logger.debug(MessageFormat.format("Update Blob: {0}", tblb.getBlobID()));
    }

    /**
     * Remove Blob Event.
     *
     * @param tblb
     */
    @Override
    public void removeTuioBlob(TuioBlob tblb) {
        logger.info(MessageFormat.format("Remove Blob: {0}", tblb.getBlobID()));
    }

    /**
     * Main: can take a port value as an argument.
     *
     * @param argv
     */
    public static void main(String argv[]) {

        int port = 3333;

        if (argv.length == 1) {
            try {
                port = Integer.parseInt(argv[1]);
            } catch (NumberFormatException e) {
                System.out.println(MessageFormat.format("Port value '{0}' not recognized.", argv[1]));
            }
        }

        try {
            MouseDriver mouse = new MouseDriver();
            TuioClient client = new TuioClient(port);

            logger.info(MessageFormat.format("Listening to TUIO message at port: {0}", Integer.toString(port)));
            client.addTuioListener(mouse);
            client.connect();
        } catch (AWTException e) {
            logger.fatal(null, e);
        }
    }
}
