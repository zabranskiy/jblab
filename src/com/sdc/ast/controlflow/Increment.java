package com.sdc.ast.controlflow;

import com.sdc.abstractLanguage.AbstractOperationPrinter;
import com.sdc.ast.OperationType;
import com.sdc.ast.expressions.Constant;
import com.sdc.ast.expressions.Expression;
import com.sdc.ast.expressions.identifiers.Variable;

import static com.sdc.ast.OperationType.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dmitrii.Pozdin
 * Date: 8/9/13
 * Time: 4:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class Increment extends Statement {
    private Variable myVariable;
    private Expression myIncrement;
    private OperationType myType;
    private boolean myIsIncrementSimple=false;

    public Increment(Variable v, int increment) {
        super();
        myVariable=v;
        myIncrement = new Constant(Math.abs(increment),false);
        if (increment == 1) {
            myIsIncrementSimple=true;
            myType = INC;
        } else if (increment == -1) {
            myIsIncrementSimple=true;
            myType = DEC;
        } else if (increment >= 0) {
            myType = ADD_INC;
        } else if (increment < 0) {
            myType = SUB_INC;
        }
    }


    public Increment(Variable v, Expression increment, OperationType type) {
        myVariable=v;
        myIncrement = increment;
        myType=type;
        switch (type) {
            case INC:
                myType = INC;
                myIsIncrementSimple=true;
                return;
            case DEC:
                myType = DEC;
                myIsIncrementSimple=true;
                return;
            case ADD:
                myType=ADD_INC;
                return;
            case SUB:
                myType=SUB_INC;
                return;
            case MUL:
                myType = MUL_INC;
                return;
            case DIV:
                myType = DIV_INC;
                return;
            case REM:
                myType = REM_INC;
                return;
            default:
                return;
        }

    }


    public Expression getIncrementExpression() {
        return myIncrement;
    }


    public String getName() {
        return myVariable.getName();
    }

    public String getOperation(AbstractOperationPrinter operationPrinter) {
        switch (myType) {
            case INC:
                return operationPrinter.getIncView();
            case DEC:
                return operationPrinter.getDecView();
            case ADD_INC:
                return operationPrinter.getAddIncView();
            case SUB_INC:
                return operationPrinter.getSubIncView();
            case MUL_INC:
                return operationPrinter.getMulIncView();
            case DIV_INC:
                return operationPrinter.getDivIncView();
            case REM_INC:
                return operationPrinter.getRemIncView();
            default:
                return "";
        }
    }

    public OperationType getType() {
        return myType;
    }

    public Variable getVariable() {
        return myVariable;
    }
    public boolean IsIncrementSimple(){
        return myIsIncrementSimple;
    }
}
