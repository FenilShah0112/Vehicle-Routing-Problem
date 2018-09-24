import java.util.ArrayList;
import java.io.*;

public class VRTests {

	public static void main(String[] args)throws Exception {
		String problemdir = "tests/";
		String outdir = "output/";
		String [] shouldPass = {
				"rand00040",
				"rand00050",
				"rand00060"
				};
		//System.out.println("Problem     \tSoln\tCusts\tTrips\tCost\tValid");
		for (int i = 0; i < shouldPass.length; i++){
		
		//	System.out.println(base+"------");
			VRProblem vrp = new VRProblem(problemdir+shouldPass[i]+"prob.csv");
			VRSolution vrs = new VRSolution(vrp);
			VRSolution vrps = new VRSolution(vrp);
			VRSolution vrds = new VRSolution(vrp);

			//Print out results of costing and verifying the solution
			vrs.clarkeWrightSolution(false);
			//System.out.printf("%s\t%s\t%d\t%d\t%.0f\t%s\n",base,"CW",vrp.size(),vrs.soln.size(),vrs.solnCost(),vrs.verify());
			
			vrps.clarkeWrightSolution(true);
			//System.out.printf("%s\t%s\t%d\t%d\t%.0f\t%s\n",base,"CWP",vrp.size(),vrps.soln.size(),vrps.solnCost(),vrps.verify());
			
			VRP.main(vrp.customers, vrp.depot);
			
		}
		
	}
}
