package br.com.fattymeerkats.simplexaf.linearprogramming;

/**
 * Created by fzschornack on 30/07/15.
 */
public class Simplex {

    // index of a non-basic column with most positive cost
    private int dantzig() {
        int q = 0;
        // numberOfConstraints == number of slack variables
        for (int j = 1; j < numberOfConstraints + numberOfOriginalVariables; j++)
            if (tableaux[numberOfConstraints][j] > tableaux[numberOfConstraints][q])
                q = j;

        if (tableaux[numberOfConstraints][q] <= 0)
            return -1; // optimal
        else
            return q;
    }

    // index of a non-basic column with most negative cost
    private int dantzigNegative() {
        int q = 0;
        for (int j = 1; j < numberOfConstraints + numberOfOriginalVariables; j++)
            if (tableaux[numberOfConstraints][j] < tableaux[numberOfConstraints][q])
                q = j;

        if (tableaux[numberOfConstraints][q] >= 0)
            return -1; // optimal
        else
            return q;
    }

    // find row p using min ratio rule (-1 if no such row)
    private int minRatioRule(int q) {
        int p = -1;
        for (int i = 0; i < numberOfConstraints; i++) {
            if (tableaux[i][q] <= 0)
                continue;
            else if (p == -1)
                p = i;
            else if ((tableaux[i][numberOfConstraints
                    + numberOfOriginalVariables] / tableaux[i][q]) < (tableaux[p][numberOfConstraints
                    + numberOfOriginalVariables] / tableaux[p][q]))
                p = i;
        }
        return p;
    }

    // index of a line with most negative b
    private int dualRule() {
        int p = 0;
        for (int i = 1; i < numberOfConstraints; i++)
            if (tableaux[i][numberOfConstraints
                    + numberOfOriginalVariables] < tableaux[p][numberOfConstraints
                    + numberOfOriginalVariables])
                p = i;

        if (tableaux[p][numberOfConstraints
                + numberOfOriginalVariables] >= 0)
            return -1; // dual not needed, goto primal
        else
            return p;
    }

    private double[][] tableaux; // tableaux
    private int numberOfConstraints; // number of constraints
    private int numberOfOriginalVariables; // number of original variables

    private boolean maximizeOrMinimize;

    private static final boolean MAXIMIZE = true;
    private static final boolean MINIMIZE = false;

    private final double INF = 1000000000.0;

    private int[] basis; // basis[i] = basic variable corresponding to row i

    private double[] objectiveFunction; // objectiveFunction[j] = coefficient of variable j

    private double[] constraintRightSide;

    public Simplex(double[][] tableaux, int numberOfConstraint,
                   int numberOfOriginalVariable, boolean maximizeOrMinimize) {
        this.maximizeOrMinimize = maximizeOrMinimize;
        this.numberOfConstraints = numberOfConstraint;
        this.numberOfOriginalVariables = numberOfOriginalVariable;
        this.tableaux = tableaux;

        basis = new int[numberOfConstraints];
        for (int i = 0; i < numberOfConstraints; i++)
            basis[i] = numberOfOriginalVariables + i; // at the beginning, the slack variables compound the basis

        objectiveFunction = new double[numberOfOriginalVariables];
        for (int j = 0; j < numberOfOriginalVariables; j++)
            objectiveFunction[j] = tableaux[numberOfConstraints][j];

        constraintRightSide = new double[numberOfConstraint];
        for (int i = 0; i < numberOfConstraints; i++)
            constraintRightSide[i] = tableaux[i][numberOfConstraints + numberOfOriginalVariables];

        solve();

    }

    // run simplex algorithm starting from initial BFS
    private void solve() {

        // DUAL SIMPLEX
        while (true) {
            show();
            int p = dualRule();

            if (p == -1)
                break; // all entries in vector b are positive, goto primal

            // find pivot column
            int q = maxRatioRule(p);
            if (q == -1)
                throw new ArithmeticException("Linear program is infeasible");

            // multiply line p by -1
            for (int j = 0; j <= numberOfConstraints + numberOfOriginalVariables; j++)
                if (tableaux[p][j] != 0)
                    tableaux[p][j] = -tableaux[p][j];

            // pivot
            pivot(p, q);

            //update basis
            basis[p] = q;
        }

        // PRIMAL SIMPLEX
        while (true) {
            show();
            int q = 0;
            // find entering column q
            if (maximizeOrMinimize) {
                q = dantzig();
            } else {
                q = dantzigNegative();
            }
            if (q == -1)
                break; // optimal

            // find leaving row p
            int p = minRatioRule(q);
            if (p == -1)
                throw new ArithmeticException("Linear program is unbounded");

            // pivot
            pivot(p, q);

            // update basis
            basis[p] = q;

        }
    }

