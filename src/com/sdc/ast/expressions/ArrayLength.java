package com.sdc.ast.expressions;

import com.sdc.languages.general.printers.OperationPrinter;
import com.sdc.ast.ExpressionType;
import com.sdc.ast.Type;
import com.sdc.ast.expressions.identifiers.Variable;

import java.util.ArrayList;
import java.util.List;

public class ArrayLength extends PriorityExpression {
    Expression myOperand;

    public ArrayLength(Expression myOperand) {
        super(ExpressionType.ARRAYLENGTH, Type.INT_TYPE);
        this.myOperand = myOperand;
    }

    public Expression getOperand() {
        return myOperand;
    }

    public String getOperation(OperationPrinter operationPrinter) {
        switch (myExpressionType) {
            case ARRAYLENGTH:
                return operationPrinter.getArrayLengthView();
            default:
                return "";
        }
    }

    @Override
    public boolean findVariable(Variable variable) {
        return myOperand.findVariable(variable);
    }

    @Override
    public List<Expression> getSubExpressions() {
        List<Expression> subExpressions = new ArrayList<Expression>();
        subExpressions.add(myOperand);
        return subExpressions;
    }
}
