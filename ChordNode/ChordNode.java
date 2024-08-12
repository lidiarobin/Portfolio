import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

class Finger
{
	public int key;
	public IChordNode node;
}

class Store
{
	String key;
	byte[] value;
}

public class ChordNode implements Runnable, IChordNode
{
	static final int KEY_BITS = 8;
	static Store store = new Store();
	
	//for each peer link that we have, we store a reference to the peer 
	//node plus a "cached" copy of that node's key; this means that whenever we 
	//change e.g. our successor reference we also set successorKey by doing successorKey = successor.getKey()
	IChordNode successor;
	int successorKey;
	
	IChordNode predecessor;
	int predecessorKey;
	
	//my finger table; note that all "node" entries will initially be "null"; your code should handle this
	int fingerTableLength;
	Finger finger[];
	int nextFingerFix;

	IChordNode intercode = null;
	
	Vector<Store> dataStore = new Vector<Store>();
	
	//note: you should always use getKey() to get a node's key; this will make the transition to RMI easier
	private int myKey;
	
	ChordNode(String myKeyString) throws RemoteException
		{
			super();

			myKey = hash(myKeyString);
			
			successor = this;
			successorKey = myKey;
			
			//initialise finger table (note all "node" links will be null!)
			finger = new Finger[KEY_BITS];
			//Store storefingers = new Store();

			for (int i = 0; i < KEY_BITS; i++)
			{
				finger[i] = new Finger();
				//finger[i].key = i;
				//finger[i].node = this;

				//System.out.println(finger[i].key + "\n" + finger[i].node);
			}
			fingerTableLength = KEY_BITS;
			
			//start up the periodic maintenance thread
			new Thread(this).start();
		}
	
	// -- API functions --
	
	public void put(String key, byte[] value) throws RemoteException, IOException, ParserConfigurationException
		{
			//find the node that should hold this 
			//key and add the key and value to that node's local store
			//the reason we use the 'aux' variables is to create a temporary set of
			//values we can modify and test
			//before committing them to the store

			System.out.println("Adding file to node n" + hash(key));

			Store auxStore = new Store();
			int auxKey = hash(key);
			IChordNode auxNode = findSuccessor(auxKey);

			auxStore.key = Integer.toString(auxNode.getKey());
			auxStore.value = auxNode.doTaskOne(auxNode,value);

			System.out.println("Task completed in node n" + auxNode.getKey());

			dataStore.add(auxStore);

		}
	
	public byte[] get(String key) throws RemoteException
		{
			//find the node that should hold this key, request the corresponding value from that node's local store, and return it
			//once again, we create a temporary variable which
			//checks if a value exists already in the final variable
			//and returns it if so

			byte[] auxValue;
			int auxKey = hash(key);
			IChordNode auxNode = findSuccessor(auxKey);
			String intNode = Integer.toString(auxNode.getKey());
			
			for(int i = 0 ; i < dataStore.size() ; i++)
			{
				if(dataStore.get(i).key.equals(intNode))
				{
					auxValue = dataStore.get(i).value;

					//System.out.println("Value is: " + auxValue);
					return auxValue;

				}
			}
		
			return null;
		}	
		
	// -- state utilities --
	
	public int getKey() throws RemoteException
		{
			return myKey;
		}
	
	public IChordNode getPredecessor() throws RemoteException
		{
			//System.out.println(predecessor.getKey());
			return predecessor;
		}
	
	// -- topology management functions --
	public void join(IChordNode atNode) throws RemoteException
		{
			predecessor = null;
			predecessorKey = 0;

			successor = atNode.findSuccessor(myKey);
			successorKey = successor.getKey();
			
		}
	
