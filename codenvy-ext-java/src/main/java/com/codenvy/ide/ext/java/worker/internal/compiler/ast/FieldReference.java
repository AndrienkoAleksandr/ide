/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package com.codenvy.ide.ext.java.worker.internal.compiler.ast;

import com.codenvy.ide.ext.java.worker.core.compiler.CharOperation;
import com.codenvy.ide.ext.java.worker.internal.compiler.ASTVisitor;
import com.codenvy.ide.ext.java.worker.internal.compiler.ClassFileConstants;
import com.codenvy.ide.ext.java.worker.internal.compiler.flow.FlowContext;
import com.codenvy.ide.ext.java.worker.internal.compiler.flow.FlowInfo;
import com.codenvy.ide.ext.java.worker.internal.compiler.impl.Constant;
import com.codenvy.ide.ext.java.worker.internal.compiler.lookup.*;

public class FieldReference extends Reference implements InvocationSite {

    public static final int READ = 0;

    public static final int WRITE = 1;

    public Expression receiver;

    public char[] token;

    public FieldBinding binding; // exact binding resulting from lookup

    public MethodBinding[] syntheticAccessors; // [0]=read accessor [1]=write accessor

    public long nameSourcePosition; //(start<<32)+end

    public TypeBinding actualReceiverType;

    public TypeBinding genericCast;

    public FieldReference(char[] source, long pos) {
        this.token = source;
        this.nameSourcePosition = pos;
        //by default the position are the one of the field (not true for super access)
        this.sourceStart = (int)(pos >>> 32);
        this.sourceEnd = (int)(pos & 0x00000000FFFFFFFFL);
        this.bits |= Binding.FIELD;

    }

