package com.sdc.ast.expressions;

import com.sdc.ast.OperationType;
import com.sdc.ast.expressions.identifiers.Identifier;

public class SquareBrackets extends Identifier {
    private Expression myOperand;
    private Expression myIndex;

    public SquareBrackets(Expression operand, Expression index) {
        myOperand = operand;
        myIndex = index;
        myType = OperationType.SQUARE_BRACKETS;
    }

    public Expression getOperand() {
        return myOperand;
    }

    public Expression getIndex() {
        return myIndex;
    }

    @Override
    public String toString() {
        return "SquareBrackets{" +
                "myOperand=" + myOperand +
                ", myIndex=" + myIndex +
                '}';
    }

    @Override
    public Expression getName() {
        return myOperand;
    }

    @Override
    public String getType() {
        if(myOperand instanceof Identifier) return ((Identifier) myOperand).getType();
        if(myOperand instanceof Invocation) return ((Invocation) myOperand).getReturnType();
        return null; // not need, I think
    }

    @Override
    public boolean isBoolean() {
        return getType().contains("boolean");
    }
}
