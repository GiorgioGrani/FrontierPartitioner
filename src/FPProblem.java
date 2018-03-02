import ilog.concert.*;
import ilog.cplex.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

public class FPProblem {

    private static long nodes;
    private static long infnodes;
    private long level;
    private static Map<String, IloAddable> constraints;
    private static Map<String, IloNumExpr> objectives;
    private static IloAddable localObjective;
    private  IloAddable localconstraint;
    private static Map<String, IloNumVar> vars;
    private static int norm;
    private static IloCplex cplex;
    private static IloModel model;
    private Map<String, Double> relsol;
    private Map<String, Double> pareto;
    //private Map<String, Double> idealvector;
    private static Map<String, Double> val;
    //public static double eps = 10e-8;

    public FPProblem(double [][] objectives, double [][] matrixA, double[] b,boolean[] binary, int norm) throws IloException {
        this.norm = norm;
        this.nodes = 1;
        this.infnodes = 0;
        this.basicFilling(objectives,  matrixA, b, binary);
        this.solverSettings();
    }

    public FPProblem( FPProblem p, IloAddable con) throws IloException {
        this.level = p.level + 1;
        this.localconstraint = con;
        this.nodes = this.nodes + 1;
    }


    private void basicFilling(double [][] objectives, double [][] matrixA, double[] b, boolean[] binary) throws IloException{
        this.cplex = new IloCplex();
        this.vars  = new TreeMap<>();
        this.createVariable(objectives[0].length, binary);
        this.constraints = new TreeMap<>();
        this.createConstraints(matrixA, b);
        this.objectives = new TreeMap<>();
        this.val = new TreeMap<>();
        this.createObjectives(objectives);
        this.model = this.cplex.getModel();
        this.level = 0;
        this.setObjective();
    }

    private void createVariable(int n, boolean[] binary) throws IloException{
        for(int i = 0; i < n; i++){
            String s = "x"+(i+1);
            if(binary[i]){
                this.vars.put(s, this.cplex.boolVar());
            }else {
                this.vars.put(s, this.cplex.intVar(Integer.MIN_VALUE, Integer.MAX_VALUE));
            }
        }
    }

    private void createConstraints(double [][] matrixA, double[] b) throws IloException{
        for(int i = 0; i < matrixA.length; i++){
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            int j = 0;
            for(String s : this.vars.keySet()){
                expr.addTerm(matrixA[i][j], this.vars.get(s));
                j++;
            }
            IloAddable con = this.cplex.addLe(expr, b[i]);
            String s = "con"+i;
            this.constraints.put(s, con);
        }
    }

    private void createObjectives(double [][] objectives) throws IloException{
        for(int i = 0; i < objectives.length; i++){
            IloNumExpr expr = this.cplex.numExpr();
            int j = 0;
            for(String s : this.vars.keySet()){
                IloNumExpr ex0 = this.cplex.prod(objectives[i][j], this.vars.get(s));
                expr = this.cplex.sum(expr,ex0);
                j++;
            }
            IloNumExpr expression = expr;
            String s = "objective" + i;
            this.objectives.put(s, expression);
            Double d = 1d;
            this.val.put(s, d);
        }
    }

    private boolean setObjective() throws IloException{
         if(this.norm == 1){
            IloNumExpr expr = this.cplex.linearNumExpr();
            int h = 1;
            for(IloNumExpr obj : this.objectives.values()){
                expr = this.cplex.sum(expr, this.cplex.prod(h,obj));
            }
            this.localObjective = this.cplex.addMinimize(expr);
        }else if(this.norm == 3){
            IloNumExpr expr = this.cplex.linearNumExpr();

            for(IloNumExpr obj : this.objectives.values()){
                double h = Math.round(Math.random()*100)+1;
                IloNumExpr objective = (IloNumExpr) obj;
                expr = this.cplex.sum(expr, this.cplex.prod(h,objective));

            }
            this.localObjective = this.cplex.addMinimize(expr);
        }
        return true;
    }

    private void solverSettings() throws IloException{
        this.cplex.setOut(null);
        this.cplex.setParam(IloCplex.IntParam.AdvInd, 0);
        this.cplex.setParam(IloCplex.DoubleParam.EpGap, 1e-9);
    }

    public boolean solve() throws IloException{
        this.model.add(this.localconstraint);
        //this.solverSettings();
        boolean solvable = this.cplex.solve();
        if(!solvable){
            this.infnodes += 1;
            //System.out.println("diodio "+this.infnodes);
            return false;
        }
        this.relsol = new TreeMap<>();
        this.pareto = new TreeMap<>();

        for(String y : this.vars.keySet()){
            this.relsol.put(y, this.cplex.getValue(this.vars.get(y)));
        }
        for(String y : this.objectives.keySet()){
            this.pareto.put(y, this.cplex.getValue(this.objectives.get(y)));
        }
        return true;
    }

    public void refresh() throws IloException{
        if(this.localconstraint == null) return;
        this.model.remove(this.localconstraint);
    }

    public List< IloAddable> branchOn() throws IloException{
        List<IloAddable> ret = new ArrayList<>();
        for(String s : this.pareto.keySet()){
            double relval = this.pareto.get(s);
            double val = this.val.get(s);
            IloAddable con = this.cplex.addLe(this.objectives.get(s), relval - val);
            ret.add(con);
            this.model.remove(con);
        }
        return ret;
    }

    public Map<String, Double> getRelsol() {
        return relsol;
    }

    public Map<String, Double> getPareto() {
        return pareto;
    }


    public void iterate(List<Map<String, Double>> Y) throws IloException{//todo questo e' il metodo ricorsivo della versioine nuova
        boolean solvable = this.solve();
        if(!solvable) return;
        Y.add(this.pareto);
        //System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{<<<<<<<<<<"+this.nodes+" Y:"+Y.size()+" InfN:"+this.infnodes+" gap"+this.cplex.getMIPRelativeGap());

        List<IloAddable> cons = this.branchOn();

        int i = 0;
        for(IloAddable con : cons){
            i++;
            FPProblem son = new FPProblem( this, con);
            son.iterate(Y);
            son.refresh();
        }

    }
    public long getNodes(){
        return this.nodes;
    }
}