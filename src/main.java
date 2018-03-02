import ilog.concert.IloException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.csvreader.*;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class main {
    public static double eps = 10e-8;
    private static int nval = 3;
    public static void main (String [] args){
        String type = args[0];
        String inputfile = args[1];
        String inputname = args[2];
        String outputfolder = args[3];
        //boolean consider_matrix = Boolean.parseBoolean(args[4]);
        //int nstat = Integer.parseInt(args[5]);

        try {
            main.upperRuns( type, inputfile,inputname, outputfolder);
        }catch (IOException e){
            System.out.println("ERROR something is wrong with the Input/Output");
            e.printStackTrace();
        }

    }

    private static ArrayList<Object> readFromFile( String type, String inputfile)throws FileNotFoundException,IOException{
        if( type.equalsIgnoreCase("2dkp")){
            return read2DKP_secondVersion(inputfile);
        }else if (type.equalsIgnoreCase("ap")){
            return readAP(inputfile);
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

        b[0] = scan.nextInt();
        b[1] = scan.nextInt();
        for(int i = 0; i < n ; i ++) c[0][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) c[1][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) A[0][i] = scan.nextInt();
        for(int i = 0; i < n ; i ++) A[1][i] = scan.nextInt();

        param.add(n);
        param.add(c);
        param.add(A);
        param.add(b);
        param.add(-1);

        return param;
    }

    private static ArrayList<Object> readAP(String inputfile)throws FileNotFoundException{
        ArrayList<Object> parameters = new ArrayList<>();
        return parameters;
    }

    private static ArrayList<Object> readStandardInputFile(String inputfile)throws FileNotFoundException{
        ArrayList<Object> parameters = new ArrayList<>();
        return parameters;
    }

    public static void upperRuns( String type,String inputfile,
                                 String inputname,
                                 String outputfolder) throws IOException{
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

        double[] ret = main.run( type, inputfile);
        outputWriterkh.write(inputname);
        for (int j = 0; j < main.nval; j++) {
            outputWriterkh.write(ret[j]+"");
        }
        outputWriterkh.endRecord();
        outputWriterkh.close();
    }

    public static double[] run( String type, String inputfile){
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

        int n = (Integer) param.get(0);
        double [][] objectives =  (double[][]) param.get(1);
        double [][] matrixA = (double[][]) param.get(2);
        double [] b = (double[]) param.get(3);
        boolean [] binary = new boolean[n];
        int binaries = (Integer) param.get(4);
        boolean allbinaries = (binaries<0? true: false);

        if( binaries < 0){
            for( int i = 0; i < n; i++) binary[i] = true;
            allbinaries = true;
        }else{
            for( int i = 0; i< binaries ; i++) binary[i] = false;
        }


        try{
            FPAlg alg = new FPAlg(norm.L1_norm);
            List<Object> res = alg.solve(objectives, matrixA, b, binary);
            
            List<Map<String,Double>> Y = (List<Map<String,Double>>)res.get(0);
            long totnodes =(long) res.get(1);
            double sec = (double) res.get(2);

            boolean printbool = true;
            //FPAlg.printFrontierCsv(Y,printbool);

            ret[0] = sec;
            ret[1] = Y.size();
            ret[2] = totnodes;
        }catch(IloException e){
            e.printStackTrace();
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
}
