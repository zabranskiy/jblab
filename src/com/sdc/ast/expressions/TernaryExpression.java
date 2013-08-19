package com.sdc.ast.expressions;

import com.sdc.ast.OperationType;

/**
 * Created with IntelliJ IDEA.
 * User: 1
 * Date: 04.05.13
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class TernaryExpression extends PriorityExpression {
    private final Expression myCondition;
    private final Expression myLeft;
    private final Expression myRight;

    public TernaryExpression(Expression myCondition, Expression myLeft, Expression myRight) {
        this.myCondition = myCondition;
        this.myLeft = myLeft;
        this.myRight = myRight;
        setDoubleLength(myLeft.hasDoubleLength() || myRight.hasDoubleLength());
        myType = OperationType.TERNARY_IF;
    }

    public Expression getLeft() {
        return myLeft;
    }

    public Expression getRight() {
        return myRight;
    }

    public Expression getCondition() {
        return myCondition;
    }

    @Override
    public String toString() {
        return "TernaryExpression{" +
                "myCondition=" + myCondition +
                ", myLeft=" + myLeft +
                ", myRight=" + myRight +
                '}';
    }
}
