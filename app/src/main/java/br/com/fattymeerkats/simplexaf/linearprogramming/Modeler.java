package br.com.fattymeerkats.simplexaf.linearprogramming;

/**
 * Created by fzschornack on 16/09/15.
 *
 * This class models any linear problem and transforms it in the Standard Minimization form.
 */
public class Modeler {
    private double[][] a; // tableaux
    private int numberOfConstraints;
    private int numberOfOriginalVariables;
    private int equals_count;
    private Constraint[] constraintOperators;

    private ProblemKind maximizeOrMinimize;

    public Modeler(double[][] constraintLeftSide,
                   double[] constraintRightSide, Constraint[] constraintOperator,
                   double[] objectiveFunction, ProblemKind maximizeOrMinimize) {

        this.maximizeOrMinimize = maximizeOrMinimize;

        this.constraintOperators = constraintOperator;

        numberOfConstraints = constraintRightSide.length;
        numberOfOriginalVariables = objectiveFunction.length;

        // count the number of "equal" constraints (which means 1 more row in the tableaux)
        equals_count = 0;
        for (int i = 0; i < numberOfConstraints; i++)
            if (constraintOperator[i] == Constraint.equal)
                equals_count++;

        // create the tableaux
        a = new double[numberOfConstraints + equals_count + 1][numberOfOriginalVariables
                + numberOfConstraints + equals_count + 1];

        // initialize the constraints
        int k = 0;
        for (int i = 0; i < numberOfConstraints; i++) {
            switch (constraintOperator[i]) {
                case greaterThan:
                    for (int j = 0; j < numberOfOriginalVariables; j++)
                        a[k][j] = -constraintLeftSide[i][j];
                    break;
                case lessThan:
                    for (int j = 0; j < numberOfOriginalVariables; j++)
                        a[k][j] = constraintLeftSide[i][j];
                    break;
                case equal:
                    for (int j = 0; j < numberOfOriginalVariables; j++) {
                        a[k][j] = constraintLeftSide[i][j];
                        a[k + 1][j] = -constraintLeftSide[i][j];
                    }
                    k++;
                    break;
            }
            k++;
        }

        // initialize the constraints right side
        k = 0;
        for (int i = 0; i < numberOfConstraints; i++) {
            switch (constraintOperator[i]) {
                case greaterThan:
                    a[k][numberOfConstraints + numberOfOriginalVariables + equals_count] = -constraintRightSide[i];
                    break;
                case lessThan:
                    a[k][numberOfConstraints + numberOfOriginalVariables + equals_count] = constraintRightSide[i];
                    break;
                case equal:
                    a[k][numberOfConstraints + numberOfOriginalVariables + equals_count] = constraintRightSide[i];
                    a[k + 1][numberOfConstraints + numberOfOriginalVariables + equals_count] = -constraintRightSide[i];
                    k++;
                    break;
            }
            k++;
        }

        // initialize the slack variables
        int slack = 1;
        for (int i = 0; i < numberOfConstraints + equals_count; i++) {
            a[i][numberOfOriginalVariables + i] = slack;
        }

        // initialize the objective function
        for (int j = 0; j < numberOfOriginalVariables; j++)
            if (this.maximizeOrMinimize == ProblemKind.MAXIMIZE)
                a[numberOfConstraints + equals_count][j] = -objectiveFunction[j];
            else
                a[numberOfConstraints + equals_count][j] = objectiveFunction[j];
    }

    public double[][] getTableaux() {
        return a;
    }

    public int getNumberOfConstraints() {
        return numberOfConstraints + equals_count;
    }

    public int getNumberOfOriginalVariables() { return numberOfOriginalVariables; }

    public ProblemKind getProblemKind() { return maximizeOrMinimize; }

    public Constraint[] getConstraintOperators() { return constraintOperators; }

}
