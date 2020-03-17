/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.query;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test simple query expression for XMLs.
 *
 * @since 1.2.0
 */
public class XMLQueryExpressionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/xml-query-expression.bal");
    }

    @Test(description = "Test simple query expression for XMLs")
    public void testSimpleQueryExprForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<name>Sherlock Holmes</name><name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression for XMLs")
    public void testSimpleQueryExprForXML2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<book>the book</book>bit of text✂✅");
    }

    @Test(description = "Test simple query expression for XMLs")
    public void testSimpleQueryExprForXML3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<title lang=\"en\">Everyday Italian</title><title lang=\"en\">Harry Potter</title>" +
                        "<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>");
    }
}
