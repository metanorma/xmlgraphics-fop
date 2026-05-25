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

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PDFTagsTestCase {

    StringBuilder tagsTree = new StringBuilder();

    @Test
    public void testPDF() throws Exception {
        String fopxconf = "<fop version=\"1.0\">"
                + "<accessibility>true</accessibility>"
                + "<renderers><renderer mime=\"application/pdf\">"
                + "<linearization>true</linearization>"
                + "<filterList>\n"
                + "    <value>null</value>\n"
                + "  </filterList>\n"
                + "      <fonts>\n"
                + "        <font name=\"Univers\" embed-url=\"test/resources/fonts/ttf/DejaVuLGCSerif.ttf\">\n"
                + "          <font-triplet name=\"Univers\" style=\"normal\" weight=\"normal\"/>\n"
                + "          <font-triplet name=\"any\" style=\"normal\" weight=\"normal\"/>\n"
                + "        </font>\n"
                + "      </fonts>\n"
                + "</renderer></renderers></fop>";
        String fo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:pdf=\"http://xmlgraphics.apache.org/fop/extensions/pdf\" xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\" font-family=\"Univers\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\">\n"
                + "      <fo:region-body/>\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block>Link 1: <fo:basic-link external-destination=\"https://www.iso.org\" fox:alt-text=\"iso.org\">www.iso.org</fo:basic-link></fo:block>\n"
                + "      <fo:block>Link 2: <fo:basic-link external-destination=\"mailto:iso@iso.org\" fox:alt-text=\"iso@iso.org\">iso@iso.org</fo:basic-link></fo:block>\n"
                + "      <fo:block>Link 3: <fo:basic-link internal-destination=\"foreword\" fox:alt-text=\"Foreword\">Foreword</fo:basic-link></fo:block>\n"
                + "      <fo:block>Link 4: <fo:basic-link internal-destination=\"scope\" fox:alt-text=\"Link&#xa0;to&#xa0;Scope\">Scope</fo:basic-link></fo:block>\n"
                + "      <fo:block>Link 5: <fo:basic-link internal-destination=\"scope\" fox:alt-text=\"Link&#xa0;to&#xa0;Scope\">Scope</fo:basic-link></fo:block>\n"
                + "      <fo:block id=\"foreword\">Foreword</fo:block>\n"
                + "      <fo:block id=\"scope\" role=\"Sect\" fox:title=\"Scope\">Scope</fo:block>\n"
                + "      <fo:block id=\"note1\" role=\"Note\">Note 1: text.</fo:block>\n"
                + "      <fo:table id=\"table1\" table-layout=\"fixed\" width=\"100%\">\n"
                + "        <fo:table-header>\n"
                + "          <fo:table-row>\n"
                + "            <fo:table-cell text-align=\"center\" number-rows-spanned=\"2\">\n"
                + "              <fo:block>Defect</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell number-columns-spanned=\"4\">\n"
                + "              <fo:block>Maximum permissible mass fraction of defects %</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "          </fo:table-row>\n"
                + "          <fo:table-row>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>Husked rice</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>Milled rice (non-glutinous)</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>Husked parboiled rice</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>Milled parboiled rice</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "          </fo:table-row>\n"
                + "        </fo:table-header>\n"
                + "        <fo:table-body>\n"
                + "          <fo:table-row min-height=\"4mm\">\n"
                + "            <fo:table-cell number-rows-spanned=\"2\">\n"
                + "              <fo:block>\n"
                + "                <fo:block>Extraneous matter:</fo:block>\n"
                + "              </fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell number-columns-spanned=\"4\">\n"
                + "              <fo:block> </fo:block>\n"
                + "            </fo:table-cell>\n"
                + "          </fo:table-row>\n"
                + "          <fo:table-row min-height=\"4mm\">\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>1,0</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>0,5</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>1,0</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "            <fo:table-cell>\n"
                + "              <fo:block>0,5</fo:block>\n"
                + "            </fo:table-cell>\n"
                + "          </fo:table-row>\n"
                + "        </fo:table-body>\n"
                + "      </fo:table>"
                + "      <fo:block role=\"P/Title\">Section title</fo:block>\n"
                + "      <fo:block>Text <fo:inline role=\"SKIP\">inline text</fo:inline></fo:block>\n"
                + "      <fo:block>Text <fo:block><fo:block-container><fo:block>Div inside P</fo:block></fo:block-container></fo:block></fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";

        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.getRendererOptions().put("pdf-a-mode", "PDF/A-3a");
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);

        checkPDF(out.toByteArray());
        checkPDFtags(out.toByteArray());
    }

    private void checkPDF(byte[] data) throws IOException {

        InputStream is = new ByteArrayInputStream(data);
        Map<String, StringBuilder> objs = PDFLinearizationTestCase.readObjs(is);

        String objects = objs.values().stream().collect(Collectors.joining(" "));

        Assert.assertTrue(objects.contains("/Contents (https://www.iso.org)"));
        Assert.assertTrue(objects.contains("/Contents (Email iso@iso.org)"));
        Assert.assertTrue(objects.contains("/Contents (Foreword)"));
        Assert.assertTrue(objects.contains("/Contents (Link to Scope)"));
        Assert.assertTrue(objects.contains("/Note\n" + "  /ID ("));
        Assert.assertTrue(objects.contains("/RowSpan 2\n" + "  /Scope /Row"));
        Assert.assertTrue(objects.contains("/ColSpan 4\n" + "  /Scope /Column"));
        // check for role="SKIP"
        Assert.assertTrue(!objects.contains("/Span"));

        // check for P/Div
        Assert.assertTrue(!objects.contains("/S /Div"));
        // check for "Add /T (title) for <Sect>" (https://github.com/metanorma/xmlgraphics-fop/issues/67)
        Assert.assertTrue(objects.contains("/T (Scope)"));
  
        // check for "Add /T (title) for <Sect>" (https://github.com/metanorma/xmlgraphics-fop/issues/67)
        Assert.assertTrue(objects.contains("/T (Scope)"));
        // check for "Annotation flags (Ff) for all Link annotations" (https://github.com/metanorma/xmlgraphics-fop/issues/72)
        Assert.assertTrue(!objects.contains("/Contents (https://www.iso.org)\n"
                + "/F 28"));
        Assert.assertTrue(objects.contains("/Contents (https://www.iso.org)\n"
                + "/F 4"));

        // check for internal link annotations (https://github.com/metanorma/xmlgraphics-fop/issues/75)
        String internalLink = "/Contents (Link to Scope)\n"
                + "/F 4\n"
                + ">>\n"
                + "endobj\n"
                + " << /Type /Action\n"
                + "/S /GoTo";
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = objects.indexOf(internalLink, lastIndex)) != -1) {
            count++;
            lastIndex += internalLink.length();
        }
        Assert.assertTrue(count == 2);

    }

    private void checkPDFtags(byte[] data) throws IOException {
        try (PDDocument document = Loader.loadPDF(data)) {
            PDStructureTreeRoot structureTreeRoot = document.getDocumentCatalog().getStructureTreeRoot();
            List<Object> kids = structureTreeRoot.getKids();

            for (Object kid : kids) {
                tagsTree(kid);
            }
        }
        // System.out.println(tagsTree.toString());
        // check for omit P/Div
        Assert.assertTrue(tagsTree.toString().contains("Document -> Part -> Sect -> P -> P -> P"));
        // check for Title role mapped to P (https://github.com/metanorma/xmlgraphics-fop/issues/73)
        Assert.assertTrue(tagsTree.toString().contains("Document -> Part -> Sect -> Title"));
    }

    private void tagsTree(Object element) {
        if (element instanceof PDStructureNode) {
            List<Object> kids = ((PDStructureNode) element).getKids();
            for (int i = 0; i < kids.size(); i++) {
                Object kid = kids.get(i);

                if (kid instanceof PDStructureElement) {
                    PDStructureElement pdStructureElement = (PDStructureElement) kid;

                    List<String> tree = new ArrayList<>();
                    tree.add(pdStructureElement.getStructureType());

                    PDStructureNode p = pdStructureElement.getParent();

                    while (p instanceof PDStructureElement) {
                        PDStructureElement se = (PDStructureElement)p;
                        tree.add(se.getStructureType());
                        p = se.getParent();
                    }
                    Collections.reverse(tree);
                    for (String item : tree) {
                        tagsTree.append(" -> " + item);
                    }
                    tagsTree.append("\n");

                    tagsTree(kid);
                }
            }
        }
    }


}