    // find column q using max ratio rule (-1 if no such column)
    private int maxRatioRule(int p) {
        int q = -1;
        for (int j = 0; j < numberOfConstraints + numberOfOriginalVariables; j++) {
            if (tableaux[p][j] >= 0)
                continue;
            else if (q == -1)
                q = j;
            else if ((tableaux[numberOfConstraints][j] / tableaux[p][j])
                    > (tableaux[numberOfConstraints][q] / tableaux[p][q]))
                q = j;
        }
        return q;
    }

    // pivot on entry (p, q) using Gauss-Jordan elimination
    private void pivot(int p, int q) {

        // everything but row p and column q
        for (int i = 0; i <= numberOfConstraints; i++)
            for (int j = 0; j <= numberOfConstraints
                    + numberOfOriginalVariables; j++)
                if (i != p && j != q)
                    tableaux[i][j] -= tableaux[p][j] * tableaux[i][q]
                            / tableaux[p][q];

        // zero out column q
        for (int i = 0; i <= numberOfConstraints; i++)
            if (i != p)
                tableaux[i][q] = 0.0;

        // scale row p
        for (int j = 0; j <= numberOfConstraints + numberOfOriginalVariables; j++)
            if (j != q)
                tableaux[p][j] /= tableaux[p][q];
        tableaux[p][q] = 1.0;
    }

    // return optimal objective value
    public double value() {
        return -tableaux[numberOfConstraints][numberOfConstraints
                + numberOfOriginalVariables];
    }

    // return primal solution vector
    public double[] primal() {
        double[] x = new double[numberOfOriginalVariables + numberOfConstraints];
        for (int i = 0; i < numberOfConstraints; i++)
            x[basis[i]] = tableaux[i][numberOfConstraints
                    + numberOfOriginalVariables];
        return x;
    }

    // return how much of each constraint is being used (original r.h. value - slack)
    public double[] constraintsFinalValues(double[] solutionVector) {
        double[] x = new double[numberOfConstraints];
        for (int i = 0; i < numberOfConstraints; i++)
            x[i] = constraintRightSide[i] - solutionVector[numberOfOriginalVariables + i];

        return x;
    }

    // return the coefficients ranges (floor, ceil) for which the basis remains optimal
    public double[][] sensitivityAnalysisVectorC() {
        double[][] x = new double[numberOfOriginalVariables][2];
        int[] visited = new int[numberOfOriginalVariables];

        // basic variables coefficients
        for (int i = 0; i < numberOfConstraints; i++) {
            if (basis[i] < numberOfOriginalVariables) {
                if (maximizeOrMinimize == MINIMIZE) {
                    x[basis[i]][0] = objectiveFunction[basis[i]] + floorSensitivityAnalysisVectorC(i);
                    x[basis[i]][1] = objectiveFunction[basis[i]] + ceilSensitivityAnalysisVectorC(i);
                } else {
                    x[basis[i]][0] = objectiveFunction[basis[i]] + ceilSensitivityAnalysisVectorC(i);
                    x[basis[i]][1] = objectiveFunction[basis[i]] + floorSensitivityAnalysisVectorC(i);
                }
                visited[basis[i]] = 1;
            }
        }

        // non-basic variables coefficients
        for (int i = 0; i < numberOfOriginalVariables; i++)
            if (visited[i] == 0) {
                if (maximizeOrMinimize == MINIMIZE) {
                    x[i][0] = objectiveFunction[i] - tableaux[numberOfConstraints][i];
                    x[i][1] = INF;
                } else {
                    x[i][0] = -INF;
                    x[i][1] = objectiveFunction[i] - tableaux[numberOfConstraints][i];
                }
            }

        return x;
    }

