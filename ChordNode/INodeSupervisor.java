import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

public interface INodeSupervisor extends Remote
{
    INodeSupervisor getNode() throws RemoteException;
    public void receiveData(byte[] bytes) throws RemoteException, IOException, ParserConfigurationException;
    public void parseData() throws RemoteException;
    public IChordNode listOfNodes(IChordNode node) throws RemoteException;
    public void addNode(Registry registry, IChordNode node, String nodename) throws AccessException, RemoteException;
    //public void listCurrentNodes() throws RemoteException;
    public ArrayList<IChordNode> getChordLog() throws RemoteException;
    public void listAliveNodes() throws RemoteException;
    public byte[] getTaskOne() throws RemoteException;
    public byte[] getFiles() throws RemoteException;
}
