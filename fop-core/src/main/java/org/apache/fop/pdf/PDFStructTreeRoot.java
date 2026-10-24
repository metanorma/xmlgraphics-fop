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

import java.io.Serializable;

/**
 * Class representing a PDF /StructTreeRoot dictionary.
 */
public class PDFStructTreeRoot extends StructureHierarchyMember implements Serializable {

    private static final long serialVersionUID = -4102820991473046724L;

    /**
     * Creates a new /StructTreeRoot dictionary.
     *
     * @param parentTree the value of the ParenTree entry
     */
    PDFStructTreeRoot(PDFParentTree parentTree) {
        put("Type", new PDFName("StructTreeRoot"));
        put("K", new PDFArray());
        put("ParentTree", parentTree);

        // A dictionary that shall map name objects designating names of structure types used in the document to a
        // name object designating the name of their approximate equivalents in the set of standard structure types
        // See 14.7.2 Structure hierarchy ISO 32000-2:2020
        // https://github.com/metanorma/mn2pdf/issues/425
        PDFDictionary dictRoleMap = new PDFDictionary();
        dictRoleMap.put("Title", new PDFName("P"));
        put("RoleMap", dictRoleMap);
    }

    /**
     * Returns the children element of this StructTreeRoot.
     *
     * @return the value of the K entry
     */
    public PDFArray getKids() {
        return (PDFArray)get("K");
    }

    /**
     * Adds the given object to the array of kids.
     *
     * @param kid an object to be added to the K entry
     */
    @Override
    public void addKid(PDFObject kid) {
        getKids().add(kid);
    }
}