    // max( (cl-zl)/ykl | ykl < 0 )
    public double floorSensitivityAnalysisVectorC(int i) {
        int[] visited = new int[numberOfOriginalVariables + numberOfConstraints];

        for (int j = 0; j < numberOfConstraints; j++)
            visited[basis[j]] = 1;

        int x = -1;
        for (int j = 0; j < numberOfOriginalVariables + numberOfConstraints; j++) {
            if (tableaux[i][j] < 0 && visited[j] == 0)
                if (x == -1)
                    x = j;
                else if (tableaux[numberOfConstraints][j] / tableaux[i][j]
                        > tableaux[numberOfConstraints][x] / tableaux[i][x])
                    x = j;
        }

        if (x == -1)
            if (maximizeOrMinimize == MINIMIZE)
                return -INF;
            else
                return INF;
        return tableaux[numberOfConstraints][x] / tableaux[i][x];
    }

    // min( (cl-zl)/ykl | ykl > 0 )
    public double ceilSensitivityAnalysisVectorC(int i) {
        int[] visited = new int[numberOfOriginalVariables + numberOfConstraints];

        for (int j = 0; j < numberOfConstraints; j++)
            visited[basis[j]] = 1;

        int x = -1;
        for (int j = 0; j < numberOfOriginalVariables + numberOfConstraints; j++) {
            if (tableaux[i][j] > 0 && visited[j] == 0)
                if (x == -1)
                    x = j;
                else if (tableaux[numberOfConstraints][j] / tableaux[i][j]
                        < tableaux[numberOfConstraints][x] / tableaux[i][x])
                    x = j;
        }

        if (x == -1)
            if (maximizeOrMinimize == MINIMIZE)
                return INF;
            else
                return -INF;
        return tableaux[numberOfConstraints][x] / tableaux[i][x];
    }

    // return the constraints ranges (floor, ceil) for which the basis remains optimal
    public double[][] sensitivityAnalysisVectorB() {
        double[][] x = new double[numberOfConstraints][2];
        for (int i = 0; i < numberOfConstraints; i++) {
            x[i][0] = constraintRightSide[i] + floorSensitivityAnalysisVectorB(numberOfOriginalVariables + i);
            x[i][1] = constraintRightSide[i] + ceilSensitivityAnalysisVectorB(numberOfOriginalVariables + i);
        }

        return x;
    }

    // max( bl/-B^-1li | B^-1li > 0)
    public double floorSensitivityAnalysisVectorB(int j) {
        int x = -1;
        for (int i = 0; i < numberOfConstraints; i++) {
            if (tableaux[i][j] > 0)
                if (x == -1)
                    x = i;
                else if (tableaux[i][numberOfOriginalVariables + numberOfConstraints] / -tableaux[i][j] >
                        tableaux[x][numberOfOriginalVariables + numberOfConstraints] / -tableaux[x][j])
                    x = i;
        }

        if (x == -1)
            return -INF;
        return tableaux[x][numberOfOriginalVariables + numberOfConstraints] / -tableaux[x][j];
    }

    // min( bl/-B^-1li | B^-1li < 0)
    public double ceilSensitivityAnalysisVectorB(int j) {
        int x = -1;
        for (int i = 0; i < numberOfConstraints; i++) {
            if (tableaux[i][j] < 0)
                if (x == -1)
                    x = i;
                else if (tableaux[i][numberOfOriginalVariables + numberOfConstraints] / -tableaux[i][j] <
                        tableaux[x][numberOfOriginalVariables + numberOfConstraints] / -tableaux[x][j])
                    x = i;
        }

        if (x == -1)
            return INF;
        return tableaux[x][numberOfOriginalVariables + numberOfConstraints] / -tableaux[x][j];
    }

    // print tableaux
    public void show() {
        System.out.println("M = " + numberOfConstraints);
        System.out.println("N = " + numberOfOriginalVariables);
        for (int i = 0; i <= numberOfConstraints; i++) {
            for (int j = 0; j <= numberOfConstraints
                    + numberOfOriginalVariables; j++) {
                System.out.printf("%7.2f ", tableaux[i][j]);
            }
            System.out.println();
        }
        System.out.println("value = " + value());
        for (int i = 0; i < numberOfConstraints; i++)
            if (basis[i] < numberOfOriginalVariables)
                System.out.println("x_"
                        + basis[i]
                        + " = "
                        + tableaux[i][numberOfConstraints
                        + numberOfOriginalVariables]);
        System.out.println();
    }

