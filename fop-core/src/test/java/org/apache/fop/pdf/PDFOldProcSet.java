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
package org.apache.fop.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class PDFOldProcSet {

    @Test
    public void testPDF() throws Exception {
        String fopxconf = "<fop version=\"1.0\">"
                + "<accessibility>true</accessibility>"
                + "<renderers><renderer mime=\"application/pdf\">"
                + "<linearization>true</linearization>"
                + "</renderer></renderers></fop>";
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\">\n"
                + "      <fo:region-body/>\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block>aaa<fo:block break-before=\"page\"/></fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        checkPDF(out.toByteArray());
    }

    private void checkPDF(byte[] data) throws IOException {

        InputStream is = new ByteArrayInputStream(data);
        Map<String, StringBuilder> objs = readObjs(is);

        String objects = objs.values().stream().collect(Collectors.joining(" "));

        Assert.assertTrue(!objects.contains("/ProcSet [/PDF /ImageB /ImageC /Text]"));
    }


    private static Map<String, StringBuilder> readObjs(InputStream inputStream) throws IOException {
        Map<String, StringBuilder> objs = new LinkedHashMap<String, StringBuilder>();
        StringBuilder sb = new StringBuilder();
        String key = null;
        while (inputStream.available() > 0) {
            int data = inputStream.read();
            if (data == '\n') {
                if (sb.toString().endsWith(" 0 obj")) {
                    key = sb.toString().trim();
                    objs.put(key, new StringBuilder());
                } else if (key != null) {
                    objs.get(key).append(sb).append("\n");
                }
                sb.setLength(0);
            } else {
                sb.append((char) data);
            }
        }
        return objs;
    }
}