    @Override
    public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo,
                                      Assignment assignment, boolean isCompound) {
        // compound assignment extra work
        if (isCompound) { // check the variable part is initialized if blank final
            if (this.binding.isBlankFinal() && this.receiver.isThis()
                && currentScope.needBlankFinalFieldInitializationCheck(this.binding)) {
                FlowInfo fieldInits =
                        flowContext.getInitsForFinalBlankInitializationCheck(this.binding.declaringClass.original(), flowInfo);
                if (!fieldInits.isDefinitelyAssigned(this.binding)) {
                    currentScope.problemReporter().uninitializedBlankFinalField(this.binding, this);
                    // we could improve error msg here telling "cannot use compound assignment on final blank field"
                }
            }
            manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
        }
        flowInfo =
                this.receiver.analyseCode(currentScope, flowContext, flowInfo, !this.binding.isStatic()).unconditionalInits();
        if (assignment.expression != null) {
            flowInfo = assignment.expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
        }
        manageSyntheticAccessIfNecessary(currentScope, flowInfo, false /*write-access*/);

        // check if assigning a final field
        if (this.binding.isFinal()) {
            // in a context where it can be assigned?
            if (this.binding.isBlankFinal() && !isCompound && this.receiver.isThis()
                && !(this.receiver instanceof QualifiedThisReference)
                && ((this.receiver.bits & ASTNode.ParenthesizedMASK) == 0) // (this).x is forbidden
                && currentScope.allowBlankFinalFieldAssignment(this.binding)) {
                if (flowInfo.isPotentiallyAssigned(this.binding)) {
                    currentScope.problemReporter().duplicateInitializationOfBlankFinalField(this.binding, this);
                } else {
                    flowContext.recordSettingFinal(this.binding, this, flowInfo);
                }
                flowInfo.markAsDefinitelyAssigned(this.binding);
            } else {
                // assigning a final field outside an initializer or constructor or wrong reference
                currentScope.problemReporter().cannotAssignToFinalField(this.binding, this);
            }
        }
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
        if (!this.binding.isStatic()) {
            if (this.receiver.isThis()) {
                currentScope.resetEnclosingMethodStaticFlag();
            }
        } else if (this.receiver.isThis()) {
            if ((this.receiver.bits & ASTNode.IsImplicitThis) == 0) {
                // explicit this, not allowed in static context
                currentScope.resetEnclosingMethodStaticFlag();
            }
        }
        return flowInfo;
    }

    @Override
    public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
        return analyseCode(currentScope, flowContext, flowInfo, true);
    }

    @Override
    public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo,
                                boolean valueRequired) {
        boolean nonStatic = !this.binding.isStatic();
        this.receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic);
        if (nonStatic) {
            this.receiver.checkNPE(currentScope, flowContext, flowInfo);
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
            if (this.receiver.isThis()) {
                currentScope.resetEnclosingMethodStaticFlag();
            }
        } else if (this.receiver.isThis()) {
            if ((this.receiver.bits & ASTNode.IsImplicitThis) == 0) {
                // explicit this receiver, not allowed in static context
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
                currentScope.resetEnclosingMethodStaticFlag();
            }
        }

        if (valueRequired || currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4) {
            manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
        }
        return flowInfo;
    }

    /**
     * @see com.codenvy.ide.ext.java.worker.internal.compiler.ast.Expression#computeConversion(com.codenvy.ide.ext.java.worker.internal.compiler.lookup.Scope,
     *      com.codenvy.ide.ext.java.worker.internal.compiler.lookup.TypeBinding, com.codenvy.ide.ext.java.worker.internal.compiler.lookup
     *      .TypeBinding)
     */
    @Override
    public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
        if (runtimeTimeType == null || compileTimeType == null) {
            return;
        }
        // set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
        if (this.binding != null && this.binding.isValidBinding()) {
            FieldBinding originalBinding = this.binding.original();
            TypeBinding originalType = originalBinding.type;
            // extra cast needed if field type is type variable
            if (originalType.leafComponentType().isTypeVariable()) {
                TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType()) ? compileTimeType
                                         // unboxing: checkcast before conversion
                                                                                                         : runtimeTimeType;
                this.genericCast = originalBinding.type.genericCast(targetType);
                if (this.genericCast instanceof ReferenceBinding) {
                    ReferenceBinding referenceCast = (ReferenceBinding)this.genericCast;
                    if (!referenceCast.canBeSeenBy(scope)) {
                        scope.problemReporter().invalidType(
                                this,
                                new ProblemReferenceBinding(CharOperation.splitOn('.', referenceCast.shortReadableName()),
                                                            referenceCast, ProblemReasons.NotVisible));
                    }
                }
            }
        }
        super.computeConversion(scope, runtimeTimeType, compileTimeType);
    }

    @Override
    public FieldBinding fieldBinding() {
        return this.binding;
    }

    @Override
    public void generateAssignment(BlockScope currentScope, Assignment assignment, boolean valueRequired) {
        FieldBinding codegenBinding = this.binding.original();
        this.receiver.generateCode(currentScope, !codegenBinding.isStatic());
        assignment.expression.generateCode(currentScope, true);
        fieldStore(currentScope, codegenBinding, this.syntheticAccessors == null ? null
                                                                                 : this.syntheticAccessors[FieldReference.WRITE],
                   this.actualReceiverType, this.receiver.isImplicitThis(),
                   valueRequired);
    }

    /**
     * Field reference code generation
     *
     * @param currentScope
     *         com.codenvy.ide.java.client.internal.compiler.lookup.BlockScope
     * @param codeStream
     *         com.codenvy.ide.java.client.internal.compiler.codegen.CodeStream
     * @param valueRequired
     *         boolean
     */
    @Override
    public void generateCode(BlockScope currentScope, boolean valueRequired) {
        if (this.constant != Constant.NotAConstant) {
            return;
        }
        FieldBinding codegenBinding = this.binding.original();
        boolean isStatic = codegenBinding.isStatic();
        boolean isThisReceiver = this.receiver instanceof ThisReference;
        Constant fieldConstant = codegenBinding.constant();
        if (fieldConstant != Constant.NotAConstant) {
            return;
        }
        if (valueRequired
            || (!isThisReceiver && currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
            || ((this.implicitConversion & TypeIds.UNBOXING) != 0) || (this.genericCast != null)) {
            this.receiver.generateCode(currentScope, !isStatic);
        }
    }

    @Override
    public void generateCompoundAssignment(BlockScope currentScope, Expression expression, int operator,
                                           int assignmentImplicitConversion, boolean valueRequired) {
        // check if compound assignment is the only usage of a private field
        reportOnlyUselesslyReadPrivateField(currentScope, this.binding, valueRequired);
        FieldBinding codegenBinding = this.binding.original();
        this.receiver.generateCode(currentScope, !codegenBinding.isStatic());
        switch (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK >> 4) {
            case T_JavaLangString:
            case T_JavaLangObject:
            case T_undefined:
                break;
            default:
                // generate the increment value (will by itself  be promoted to the operation value)
                if (expression != IntLiteral.One) { // prefix operation
                    expression.generateCode(currentScope, true);
                }
        }
        fieldStore(currentScope, codegenBinding, this.syntheticAccessors == null ? null
                                                                                 : this.syntheticAccessors[FieldReference.WRITE],
                   this.actualReceiverType, this.receiver.isImplicitThis(),
                   valueRequired);
        // no need for generic cast as value got dupped
    }

    @Override
    public void generatePostIncrement(BlockScope currentScope, CompoundAssignment postIncrement, boolean valueRequired) {
        // check if postIncrement is the only usage of a private field
        reportOnlyUselesslyReadPrivateField(currentScope, this.binding, valueRequired);
        FieldBinding codegenBinding = this.binding.original();
        this.receiver.generateCode(currentScope, !codegenBinding.isStatic());
        fieldStore(currentScope, codegenBinding, this.syntheticAccessors == null ? null
                                                                                 : this.syntheticAccessors[FieldReference.WRITE],
                   this.actualReceiverType, this.receiver.isImplicitThis(),
                   false);
    }

    /** @see com.codenvy.ide.ext.java.worker.internal.compiler.lookup.InvocationSite#genericTypeArguments() */
    @Override
    public TypeBinding[] genericTypeArguments() {
        return null;
    }

    @Override
    public boolean isSuperAccess() {
        return this.receiver.isSuper();
    }

    @Override
    public boolean isTypeAccess() {
        return this.receiver != null && this.receiver.isTypeReference();
    }

    /*
     * No need to emulate access to protected fields since not implicitly accessed
     */
    public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {
        if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0) {
            return;
        }

        // if field from parameterized type got found, use the original field at codegen time
        FieldBinding codegenBinding = this.binding.original();
        if (this.binding.isPrivate()) {
            if ((currentScope.enclosingSourceType() != codegenBinding.declaringClass)
                && this.binding.constant() == Constant.NotAConstant) {
                if (this.syntheticAccessors == null) {
                    this.syntheticAccessors = new MethodBinding[2];
                }
                this.syntheticAccessors[isReadAccess ? FieldReference.READ : FieldReference.WRITE] =
                        ((SourceTypeBinding)codegenBinding.declaringClass).addSyntheticMethod(codegenBinding, isReadAccess,
                                                                                              false /* not super ref in remote type*/);
                currentScope.problemReporter().needToEmulateFieldAccess(codegenBinding, this, isReadAccess);
                return;
            }
        } else if (this.receiver instanceof QualifiedSuperReference) { // qualified super
            // qualified super need emulation always
            SourceTypeBinding destinationType =
                    (SourceTypeBinding)(((QualifiedSuperReference)this.receiver).currentCompatibleType);
            if (this.syntheticAccessors == null) {
                this.syntheticAccessors = new MethodBinding[2];
            }
            this.syntheticAccessors[isReadAccess ? FieldReference.READ : FieldReference.WRITE] =
                    destinationType.addSyntheticMethod(codegenBinding, isReadAccess, isSuperAccess());
            currentScope.problemReporter().needToEmulateFieldAccess(codegenBinding, this, isReadAccess);
            return;

        } else if (this.binding.isProtected()) {
            SourceTypeBinding enclosingSourceType;
            if (((this.bits & ASTNode.DepthMASK) != 0)
                && this.binding.declaringClass.getPackage() != (enclosingSourceType = currentScope.enclosingSourceType())
                    .getPackage()) {

                SourceTypeBinding currentCompatibleType =
                        (SourceTypeBinding)enclosingSourceType
                                .enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
                if (this.syntheticAccessors == null) {
                    this.syntheticAccessors = new MethodBinding[2];
                }
                this.syntheticAccessors[isReadAccess ? FieldReference.READ : FieldReference.WRITE] =
                        currentCompatibleType.addSyntheticMethod(codegenBinding, isReadAccess, isSuperAccess());
                currentScope.problemReporter().needToEmulateFieldAccess(codegenBinding, this, isReadAccess);
                return;
            }
        }
    }

    @Override
    public int nullStatus(FlowInfo flowInfo) {
        return FlowInfo.UNKNOWN;
    }

    @Override
    public Constant optimizedBooleanConstant() {
        switch (this.resolvedType.id) {
            case T_boolean:
            case T_JavaLangBoolean:
                return this.constant != Constant.NotAConstant ? this.constant : this.binding.constant();
            default:
                return Constant.NotAConstant;
        }
    }

    /** @see com.codenvy.ide.ext.java.worker.internal.compiler.ast.Expression#postConversionType(Scope) */
    @Override
    public TypeBinding postConversionType(Scope scope) {
        TypeBinding convertedType = this.resolvedType;
        if (this.genericCast != null) {
            convertedType = this.genericCast;
        }
        int runtimeType = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
        switch (runtimeType) {
            case T_boolean:
                convertedType = TypeBinding.BOOLEAN;
                break;
            case T_byte:
                convertedType = TypeBinding.BYTE;
                break;
            case T_short:
                convertedType = TypeBinding.SHORT;
                break;
            case T_char:
                convertedType = TypeBinding.CHAR;
                break;
            case T_int:
                convertedType = TypeBinding.INT;
                break;
            case T_float:
                convertedType = TypeBinding.FLOAT;
                break;
            case T_long:
                convertedType = TypeBinding.LONG;
                break;
            case T_double:
                convertedType = TypeBinding.DOUBLE;
                break;
            default:
        }
        if ((this.implicitConversion & TypeIds.BOXING) != 0) {
            convertedType = scope.environment().computeBoxingType(convertedType);
        }
        return convertedType;
    }

    @Override
    public StringBuffer printExpression(int indent, StringBuffer output) {
        return this.receiver.printExpression(0, output).append('.').append(this.token);
    }

    @Override
    public TypeBinding resolveType(BlockScope scope) {
        // Answer the signature type of the field.
        // constants are propaged when the field is final
        // and initialized with a (compile time) constant

        //always ignore receiver cast, since may affect constant pool reference
        boolean receiverCast = false;
        if (this.receiver instanceof CastExpression) {
            this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
            receiverCast = true;
        }
        this.actualReceiverType = this.receiver.resolveType(scope);
        if (this.actualReceiverType == null) {
            this.constant = Constant.NotAConstant;
            return null;
        }
        if (receiverCast) {
            // due to change of declaring class with receiver type, only identity cast should be notified
            if (((CastExpression)this.receiver).expression.resolvedType == this.actualReceiverType) {
                scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);
            }
        }
        // the case receiverType.isArrayType and token = 'length' is handled by the scope API
        FieldBinding fieldBinding = this.binding = scope.getField(this.actualReceiverType, this.token, this);
        if (!fieldBinding.isValidBinding()) {
            this.constant = Constant.NotAConstant;
            if (this.receiver.resolvedType instanceof ProblemReferenceBinding) {
                // problem already got signaled on receiver, do not report secondary problem
                return null;
            }
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=245007 avoid secondary errors in case of
            // missing super type for anonymous classes ...
            ReferenceBinding declaringClass = fieldBinding.declaringClass;
            boolean avoidSecondary =
                    declaringClass != null && declaringClass.isAnonymousType()
                    && declaringClass.superclass() instanceof MissingTypeBinding;
            if (!avoidSecondary) {
                scope.problemReporter().invalidField(this, this.actualReceiverType);
            }
            if (fieldBinding instanceof ProblemFieldBinding) {
                ProblemFieldBinding problemFieldBinding = (ProblemFieldBinding)fieldBinding;
                FieldBinding closestMatch = problemFieldBinding.closestMatch;
                switch (problemFieldBinding.problemId()) {
                    case ProblemReasons.InheritedNameHidesEnclosingName:
                    case ProblemReasons.NotVisible:
                    case ProblemReasons.NonStaticReferenceInConstructorInvocation:
                    case ProblemReasons.NonStaticReferenceInStaticContext:
                        if (closestMatch != null) {
                            fieldBinding = closestMatch;
                        }
                }
            }
            if (!fieldBinding.isValidBinding()) {
                return null;
            }
        }
        // handle indirect inheritance thru variable secondary bound
        // receiver may receive generic cast, as part of implicit conversion
        TypeBinding oldReceiverType = this.actualReceiverType;
        this.actualReceiverType = this.actualReceiverType.getErasureCompatibleType(fieldBinding.declaringClass);
        this.receiver.computeConversion(scope, this.actualReceiverType, this.actualReceiverType);
        if (this.actualReceiverType != oldReceiverType
            && this.receiver.postConversionType(scope) !=
               this.actualReceiverType) { // record need for explicit cast at codegen since receiver could not handle it
            this.bits |= NeedReceiverGenericCast;
        }
        if (isFieldUseDeprecated(fieldBinding, scope, this.bits)) {
            scope.problemReporter().deprecatedField(fieldBinding, this);
        }
        boolean isImplicitThisRcv = this.receiver.isImplicitThis();
        this.constant = isImplicitThisRcv ? fieldBinding.constant() : Constant.NotAConstant;
        if (fieldBinding.isStatic()) {
            // static field accessed through receiver? legal but unoptimal (optional warning)
            if (!(isImplicitThisRcv ||
                  (this.receiver instanceof NameReference && (((NameReference)this.receiver).bits & Binding.TYPE) != 0))) {
                scope.problemReporter().nonStaticAccessToStaticField(this, fieldBinding);
            }
            ReferenceBinding declaringClass = this.binding.declaringClass;
            if (!isImplicitThisRcv && declaringClass != this.actualReceiverType && declaringClass.canBeSeenBy(scope)) {
                scope.problemReporter().indirectAccessToStaticField(this, fieldBinding);
            }
            // check if accessing enum static field in initializer
            if (declaringClass.isEnum()) {
                MethodScope methodScope = scope.methodScope();
                SourceTypeBinding sourceType = scope.enclosingSourceType();
                if (this.constant == Constant.NotAConstant && !methodScope.isStatic
                    && (sourceType == declaringClass || sourceType.superclass == declaringClass) // enum constant body
                    && methodScope.isInsideInitializerOrConstructor()) {
                    scope.problemReporter().enumStaticFieldUsedDuringInitialization(this.binding, this);
                }
            }
        }
        TypeBinding fieldType = fieldBinding.type;
        if (fieldType != null) {
            if ((this.bits & ASTNode.IsStrictlyAssigned) == 0) {
                fieldType = fieldType.capture(scope, this.sourceEnd); // perform capture conversion if read access
            }
            this.resolvedType = fieldType;
            if ((fieldType.tagBits & TagBits.HasMissingType) != 0) {
                scope.problemReporter().invalidType(this, fieldType);
                return null;
            }
        }
        return fieldType;
    }

    @Override
    public void setActualReceiverType(ReferenceBinding receiverType) {
        this.actualReceiverType = receiverType;
    }

    @Override
    public void setDepth(int depth) {
        this.bits &= ~ASTNode.DepthMASK; // flush previous depth if any
        if (depth > 0) {
            this.bits |= (depth & 0xFF) << ASTNode.DepthSHIFT; // encoded on 8 bits
        }
    }

    @Override
    public void setFieldIndex(int index) {
        // ignored
    }

    @Override
    public void traverse(ASTVisitor visitor, BlockScope scope) {
        if (visitor.visit(this, scope)) {
            this.receiver.traverse(visitor, scope);
        }
        visitor.endVisit(this, scope);
    }
}
