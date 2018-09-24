import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;


class Route implements Comparable<Route>
{
	private int _capacity;
	private int _weight;
	private double _cost;
	private double _savings;
	public ArrayList<Customer> customers;

	private void calculateSavings(){
		double originalCost = 0;
		double newCost = 0;
		double tempcost =0;
		Customer prev = null;

		//Foreach customer in the route:
		for(Customer c:customers){ 
			// Distance from Depot
			tempcost = Math.sqrt((c.x*c.x)+(c.y*c.y));
			originalCost += (2.0*tempcost);

			if(prev != null){
				// Distance from previous customer to this customer
				double x = (prev.x - c.x);
				double y = (prev.y - c.y);
				newCost += Math.sqrt((x*x)+(y*y));
			}else{
				//If this is the first customer in the route, no change
				newCost += tempcost;
			}
			prev = c;
		}
		newCost += tempcost;
		_cost = newCost;
		_savings = originalCost - newCost;
	}

	public Route(int capacity){
		_capacity = capacity;
		customers = new ArrayList<Customer>();
		_weight =0;
		_cost= 0;
		_savings =0;
	}

	public void addCustomer(Customer c, boolean order){
		//Add customer to the start or end of the route?
		if(order){
			customers.add(0,c);
		}else{
			customers.add(c);
		}

		if(c.c > _capacity){
			System.out.println("Customer order too large");
		}

		_weight += c.c;

		if(_weight > _capacity){
			System.out.println("Route Overloaded");
		}

		calculateSavings();
	}

	public double getSavings(){
		return _savings;
	}
	public double getCost(){
		return _cost;
	}
	public int getWeight(){
		return _weight;
	}
	public int compareTo(Route r) {
		return Double.compare(r.getSavings(), this._savings);
	}

}

//
//##Sequential solver##
//

public class ClarkeWright 
{
	public static int truckCapacity = 0;
	public static Customer depot;

	public static ArrayList<List<Customer>> solve(ArrayList<Customer> customers, Customer dept){
		ArrayList<List<Customer>> solution = new ArrayList<List<Customer>>();
		HashSet<Customer> abandoned = new HashSet<Customer>();

		//calculate the savings of all the pairs
		ArrayList<Route> pairs = new ArrayList<Route>(); 
		depot = dept;
		for(int i=0; i<customers.size(); i++){
			for(int j=i+1; j<customers.size(); j++){
				Route r = new Route(truckCapacity);
				r.addCustomer(customers.get(i),false);
				r.addCustomer(customers.get(j),false);
				pairs.add(r);
			}
		}
		//order pairs by savings
		Collections.sort(pairs);		

		HashSet<Route> routes = new HashSet<Route>();
		routes.add(pairs.get(0));
		pairs.remove(0);

		//start combining pairs into routes
		for(Route ro :routes)
		{			
			outerloop: for(int i=0; i<pairs.size(); i++){
				Route r = pairs.get(i);
				Customer c1 = r.customers.get(0);
				Customer c2 = r.customers.get(r.customers.size()-1);
				Customer cr1 = ro.customers.get(0);
				Customer cr2 = ro.customers.get(ro.customers.size()-1);

				boolean edge = false;
				for(int a=0; a<2;a++)
				{
					edge = !edge;
					Customer e1 = (!edge) ? c1 : c2;
					Customer e2 = (edge) ? c1 : c2;
					//do they have any common nodes?
					if(e1 == cr1 ||e1 == cr2){
						//could we combine these based on weight?
						if(e2.c + ro.getWeight() <= truckCapacity){
							//Does route already contain BOTH these nodes?
							if(!ro.customers.contains(e2)){
								//no, but is it in another route already?
								boolean istaken = false;
								for(Route rr :routes)
								{
									if(rr.customers.contains(e2)){
										istaken = true;
										break;
									}
								}
								if(!istaken){
									//No other route have this, add to route
									if(e1 == cr1){
										ro.addCustomer(e2, true);
									}else{
										ro.addCustomer(e2, false);
									}
								}
							}
							abandoned.remove(e2);
							pairs.remove(r);
							i--;
							continue outerloop;
						}
					}
				}

				//If we reach here, the pair hasn't been added to any routes			
				boolean a = false;
				boolean b = false;
				for(Route rr :routes){
					if(rr.customers.contains(c1)){
						a = true;
					}
					if(rr.customers.contains(c2)){
						b = true;
					}
				}
				if(!(a||b)){
					//no routes have any of these customers, make new route
					abandoned.remove(c1);
					abandoned.remove(c2);
					routes.add(r);
				}else{
					//Some routes have some of these customers already
					if(!a){
						abandoned.add(c1);
					}
					if(!b){
						abandoned.add(c2);
					}
				}
				pairs.remove(r);
				i--;
			}

		}

		//A Customer can be left over due to capacity constraints
		outerloop:for(Customer C:abandoned){
			//we could tack this onto the end of a route if it would fit
			for(Route r:routes){
				if(r.getWeight() + C.c < truckCapacity)
				{
					//would this be more efficient than sending a new truck?
					Customer[] cca={r.customers.get(r.customers.size()-1),
					r.customers.get(0)};
					for(Customer cc:cca){
						double X = C.x - cc.x;
						double Y = C.y - cc.y;
						if(Math.sqrt((X*X)+(Y*Y)) 
						< Math.sqrt((C.x*C.x)+(C.y*C.y)))
						{
							r.addCustomer(C, false);
							break outerloop;
						}
					}
				}
			}

			//Send a new truck, just for this Customer
			ArrayList<Customer> l = new ArrayList<Customer>();
			l.add(C);
			solution.add(l);
		}

		//output
		int i = 0;
		System.out.println("=========================================================");
		System.out.println("Solution after Clarke Wright Serial Algorithm");
		System.out.println();
		for(Route r:routes) {
			i++;
			ArrayList<Customer> l = new ArrayList<Customer>();
			l.add(depot);
			l.addAll(r.customers);
			l.add(depot);
			System.out.print("Vehicle "+ i +" :");
			for(int j = 0; j < l.size()-1;j++)
				{
					System.out.print(l.get(j).id+ "->");
				}			
				System.out.println(l.get(l.size()-1).id);
				
			solution.add(l);
		}
		drawRoutes(solution, customers.size() + "CWS_Solution");
		return solution;
	}

