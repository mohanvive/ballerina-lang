/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStringSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public class SymbolTable {

    private static final CompilerContext.Key<SymbolTable> SYM_TABLE_KEY =
            new CompilerContext.Key<>();

    public static final PackageID TRANSACTION = new PackageID(Names.BUILTIN_ORG, Names.TRANSACTION_PACKAGE,
            Names.EMPTY);

    public static final Integer BBYTE_MIN_VALUE = 0;
    public static final Integer BBYTE_MAX_VALUE = 255;
    public static final Integer SIGNED32_MAX_VALUE = 2147483647;
    public static final Integer SIGNED32_MIN_VALUE = -2147483648;
    public static final Integer SIGNED16_MAX_VALUE = 32767;
    public static final Integer SIGNED16_MIN_VALUE = -32768;
    public static final Integer SIGNED8_MAX_VALUE = 127;
    public static final Integer SIGNED8_MIN_VALUE = -128;
    public static final Long UNSIGNED32_MAX_VALUE = 4294967295L;
    public static final Integer UNSIGNED16_MAX_VALUE = 65535;
    public static final Integer UNSIGNED8_MAX_VALUE = 255;

    public final BLangPackage rootPkgNode;
    public final BPackageSymbol rootPkgSymbol;
    public final BSymbol notFoundSymbol;
    public final BSymbol invalidUsageSymbol;
    public final Scope rootScope;

    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nilType = new BNilType();
    public final BType intType = new BType(TypeTags.INT, null);
    public final BType byteType = new BType(TypeTags.BYTE, null);
    public final BType floatType = new BType(TypeTags.FLOAT, null);
    public final BType decimalType = new BType(TypeTags.DECIMAL, null);
    public final BType stringType = new BType(TypeTags.STRING, null);
    public final BType booleanType = new BType(TypeTags.BOOLEAN, null);
    public final BType jsonType = new BJSONType(TypeTags.JSON, null);
    public final BType xmlType = new BXMLType(TypeTags.XML, null);
    public final BType tableType = new BTableType(TypeTags.TABLE, noType, null);
    public final BType anyType = new BAnyType(TypeTags.ANY, null);
    public final BType anydataType = new BAnydataType(TypeTags.ANYDATA, null);
    public final BType mapType = new BMapType(TypeTags.MAP, anyType, null);
    public final BType mapStringType = new BMapType(TypeTags.MAP, stringType, null);
    public final BType mapAnydataType = new BMapType(TypeTags.MAP, anydataType, null);
    public final BType mapJsonType = new BMapType(TypeTags.MAP, jsonType, null);
    public final BType futureType = new BFutureType(TypeTags.FUTURE, nilType, null);
    public final BType arrayType = new BArrayType(anyType);
    public final BArrayType arrayStringType = new BArrayType(stringType);
    public final BArrayType arrayAnydataType = new BArrayType(anydataType);
    public final BArrayType arrayJsonType = new BArrayType(jsonType);
    public final BType tupleType = new BTupleType(Lists.of(noType));
    public final BType recordType = new BRecordType(null);
    public final BType anyServiceType = new BServiceType(null);
    public final BType handleType = new BHandleType(TypeTags.HANDLE, null);
    public final BType typeDesc = new BTypedescType(this.anyType, null);

    public final BType semanticError = new BType(TypeTags.SEMANTIC_ERROR, null);

    public BType streamType = new BStreamType(TypeTags.STREAM, anydataType, null, null);
    public BErrorType errorType;
    public BRecordType detailType;
    public BConstructorSymbol errorConstructor;
    public BUnionType anyOrErrorType;
    public BUnionType pureType;
    public BUnionType errorOrNilType;
    public BFiniteType trueType;
    public BObjectType intRangeType;
    public BMapType mapAllType;

    // builtin subtypes
    public final BIntSubType signed32IntType = new BIntSubType(TypeTags.SIGNED32_INT, Names.SIGNED32);
    public final BIntSubType signed16IntType = new BIntSubType(TypeTags.SIGNED16_INT, Names.SIGNED16);
    public final BIntSubType signed8IntType = new BIntSubType(TypeTags.SIGNED8_INT, Names.SIGNED8);
    public final BIntSubType unsigned32IntType = new BIntSubType(TypeTags.UNSIGNED32_INT, Names.UNSIGNED32);
    public final BIntSubType unsigned16IntType = new BIntSubType(TypeTags.UNSIGNED16_INT, Names.UNSIGNED16);
    public final BIntSubType unsigned8IntType = new BIntSubType(TypeTags.UNSIGNED8_INT, Names.UNSIGNED8);
    public final BStringSubType charStringType = new BStringSubType(TypeTags.CHAR_STRING, Names.CHAR);

    public BPackageSymbol langInternalModuleSymbol;
    public BPackageSymbol langAnnotationModuleSymbol;
    public BPackageSymbol langArrayModuleSymbol;
    public BPackageSymbol langDecimalModuleSymbol;
    public BPackageSymbol langErrorModuleSymbol;
    public BPackageSymbol langFloatModuleSymbol;
    public BPackageSymbol langFutureModuleSymbol;
    public BPackageSymbol langIntModuleSymbol;
    public BPackageSymbol langMapModuleSymbol;
    public BPackageSymbol langObjectModuleSymbol;
    public BPackageSymbol langStreamModuleSymbol;
    public BPackageSymbol langStringModuleSymbol;
    public BPackageSymbol langTableModuleSymbol;
    public BPackageSymbol langTypedescModuleSymbol;
    public BPackageSymbol langValueModuleSymbol;
    public BPackageSymbol langXmlModuleSymbol;
    public BPackageSymbol langBooleanModuleSymbol;

    private Names names;
    public Map<BPackageSymbol, SymbolEnv> pkgEnvMap = new HashMap<>();

    public static SymbolTable getInstance(CompilerContext context) {
        SymbolTable symTable = context.get(SYM_TABLE_KEY);
        if (symTable == null) {
            symTable = new SymbolTable(context);
        }

        return symTable;
    }

    private SymbolTable(CompilerContext context) {
        context.put(SYM_TABLE_KEY, this);

        this.names = Names.getInstance(context);

        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgSymbol = new BPackageSymbol(PackageID.ANNOTATIONS, null);
        this.rootPkgNode.pos = new DiagnosticPos(new BDiagnosticSource(rootPkgSymbol.pkgID, Names.EMPTY.value), 0, 0,
                0, 0);
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new Scope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;

        this.notFoundSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID,
                rootPkgSymbol.pkgID, noType, rootPkgSymbol);
        this.invalidUsageSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID, rootPkgSymbol.pkgID, noType,
                                              rootPkgSymbol);
        // Initialize built-in types in Ballerina
        initializeType(intType, TypeKind.INT.typeName());
        initializeType(byteType, TypeKind.BYTE.typeName());
        initializeType(floatType, TypeKind.FLOAT.typeName());
        initializeType(decimalType, TypeKind.DECIMAL.typeName());
        initializeType(stringType, TypeKind.STRING.typeName());
        initializeType(booleanType, TypeKind.BOOLEAN.typeName());
        initializeType(jsonType, TypeKind.JSON.typeName());
        initializeType(xmlType, TypeKind.XML.typeName());
        initializeType(tableType, TypeKind.TABLE.typeName());
        initializeType(streamType, TypeKind.STREAM.typeName());
        initializeType(mapType, TypeKind.MAP.typeName());
        initializeType(mapStringType, TypeKind.MAP.typeName());
        initializeType(mapAnydataType, TypeKind.MAP.typeName());
        initializeType(futureType, TypeKind.FUTURE.typeName());
        initializeType(anyType, TypeKind.ANY.typeName());
        initializeType(anydataType, TypeKind.ANYDATA.typeName());
        initializeType(nilType, TypeKind.NIL.typeName());
        initializeType(anyServiceType, TypeKind.SERVICE.typeName());
        initializeType(handleType, TypeKind.HANDLE.typeName());
        initializeType(typeDesc, TypeKind.TYPEDESC.typeName());

        // Define subtypes
        initializeTSymbol(signed32IntType, Names.SIGNED32, PackageID.INT);
        initializeTSymbol(signed16IntType, Names.SIGNED16, PackageID.INT);
        initializeTSymbol(signed8IntType, Names.SIGNED8, PackageID.INT);
        initializeTSymbol(unsigned32IntType, Names.UNSIGNED32, PackageID.INT);
        initializeTSymbol(unsigned16IntType, Names.UNSIGNED16, PackageID.INT);
        initializeTSymbol(unsigned8IntType, Names.UNSIGNED8, PackageID.INT);
        initializeTSymbol(charStringType, Names.CHAR, PackageID.STRING);

        BLangLiteral trueLiteral = new BLangLiteral();
        trueLiteral.type = this.booleanType;
        trueLiteral.value = Boolean.TRUE;

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, Flags.PUBLIC,
                names.fromString("$anonType$TRUE"),
                rootPkgNode.packageID, null, rootPkgNode.symbol.owner);
        this.trueType = new BFiniteType(finiteTypeSymbol, new HashSet<BLangExpression>() {{
            add(trueLiteral);
        }});
    }

    public BType getTypeFromTag(int tag) {
        switch (tag) {
            case TypeTags.INT:
                return intType;
            case TypeTags.BYTE:
                return byteType;
            case TypeTags.FLOAT:
                return floatType;
            case TypeTags.DECIMAL:
                return decimalType;
            case TypeTags.STRING:
                return stringType;
            case TypeTags.BOOLEAN:
                return booleanType;
            case TypeTags.JSON:
                return jsonType;
            case TypeTags.XML:
                return xmlType;
            case TypeTags.TABLE:
                return tableType;
            case TypeTags.STREAM:
                return streamType;
            case TypeTags.NIL:
                return nilType;
            case TypeTags.ERROR:
                return errorType;
            case TypeTags.SIGNED32_INT:
                return signed32IntType;
            case TypeTags.SIGNED16_INT:
                return signed16IntType;
            case TypeTags.SIGNED8_INT:
                return signed8IntType;
            case TypeTags.UNSIGNED32_INT:
                return unsigned32IntType;
            case TypeTags.UNSIGNED16_INT:
                return unsigned16IntType;
            case TypeTags.UNSIGNED8_INT:
                return unsigned8IntType;
            case TypeTags.CHAR_STRING:
                return charStringType;
            default:
                return semanticError;
        }
    }

    public BType getLangLibSubType(String name) {
        // Assuming subtype names are unique across LangLib
        switch (name) {
            case Names.STRING_SIGNED32:
                return this.signed32IntType;
            case Names.STRING_SIGNED16:
                return this.signed16IntType;
            case Names.STRING_SIGNED8:
                return this.signed8IntType;
            case Names.STRING_UNSIGNED32:
                return this.unsigned32IntType;
            case Names.STRING_UNSIGNED16:
                return this.unsigned16IntType;
            case Names.STRING_UNSIGNED8:
                return this.unsigned8IntType;
            case Names.STRING_CHAR:
                return this.charStringType;
        }
        throw new IllegalStateException("LangLib Subtype not found: " + name);
    }

    private void initializeType(BType type, String name) {
        initializeType(type, names.fromString(name));
    }

    private void initializeType(BType type, Name name) {
        defineType(type, new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, name, rootPkgSymbol.pkgID, type, rootPkgSymbol));
    }

    private void initializeTSymbol(BType type, Name name, PackageID packageID) {

        type.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, name, packageID, type, rootPkgSymbol);
    }

    private void defineType(BType type, BTypeSymbol tSymbol) {
        type.tsymbol = tSymbol;
        rootScope.define(tSymbol.name, tSymbol);
    }

    public void defineOperators() {
        // Binary arithmetic operators
        defineIntegerArithmeticOperations();
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, xmlType, stringType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, xmlType, charStringType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, stringType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, stringType, charStringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, stringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, charStringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.ADD, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.SUB, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.DIV, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.MUL, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.MOD, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, intType, decimalType, decimalType);

        defineIntegerBitwiseAndOperations();
        defineIntegerBitwiseOrOperations(OperatorKind.BITWISE_OR);
        defineIntegerBitwiseOrOperations(OperatorKind.BITWISE_XOR);
        defineIntegerLeftShiftOperations();
        defineIntegerRightShiftOperations(OperatorKind.BITWISE_RIGHT_SHIFT);
        defineIntegerRightShiftOperations(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT);

        // Binary equality operators ==, !=
        defineBinaryOperator(OperatorKind.EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, nilType, booleanType);

        // Binary reference equality operators ===, !==
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, intType, booleanType);

        // Binary comparison operators <=, <, >=, >
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.AND, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.OR, booleanType, booleanType, booleanType);

        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType);
        defineUnaryOperator(OperatorKind.ADD, decimalType, decimalType);
        defineUnaryOperator(OperatorKind.ADD, intType, intType);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType);
        defineUnaryOperator(OperatorKind.SUB, decimalType, decimalType);
        defineUnaryOperator(OperatorKind.SUB, intType, intType);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, byteType, byteType);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, intType, intType);

    }

    private void defineIntegerArithmeticOperations() {

        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType,
                unsigned8IntType};
        for (BType lhs : intTypes) {
            for (BType rhs : intTypes) {
                defineBinaryOperator(OperatorKind.ADD, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.SUB, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.DIV, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.MUL, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.MOD, lhs, rhs, intType);
            }
        }
    }

    private void defineIntegerBitwiseAndOperations() {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        for (BType unsigned : unsignedIntTypes) {
            for (BType signed : signedIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsigned, signed, unsigned);
            }
        }

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            for (int j = 0; j < unsignedIntTypes.length; j++) {
                BType unsignedIntTypeLhs = unsignedIntTypes[i];
                BType unsignedIntTypeRhs = unsignedIntTypes[j];
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedIntTypeLhs, unsignedIntTypeRhs,
                                     i <= j ? unsignedIntTypeLhs : unsignedIntTypeRhs);
            }
        }

        for (BType signed : signedIntTypes) {
            for (BType unsigned : unsignedIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, signed, unsigned, unsigned);
            }
        }

        for (BType signedLhs : signedIntTypes) {
            for (BType signedRhs : signedIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, signedLhs, signedRhs, intType);
            }
        }
    }

    private void defineIntegerBitwiseOrOperations(OperatorKind orOpKind) {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        for (BType unsigned : unsignedIntTypes) {
            for (BType signed : signedIntTypes) {
                defineBinaryOperator(orOpKind, unsigned, signed, intType);
            }
        }

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            for (int j = 0; j < unsignedIntTypes.length; j++) {
                BType unsignedIntTypeLhs = unsignedIntTypes[i];
                BType unsignedIntTypeRhs = unsignedIntTypes[j];
                defineBinaryOperator(orOpKind, unsignedIntTypeLhs, unsignedIntTypeRhs,
                                     i <= j ? unsignedIntTypeLhs : unsignedIntTypeRhs);
            }
        }

        for (BType signed : signedIntTypes) {
            for (BType unsigned : unsignedIntTypes) {
                defineBinaryOperator(orOpKind, signed, unsigned, intType);
            }
        }

        for (BType signedLhs : signedIntTypes) {
            for (BType signedRhs : signedIntTypes) {
                defineBinaryOperator(orOpKind, signedLhs, signedRhs, intType);
            }
        }
    }

    private void defineIntegerLeftShiftOperations() {
        BType[] allIntTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};

        for (BType lhs : allIntTypes) {
            for (BType rhs : allIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, lhs, rhs, intType);
            }
        }
    }

    private void defineIntegerRightShiftOperations(OperatorKind rightShiftOpKind) {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        BType[] allIntTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};

        for (BType unsignedLhs : unsignedIntTypes) {
            for (BType intRhs : allIntTypes) {
                defineBinaryOperator(rightShiftOpKind, unsignedLhs, intRhs, unsignedLhs);
            }
        }

        for (BType signedLhs : signedIntTypes) {
            for (BType intRhs : allIntTypes) {
                defineBinaryOperator(rightShiftOpKind, signedLhs, intRhs, intType);
            }
        }
    }

    public void defineBinaryOperator(OperatorKind kind,
                                     BType lhsType,
                                     BType rhsType,
                                     BType retType) {

        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        defineOperator(names.fromString(kind.value()), paramTypes, retType);
    }

    private void defineUnaryOperator(OperatorKind kind,
                                     BType type,
                                     BType retType) {
        List<BType> paramTypes = Lists.of(type);
        defineOperator(names.fromString(kind.value()), paramTypes, retType);
    }

    private void defineOperator(Name name,
                                List<BType> paramTypes,
                                BType retType) {
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, rootPkgSymbol.pkgID, opType, rootPkgSymbol);
        rootScope.define(name, symbol);
    }

    public void loadDefaultXMLNamespace() {
        rootPkgSymbol.scope.define(Names.XML, new BXMLNSSymbol(Names.XML, "http://www.w3.org/XML/1998/namespace",
                this.rootPkgSymbol.pkgID, this.rootPkgSymbol));
    }
}
