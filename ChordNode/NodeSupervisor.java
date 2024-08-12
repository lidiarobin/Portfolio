import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

public class NodeSupervisor implements INodeSupervisor, Runnable
{

    INodeSupervisor nodesuper = null;

    NodeSupervisor() throws RemoteException 
    {
        super();
        //starts a thread that occasionally updates us about changes

        new Thread(this).start();
    }

    //we create multiple variables
    //the chordlog list keeps a log of all of the nodes we have in ChordNode
    //the chordnames list keeps track of the name we've given it; not the hashed key

    ArrayList<IChordNode> chordlog = new ArrayList<IChordNode>();
    ArrayList<String> chordnames = new ArrayList<>();
    static IChordNode chordnode;
    static String mykey;
    //Client client = new Client();
    Web web = new Web();
    //ChordNode nodes = new ChordNode(mykey);

    public IChordNode listOfNodes(IChordNode node) throws RemoteException
    {
        chordnode.join(node);
        return chordnode;
    }

    //the addNode function adds a new node to our list of nodes
    //we remember both the node and the node's name
    //this can be useful if we're asked for the node's name, rather than its key
    //as we can then do .getKey()
    //and find it
    
    public void addNode(Registry registry, IChordNode node, String nodename) throws AccessException, RemoteException
    {
        System.out.println("Adding a new found node. . . ");

        String[] nodeList = registry.list();
        for(String nodeName: nodeList)
        {
            System.out.println(nodeName);
            if(node.getKey() == (nodeName.hashCode()))
            {
                System.out.println("Found node " + nodeName);
            }
            else
            {
                chordlog.add(node);
                chordnames.add(nodename);
            }
            
        }
    }

    //the getChordLog function just gets us the log we made
    //this is useful as we can check if we've added any nodes
    //and also add new nodes to a random existing one

    public ArrayList<IChordNode> getChordLog() throws RemoteException
    {
        return chordlog;
    }

    //the listalivenodes function goes through all nodes and checks whether they are all alive
    //for clarity sake, i've commented out the "is alive" version
    //as it would take up too much space if we checked if the node is alive constantly
    //it will however let the user know if a node has died
    //and remove it from the list
    public void listAliveNodes() throws RemoteException
    {
        for(IChordNode chordname: chordlog)
        {
            try
            {
                if(chordname.isAlive())
                {
                /** 
                {
                    System.out.println("Node " + chordname.getKey() + " is alive");
                }
                */
            }
            }
            catch(Exception e)
            {
                System.out.println("Node has died");
                chordlog.remove(chordname);
                //Registry registry = LocateRegistry.getRegistry("localhost");
                //registry.unbind();

            }
        }
    }

    public void parseData() throws RemoteException
    {

    }

    //the function below starts the task upon receiving the data
    public void receiveData(byte[] bytes) throws RemoteException, IOException, ParserConfigurationException
    {
        //System.out.println(bytes);

        Random rand = new Random();
        if(!chordlog.isEmpty())
		{   
			int k = rand.nextInt(chordlog.size());

            System.out.println("Starting task in node " + chordlog.get(k).getKey());
            chordnode = chordlog.get(k);

            chordnode.put(chordnames.get(k),bytes);
		}
    }


    //the function below gets a file as requested
    public byte[] getFiles() throws RemoteException
    {
        System.out.println(chordlog.size());
        for(int i = 0; i < chordlog.size(); i++)
        {
            //System.out.println("Looking for files");
            chordnode = chordlog.get(i);

            return chordnode.get(chordnames.get(i));
        }

        return null;
    }

    //this function starts task one and returns the value
    public byte[] getTaskOne() throws RemoteException
    {
        System.out.println("Starting task one");

        for(int i = 0; i < chordlog.size(); i++)
        {
            System.out.println("Looking for files");

            /**chordnode = chordlog.get(i);
            return chordnode.get(chordnames.get(i));
            */

            chordnode = chordlog.get(i);
            if(chordnode.get(chordnames.get(i)) != null)
            {
                return chordnode.get(chordnames.get(i));
            }
        }

        return null;
    }

    public void run()
		{
            while(true)
            {
                try
                {
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					System.out.println("Interrupted");
				}
                try
                {
                    listAliveNodes();
                }catch (Exception e){e.printStackTrace();}
            }
        }

    public static void main(String[] args) throws RemoteException
    {
        NodeSupervisor supervisor = new NodeSupervisor();

        try
        {
            System.out.println("Working on threads...");

            String name = "NodeSupervisor";
            Registry registry = LocateRegistry.getRegistry("localhost");
            supervisor.nodesuper = (INodeSupervisor) UnicastRemoteObject.exportObject((Remote)supervisor, 0);
            registry.rebind(name, (Remote)supervisor.nodesuper);

            
        }
        catch(Exception e)
        {

        }
    }

    // to edit later
    @Override
    public INodeSupervisor getNode() throws RemoteException 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNode'");
    }

}
