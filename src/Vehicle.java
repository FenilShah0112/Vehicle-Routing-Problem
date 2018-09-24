import java.util.ArrayList;

public class Vehicle {

    public int VehId;
    public ArrayList<Node> Route = new ArrayList<Node>();
    public int capacity;
    public int load;
    public int CurLoc;
    public boolean Closed;

    public Vehicle(int id, int cap)
    {
        this.VehId = id;
        this.capacity = cap;
        this.load = 0;
        this.CurLoc = 0; //In depot Initially
        this.Closed = false;
        this.Route.clear();
    }

    public void AddNode(Node Customer )//Add Customer to Vehicle Route
    {
        Route.add(Customer);
        this.load +=  Customer.demand;
        this.CurLoc = Customer.NodeId;
    }

    public boolean CheckIfFits(int dem) //Check if we have Capacity Violation
    {
        return ((load + dem <= capacity));
    }
}