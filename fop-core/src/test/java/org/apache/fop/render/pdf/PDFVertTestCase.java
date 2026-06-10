/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
package org.apache.fop.render.pdf;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.render.intermediate.AbstractIFDocumentHandlerMaker;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;



public class PDFVertTestCase {
    private static final String TEST_MIME = "Test";

    @Test
    public void testFO() throws IOException, SAXException, TransformerException, IFException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        foToOutput(out, TEST_MIME);
    }


    private void foToOutput(ByteArrayOutputStream out, String mimeFopIf)
        throws IOException, SAXException, TransformerException {
        FopFactory fopFactory = getFopFactory();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();

        Fop fop = fopFactory.newFop(mimeFopIf, userAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(PDFVertTestCase.class.getResource("PDFVert.fo").openStream());
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }

    private FopFactory getFopFactory() throws IOException, SAXException {
        return FopFactory.newInstance(new File(".").toURI(),
                PDFVertTestCase.class.getResource("PDFVert.xconf").openStream());
    }


    public static class DocumentHandlerMaker extends AbstractIFDocumentHandlerMaker {

        private static final String[] MIMES = new String[] { TEST_MIME };

        @Override
        public IFDocumentHandler makeIFDocumentHandler(IFContext ifContext) {
            PDFDocumentHandler handler = new PDFDocumentHandler(ifContext) {
                @Override
                public IFPainter startPageContent() throws IFException {
                    return new PDFPainter(this, logicalStructureHandler) {
                        @Override
                        public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text)
                                throws IFException {
                            AffineTransform tr = generator.getState().getData().getTransform();
                            for (int i = 0; i < text.length(); i++) {
                                int cp = text.codePointAt(i);
                                if (!Character.isWhitespace(cp) && cp != 0x2015) {
                                    boolean isHieroglyph = cp >= 0x2e80;
                                    boolean isRotated = tr.getShearX() < tr.getShearY();
                                    //System.out.println("Character '" + text.charAt(i) + "', isHieroglyph " + isHieroglyph +
                                    //        ", isRotated " + isRotated);
                                    assertTrue("Character '" + text.charAt(i) + "' should "
                                            + (isRotated ? "not " : "") + "be rotated", isHieroglyph != isRotated);
                                }
                            }
                            super.drawText(x, y, letterSpacing, wordSpacing, dp, text);
                        }
                    };
                }
            };
            FOUserAgent ua = ifContext.getUserAgent();
            if (ua.isAccessibilityEnabled()) {
                ua.setStructureTreeEventHandler(handler.getStructureTreeEventHandler());
            }
            return handler;
        }

        public boolean needsOutputStream() {
            return true;
        }

        public String[] getSupportedMimeTypes() {
            return MIMES;
        }
    }
}
