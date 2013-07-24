package com.sdc.ast.expressions.identifiers;

import com.sdc.ast.expressions.Expression;

public abstract class Identifier extends Expression {
    abstract public String getName();

    abstract public String getType();
}