	// -- utility functions --
	public IChordNode findSuccessor(int key) throws RemoteException
		{
			//the findSuccessor class checks if we already have a set succesor, if it's our current one
			//or if it belongs to a certain range
			//if it exists, we return it
			//otherwise we will create a new successor by using the closest preceeding node method
			successorKey = successor.getKey(); 

			if(successor == this || (successor != null) && isInHalfOpenRangeR(key,myKey,successorKey))
			{
				return successor;
			}

			try
			{
				IChordNode auxNode = closestPrecedingNode(key);
				//successorKey = successor.getKey();
				if(auxNode != null && auxNode != this)
				{
					return(auxNode.findSuccessor(key));
				}
				return this;
			}
			catch(Exception e)
			{
				return null;
			}
		}
	
	public IChordNode closestPrecedingNode(int key) throws RemoteException
		{
			//the closest preceeding node method checks if
			//we've already created a value for our key in the finger table
			//and then returns it if so
			
			for(int i = fingerTableLength - 1; i >= 0 && i < fingerTableLength; i--)
			{
				if(isInOpenRange(finger[i].key, myKey, key))
					{
						return finger[i].node;
					}
			}
			return this;
		}
	
	// -- range check functions; they deal with the added complexity of range wraps --
	
	// x is in [a,b] ?
	boolean isInClosedRange(int key, int a, int b)
		{
			if (b > a) return key >= a && key <= b;
			else return key >= a || key <= b;
		}
	
	// x is in (a,b) ?
	boolean isInOpenRange(int key, int a, int b)
		{
			if (b > a) return key > a && key < b;
			else return key > a || key < b;
		}
	
	// x is in [a,b) ?
	boolean isInHalfOpenRangeL(int key, int a, int b)
		{
			if (b > a) return key >= a && key < b;
			else return key >= a || key < b;
		}
	
	// x is in (a,b] ?
	boolean isInHalfOpenRangeR(int key, int a, int b)
		{
			if (b > a) return key > a && key <= b;
			else return key > a || key <= b;
		}
	
	// -- hash functions --
	//this function converts a string "s" to a key that can be used with the DHT's API functions
	public int hash(String s) throws RemoteException
		{
			int hash = 0;
			
			for (int i = 0; i < s.length(); i++)
				hash = hash * 31 + (int) s.charAt(i);
			
			if (hash < 0) hash = hash * -1;
			
			return hash % ((int) Math.pow(2, KEY_BITS));
		}
	
	// -- maintenance --
	public void notify(IChordNode potentialPredecessor) throws RemoteException
		{
			//the notify function notifies of a potential predecessor for our node
			//if we don't have one, or if it's smaller than our currently set predecessor
			//it becomes our new node predecessor

			if(predecessor == null || isInOpenRange(potentialPredecessor.getKey(), predecessor.getKey(), myKey))
			{
				predecessor = potentialPredecessor;
				predecessorKey = predecessor.getKey();
			}
		}
	
	public void stabilise() throws RemoteException
		{
			//System.out.println(x);
			//the stabilis function simply checks if we have a predecessor, and then
			//notifies of a change to be updated

			if(successor != null)
			{
				IChordNode x = successor.getPredecessor();

				if(x != null)
				{
					if(isInOpenRange(x.getKey(), myKey, successor.getKey()))
					{
						successor = x;
						successorKey = x.getKey();
						//System.out.println(successor.getKey());
						//System.out.println(successor.getPredecessor().getKey());
					}
				}
			}

			successor.notify(this);
		}
	
	public void fixFingers() throws RemoteException
		{
			//the fixfingers table just fixes our table following changes we've made in successors/predecessors
			//the commented out bit of code is the one taken off of wikipedia's pseudocode and adapted
			//the functional code is below it; it updates all of the code without any if's.

			nextFingerFix++;
			int auxNextKey = 0;

			if(nextFingerFix >= fingerTableLength)
			{
				nextFingerFix = 1;
			}

			auxNextKey = (int) Math.round(myKey + Math.pow(2, nextFingerFix - 1)) % (int) Math.pow(2, KEY_BITS);

			/** 
			if(nextFingerFix < fingerTableLength)
			{
				finger[nextFingerFix].node = findSuccessor(auxNextKey);
				//System.out.println((finger[nextFingerFix]));
			}
			*/

			Finger auxFinger = new Finger();
			auxFinger.node = findSuccessor(auxNextKey);
			auxFinger.key = auxFinger.node.getKey();

			finger[nextFingerFix] = auxFinger;

		}
	
