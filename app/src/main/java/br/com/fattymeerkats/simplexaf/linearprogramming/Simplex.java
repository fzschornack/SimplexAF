package br.com.fattymeerkats.simplexaf.linearprogramming;

/**
 * Created by fzschornack on 30/07/15.
 *
 * This class solves a linear programming problem using the Simplex algorithm.
 *
 */
public class Simplex {

    private double[][] tableaux; // tableaux
    private int numberOfConstraints;
    private int numberOfOriginalVariables;
    private ProblemKind maximizeOrMinimize;
    private Constraint[] constraintOperators;

    private final double INF = Double.POSITIVE_INFINITY;

    private int[] basis; // basis[i] = basic variable corresponding to row i
    private double[] objectiveFunction; // objectiveFunction[j] = coefficient of variable j
    private double[] constraintRightSide;

    /**
     * Returns an Simplex object, solving the linear programming problem.
     *
     * @param model the linear programming problem modeled as a MINIMIZATION problem.
     */
    public Simplex(Modeler model) {

        this.numberOfConstraints = model.getNumberOfConstraints();
        this.numberOfOriginalVariables = model.getNumberOfOriginalVariables();
        this.tableaux = model.getTableaux();
        this.maximizeOrMinimize = model.getProblemKind();
        this.constraintOperators = model.getConstraintOperators();

        basis = new int[numberOfConstraints];

        for (int i = 0; i < numberOfConstraints; i++)
            basis[i] = numberOfOriginalVariables + i; // at the beginning, the slack variables compound the basis

        objectiveFunction = new double[numberOfOriginalVariables];
        for (int j = 0; j < numberOfOriginalVariables; j++)
            objectiveFunction[j] = tableaux[numberOfConstraints][j];

        constraintRightSide = new double[numberOfConstraints];
        for (int i = 0; i < numberOfConstraints; i++)
            constraintRightSide[i] = tableaux[i][numberOfConstraints + numberOfOriginalVariables];

        solve();

    }

    /**
     * Run simplex algorithm starting from initial Best Feasible Solution.
     */
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
            q = dantzigNegative();

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

    /**
     * Dantzig negative rule.
     * Find the pivot column of Primal Simplex.
     *
     * @return the index q of a non-basic column with most negative cost
     */
    private int dantzigNegative() {
        int q = 0;
        // numberOfConstraints == number of slack variables
        for (int j = 1; j < numberOfConstraints + numberOfOriginalVariables; j++)
            if (tableaux[numberOfConstraints][j] < tableaux[numberOfConstraints][q])
                q = j;

        if (tableaux[numberOfConstraints][q] >= 0)
            return -1; // optimal
        else
            return q;
    }

    /**
     * Find the pivot row p using min ratio rule (-1 if no such row).
     *
     * @param q column with most negative cost.
     * @return the row p whose variable is leaving the basis.
     */
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

    /**
     * Find the pivot row of Dual Simplex.
     *
     * @return the index of a row with most negative b.
     */
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

    /**
     * Find the pivot column of Dual Simplex.
     *
     * @param p the index of the row
     * @return the index of column q using max ratio rule (-1 if no such column)
     */
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

    /**
     * Perform the pivot on entry (p, q) using Gauss-Jordan elimination
     *
     * @param p the index of the row
     * @param q the index of the column
     */
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

    /**
     * Optimal value of the problem.
     *
     * @return the optimal objective value
     */
    public double value() {
        double solution = tableaux[numberOfConstraints][numberOfConstraints
                + numberOfOriginalVariables];

        if (maximizeOrMinimize.equals(ProblemKind.MAXIMIZE))
            return solution;

        return - solution;
    }

    /**
     * The solution values of each variable.
     *
     * @return the primal solution vector.
     */
    public double[] primal() {
        double[] x = new double[numberOfOriginalVariables + numberOfConstraints];
        for (int i = 0; i < numberOfConstraints; i++)
            x[basis[i]] = tableaux[i][numberOfConstraints
                    + numberOfOriginalVariables];
        return x;
    }

    /**
     * Calculate how much of each constraint is being used (original r.h. value - slack).
     * Note: slack = waste/leftover
     *
     * @param solutionVector the solution values of each variable
     * @return a vector describing how much of each constraint is being used
     */
    public double[] constraintsFinalValues(double[] solutionVector) {
        double[] x = new double[numberOfConstraints];
        for (int i = 0; i < numberOfConstraints; i++)
            if (constraintOperators[i].equals(Constraint.greaterThan))
                x[i] = - constraintRightSide[i] - solutionVector[numberOfOriginalVariables + i];
            else
                x[i] = constraintRightSide[i] - solutionVector[numberOfOriginalVariables + i];

        return x;
    }

