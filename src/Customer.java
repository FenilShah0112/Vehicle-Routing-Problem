import java.awt.geom.Point2D;


public class Customer extends Point2D.Double{

	// Requirements of the customer (number to be delivered)
	public int c;
	public int id;
	public Customer(int x, int y, int requirement, int i) {
		this.x = x;
		this.y = y;
		this.c = requirement;
		this.id = i;
	}
}


class Node {

    public int NodeId;
    public double Node_X ,Node_Y; //Node Coordinates
    public int demand; //Node Demand if Customer
    public boolean IsRouted;
    private boolean IsDepot; //True if it Depot Node

    public Node(double depot_x,double depot_y) //Constructor for depot
    {
        this.NodeId = 0;
        this.Node_X = depot_x;
        this.Node_Y = depot_y;
        this.IsDepot = true;
    }

    public Node(int id ,double x, double y, int demand) //Constructor for Customers
    {
        this.NodeId = id;
        this.Node_X = (int) x;
        this.Node_Y = (int) y;
        this.demand = demand;
        this.IsRouted = false;
        this.IsDepot = false;
    }
   
}

