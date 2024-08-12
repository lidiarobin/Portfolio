import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

public interface IChordNode extends Remote
{
    void put(String key, byte[] value) throws RemoteException, IOException, ParserConfigurationException;
    byte[] get(String key) throws RemoteException;
    int getKey() throws RemoteException;
    IChordNode getPredecessor() throws RemoteException;
    void join(IChordNode n2) throws RemoteException;
    IChordNode findSuccessor(int key) throws RemoteException;
    IChordNode closestPrecedingNode(int key) throws RemoteException;
    int hash(String s) throws RemoteException;
    public void notify(IChordNode potentialPredecessor) throws RemoteException;
    void stabilise() throws RemoteException;
    void fixFingers() throws RemoteException;
    void checkPredecessor() throws RemoteException;
    void checkSuccessor() throws RemoteException;
    public void checkDataMoveDown() throws RemoteException, IOException, ParserConfigurationException;
    public boolean isAlive() throws RemoteException;
    public void listFamily() throws RemoteException;
    public int countWords(byte[] bytes) throws RemoteException, IOException;
    public String commonWord(byte[] bytes) throws RemoteException, IOException;
    public float averageLength(byte[] bytes)  throws RemoteException;
	public byte[] doTaskOne(IChordNode node, byte[] bytes) throws IOException, RemoteException, ParserConfigurationException;
}