	public void checkPredecessor() throws RemoteException
		{
			//this function checks if we have a predecessor and lets the user know
			//if we don't, we let the user know until the node stabilises

			try
			{
				if(predecessor != null && predecessor.isAlive())
				{
					System.out.println("Found predecessor at " + predecessor.getKey());
				}
			}
			catch(RemoteException e)
			{
				System.out.println("Couldn't check predecessor status");
				//predecessor = null;
				//this.stabilise();
				predecessor = null;
				stabilise();
			}

		}

	public void checkSuccessor() throws RemoteException
		{
			//same as the checkPredecessor function, but for our successor

			try
			{
				if(successor.isAlive())
				{
					System.out.println("Found successor at " + successor.getKey());
				}
			}
			catch(RemoteException e)
			{
				System.out.println("Couldn't check successor status");
				successor = this;
				this.stabilise();
			}
		}
	
	public void checkDataMoveDown() throws RemoteException, IOException, ParserConfigurationException
		{
			//if I'm storing data that my current predecessor should be holding, move it

			for(int i = 0; i < dataStore.size(); i++)
			{
				String auxKey = dataStore.get(i).key;

				if(predecessor != null)
				{
					if(isInClosedRange(hash(auxKey), predecessorKey, myKey))
					{
						put(auxKey, get(auxKey));
						dataStore.remove(i);
					}
				}

			}

		}

	public boolean isAlive() throws RemoteException
	{
		//boolean that sends a ping if the node exists
		//if it doesn't send one, then the node has died
		return true;
	}
	
	
	public void run()
		{
		while (true)
			{
			try{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					System.out.println("Interrupted");
				}
			
			try{
					stabilise();
				}
				catch (Exception e){e.printStackTrace();}
			
			try{
					fixFingers();
				}
				catch (Exception e){e.printStackTrace();}
			
			try{
				checkPredecessor();
				}
				catch (Exception e){e.printStackTrace();}
			try{
				checkSuccessor();
				}
				catch (Exception e){e.printStackTrace();}
			
			try{
				checkDataMoveDown();
				}
				catch (Exception e){e.printStackTrace();}
			try{
				listFamily();
				}catch (Exception e){e.printStackTrace();}
			}
		}

	public void listFamily() throws RemoteException
	{
		//lists the current node

		System.out.println("Current node is: " + myKey);
		//System.out.println("Node predecessor is: " + predecessorKey);
		//System.out.println("Node successor is: " + successorKey);
		System.out.println("---------------");
	}

	// -- TASK ONE --

	@Override
    public int countWords(byte[] bytes) throws RemoteException, IOException
    {
		//function that counts the words of the file we sent

		String file = new String(bytes);
		String[] fileSplit = file.split(" ");
 		
		return fileSplit.length;
	}

    @Override
    public String commonWord(byte[] bytes) throws RemoteException, IOException
    {
		//function that finds the most common word in the file 

		String file = new String(bytes);
		String[] fileSplit = file.split(" ");
		ArrayList<String> wordsfound = new ArrayList<>();
        int amountofwords = 0;
        int wordscount = 0;
        String currentWord = "";
        int biggestcount = 0;
        String mostCommon = "";

		for(int i = 0; i < fileSplit.length; i++)
		{
			amountofwords = amountofwords + 1;
			currentWord = fileSplit[i];
            
            if(amountofwords == 1)
            {
                wordsfound.add(currentWord);
            }

            wordscount = 1;
            int k = 0;

			while(i < amountofwords)
                {
                    //System.out.println(i);
                    if(wordsfound.get(i).equals(currentWord))
                    {
                        //System.out.println("gets here!!!");
                        wordscount++;
                    }
                    else
                    {
                        wordsfound.add(currentWord);
                    }
                    i++;
                }
            if(wordscount > biggestcount)
                {
                    biggestcount = wordscount;
                    mostCommon = currentWord;
                }
		}

		return mostCommon;

    }

