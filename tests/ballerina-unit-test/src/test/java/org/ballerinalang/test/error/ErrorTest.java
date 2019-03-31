/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.error;

import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for Error.
 *
 * @since 0.985.0
 */
public class ErrorTest {

    private CompileResult compileResult;
    private CompileResult negativeCompileResult;

    private static final String ERROR1 = "error1";
    private static final String ERROR2 = "error2";
    private static final String ERROR3 = "error3";
    private static final String EMPTY_CURLY_BRACE = "{}";
    private static final String CONST_ERROR_REASON = "reason one";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/error/error_test.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/error/error_test_negative.bal");
    }

    @Test
    public void errorConstructReasonTest() {
        BValue[] returns = BRunUtil.invoke(compileResult, "errorConstructReasonTest");

        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).reason, ERROR1);
        Assert.assertEquals(((BError) returns[0]).details.stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertEquals(((BError) returns[1]).reason, ERROR2);
        Assert.assertEquals(((BError) returns[1]).details.stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertTrue(returns[2] instanceof BError);
        Assert.assertEquals(((BError) returns[2]).reason, ERROR3);
        Assert.assertEquals(((BError) returns[2]).details.stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertEquals(returns[3].stringValue(), ERROR1);
        Assert.assertEquals(returns[4].stringValue(), ERROR2);
        Assert.assertEquals(returns[5].stringValue(), ERROR3);
    }

    @Test
    public void errorConstructDetailTest() {
        BValue[] returns = BRunUtil.invoke(compileResult, "errorConstructDetailTest");
        String detail1 = "{\"message\":\"msg1\"}";
        String detail2 = "{\"message\":\"msg2\"}";
        String detail3 = "{\"message\":\"msg3\"}";
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).reason, ERROR1);
        Assert.assertEquals(((BError) returns[0]).details.stringValue().trim(), detail1);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertEquals(((BError) returns[1]).reason, ERROR2);
        Assert.assertEquals(((BError) returns[1]).details.stringValue().trim(), detail2);
        Assert.assertTrue(returns[2] instanceof BError);
        Assert.assertEquals(((BError) returns[2]).reason, ERROR3);
        Assert.assertEquals(((BError) returns[2]).details.stringValue().trim(), detail3);
        Assert.assertEquals(returns[3].stringValue().trim(), detail1);
        Assert.assertEquals(returns[4].stringValue().trim(), detail2);
        Assert.assertEquals(returns[5].stringValue().trim(), detail3);
    }

    @Test
    public void errorPanicTest() {
        // Case without panic
        BValue[] args = new BValue[] { new BInteger(10) };
        BValue[] returns = BRunUtil.invoke(compileResult, "errorPanicTest", args);
        Assert.assertEquals(returns[0].stringValue(), "done");

        // Now panic
        args = new BValue[] { new BInteger(15) };
        Exception expectedException = null;
        try {
            BRunUtil.invoke(compileResult, "errorPanicTest", args);
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: largeNumber {\"message\":\"large number\"}\n" +
                "\tat errorPanicCallee(error_test.bal:36)\n" +
                "\t   errorPanicTest(error_test.bal:30)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void errorTrapTest() {
        // Case without panic
        BValue[] args = new BValue[] { new BInteger(10) };
        BValue[] returns = BRunUtil.invoke(compileResult, "errorTrapTest", args);
        Assert.assertEquals(returns[0].stringValue(), "done");

        // Now panic
        args = new BValue[] { new BInteger(15) };
        returns = BRunUtil.invoke(compileResult, "errorTrapTest", args);
        String result = "largeNumber {\"message\":\"large number\"}";
        Assert.assertEquals(returns[0].stringValue(), result.trim());
    }

    @Test
    public void customErrorDetailsTest() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCustomErrorDetails");
        Assert.assertEquals(returns[0].stringValue(), "trxErr {message:\"\", cause:(), data:\"test\"}");
        Assert.assertEquals(((BError) returns[0]).details.getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(((BError) returns[0]).details.getType().getName(), "TrxErrorData");
    }

    @Test
    public void testCustomErrorDetails2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCustomErrorDetails2");
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    @Test
    public void testErrorWithErrorConstructor() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorWithErrorConstructor");
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    @Test
    public void testGetCallStack() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getCallStackTest");
        Assert.assertEquals(returns[0].stringValue(), "{callableName:\"getCallStack\", " +
                "moduleName:\"ballerina/runtime\", fileName:\"<native>\", lineNumber:0}");
    }

    @Test
    public void testConsecutiveTraps() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConsecutiveTraps");
        Assert.assertEquals(returns[0].stringValue(), "Error");
        Assert.assertEquals(returns[1].stringValue(), "Error");
    }

    @Test
    public void testOneLinePanic() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOneLinePanic");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray array = (BValueArray) returns[0];
        Assert.assertEquals(array.getString(0), "Error1");
        Assert.assertEquals(array.getString(1), "Error2");
        Assert.assertEquals(array.getString(2), "Something Went Wrong");
        Assert.assertEquals(array.getString(3), "Error3");
        Assert.assertEquals(array.getString(4), "Something Went Wrong");
        Assert.assertEquals(array.getString(5), "1");
    }

    @Test
    public void testGenericErrorWithDetailRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGenericErrorWithDetailRecord");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "userDefTypeAsReasonTests")
    public void testErrorWithUserDefinedReasonType(String testFunction) {
        BValue[] returns = BRunUtil.invoke(compileResult, testFunction);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).reason, CONST_ERROR_REASON);
    }

    @Test(dataProvider = "constAsReasonTests")
    public void testErrorWithConstantAsReason(String testFunction) {
        BValue[] returns = BRunUtil.invoke(compileResult, testFunction);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).reason, CONST_ERROR_REASON);
        Assert.assertEquals(((BMap) ((BError) returns[0]).details).get("message").stringValue(),
                            "error detail message");
    }

    @Test
    public void testCustomErrorWithMappingOfSelf() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCustomErrorWithMappingOfSelf");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testErrorNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 10);
        BAssertUtil.validateError(negativeCompileResult, 0,
                                  "incompatible types: expected 'reason one|reason two', found 'string'", 26, 31);
        BAssertUtil.validateError(negativeCompileResult, 1,
                                  "incompatible types: expected 'reason one', found 'reason two'", 31, 31);
        BAssertUtil.validateError(negativeCompileResult, 2,
                                  "invalid error reason type 'int', expected a subtype of 'string'", 40, 28);
        BAssertUtil.validateError(negativeCompileResult, 3, "invalid error detail type 'map', " +
                "expected a subtype of 'record { }' or 'map<anydata|error>'", 40, 33);
        BAssertUtil.validateError(negativeCompileResult, 4, "invalid error detail type 'boolean'," +
                " expected a subtype of 'record { }' or 'map<anydata|error>'", 41, 36);
        BAssertUtil.validateError(negativeCompileResult, 5,
                                  "invalid error reason type '1.0', expected a subtype of 'string'", 44, 7);
        BAssertUtil.validateError(negativeCompileResult, 6,
                                  "invalid error reason type 'boolean', expected a subtype of 'string'", 47, 11);
        BAssertUtil.validateError(negativeCompileResult, 7, "self referenced variable 'e3'", 53, 22);
        BAssertUtil.validateError(negativeCompileResult, 8, "self referenced variable 'e3'", 53, 42);
        BAssertUtil.validateError(negativeCompileResult, 9, "self referenced variable 'e4'", 54, 41);
    }

    @DataProvider(name = "userDefTypeAsReasonTests")
    public Object[][] userDefTypeAsReasonTests() {
        return new Object[][] {
                { "testErrorConstrWithConstForUserDefinedReasonType" },
                { "testErrorConstrWithLiteralForUserDefinedReasonType" }
        };
    }

    @DataProvider(name = "constAsReasonTests")
    public Object[][] constAsReasonTests() {
        return new Object[][] {
                { "testErrorConstrWithConstForConstReason" },
                { "testErrorConstrWithConstLiteralForConstReason" }
        };
    }
}
