package com.sdc.util;

import com.sdc.ast.controlflow.Assignment;
import com.sdc.ast.controlflow.Increment;
import com.sdc.ast.controlflow.Statement;
import com.sdc.ast.expressions.ArrayLength;
import com.sdc.ast.expressions.InstanceInvocation;
import com.sdc.ast.expressions.UnaryExpression;
import com.sdc.ast.expressions.identifiers.Variable;
import com.sdc.cfg.constructions.*;
import com.sdc.cfg.nodes.DoWhile;
import com.sdc.cfg.nodes.Node;
import com.sdc.cfg.nodes.Switch;
import com.sdc.cfg.nodes.SwitchCase;

import java.util.ArrayList;
import java.util.List;

public class ConstructionBuilder {
    private List<Node> myNodes;
    private final DominatorTreeGenerator gen;
    private final int size;
    private final int[] domi;

    public ConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
        this.myNodes = myNodes;
        this.gen = gen;
        this.domi = gen.getDomi();
        this.size = myNodes.size();
    }

    protected ConstructionBuilder createConstructionBuilder(final List<Node> myNodes, final DominatorTreeGenerator gen) {
        return new ConstructionBuilder(myNodes, gen);
    }

    public Construction build() {
        return extractArrayForEach(extractIteratorForEach(extractFor(build(myNodes.get(0)))));
    }

    protected Construction extractArrayForEach(Construction baseConstruction) {
        final Construction forStartConstruction = baseConstruction.getNextConstruction();

        if (baseConstruction instanceof ElementaryBlock && forStartConstruction != null && forStartConstruction instanceof For) {
            final ElementaryBlock blockBeforeForConstruction = (ElementaryBlock) baseConstruction;

            if (!blockBeforeForConstruction.getStatements().isEmpty()) {
                final Statement containerDeclarationForFor = blockBeforeForConstruction.getBeforeLastStatement();
                final Statement containerLengthDeclaration = blockBeforeForConstruction.getLastStatement();

                if (containerDeclarationForFor instanceof Assignment && containerLengthDeclaration instanceof Assignment) {
                    final Assignment containerAssignmentForFor = (Assignment) containerDeclarationForFor;
                    final Assignment containerLengthAssignment = (Assignment) containerLengthDeclaration;

                    if ((containerAssignmentForFor.getLeft().getType().isArray())
                            && containerLengthAssignment.getRight() instanceof ArrayLength)
                    {
                        List<Variable> forEachVariables = new ArrayList<Variable>();
                        forEachVariables.add((Variable)((Assignment)((ElementaryBlock) ((For) forStartConstruction).getBody()).getFirstStatement()).getLeft());
                        ForEach forEach = new ForEach(forEachVariables, containerAssignmentForFor.getRight());

                        Construction body = ((For) forStartConstruction).getBody();

                        forEach.setBody(body);

                        ((ElementaryBlock) body).removeFirstStatement();
                        blockBeforeForConstruction.removeLastStatement();
                        blockBeforeForConstruction.removeLastStatement();

                        forEach.setNextConstruction(forStartConstruction.getNextConstruction());
                        blockBeforeForConstruction.setNextConstruction(forEach);
                    }
                }
            }
        }

        return baseConstruction;
    }

    private Construction extractIteratorForEach(Construction baseConstruction) {

        final Construction whileStartConstruction = baseConstruction.getNextConstruction();

        if (baseConstruction instanceof ElementaryBlock && whileStartConstruction != null && whileStartConstruction instanceof While) {
            final ElementaryBlock blockBeforeWhileConstruction = (ElementaryBlock) baseConstruction;

            if (!blockBeforeWhileConstruction.getStatements().isEmpty()) {
                final Statement variableDeclarationForWhile = blockBeforeWhileConstruction.getLastStatement();

                if (variableDeclarationForWhile instanceof Assignment) {
                    final Assignment variableAssignmentForWhen = (Assignment) variableDeclarationForWhile;

                     if (variableAssignmentForWhen.getLeft().getType().toString().toLowerCase().replace("?", "").trim().equals("iterator")
                             && ((While) whileStartConstruction).getCondition() instanceof UnaryExpression
                             && ((InstanceInvocation) ((UnaryExpression) ((While) whileStartConstruction).getCondition()).getOperand()).getFunction().equals("hasNext"))
                     {
                         List<Variable> forEachVariables = new ArrayList<Variable>();
                         forEachVariables.add((Variable)((Assignment)((ElementaryBlock) ((While) whileStartConstruction).getBody()).getFirstStatement()).getLeft());
                         ForEach forEach = new ForEach(forEachVariables, ((InstanceInvocation) variableAssignmentForWhen.getRight()).getInstance());

                         Construction body = ((While) whileStartConstruction).getBody();

                         forEach.setBody(body);
                         ((ElementaryBlock) body).removeFirstStatement();
                         blockBeforeWhileConstruction.removeLastStatement();

                         forEach.setNextConstruction(whileStartConstruction.getNextConstruction());
                         blockBeforeWhileConstruction.setNextConstruction(forEach);
                     }
                }
            }
        }

        return baseConstruction;
    }

    private Construction extractFor(Construction baseConstruction) {

        final Construction whileStartConstruction = baseConstruction.getNextConstruction();

        if (baseConstruction instanceof ElementaryBlock && whileStartConstruction != null && whileStartConstruction instanceof While) {
            final ElementaryBlock blockBeforeWhileConstruction = (ElementaryBlock) baseConstruction;

            if (!blockBeforeWhileConstruction.getStatements().isEmpty()) {
                final Statement variableDeclarationForWhile = blockBeforeWhileConstruction.getLastStatement();

                if (variableDeclarationForWhile instanceof Assignment) {
                    final Assignment variableAssignmentForWhen = (Assignment) variableDeclarationForWhile;

                    if (variableAssignmentForWhen.getLeft() instanceof Variable) {
                        final int forVariableIndex = ((Variable) variableAssignmentForWhen.getLeft()).getIndex();
                        Construction currentConstruction = ((While) whileStartConstruction).getBody();

                        while (currentConstruction.getNextConstruction() != null) {
                            currentConstruction = currentConstruction.getNextConstruction();
                        }
                        if (currentConstruction instanceof ElementaryBlock) {
                            ElementaryBlock forAfterThoughtBlock = (ElementaryBlock) currentConstruction;
                            final Statement forAfterThought = forAfterThoughtBlock.getLastStatement();

                            if (forAfterThought instanceof Assignment) {
                                final Assignment afterThoughtAssignment = (Assignment) forAfterThought;
                                if (afterThoughtAssignment.getLeft() instanceof Variable) {
                                    final int afterThoughtVariableIndex = ((Variable) afterThoughtAssignment.getLeft()).getIndex();
                                    if (afterThoughtVariableIndex == forVariableIndex) {
                                        For forConstruction = new For(variableAssignmentForWhen, ((While) whileStartConstruction).getCondition(), afterThoughtAssignment);
                                        forConstruction.setBody(((While) whileStartConstruction).getBody());

                                        blockBeforeWhileConstruction.removeLastStatement();
                                        blockBeforeWhileConstruction.setNextConstruction(forConstruction);
                                        forConstruction.setNextConstruction(whileStartConstruction.getNextConstruction());
                                        forAfterThoughtBlock.removeLastStatement();

                                        return blockBeforeWhileConstruction;
                                    }
                                }
                            } else if (forAfterThought instanceof Increment) {
                                final Increment afterThoughtIncrement = (Increment) forAfterThought;
                                final int afterThoughtVariableIndex = afterThoughtIncrement.getVariable().getIndex();

                                if (afterThoughtVariableIndex == forVariableIndex) {
                                    For forConstruction = new For(variableAssignmentForWhen, ((While) whileStartConstruction).getCondition(), afterThoughtIncrement);
                                    forConstruction.setBody(((While) whileStartConstruction).getBody());

                                    blockBeforeWhileConstruction.removeLastStatement();
                                    blockBeforeWhileConstruction.setNextConstruction(forConstruction);
                                    forConstruction.setNextConstruction(whileStartConstruction.getNextConstruction());
                                    forAfterThoughtBlock.removeLastStatement();

                                    return blockBeforeWhileConstruction;
                                }
                            }
                        }
                    }
                }

            }
        }
        return baseConstruction;
    }

    private Construction build(Node node) {
        final Node doWhileNode = checkForDoWhileLoop(node);

        if (doWhileNode == null) {
            Construction elementaryBlock = extractElementaryBlock(node);
            Construction currentConstruction = extractConstruction(node);

            if (node.getCondition() == null && !(node instanceof Switch)) {
                node.setConstruction(elementaryBlock);
            } else {
                node.setConstruction(currentConstruction);
            }

            if (currentConstruction == null && !node.getListOfTails().isEmpty()) {
                Node myNextNode = node.getListOfTails().get(0);
                if (checkForIndexOutOfBound(myNextNode)) {
                    node.setNextNode(myNextNode);
                    extractNextConstruction(elementaryBlock, node);
                    return elementaryBlock;
                }
            }

            elementaryBlock.setNextConstruction(currentConstruction);
            return elementaryBlock;
        } else {
            return extractDoWhile(node, doWhileNode);
        }
    }

    private boolean checkForIndexOutOfBound(Node node) {
        return getRelativeIndex(node.getIndex()) < size && getRelativeIndex(node.getIndex()) >= 0;
    }

    private Construction extractConstruction(Node node) {
        Construction result;

        result = extractConditionBlock(node);
        if (result != null) {
            return result;
        }

        result = extractSwitch(node);
        if (result != null) {
            return result;
        }

        return null;
    }

    private Node checkForDoWhileLoop(final Node node) {
        for (int i = node.getAncestors().size() - 1; i >= 0; i--) {
            Node ancestor = node.getAncestors().get(i);
            if (ancestor instanceof DoWhile && node.getIndex() < ancestor.getIndex() && checkForIndexOutOfBound(ancestor)) {
                return ancestor;
            }
        }
        return null;
    }

    private Construction extractDoWhile(Node begin, Node node) {
        ElementaryBlock elementaryBlock = new ElementaryBlock();
        elementaryBlock.setStatements(node.getStatements());

        com.sdc.cfg.constructions.DoWhile doWhileConstruction = new com.sdc.cfg.constructions.DoWhile(node.getCondition());

        begin.removeAncestor(node);

        final int leftBound = getRelativeIndex(begin.getIndex());
        final int rightBound = getRelativeIndex(node.getIndex());
        doWhileConstruction.setBody(createConstructionBuilder(myNodes.subList(leftBound, rightBound), gen).build());

        final int nextNodeIndex = node.getIndex() + 1;
        if (nextNodeIndex <= myNodes.get(size - 1).getIndex()) {
            node.setNextNode(myNodes.get(getRelativeIndex(nextNodeIndex)));
        }

        elementaryBlock.setNextConstruction(doWhileConstruction);

        if (node.getNextNode() != null) {
            extractNextConstruction(doWhileConstruction, node);
        }

        return elementaryBlock;
    }

    private Construction extractElementaryBlock(final Node node) {
        ElementaryBlock elementaryBlock = new ElementaryBlock();
        elementaryBlock.setStatements(node.getStatements());
        return elementaryBlock;
    }

    private Construction extractSwitch(Node node) {
        if (node instanceof Switch) {
            Switch switchNode = (Switch) node;
            com.sdc.cfg.constructions.Switch switchConstruction = new com.sdc.cfg.constructions.Switch(switchNode.getExpr());

            Node nextNode = findNextNodeToSwitchWithDefaultCase(switchNode);

            if (!switchNode.hasRealDefaultCase()) {
                switchNode.removeFakeDefaultCase();
                nextNode = findNextNodeToSwitchWithoutDefaultCase(switchNode);
            }

            final List<SwitchCase> switchCases = switchNode.getCases();

            for (int i = 0; i < switchCases.size(); i++) {
                final int leftBound = getRelativeIndex(switchCases.get(i).getCaseBody().getIndex());
                final int rightBound = i != switchCases.size() - 1
                        ? getRelativeIndex(switchCases.get(i + 1).getCaseBody().getIndex())
                        : nextNode == null ? size : getRelativeIndex(nextNode.getIndex());

                final Construction caseBody = createConstructionBuilder(myNodes.subList(leftBound, rightBound), gen).build();

                com.sdc.cfg.constructions.SwitchCase switchCase = new com.sdc.cfg.constructions.SwitchCase(caseBody);
                switchCase.setKeys(switchCases.get(i).getKeys());

                switchConstruction.addCase(switchCase);
            }

            if (nextNode == null) {
                addBreakToAllOutgoingLinks();
            } else {
                addBreakToAncestors(nextNode);
            }

            if (nextNode != null && checkForIndexOutOfBound(nextNode)) {
                switchNode.setNextNode(nextNode);
                extractNextConstruction(switchConstruction, switchNode);
            }

            return switchConstruction;
        }
        return null;
    }

    private Node findNextNode(final Node node) {
        Node result = null;

        for (int i = 0; i < domi.length; i++) {
            if (domi[i] == node.getIndex()) {
                boolean isTail = false;
                for (final Node tail : node.getListOfTails()) {
                    if (i == tail.getIndex()) {
                        isTail = true;
                        break;
                    }
                }
                if (!isTail) {
                    if (getRelativeIndex(i) >= 0 && getRelativeIndex(i) < size) {
                        result = myNodes.get(getRelativeIndex(i));
                    }
                    break;
                }
            }
        }

        return result;
    }

    private Node findNextNodeToSwitchWithDefaultCase(Switch switchNode) {
        return findNextNode(switchNode);
    }

    private Node findNextNodeToSwitchWithoutDefaultCase(Switch switchNode) {
        Node defaultBranch = switchNode.getNodeByKeyIndex(-1);

        switchNode.removeChild(defaultBranch);
        defaultBranch.removeAncestor(switchNode);

        return defaultBranch;
    }

    private Construction extractConditionBlock(Node node) {
        if (node.getCondition() != null) {
            for (Node ancestor : node.getAncestors()) {
                if (node.getIndex() < ancestor.getIndex()) {
                    if (domi[node.getIndex()] != domi[node.getListOfTails().get(1).getIndex()] && checkForIndexOutOfBound(node)) {
                        node.setNextNode(node.getListOfTails().get(1));
                    }
                    com.sdc.cfg.constructions.While whileConstruction = new com.sdc.cfg.constructions.While(node.getCondition());


                    int relativeIndexOfLeftTail = getRelativeIndex(node.getListOfTails().get(0));
                    int relativeIndexOfLoop = getRelativeIndex(gen.getRightIndexForLoop(node.getIndex()));
                    List<Node> whileBody = myNodes.subList(relativeIndexOfLeftTail, relativeIndexOfLeftTail > relativeIndexOfLoop ? getRelativeIndex(node.getListOfTails().get(1)) : relativeIndexOfLoop);

                    whileConstruction.setBody(createConstructionBuilder(whileBody, gen).build());
                    if (node.getNextNode() != null && checkForIndexOutOfBound(node.getNextNode())) {
                        extractNextConstruction(whileConstruction, node);
                    }

                    placeBreakAndContinue(node, whileBody);
                    removeBreakAndContinueFromLastConstruction(whileConstruction.getBody());

                    return whileConstruction;
                }
            }

            /// IF
            com.sdc.cfg.constructions.ConditionalBlock conditionalBlock = new ConditionalBlock(node.getCondition());

            boolean fl = false;
            for (Node ancestor : node.getAncestors()) {
                if (ancestor.getIndex() > node.getIndex()) {
                    fl = true;
                    break;
                }
            }

            if (!fl) {
                Node nextNode = findNextNode(node);
                node.setNextNode(nextNode);

                Node leftNode = node.getListOfTails().get(0);
                Node rightNode = node.getListOfTails().get(1);
                int rightIndex = getRelativeIndex(rightNode);

                if (node.getNextNode() == null) {
                    if (hasNotElse(rightNode) || checkRightTail(node)) {
                        if (rightNode.getIndex() <= myNodes.get(size - 1).getIndex()) {
                            node.setNextNode(checkForIndexOutOfBound(rightNode) ? rightNode : null);
                        }
                        conditionalBlock.setThenBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), checkForIndexOutOfBound(rightNode) ? rightIndex : size), gen).build());
                    } else {
                        conditionalBlock.setThenBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), gen).build());
                        if (rightIndex < size) {
                            List<Node> elseBody = myNodes.subList(rightIndex, size);
                            if (elseBody.size() > 1 || !elseBody.get(0).getStatements().isEmpty()) {
                                conditionalBlock.setElseBlock(createConstructionBuilder(myNodes.subList(rightIndex, size), gen).build());
                            }
                        } else {
                            ElementaryBlock block = new ElementaryBlock();
                            block.setBreak("");
                            conditionalBlock.setElseBlock(block);
                            node.getAncestors().get(0).setNextNode(node.getListOfTails().get(1));
                        }
                    }
                } else {
                    conditionalBlock.setThenBlock(createConstructionBuilder(myNodes.subList(getRelativeIndex(leftNode), rightIndex), gen).build());
                    conditionalBlock.setElseBlock(createConstructionBuilder(myNodes.subList(rightIndex, getRelativeIndex(node.getNextNode())), gen).build());
                }
            }
            // TODO: test second condition for switch with if in a case
            if (node.getNextNode() != null && checkForIndexOutOfBound(node.getNextNode())) {
                extractNextConstruction(conditionalBlock, node);
            }
            return conditionalBlock;
        }
        return null;
    }

    private void placeBreakAndContinue(final Node begin, final List<Node> nodes) {
        final int leftBound = nodes.get(0).getIndex();
        final int rightBound = nodes.get(nodes.size() - 1).getIndex();
        final int beginIndex = begin.getIndex();

        for (final Node node : nodes) {
            for (final Node tail : node.getListOfTails()) {
                final int tailIndex = tail.getIndex();

                if (tailIndex != beginIndex && (tailIndex < leftBound || tailIndex > rightBound)) {
                    node.getConstruction().setBreak("");
                    if (node.getConstruction().hasContinue()) {
                        node.getConstruction().setContinue(null);
                    }
                }

                if (tailIndex == beginIndex && !node.getConstruction().hasBreak()) {
                    node.getConstruction().setContinue("");
                }
            }
        }
    }

    private void removeBreakAndContinueFromLastConstruction(Construction start) {
        while (start.getNextConstruction() != null) {
            start = start.getNextConstruction();
        }

        start.setBreak(null);
        start.setContinue(null);

        if (start instanceof ConditionalBlock) {
            ConditionalBlock conditionalBlock = (ConditionalBlock) start;
            if (conditionalBlock.getElseBlock() != null && conditionalBlock.getThenBlock().hasBreak()) {
                removeBreakAndContinueFromLastConstruction(conditionalBlock.getElseBlock());
            }
        }
    }

    private boolean hasNotElse(final Node node) {
        int count = 0;
        // ????
        for (final Node ancestor : node.getAncestors()) {
            if (node.getIndex() > ancestor.getIndex()) {
                count++;
            }
        }

        return count > 1;
    }

    private boolean checkRightTail(final Node node) {
        return node.getListOfTails().get(1).getIndex() < node.getIndex();
    }

    private int getRelativeIndex(Node node) {
        return getRelativeIndex(node.getIndex());
    }

    private int getRelativeIndex(int index) {
        return index - myNodes.get(0).getIndex();
    }

    private void extractNextConstruction(Construction construction, final Node currentNode) {
        final int leftBound = getRelativeIndex(currentNode.getNextNode());

        construction.setNextConstruction(createConstructionBuilder(myNodes.subList(leftBound, size), gen).build());
    }

    private void addBreakToAncestors(final Node child) {
        for (final Node parent : child.getAncestors()) {
            if (parent.getConstruction() != null) {
                parent.getConstruction().setBreak("");
            }
        }
    }

    private void addBreakToAllOutgoingLinks() {
        final int firstNodeIndex = myNodes.get(0).getIndex();
        final int lastNodeIndex = myNodes.get(size - 1).getIndex();

        for (final Node node : myNodes) {
            for (final Node tail : node.getListOfTails()) {
                final int tailIndex = tail.getIndex();
                if ((tailIndex < firstNodeIndex || tail.getIndex() > lastNodeIndex) && node.getConstruction() != null) {
                    node.getConstruction().setBreak("");
                }
            }
        }
    }
}