	//
	//##Parallel solver##
	//

	public static ArrayList<List<Customer>> solveP(ArrayList<Customer> customers, Customer dept){
		ArrayList<List<Customer>> solution = new ArrayList<List<Customer>>();
		HashSet<Customer> abandoned = new HashSet<Customer>();

		//calculate the savings of all the pairs
		ArrayList<Route> pairs = new ArrayList<Route>(); 
		depot = dept;
		for(int i=0; i<customers.size(); i++){
			for(int j=i+1; j<customers.size(); j++){
				Route r = new Route(truckCapacity);
				r.addCustomer(customers.get(i),false);
				r.addCustomer(customers.get(j),false);
				pairs.add(r);
			}
		}
		//order pairs by savings
		Collections.sort(pairs);		

		HashSet<Route> routes = new HashSet<Route>();
		routes.add(pairs.get(0));
		pairs.remove(0);

		//start combining pairs into routes
		outerloop: for(int j=0; j<pairs.size(); j++){
			Route r = pairs.get(j);
			Customer c1 = r.customers.get(0);
			Customer c2 = r.customers.get(r.customers.size()-1);

			for(Route ro :routes)
			{
				Customer cr1 = ro.customers.get(0);
				Customer cr2 = ro.customers.get(ro.customers.size()-1);
				boolean edge = false;
				for(int a=0; a<2;a++)
				{
					edge = !edge;
					Customer e1 = (!edge) ? c1 : c2;
					Customer e2 = (edge) ? c1 : c2;
					//do they have any common nodes?
					if(e1 == cr1 || e1 == cr2){
						//could we combine these based on weight?
						if(e2.c + ro.getWeight() <= truckCapacity){
							//Does route already contain BOTH these nodes?
							if(!ro.customers.contains(e2)){
								//no, but is it in another route already?
								boolean istaken = false;
								for(Route rr :routes){
									if(rr.customers.contains(e2)){
										istaken = true;
										break;
									}
								}
								if(!istaken){
									//No other route have this, add to route.
									if(c1 == cr1){
										ro.addCustomer(e2, true);
									}else{
										ro.addCustomer(e2, false);
									}
								}
							}
							abandoned.remove(e2);
							pairs.remove(r);
							j--;
							continue outerloop;
						}
					}
				}
				
			}

			//If we reach here, the pair hasn't been added to any routes			
			boolean a = false;
			boolean b = false;
			for(Route ro :routes){
				if(ro.customers.contains(c1)){
					a = true;
				}
				if(ro.customers.contains(c2)){
					b = true;
				}
			}
			if(!(a||b)){
				//no routes have any of these customers, make new route
				abandoned.remove(c1);
				abandoned.remove(c2);
				routes.add(r);
			}else{
				//Some routes have some of these customers already
				if(!a){
					abandoned.add(c1);
				}
				if(!b){
					abandoned.add(c2);
				}
			}
			pairs.remove(r);
			j--;

		}

		//A Customer can be left over due to capacity constraints
		outerloop:for(Customer C:abandoned){
			//we could tack this onto the end of a route if it would fit
			for(Route r:routes){
				if(r.getWeight() + C.c < truckCapacity)
				{
					//would this be more efficient than sending a new truck?
					Customer[] cca={r.customers.get(r.customers.size()-1),
					r.customers.get(0)};
					for(Customer cc:cca){
						double X = C.x - cc.x;
						double Y = C.y - cc.y;
						if(Math.sqrt((X*X)+(Y*Y)) 
						< Math.sqrt((C.x*C.x)+(C.y*C.y)))
						{
							r.addCustomer(C, false);
							break outerloop;
						}
					}
				}
			}

			//Send a new truck, just for this Customer
			ArrayList<Customer> l = new ArrayList<Customer>();
			l.add(C);
			solution.add(l);
		}

		//output
		int i = 0;
		System.out.println("=========================================================");
		System.out.println("Solution after Clarke Wright Parallel Algorithm");
		System.out.println();
        for(Route r:routes) {
			i++;
			ArrayList<Customer> l = new ArrayList<Customer>();
			l.add(depot);
			l.addAll(r.customers);
			l.add(depot);
			System.out.print("Vehicle "+ i +" :");
			for(int j = 0; j < l.size()-1;j++)
				{
					System.out.print(l.get(j).id+ "->");
				}			
				System.out.println(l.get(l.size()-1).id);
				
			solution.add(l);
		}
		drawRoutes(solution, customers.size() + "CWP_Solution");
		return solution;
	}