    // test client
    public static void main(String[] args) {

//        double[] objectiveFunc = { 18, -6, 4 };
//        double[][] constraintLeftSide = {
//
//                { 7, -3, 7 }, { 1, -1, 2 }, { 8, -4, -1 } };
//        Constraint[] constraintOperator = { Constraint.lessThan,
//                Constraint.greatherThan, Constraint.lessThan };
//        double[] constraintRightSide = { 2, -2, 0 };
//
//        Modeler model = new Modeler(constraintLeftSide, constraintRightSide,
//                constraintOperator, objectiveFunc);
//
//        Simplex simplex = new Simplex(model.getTableaux(),
//                model.getNumberOfConstraint(),
//                model.getNumberOfOriginalVariable(), MAXIMIZE);

        double[] objectiveFunc = {4, 1, 7};
        double[][] constraintLeftSide = {

                {1, 1, 2}, {1, 0, 1}, {2, 1, 3}};
        Constraint[] constraintOperator = {Constraint.greatherThan,
                Constraint.greatherThan, Constraint.greatherThan};
        double[] constraintRightSide = {3, 2, 4};

        Modeler model = new Modeler(constraintLeftSide, constraintRightSide,
                constraintOperator, objectiveFunc);

        Simplex simplex = new Simplex(model.getTableaux(),
                model.getNumberOfConstraint(),
                model.getNumberOfOriginalVariable(), MINIMIZE);

        double[] x = simplex.primal();
        double[][] sac = simplex.sensitivityAnalysisVectorC();
        double[][] sab = simplex.sensitivityAnalysisVectorB();
        double[] b = simplex.constraintsFinalValues(x);
        for (int i = 0; i < x.length; i++)
            System.out.println("x[" + i + "] = " + x[i]);
        for (int i = 0; i < b.length; i++)
            System.out.println("b[" + i + "] = " + b[i]);
        System.out.println("Solution: " + simplex.value());
        for (int i = 0; i < simplex.numberOfOriginalVariables; i++) {
            System.out.printf("c_%d MIN = %7.2f MAX = %7.2f", i, sac[i][0], sac[i][1]);
            System.out.println();
        }
        for (int i = 0; i < simplex.numberOfConstraints; i++) {
            System.out.printf("b_%d MIN = %7.2f MAX = %7.2f", i, sab[i][0], sab[i][1]);
            System.out.println();
        }
    }

    private enum Constraint {
        lessThan, equal, greatherThan
    }

    public static class Modeler {
        private double[][] a; // tableaux
        private int numberOfConstraints; // number of constraints
        private int numberOfOriginalVariables; // number of original variables

        public Modeler(double[][] constraintLeftSide,
                       double[] constraintRightSide, Constraint[] constraintOperator,
                       double[] objectiveFunction) {
            numberOfConstraints = constraintRightSide.length;
            numberOfOriginalVariables = objectiveFunction.length;
            a = new double[numberOfConstraints + 1][numberOfOriginalVariables
                    + numberOfConstraints + 1];

            // initialize constraint
            for (int i = 0; i < numberOfConstraints; i++) {
                for (int j = 0; j < numberOfOriginalVariables; j++) {
                    a[i][j] = constraintLeftSide[i][j];
                }
            }

            for (int i = 0; i < numberOfConstraints; i++)
                a[i][numberOfConstraints + numberOfOriginalVariables] = constraintRightSide[i];

            // initialize slack variable
            for (int i = 0; i < numberOfConstraints; i++) {
                int slack = 1;
                switch (constraintOperator[i]) {
                    case greatherThan:
                        // multiply line i by -1 to invert the constraint operator
                        for (int j = 0; j <= numberOfConstraints + numberOfOriginalVariables; j++)
                            if (a[i][j] != 0)
                                a[i][j] = -a[i][j];
                        break;
                    case lessThan:
                        break;
                    default:
                }
                a[i][numberOfOriginalVariables + i] = slack;
            }

            // initialize objective function
            for (int j = 0; j < numberOfOriginalVariables; j++)
                a[numberOfConstraints][j] = objectiveFunction[j];
        }

        public double[][] getTableaux() {
            return a;
        }

        public int getNumberOfConstraint() {
            return numberOfConstraints;
        }

        public int getNumberOfOriginalVariable() {
            return numberOfOriginalVariables;
        }
    }


}
