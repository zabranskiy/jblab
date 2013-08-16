package com.sdc.kotlin;

import com.sdc.ast.controlflow.Assignment;
import com.sdc.ast.controlflow.InstanceInvocation;
import com.sdc.ast.controlflow.Invocation;
import com.sdc.ast.controlflow.Statement;
import com.sdc.ast.expressions.BinaryExpression;
import com.sdc.ast.expressions.Expression;
import com.sdc.ast.expressions.identifiers.Variable;
import com.sdc.cfg.constructions.ConditionalBlock;
import com.sdc.cfg.constructions.Construction;
import com.sdc.cfg.constructions.ElementaryBlock;
import com.sdc.cfg.constructions.When;
import com.sdc.cfg.nodes.Node;
import com.sdc.util.ConstructionBuilder;
import com.sdc.util.DominatorTreeGenerator;

import java.util.List;

public class KotlinConstructionBuilder extends ConstructionBuilder {
    public KotlinConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
        super(myNodes, gen);
    }

    @Override
    protected ConstructionBuilder createConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
        return new KotlinConstructionBuilder(myNodes, gen);
    }

    @Override
    public Construction build() {
        Construction generalConstruction = super.build();

        extractNullSafeFunctionCall(generalConstruction);
//        extractWhen(generalConstruction);

        return generalConstruction;
    }

    private boolean extractNullSafeFunctionCall(Construction baseConstruction) {
        final Construction throwNpeIf = baseConstruction.getNextConstruction();

        if (throwNpeIf != null && throwNpeIf instanceof ConditionalBlock) {
            final Construction throwNpeThenBlock = ((ConditionalBlock) throwNpeIf).getThenBlock();

            if (throwNpeThenBlock instanceof ElementaryBlock) {
                final ElementaryBlock throwNpeBlock = (ElementaryBlock) throwNpeThenBlock;

                if (throwNpeBlock.getStatements().size() == 1) {
                    final Statement throwNpeStatement = throwNpeBlock.getStatements().get(0);

                    if (throwNpeStatement instanceof Invocation) {
                        final com.sdc.ast.expressions.Invocation throwNpeInvocation = (com.sdc.ast.expressions.Invocation) (((Invocation) throwNpeStatement).toExpression());

                        if (throwNpeInvocation.getFunction().equals("Intrinsics.throwNpe")) {
                            if (throwNpeIf.getNextConstruction() != null && throwNpeIf.getNextConstruction() instanceof ElementaryBlock) {
                                final ElementaryBlock blockAfterThrowNpeIf = (ElementaryBlock) throwNpeIf.getNextConstruction();

                                if (!blockAfterThrowNpeIf.getStatements().isEmpty()) {
                                    final Statement nullSafeInvocation = blockAfterThrowNpeIf.getStatements().get(0);

                                    if (nullSafeInvocation instanceof InstanceInvocation) {
                                        ((com.sdc.ast.expressions.InstanceInvocation) ((InstanceInvocation) nullSafeInvocation).toExpression()).setIsNotNullCheckedCall(true);
                                        baseConstruction.setNextConstruction(blockAfterThrowNpeIf);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean extractWhen(Construction baseConstruction) {
        final Construction whenStartConstruction = baseConstruction.getNextConstruction();

        if (baseConstruction instanceof ElementaryBlock && whenStartConstruction != null && whenStartConstruction instanceof ConditionalBlock) {
            final ElementaryBlock blockBeforeWhenConstruction = (ElementaryBlock) baseConstruction;

            if (!blockBeforeWhenConstruction.getStatements().isEmpty()) {
                final Statement variableDeclarationForWhen = blockBeforeWhenConstruction.getLastStatement();

                if (variableDeclarationForWhen instanceof Assignment) {
                    final Assignment variableAssignmentForWhen = (Assignment) variableDeclarationForWhen;

                    if (variableAssignmentForWhen.getLeft() instanceof Variable) {
                        final int whenVariableIndex = ((Variable) variableAssignmentForWhen.getLeft()).getIndex();

                        When extractedWhen = extractWhen(whenStartConstruction, whenVariableIndex);
                        extractedWhen.setCondition(variableAssignmentForWhen.getRight());

                        blockBeforeWhenConstruction.removeLastStatement();
                        blockBeforeWhenConstruction.setNextConstruction(extractedWhen);
                        extractedWhen.setNextConstruction(whenStartConstruction.getNextConstruction());

                        return true;
                    }
                }
            }
        }

        return false;
    }

    private When extractWhen(final Construction construction, final int variableIndex) {
        if (construction == null || !(construction instanceof ConditionalBlock)) {
            return null;
        }

        When result = null;
        final ConditionalBlock whenCondition = (ConditionalBlock) (construction);
        final Expression condition = whenCondition.getCondition();

        if (condition instanceof BinaryExpression) {
            final BinaryExpression conditionAsBinaryExpression = (BinaryExpression) condition;

            if (conditionAsBinaryExpression.getLeft() instanceof Variable) {
                final Variable conditionVariable = (Variable) conditionAsBinaryExpression.getLeft();

                if (conditionVariable.getIndex() == variableIndex && whenCondition.getElseBlock() instanceof ElementaryBlock) {
                    result = extractWhen(whenCondition.getElseBlock().getNextConstruction(), variableIndex);

                    if (result != null) {
                        result.addCase(conditionAsBinaryExpression.getRight(), whenCondition.getThenBlock());
                    } else {
                        result = new When(null);
                        result.addCase(conditionAsBinaryExpression.getRight(), whenCondition.getThenBlock());
                        result.setDefaultCase(whenCondition.getElseBlock());
                    }
                }
            }
        }

        return result;
    }
}