	private static void drawRoutes(ArrayList<List<Customer>> s, String fileName) {


        int VRP_Y = 800;
        int VRP_INFO = 200;
        int X_GAP = 600;
        int margin = 30;
        
        int marginNode = 1;


        int XXX = VRP_INFO + X_GAP;
        int YYY = VRP_Y;


        BufferedImage output = new BufferedImage(XXX, YYY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = output.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, XXX, YYY);
        g.setColor(Color.BLACK);


        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (int k = 0; k < s.size() ; k++)
        {
            for (int i = 0; i < s.get(k).size(); i++)
            {
                Customer n = s.get(k).get(i);
                if (n.x > maxX) maxX = n.x;
                if (n.x < minX) minX = n.x;
                if (n.y > maxY) maxY = n.y;
                if (n.y < minY) minY = n.y;

            }
        }

        int mX = XXX - 2 * margin;
        int mY = VRP_Y - 2 * margin;

        int A, B;
        if ((maxX - minX) > (maxY - minY))
        {
            A = mX;
            B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
            if (B > mY)
            {
                B = mY;
                A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
            }
        }
        else
        {
            B = mY;
            A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
            if (A > mX)
            {
                A = mX;
                B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
            }
        }

        // Draw Route
        for (int i = 0; i < s.size() ; i++)
        {
            for (int j = 1; j < s.get(i).size() ; j++) {
                Customer n;
                n = s.get(i).get(j-1);

                int ii1 = (int) ((double) (A) * ((n.x - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj1 = (int) ((double) (B) * (0.5 - (n.y - minY) / (maxY - minY)) + (double) mY / 2) + margin;

                n = s.get(i).get(j);
                int ii2 = (int) ((double) (A) * ((n.x - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj2 = (int) ((double) (B) * (0.5 - (n.y - minY) / (maxY - minY)) + (double) mY / 2) + margin;


                g.drawLine(ii1, jj1, ii2, jj2);
            }
        }

        for (int i = 0; i < s.size() ; i++)
        {
            for (int j = 0; j < s.get(i).size() ; j++) {

                Customer n = s.get(i).get(j);

                int ii = (int) ((double) (A) * ((n.x  - minX) / (maxX - minX) - 0.5) + (double) mX / 2) + margin;
                int jj = (int) ((double) (B) * (0.5 - (n.y - minY) / (maxY - minY)) + (double) mY / 2) + margin;
                if (i != 0) {
                    g.fillOval(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode); //2244
                    String id = Integer.toString(n.id);
                    g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                } else {
                    g.fillRect(ii - 3 * marginNode, jj - 3 * marginNode, 6 * marginNode, 6 * marginNode);  //4488
                    String id = Integer.toString(n.id);
                    g.drawString(id, ii + 6 * marginNode, jj + 6 * marginNode); //88
                }
            }

        }

        String cst = "VRP solution for CW";
        g.drawString(cst, 10, 10);

        fileName = "output/" + fileName + ".png";
        File f = new File(fileName);
        try
        {
            ImageIO.write(output, "PNG", f);
        } catch (IOException ex) {
            //  Logger.getLogger(s.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}