    /**
     * Perform the Sensitivity Analysis on vector c, computing the coefficients (vector c) ranges
     * (floor, ceil) for which the basis remains optimal.
     *
     * @return the coefficients ranges (floor, ceil) for which the basis remains optimal
     */
    public double[][] sensitivityAnalysisVectorC() {
        double[][] x = new double[numberOfOriginalVariables][2];
        int[] visited = new int[numberOfOriginalVariables];

        // basic variables coefficients
        for (int i = 0; i < numberOfConstraints; i++) {
            if (basis[i] < numberOfOriginalVariables) {

                x[basis[i]][0] = objectiveFunction[basis[i]] + floorSensitivityAnalysisVectorC(i);
                x[basis[i]][1] = objectiveFunction[basis[i]] + ceilSensitivityAnalysisVectorC(i);

                visited[basis[i]] = 1;
            }
        }

        // non-basic variables coefficients
        for (int i = 0; i < numberOfOriginalVariables; i++)
            if (visited[i] == 0) {
                x[i][0] = objectiveFunction[i] - tableaux[numberOfConstraints][i];
                x[i][1] = INF;
            }

        // maximization problem: switch (floor, ceil) -> (ceil,floor) and change signals
        double aux;
        if (maximizeOrMinimize.equals(ProblemKind.MAXIMIZE))
            for (int i = 0; i < numberOfOriginalVariables; i++) {
                aux = x[i][0];
                x[i][0] = - x[i][1];
                x[i][1] = - aux;
            }

        return x;
    }

    /**
     * Compute the coefficient floor value such that the problem solution doesn't change,
     * using the rule max( (cl-zl)/ykl | ykl < 0 )
     *
     * @param i the basic variable coefficient index
     * @return the variation allowed
     */
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
            return -INF;

        return tableaux[numberOfConstraints][x] / tableaux[i][x];
    }

    /**
     * Compute the coefficient ceil value such that the problem solution doesn't change,
     * using the rule min( (cl-zl)/ykl | ykl > 0 )
     *
     * @param i the basic variable coefficient index
     * @return the variation allowed
     */
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
            return INF;

        return tableaux[numberOfConstraints][x] / tableaux[i][x];
    }

    /**
     * Perform the Sensitivity Analysis on vector b, computing the constraints (vector b) ranges
     * (floor, ceil) for which the basis remains optimal.
     *
     * @return the constraints ranges (floor, ceil) for which the basis remains optimal
     */
    public double[][] sensitivityAnalysisVectorB() {
        double[][] x = new double[numberOfConstraints][2];
        for (int i = 0; i < numberOfConstraints; i++) {
            x[i][0] = constraintRightSide[i] + floorSensitivityAnalysisVectorB(numberOfOriginalVariables + i);
            x[i][1] = constraintRightSide[i] + ceilSensitivityAnalysisVectorB(numberOfOriginalVariables + i);
        }

        // greater than constraint: switch (floor, ceil) -> (ceil,floor) and change signals
        double aux;
        for (int i = 0; i < numberOfConstraints; i++)
            if (constraintOperators[i].equals(Constraint.greaterThan)) {
                aux = x[i][0];
                x[i][0] = - x[i][1];
                x[i][1] = - aux;
            }

        return x;
    }

    /**
     * Compute the constraint floor value such that the problem solution doesn't change,
     * using the rule max( bl/-B^-1li | B^-1li > 0)
     *
     * @param j the constraint index
     * @return the variation allowed
     */
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

    /**
     * Compute the constraint ceil value such that the problem solution doesn't change,
     * using the rule min( bl/-B^-1li | B^-1li < 0)
     *
     * @param j the constraint index
     * @return the variation allowed
     */
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

    /**
     * Print the tableaux (good for debugging or linear programming students)
     */
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

        final boolean MAXIMIZE = true;
        final boolean MINIMIZE = false;

        double[] objectiveFunc = { 12, 40 };
        double[][] constraintLeftSide = {   { 1, 1 },
                                            { 1, 3 },
                                            { 1, 0 } };

        Constraint[] constraintOperator = { Constraint.lessThan,
                Constraint.lessThan, Constraint.greaterThan };
        double[] constraintRightSide = { 16, 36, 10 };

        // create the model of the problem
        Modeler model = new Modeler(constraintLeftSide, constraintRightSide,
                constraintOperator, objectiveFunc, ProblemKind.MAXIMIZE);

        // solve using Simplex
        Simplex simplex = new Simplex(model);

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


}
