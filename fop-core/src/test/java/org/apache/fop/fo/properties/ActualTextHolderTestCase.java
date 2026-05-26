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

package org.apache.fop.fo.properties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FONodeMocks;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.flow.InstreamForeignObject;

/**
 * Tests that the fox:actual-text property is correctly set on objects that support it.
 */
public class ActualTextHolderTestCase {

    private final String actualText = "actual text";

    @Test
    public void instreamForeignObjectHasActualText() throws FOPException {
        testActualTextGetter(new InstreamForeignObject(mockFONode()));
    }

    private FONode mockFONode() {
        FONode mockFONode = FONodeMocks.mockFONode();
        FOUserAgent mockFOUserAgent = mockFONode.getFOEventHandler().getUserAgent();
        when(mockFOUserAgent.isAccessibilityEnabled()).thenReturn(true);
        return mockFONode;
    }

    private void testActualTextGetter(AbstractGraphics g) throws FOPException {
        g.bind(mockPropertyList());
        assertEquals(actualText, g.getActualText());
    }

    private PropertyList mockPropertyList() throws PropertyException {
        PropertyList mockPropertyList = PropertyListMocks.mockPropertyList();
        Property mockActualText = mock(Property.class);
        when(mockActualText.getString()).thenReturn(actualText);
        when(mockPropertyList.get(Constants.PR_X_ACTUAL_TEXT)).thenReturn(mockActualText);
        return mockPropertyList;
    }

}
