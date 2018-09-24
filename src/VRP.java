import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

public class  VRP{

    public static void main(ArrayList<Customer> cstms, Customer dpt) {

        //Random ran = new Random(151190);

        //Problem Parameters
        int NoOfCustomers = cstms.size();
        int NoOfVehicles = 10;
        int VehicleCap = dpt.c;

        //Depot Coordinates
        int Depot_x = (int) dpt.x;
        int Depot_y = (int) dpt.y;

        //Tabu Parameter
        int TABU_Horizon = 10;

        //Initialise
        //Create Random Customers
        Node[] Nodes = new Node[NoOfCustomers + 1];
        Node depot = new Node(Depot_x, Depot_y);

        Nodes[0] = depot;
        for (int i = 1; i <= NoOfCustomers; i++) {
            Nodes[i] = new Node(i, cstms.get(i-1).x, cstms.get(i-1).y, cstms.get(i-1).c);
        }

        double[][] distanceMatrix = new double[NoOfCustomers + 1][NoOfCustomers + 1];
        double Delta_x, Delta_y;
        for (int i = 0; i <= NoOfCustomers; i++) {
            for (int j = i + 1; j <= NoOfCustomers; j++) {

                Delta_x = (Nodes[i].Node_X - Nodes[j].Node_X);
                Delta_y = (Nodes[i].Node_Y - Nodes[j].Node_Y);

                double distance = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y));

                distance = Math.round(distance);               
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }
        
        //Compute the greedy Solution
        Solution s = new Solution(NoOfCustomers, NoOfVehicles, VehicleCap);
        
        s.GreedySolution(Nodes, distanceMatrix);
        s.SolutionPrint("Greedy Solution");
        draw.drawRoutes(s, NoOfCustomers + "Greedy_Solution");
        
        s.IntraRouteLocalSearch(Nodes, distanceMatrix);
        s.SolutionPrint("Solution after Intra-Route Heuristic Neighborhood Search");
        draw.drawRoutes(s, NoOfCustomers + "Intra-Route");

        s.GreedySolution(Nodes, distanceMatrix);
        s.InterRouteLocalSearch(Nodes, distanceMatrix);
        s.SolutionPrint("Solution after Inter-Route Heuristic Neighborhood Search");
        draw.drawRoutes(s, NoOfCustomers + "Inter-Route");
        
        s.GreedySolution(Nodes, distanceMatrix);
        s.TabuSearch(TABU_Horizon, distanceMatrix);
        s.SolutionPrint("Solution After Tabu Search");
        draw.drawRoutes(s, NoOfCustomers + "Tabu_Solution"); 
    }
}