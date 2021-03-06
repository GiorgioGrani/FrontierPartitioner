import ilog.concert.IloException;
import java.io.File;
import java.util.*;

import com.csvreader.*;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class main {
    public static double eps = 10e-8;
    private static int nval = 3;
    public static void main (String [] args){
        String type = args[0];
        String inputfile = args[1];
        String inputname = args[2];
        String outputfolder = args[3];
        boolean quad = Boolean.parseBoolean(args[4]);
        //boolean consider_matrix = Boolean.parseBoolean(args[4]);
        //int nstat = Integer.parseInt(args[5]);

        try {
            main.upperRuns( type, inputfile,inputname, outputfolder, quad);
        }catch (IOException e){
            System.out.println("ERROR something is wrong with the Input/Output");
            e.printStackTrace();
        }

    }

    public static ArrayList<Object> readMPS(String mps) throws FileNotFoundException{
        Scanner scan = new Scanner(new File(mps));
        String name = mps;
        TreeMap<String, String> con_name_type = new TreeMap<>();
        String next = scan.next();
        if(next.equalsIgnoreCase("NAME")){
            name = scan.next();
            next = scan.next();
        }
        if(next.equalsIgnoreCase("ROWS")){
            next = scan.next();
            while( !next.equalsIgnoreCase("COLUMNS")){
                String direction = next;
                String conname = scan.next();
                con_name_type.put(conname, direction);
                next = scan.next();
            }
        }
        TreeMap<String, TreeMap<String, Double>> intvars = new TreeMap<>();
        TreeMap<String, TreeMap<String, Double>> numvars = new TreeMap<>();

        if (next.equalsIgnoreCase("COLUMNS")) {
            next = scan.next();
            while (!next.equalsIgnoreCase("RHS") && !next.equalsIgnoreCase("BOUNDS")) {
                if (next.equalsIgnoreCase("MARK0000") || next.equalsIgnoreCase("MARK") ) {
                    scan.next();
                    next = scan.next();
                    next = scan.next();
                    while (!next.equalsIgnoreCase("MARK0001") && !next.equalsIgnoreCase("MARKEND")) {


                        String var = next;
                        TreeMap<String, Double> submap = new TreeMap<>();

                        while (true) {
                            next = scan.next();
                            //System.out.println(next);
                            if (scan.hasNextDouble()) {
                                Double d = scan.nextDouble();
                                submap.put(next, d);
                            } else if (next.equalsIgnoreCase(var)) {

                            } else {
                                break;
                            }
                        }
                        intvars.put(var, submap);

                    }

                    scan.next();
                    scan.next();
                    next = scan.next();
                }
                if (!next.equalsIgnoreCase("RHS")) {
                    while (!next.equalsIgnoreCase("RHS") && !next.equalsIgnoreCase("BOUNDS") && !next.equalsIgnoreCase("MARK0000") && !next.equalsIgnoreCase("MARK")) {


                        String var = next;
                        TreeMap<String, Double> submap = new TreeMap<>();

                        while (true) {
                            next = scan.next();
                            if (scan.hasNextDouble()) {
                                Double d = scan.nextDouble();
                                submap.put(next, d);
                                //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@           "+d);
                            } else if (next.equalsIgnoreCase(var)) {

                            } else {
                                break;
                            }
                        }
                        numvars.put(var, submap);

                    }

                }

            }

        }

//

        TreeMap<String, Double> rhs = new TreeMap<>();
        if (next.equalsIgnoreCase("RHS")) {
            next = scan.next();
            while (!next.equalsIgnoreCase("BOUNDS") && !next.equalsIgnoreCase("ENDATA") &&  !next.equalsIgnoreCase("RANGES") ) {
                //System.out.println("                                        )) " +next);
                rhs.put(scan.next(), scan.nextDouble());
                next = scan.next();
                if(scan.hasNextDouble()){
                    rhs.put(next, scan.nextDouble());
                    next = scan.next();
                }
                //
            }
        }
        TreeMap<String, Double> upperbound = new TreeMap<>();
        TreeMap<String, Double> lowerbound = new TreeMap<>();

        if (next.equalsIgnoreCase("BOUNDS")) {
            //next = scan.next();
            while (scan.hasNext()) {
                next = scan.next();
                //System.out.println(next);
                if (next.equalsIgnoreCase("UP") || next.equalsIgnoreCase("UI")) {
                    scan.next();
                    String xname = scan.next();
                    upperbound.put(xname, scan.nextDouble());
                    //System.out.println("upupupupupup   "+upperbound.get(xname));
                } else if (next.equalsIgnoreCase("LO") || next.equalsIgnoreCase("LI")) {

                    scan.next();
                    String xname = scan.next();
                    lowerbound.put(xname, scan.nextDouble());
                    //System.out.println("ooooomionodno   "+xname);
                } else if (next.equalsIgnoreCase("FX")) {
                    scan.next();
                    String xname = scan.next();
                    Double xval = scan.nextDouble();
                    lowerbound.put(xname, xval);
                    upperbound.put(xname, xval);
                    //System.out.println(xname+" yyyyyyyyyyyyyyyyyyyyyy  "+xval);
                } else if (next.equals("FR")) {
                    scan.next();
                    String xname = scan.next();
                    //Double xval = scan.nextDouble();
                    lowerbound.put(xname, -(Double.MAX_VALUE-1));
                    upperbound.put(xname, Double.MAX_VALUE);
                    //System.out.println(xname+" yyyyyyyyyyyyyyyyyyyyyy  "+xval);
                } else if (next.equalsIgnoreCase("BV")) {
                    scan.next();
                    String xname = scan.next();
                    //Double xval = scan.nextDouble();
                    lowerbound.put(xname, 0d);
                    upperbound.put(xname, 1d);
                    //System.out.println(xname+" yyyyyyyyyyyyyyyyyyyyyy  "+xval);
                } else if (next.equalsIgnoreCase("MI")) {
                    scan.next();
                    String xname = scan.next();
                    //Double xval = scan.nextDouble();
                    lowerbound.put(xname, -(Double.MAX_VALUE-1));
                    upperbound.put(xname, 0d);
                    //System.out.println(xname+" yyyyyyyyyyyyyyyyyyyyyy  "+xval);
                }else if (next.equalsIgnoreCase("PL")) {
                    scan.next();
                    String xname = scan.next();
                    //Double xval = scan.nextDouble();
                    lowerbound.put(xname, 0d);
                    upperbound.put(xname, Double.MAX_VALUE);
                    //System.out.println(xname+" yyyyyyyyyyyyyyyyyyyyyy  "+xval);
                }else if(next.equalsIgnoreCase("ENDATA")){
                    break;
                }
            }
        }


        TreeMap<String, Integer> codvars = new TreeMap<>();
        TreeMap<String, Integer> codcons = new TreeMap<>();

        int i = 0;
        for(String xi : intvars.keySet()){
            codvars.put(xi, i);
            i++;
        }
        for(String xi : numvars.keySet()){
            codvars.put(xi, i);
            i++;
        }


        int objval = 0;
        i = 0;
        for(String xi : con_name_type.keySet()){
            codcons.put(xi, i);
            if(con_name_type.get(xi).equalsIgnoreCase("N")) objval = i;
            i++;
        }


        int numintvar = intvars.size();
        int n = numintvar + numvars.size();
        int p = con_name_type.size() - 1;

        double [] [] matrix = new double [p] [n];
        double [] b = new double [p];
        int [] directions = new int [p];
        double [][] o = new double [2][n];
        boolean [] binary = new boolean [n];
        double [] lower = new double[n];
        double [] upper = new double[n];
        for(int r = 0; r< upper.length; r++){
            upper[r] = Double.MAX_VALUE;
        }


        double absmaxval = 0;
        for(String xi : intvars.keySet()){
            int k = codvars.get(xi);
            for(String ci : intvars.get(xi).keySet()){
                int h = 0;
                double val = 0;
                for(String con : codcons.keySet()){

                    if(con.equalsIgnoreCase(ci)){
                        h = codcons.get(con);
                        break;
                    }
                }

                val = intvars.get(xi).get(ci);
                if(h==objval){
                    o[0][k] = val;
                    double compval = Math.abs(val);
                    if(compval > absmaxval){
                        absmaxval = compval;
                    }

                }else {
                    if( h > objval) matrix[h-1][k] = val;
                    else matrix[h][k] = val;

                }
            }
        }

        if(absmaxval == 0d){ absmaxval =1d;}
        double order = Math.pow(10,Math.ceil(Math.log10(absmaxval)));
        for(int ind = 0; ind < n ; ind ++){
            o[1][ind] = Math.round((Math.random()-0.5)*order);
        }


        for(String xi : numvars.keySet()){
            int k = codvars.get(xi);
            for(String ci : numvars.get(xi).keySet()){
                int h = 0;
                double val = 0;
                for(String con : codcons.keySet()){

                    if(con.equalsIgnoreCase(ci)){
                        h = codcons.get(con);
                        break;
                    }
                }
                val = numvars.get(xi).get(ci);
                if(h==objval){
                    o[0][k] = val;
                    o[1][k] = Math.round(Math.random()-0.5)*order;
                }else {
                    if( h > objval) matrix[h-1][k] = val;
                    else matrix[h][k] = val;

                }
            }
        }

        for(String ci : rhs.keySet()){
            int h = 0;
            for(String s : codcons.keySet()){
                if(s.equalsIgnoreCase(ci)){
                    h = codcons.get(s);
                    break;
                }
            }
            if (h != objval) {
                if (h > objval) b[h - 1] = rhs.get(ci);
                else b[h] = rhs.get(ci);
            }

        }
        for (String ci : con_name_type.keySet()) {
            int h = 0;
            for (String s : codcons.keySet()) {
                if (s.equalsIgnoreCase(ci)) {
                    h = codcons.get(s);
                    break;
                }
            }
            if (h != objval) {
                if (h > objval) directions[h - 1] = codify(con_name_type.get(ci));
                else directions[h] = codify(con_name_type.get(ci));
            }
        }

        for(String xi : upperbound.keySet()){
            int k = 0;
            for(String s : codvars.keySet()){
                if(s.equalsIgnoreCase(xi)){
                    k = codvars.get(s);
                    break;
                }
            }

            upper[k] = upperbound.get(xi);

        }

        for(String xi : lowerbound.keySet()){
            int k = 0;
            for(String s : codvars.keySet()){
                if(s.equalsIgnoreCase(xi)){
                    k = codvars.get(s);
                    break;
                }
            }

            lower[k] = lowerbound.get(xi);

        }


        ArrayList<Object> param = new ArrayList<>();
        param.add(numintvar);
        param.add(o);
        param.add(matrix);
        param.add(b);
        param.add(lower);
        param.add(upper);
        param.add(directions);

        return param;
    }

    public static int codify(String s){
        if(s.equalsIgnoreCase("L")) return -1;
        else if(s.equalsIgnoreCase("E")) return 0;
        else return 1;
    }

    private static ArrayList<Object> readFromFile( String type, String inputfile)throws FileNotFoundException,IOException{
        if( type.equalsIgnoreCase("2dkp")){
            return read2DKP_secondVersion(inputfile);
        }else if (type.equalsIgnoreCase("ap")){
            return readAP(inputfile);
        }else if  (type.equalsIgnoreCase("mps")){
            return readMPS(inputfile);
        }else if  (type.equalsIgnoreCase("dat")){
            return readDAT(inputfile);
        }
        return readStandardInputFile( inputfile);
    }

    private static ArrayList<Object> read2DKP_secondVersion(String inputfile) throws FileNotFoundException, IOException{
        ArrayList<Object> param = new ArrayList<>();
        Scanner scan = new Scanner(new File(inputfile));
        if(!scan.hasNextInt()) return param;
        int n = scan.nextInt();

        double [] b = new double [2];
        double [][] c = new double [2][n];
        double [][] A = new double [2][n];
        int [] directions = new int [2];

        b[0] = scan.nextInt();
        b[1] = scan.nextInt();
        for(int i = 0; i < n ; i ++) c[0][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) c[1][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) A[0][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) A[1][i] = scan.nextInt();
        directions[0] = -1;
        directions[1] = -1;

        param.add(n);
        param.add(c);
        param.add(A);
        param.add(b);
        param.add(-1);
        param.add(directions);


        return param;
    }

    private static double removeStuff(String s){
        char[] carray = s.toCharArray();
        String res = "";
        for(int i = 0 ; i < carray.length; i++){
            if(carray[i] != '[' && carray[i] != ']' && carray[i] != ','){
                res = res + carray[i];
            }
        }
        return Double.parseDouble(res);
    }

    private static ArrayList<Object> readDAT(String inputfile) throws FileNotFoundException, IOException{
        ArrayList<Object> param = new ArrayList<>();
        Scanner scan = new Scanner(new File(inputfile));
        if(!scan.hasNextInt()) return param;
        int p = scan.nextInt();
        int n = scan.nextInt();
        int m = scan.nextInt();

        double [] b = new double [m];
        double [][] c0 = new double [p][n];
        double [][] c = new double [2][n];
        double [][] A = new double [m][n];
        int [] directions = new int [m];
        double [] lower = new double [n];
        double [] upper = new double [n];
        for(int i = 0 ; i < n; i++){
            upper[i] = Integer.MAX_VALUE;
        }

        for(int i = 0; i < p; i ++){
            for(int j = 0 ; j< n ; j++){
                c0[i][j] = - removeStuff(scan.next());
                if(i < 2){
                    c[i][j] = c0[i][j];
                }
            }
        }
        for(int i = 0; i < m; i ++){
            for(int j = 0 ; j< n ; j++){
                A[i][j] =  removeStuff(scan.next());
            }
        }
        for(int i = 0; i < m; i ++){
            b[i] =  removeStuff(scan.next());
            directions[i] = -1;
        }



        param.add(n);
        param.add(c);
        param.add(A);
        param.add(b);
        param.add(lower);
        param.add(upper);
        param.add(directions);

//System.out.println(p);
//System.out.println(m);
//System.out.println(n);
//        printMatrix("c0", c);
//        printMatrix("A", A);
//        printMatrix("b", b);

        return param;
    }

    private static ArrayList<Object> readAP(String inputfile)throws FileNotFoundException{
        ArrayList<Object> param = new ArrayList<>();
        Scanner scan = new Scanner(new File(inputfile));
        if(!scan.hasNextInt()) return param;
        int njobs = scan.nextInt();
        int n = njobs*njobs;

        double [] b = new double [2*njobs];
        double [][] c = new double [2][n];
        double [][] A = new double [2*njobs][n];
        int [] directions = new int [2*njobs];


        for(int i = 0; i < n ; i ++) c[0][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) c[1][i] = scan.nextInt();
        for ( int h = 0 ; h < njobs; h++) {
            for (int i = njobs*h; i < njobs*(h+1); i++) {
                A[h][i] = 1;
                b[h] = 1;
                b[h+njobs] = 1;
            }
        }
        for ( int h = 0 ; h < njobs; h++) {
            for (int i = 0; i < njobs; i++) {
                A[h+njobs][i*njobs+h] = 1;
            }
        }


        param.add(n);
        param.add(c);
        param.add(A);
        param.add(b);
        param.add(-1);
        param.add(directions);

        return param;
    }

    private static ArrayList<Object> readStandardInputFile(String inputfile)throws FileNotFoundException{
        ArrayList<Object> parameters = new ArrayList<>();
        return parameters;
    }

    public static void upperRuns( String type,String inputfile,
                                 String inputname,
                                 String outputfolder, boolean quad) throws IOException{
        String output = outputfolder +"Stats.csv";

        String outputkh = outputfolder + File.separator+"SingleRun_"+inputname+"_Results.csv";
        boolean check = false;
        if(!new File(output).exists()){
            check = true;
        }
        FileWriter file = new FileWriter(output, !check);
        CsvWriter outputWriterkh = new CsvWriter(file, ',');
        if(check) {
            outputWriterkh.write("Name");

            outputWriterkh.write("FPA_time");
            outputWriterkh.write("FPA_total_points");
            outputWriterkh.write("FPA_total_nodes");
            outputWriterkh.endRecord();
        }

        double[] ret = main.run( type, inputfile, quad);
        outputWriterkh.write(inputname);
        for (int j = 0; j < main.nval; j++) {
            outputWriterkh.write(ret[j]+"");
        }
        outputWriterkh.endRecord();
        outputWriterkh.close();
    }

    public static double[] run( String type, String inputfile, boolean quad){
        double [] ret = new double [main.nval];

        ArrayList<Object> param = new ArrayList<>();
        try{
            param = readFromFile(type, inputfile);

        }catch( FileNotFoundException f){
            f.printStackTrace();
            return ret;
        }catch(IOException e){
            e.printStackTrace();
            return ret;
        }

        int n ;
        double [][] objectives ;
        double [][] matrixA ;
        double [] b ;
        boolean [] binary = new boolean[1];
        int binaries ;
        int [] directions;
        double [] lower = new double [1];
        double [] upper = new double [1];


if(type.equalsIgnoreCase("mps") || type.equalsIgnoreCase("dat")){
    n = (Integer) param.get(0);
    objectives =  (double[][]) param.get(1);
    matrixA = (double[][]) param.get(2);
    //printMatrix("A",matrixA);
    b = (double[]) param.get(3);
    lower = (double[]) param.get(4);
    upper = (double[]) param.get(5);
    directions = (int [] ) param.get(6);
}else{
    n = (Integer) param.get(0);
    objectives =  (double[][]) param.get(1);
    matrixA = (double[][]) param.get(2);
    b = (double[]) param.get(3);
    binary = new boolean[n];
    binaries = (Integer) param.get(4);
    directions = (int [] ) param.get(5);

    if( binaries < 0){
        for( int i = 0; i < n; i++) binary[i] = true;
    }else{
        for( int i = 0; i< binaries ; i++) binary[i] = false;
    }


}

if(!quad) {


    try {
        FPAlg alg = new FPAlg(norm.L1_norm);
        List<Object> res;
        if (type.equalsIgnoreCase("mps") || type.equalsIgnoreCase("dat")) {
            res = alg.solve(objectives, matrixA, b, lower, upper, directions);
        } else {
            res = alg.solve(objectives, matrixA, b, binary, directions);
        }

        List<Map<String, Double>> Y = (List<Map<String, Double>>) res.get(0);
        long totnodes = (long) res.get(1);
        double sec = (double) res.get(2);

        boolean printbool = true;
        //FPAlg.printFrontierCsv(Y,printbool);

        ret[0] = sec;
        ret[1] = Y.size();
        ret[2] = totnodes;
    } catch (IloException e) {
        e.printStackTrace();
    }
}else{
    try {
        double [][][] qobjs = qobjs(objectives, inputfile);
        FPAlgQuad alg = new FPAlgQuad(norm.L1_norm);
        List<Object> res;
        if (type.equalsIgnoreCase("mps") || type.equalsIgnoreCase("dat")) {
            res = alg.solve(qobjs, objectives, matrixA, b, lower, upper, directions);
        } else {
            res = alg.solve(qobjs, objectives,  matrixA, b, binary, directions);
        }

        List<Map<String, Double>> Y = (List<Map<String, Double>>) res.get(0);
        long totnodes = (long) res.get(1);
        double sec = (double) res.get(2);

        boolean printbool = true;
        //FPAlg.printFrontierCsv(Y,printbool);

        ret[0] = sec;
        ret[1] = Y.size();
        ret[2] = totnodes;
    } catch (IloException e) {
        e.printStackTrace();
    }
}
        return ret;
    }

    public static ArrayList<Object> RANDOMINSIDEABOX(boolean allbinaries, int nsize){
        int m = 2;
        int n = nsize;//nsize;// (int) Math.round(Math.random()*200);
        int p = 5*n;//(int) Math.round(Math.random()*200);
        int ord = 100;
        //System.out.println(m+" "+n+" "+p);


        double [] [] matrix = new double [p+2*n] [n];
        double [] b = new double [p+2*n];
        double [][] o = new double [m][n];
        double [][][] matrixO = main.generateRandomPositiveSemidefiniteMatrix(m,n,2*n,ord);
        boolean [] binary = new boolean [n];

        for(int i = 0; i<p; i++){
            for(int j = 0; j<n; j++)
                matrix[i][j] = Math.round(Math.random()*21-1);
        }
        for(int i = 0; i<p; i++){
            b[i] =Math.round(10 - Math.random()*5);
        }

        double val =Math.round(1*L1NormBounder(matrix, b));

        for(int i = p; i<(p+2*n); i++){
            for(int j = 0; j<n; j++)
                if(i==p+j) {
                    matrix[i][j] = -1;
                }else if( i == p+n+j){
                    matrix[i][j] = 1;
                }
        }
        for(int i = p; i<(p+2*n); i++){
            if(i<p+n) {
                b[i] = -val;
            }else{
                b[i] = -val;
            }
        }
        //////////////////////////////////////





        for(int i = 0; i<m; i++){
            for(int j = 0; j<n; j++)
                o[i][j] = Math.round(Math.random()*ord);//-50;
        }



        if(allbinaries){
            for(int i = 0 ; i < n ; i++){
                binary[i] = true;
            }
        }else {
            int count = 0;
            loop:
            while (true) {
                for (int i = 0; i < n; i++) {
                    if (Math.random() > 0.5d && !binary[i]) {
                        binary[i] = true;
                        count++;
                    }
                    if (count >= n / 2) {
                        break loop;
                    }

                }
            }
        }

        ArrayList<Object> ret = new ArrayList<>();
        ret.add(o);
        ret.add(matrix);
        ret.add(b);
        ret.add(binary);
        ret.add(matrixO);


        return ret;
    }


    public static double L1NormBounder( double[][] a, double [] b){
        double ret = 10e-200;
        for(int i = 0; i<a.length;i++){
            if(Math.abs(b[i]) > eps) {
                double maxb = b[i];
                for (int j = 0; j < a[0].length; j++) {
                    if (Math.abs(a[i][j]) > eps) {
                        double maxa = a[i][j];
                        double val = Math.abs(maxb/maxa);
                        if( val > ret){
                            ret = val;
                        }
                    }
                }
            }
        }
        return ret;
    }
    public static void printMatrix(String name, double[][] a){
        System.out.println("Matrix Printer-- Matrix Name: "+name);
        for(int i = 0; i<a.length;i++){
            for(int j = 0; j<a[0].length; j++){
                System.out.print(a[i][j]+" ");
            }
            System.out.println();
        }
    }
    public static void printMatrix(String name, double[] a){
        System.out.println("Matrix Printer-- Matrix Name: "+name);
        for(int i = 0; i<a.length;i++){
            System.out.println(a[i]+", ");
        }
    }

    public static ArrayList<Object> BOX(){
        int m = 2;
        int n = 3;// (int) Math.round(Math.random()*200);
        int p = 2*n;//(int) Math.round(Math.random()*200);
        System.out.println(m+" "+n+" "+p);


        double [] [] matrix = new double [p] [n];
        double [] b = new double [p];
        double [][] o = new double [m][n];

        for(int i = 0; i<p; i++){
            for(int j = 0; j<n; j++)
                if(i==j) {
                    matrix[i][j] = -1;
                }else if( i == n+j){
                    matrix[i][j] = 1;
                }
        }
        for(int i = 0; i<p; i++){
            if(i<n) {
                b[i] = -1;
            }else{
                b[i] = 2;
            }
        }
        for(int i = 0; i<m; i++){
            for(int j = 0; j<n; j++) {
                o[0][j] = 1;
                o[1][0] = -1;
                o[1][1] = 0;
                o[1][2] = 0;
            }
        }

        ArrayList<Object> ret = new ArrayList<>();
        ret.add(o);
        ret.add(matrix);
        ret.add(b);
        return ret;
    }


    public static double [][][] generateRandomPositiveSemidefiniteMatrix(int a, int b, int c, int ord){
        double[][][] ret = new double[a][b][c];
        double[][][] piv = new double[a][b][b];


        for(int i = 0; i< a ; i++){
            for(int h = 0; h < b; h++){
                for(int k = 0; k < c; k++){
                    ret[i][h][k] = Math.round(Math.random()*ord)-ord/2;
                }
            }
        }

        for(int i = 0; i< a ; i++){
            for(int h = 0; h < b; h++){
                for(int k = 0; k < b; k++){
                    piv[i][h][k] = main.vectorialProd(ret[i][h], ret[i][k]);
                }
            }
        }


        return piv;
    }

    public static double vectorialProd(double [] a, double [] b){
        int n = a.length;
        if( n != b.length){
            return Double.NaN;
        }

        double ret = 0;

        for(int i = 0; i< n ; i ++){
            ret += a[i]*b[i];
        }
        return ret;
    }
//pippo
    /*public static double [][][] qobjs(double [][] objs, String inputname){
        double [][][] ret = new double [2][2][2];

        ret[0][0][0] = 1d;
        ret[1][1][1] = 1d;

        return ret ;
    }*/

    public static double [][][] qobjs(double [][] objs, String inputname){
        int n = objs.length;
        int m = objs[0].length;

        double [][][] ret = new double [n][m][m];
        Random rand = new Random();
        int hash = inputname.hashCode();
        System.out.println("Objective generator entered. HashCode: "+hash+" n: "+n+" m: "+m);
        rand.setSeed(hash);
        for(int i = 0; i < n; i++){
            int rank = approx(1,m, rand.nextDouble());
            System.out.println("                            Rank("+i+"): "+rank);
            double [][] obj = new double [m][rank];
            for(int h = 0; h < m; h++){
                for(int k = 0 ; k< rank ; k++){
                    obj[h][k] = approx(0,2, rand.nextDouble());
                }
            }
            for(int h = 0; h < m; h++){
                for(int k = 0 ; k< m ; k++){
                    double val = 0;
                    for(int z = 0; z < rank; z++){
                        val += obj[h][z]*obj[k][z];
                    }
                    ret[i][h][k] = val;
                }
            }
            // printMatrix("pippo", ret[i]);

        }

        return ret;
    }
    /*public static double [][][] qobjs(double [][] objs, String inputname){
        int n = objs.length;
        int m = objs[0].length;

        double [][][] ret = new double [n][m][m];
        Random rand = new Random();
        int hash = inputname.hashCode();
        System.out.println("Objective generator entered. HashCode: "+hash+" n: "+n+" m: "+m);
        rand.setSeed(hash);
        for(int i = 0; i < n; i++){
            for(int j = 0 ; j < m ; j ++){
                ret[i][j][j] = Math.pow(objs[i][j],2);
            }
             //printMatrix("pippo", ret[i]);

        }

        return ret;
    }*/

    public static  int approx(double low, double up, double val){
        double ret = val*(up - low) + low;
        int rret = (int) Math.floor(ret + 0.5);
        return rret;
    }
}