    @Override
    public float averageLength(byte[] bytes)  throws RemoteException
    {
		//function that finds the average word length 

        int wordslength = 0;
		int words = 0;

		String file = new String(bytes);
		String[] fileSplit = file.split(" ");

		words = fileSplit.length;

		for(int i = 0; i < words; i++)
		{
			wordslength = wordslength + fileSplit[i].length();
		}

        return wordslength/words;
		
    }

	public byte[] doTaskOne(IChordNode node, byte[] bytes) throws IOException, RemoteException, ParserConfigurationException
	{

			System.out.println("Starting task one!");

			//function that does our task one by calling the XML file creator class
			//and sending it the values we can get via the sub-functions

			/** 
			byte[] taskOne = null;
			String auxString = "";

			auxString = auxString + "Number of words: " + Integer.toString(node.countWords(bytes)) + ".";
			auxString = auxString + " Most common word: " + node.commonWord(bytes) + ".";
			auxString = auxString + " Average word length: " +Float.toString(node.averageLength(bytes)) + ".";

			System.out.println(auxString);
			taskOne = auxString.getBytes();
			*/

			TurnIntoXML convertXml = new TurnIntoXML();
			//System.out.println(convertXml.getXMLFile(node.countWords(bytes), node.commonWord(bytes), node.averageLength(bytes)));
			return convertXml.getXMLFile(node.countWords(bytes), node.commonWord(bytes), node.averageLength(bytes));

	}


	
	public static void main(String args[]) throws RemoteException, NotBoundException
		{
		// --- local test ---

		if(args.length == 1)
			{
				//in order for the code to work, we need to name the node
				//then we will hash that 'name' and that will be our stored key

				String requestinput = args[0];
				ChordNode currentnode = new ChordNode(requestinput);
				String nodename = "n" + currentnode.myKey;

				Registry registry = LocateRegistry.getRegistry("localhost");
				currentnode.intercode = (IChordNode) UnicastRemoteObject.exportObject((Remote) currentnode, 0);
				registry.rebind(nodename, (Remote)currentnode.intercode);
				INodeSupervisor supervisor=(INodeSupervisor)registry.lookup("NodeSupervisor");
				// -- wait a bit for stabilisation --
				
				System.out.println("Waiting for topology to stabilise...");
				try
					{
						//int currentkey = currentnode.getKey();

						System.out.println("Registered request for node " + requestinput + " with key " + nodename);

						System.out.println("Joining nodes to network...");
						System.out.println("Connecting to network...");


						//System.out.println("Gets here?");
						
						//find a random node on the list
						//if there's nothing in the list, don't join

						//currentnode.join(currentnode);

						if(!supervisor.getChordLog().isEmpty())
						{

							Random rand = new Random();
							int k = rand.nextInt(supervisor.getChordLog().size());

							((IChordNode)supervisor.getChordLog().get(k)).join(currentnode);
							supervisor.listAliveNodes();
						}

						supervisor.addNode(registry,currentnode,requestinput);
						//currentnode.listFamily(currentnode);

						//System.out.println("Testing predecessor: " + currentnode.getPredecessor().getKey());

						//currentnode.listCurrentNodes();
						
						//Thread.sleep(7000);
					}
						
					catch (Exception e)
					{
						System.out.println("Interrupted");
					}
				
				
				System.out.println("Inserting keys...");

				System.out.println("All done (press ctrl-c to quit)");
				}
			
		else
		{
			System.out.println("Please include a node name to start the nodes!");
		}
	}
	
	}