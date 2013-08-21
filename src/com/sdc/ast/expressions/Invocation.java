package com.sdc.ast.expressions;

import com.sdc.ast.OperationType;

import java.util.List;

public class Invocation extends PriorityExpression {
    private final String myFunction;
    private final List<Expression> myArguments;
    private final String myReturnType;

    public Invocation(final String function, final String returnType, final List<Expression> arguments) {
        this.myFunction = function;
        this.myArguments = arguments;
        this.myReturnType = returnType;
        setDoubleLength(returnType.contains("double") || returnType.contains("long"));
        myType = OperationType.INVOCATION;

    }

    public String getFunction() {
        return myFunction;
    }

    public String getReturnType() {
        return myReturnType;
    }

    public List<Expression> getArguments() {
        return myArguments;
    }

    @Override
    public boolean isBoolean() {
        return myReturnType.contains("boolean");
    }
}
