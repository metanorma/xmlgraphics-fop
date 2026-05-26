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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.URIAction;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.extensions.PDFEmbeddedFileAttachment;
import static org.apache.fop.pdf.PDFLinearizationTestCase.readObjs;


public class PDFAttachmentTestCase {
    private FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();

    @Test
    public void testAddEmbeddedFile() throws IFException {
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());
        docHandler.handleExtensionObject(new PDFEmbeddedFileAttachment("filename", "src", "desc"));
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction("embedded-file:filename", false, null), new Rectangle()));
        docHandler.endDocument();
        Assert.assertTrue(out.toString().contains(
                "<<\n  /Type /Filespec\n  /F (filename)\n  /UF (filename)\n  /AFRelationship /Data"));
        Assert.assertTrue(out.toString().contains("<<\n/S /JavaScript\n"
                + "/JS (this.exportDataObject\\({cName:\"filename\", nLaunch:2}\\);)\n>>"));
    }

    @Test
    public void testAddEmbeddedFileGermanUmlaut() throws IFException {
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());

        String germanAe = "\u00E4";
        String unicodeFilename = "t" + germanAe + "st";
        PDFEmbeddedFileAttachment fileAtt = new PDFEmbeddedFileAttachment(unicodeFilename,
                "src", "desc");
        docHandler.handleExtensionObject(fileAtt);
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction("embedded-file:" + unicodeFilename, false, null), new Rectangle()));
        docHandler.endDocument();
        Assert.assertTrue(out.toString().contains(
                "<<\n  /Type /Filespec\n  /F (" + fileAtt.getFilename() + ")\n  /UF "
                        + PDFText.escapeText(fileAtt.getUnicodeFilename()) + "\n  /AFRelationship /Data"));
        Assert.assertTrue(out.toString().contains("<<\n/S /JavaScript\n"
                + "/JS (this.exportDataObject\\({cName:\"" + fileAtt.getFilename() + "\", nLaunch:2}\\);)\n>>"));
    }

    @Test
    public void testAddEmbeddedFileParenthesis() throws IFException {
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());

        String unicodeFilename = "t(st";
        PDFEmbeddedFileAttachment fileAtt = new PDFEmbeddedFileAttachment(unicodeFilename,
                "src", "desc");
        docHandler.handleExtensionObject(fileAtt);
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction("embedded-file:" + unicodeFilename, false, null), new Rectangle()));
        docHandler.endDocument();
        Assert.assertTrue(out.toString().contains(
                "<<\n  /Type /Filespec\n  /F (t\\(st)\n  /UF (t\\(st)\n  /AFRelationship /Data"));
        Assert.assertTrue(out.toString().contains("<<\n/S /JavaScript\n"
                + "/JS (this.exportDataObject\\({cName:\"t\\(st\", nLaunch:2}\\);)\n>>"));
    }


    @Test
    public void testFileAttachmentAnnotation() throws Exception {
        String fopxconf = "<fop version=\"1.0\" encoding=\"UTF-8\">"
                + "<accessibility>true</accessibility>"
                + "<renderers><renderer mime=\"application/pdf\">"
                + "<linearization>true</linearization>"
                + "<filterList>\n"
                + "    <value>null</value>\n"
                + "  </filterList>\n"
                + "</renderer></renderers></fop>";
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\""
                + " xmlns:pdf=\"http://xmlgraphics.apache.org/fop/extensions/pdf\""
                + " xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\">\n"
                + "      <fo:region-body/>\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "<fo:declarations>\n"
                + "    <pdf:catalog>\n"
                + "      <pdf:dictionary key=\"ViewerPreferences\" type=\"normal\">\n"
                + "        <pdf:boolean key=\"DisplayDocTitle\">true</pdf:boolean>\n"
                + "      </pdf:dictionary>\n"
                + "    </pdf:catalog>\n"
                + "    <x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n"
                + "      <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                + "        <rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                + " xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\" rdf:about=\"\">\n"
                + "          <dc:title>\n"
                + "            <rdf:Alt>\n"
                + "              <rdf:li xml:lang=\"x-default\">Document for test attachment</rdf:li>\n"
                + "            </rdf:Alt>\n"
                + "          </dc:title>\n"
                + "          <dc:creator>\n"
                + "            <rdf:Seq>\n"
                + "              <rdf:li>Metanorma</rdf:li>\n"
                + "            </rdf:Seq>\n"
                + "          </dc:creator>\n"
                + "          <pdf:Keywords>PDF, file attachment, test.</pdf:Keywords>\n"
                + "        </rdf:Description>\n"
                + "        <rdf:Description xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" rdf:about=\"\">\n"
                + "          <xmp:CreatorTool/>\n"
                + "        </rdf:Description>\n"
                + "      </rdf:RDF>\n"
                + "    </x:xmpmeta>\n"
                + "    <pdf:embedded-file link-as-file-annotation=\"true\" filename=\"simple_text.txt\""
                + " src=\"data:application/octet-stream;base64,U2ltcGxlIHRleHQuDQo=\""
                + " description=\"File &#x201c;description&#x201d;\" afrelationship=\"AFR_Data\" volatile=\"true\"/>\n"
                + "  </fo:declarations>"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block>File: <fo:basic-link fox:alt-text=\"reference to Attachment\""
                + " external-destination=\"url(embedded-file:simple_text.txt)\" role=\"Annot\">"
                + "reference to Attachment</fo:basic-link></fo:block>\n"
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

        Assert.assertTrue(objects.contains("/Type /Annot /Subtype /FileAttachment\n"
                + "/FS"));

        byte[] bytesContents =  ("File " + '\u201c' + "description" + '\u201d').getBytes("UTF-16BE");

        // "/Contents (" +
        Assert.assertTrue(objects.contains(new String(bytesContents) + ")\n"
                + "/Name /Paperclip"));

        Assert.assertTrue(objects.contains("/Type /Filespec\n"
                + "  /F (simple_text.txt)\n"
                + "  /UF (simple_text.txt)\n"
                + "  /AFRelationship /AFR_Data"));
        //Assert.assertTrue(objects.contains("/Desc (File description)\n" +
        Assert.assertTrue(objects, objects.contains(
                "/Desc <FEFF00460069006C00650020201C006400650073006300720069007000740069006F006E201D>\n"
                + "  /V true"));
    }

}
