import ilog.concert.IloException;
import ilog.concert.IloAddable;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import java.util.concurrent.*;


public class FPAlg {
    private long timemax = Long.MAX_VALUE;
    private norm nmode = norm.L1_norm;

    public FPAlg(){

    }

    public FPAlg(norm nmode){
        this.nmode = nmode;
    }

    public int codifyNorm(){
        int nmodeint = 0;
        if(this.nmode == norm.L1_norm){
            nmodeint = 1;
        }else if(this.nmode == norm.L2_norm){
            nmodeint = 2;
        }else if(this.nmode == norm.Random_Weights){
            nmodeint = 3;
        }
        return nmodeint;
    }


    public List<Object> solve(double [][] objectives,
                              double [][] matrixA, double[] b,
                              boolean [] binary, int[] directions
    ) throws IloException{
        int nmodeint = this.codifyNorm();


        FPProblem root = new FPProblem(objectives, matrixA, b,binary, nmodeint, directions);
        List<Map<String, Double>> Y = new ArrayList<>();

        long start = System.currentTimeMillis();
        root.iterate(Y);
        long end = System.currentTimeMillis();

        List<Object> ret = new ArrayList<>();
        ret.add(0,Y);
        ret.add(1,(root.getNodes()));
        ret.add(2,(end - start+ 0.0)/1000d);

        return ret;
    }

    public List<Object> solve(double [][] objectives,
                               double [][] matrixA, double[] b,
                              double[] lower, double[] upper, int [] directions
    ) throws IloException{
        int nmodeint = this.codifyNorm();


        FPProblem root = new FPProblem(objectives, matrixA, b, lower, upper, nmodeint, directions);
        List<Map<String, Double>> Y = new ArrayList<>();

        long start = System.currentTimeMillis();
        root.iterate(Y);
        long end = System.currentTimeMillis();
        FPAlg.printFrontierCsv(Y,true);

        List<Object> ret = new ArrayList<>();
        ret.add(0,Y);
        ret.add(1,(root.getNodes()));
        ret.add(2,(end - start+ 0.0)/1000d);

        return ret;
    }

    public static void printFrontier(List<Map<String, Double>> Y){
        int i = 1;
        System.out.println("---Optimal frontier FPA---");
        for(Map<String, Double> y : Y ){
            System.out.print("Point: "+i+"    (");
            for(String s : y.keySet()){
                System.out.print(y.get(s)+" ");
            }
            System.out.print(")\n");
            i++;
        }
    }

    public static void printFrontierCsv(List<Map<String, Double>> Y, boolean printbool){
        if(!printbool) return;
        int i = 1;
        System.out.println("---Optimal frontier FPA---");
        for(Map<String, Double> y : Y ){
            for(String s : y.keySet()){
                System.out.print(y.get(s)+", ");
            }
            System.out.print("\n");
            i++;
        }
    }

}

