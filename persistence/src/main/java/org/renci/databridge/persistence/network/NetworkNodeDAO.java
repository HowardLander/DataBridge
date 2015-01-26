package org.renci.databridge.persistence.network;
import  java.util.*;

public interface NetworkNodeDAO {
    /**
     * Insert a node into the network.
     * @param transferNode The node to insert
     * @return 1 on success, -1 on failure, 0 if the node already exists.
     */
    public int insertNetworkNode(NetworkNodeTransferObject transferNode);

    /**
     * Add a property to the given node.
     * @param transferNode The node for which to insert the property
     * @param key The key for the new property
     * @param value The value for the new property
     * @return true on success, false on failure
     */
    public boolean addPropertyToNetworkNode(NetworkNodeTransferObject transferNode, String key, Object value);

    /**
     * Retrieve the value of a property of a node
     * @param transferNode The node from which to retrieve the property
     * @param key Which property to retrieve
     * @return Object with the value of the property or null if the property does not exist on the node.
     */
    public Object getPropertyFromNetworkNode(NetworkNodeTransferObject transferNode, String key);

    /**
     * Delete a property from the node
     * @param transferNode The node from which to delete the property
     * @param key Which property to delete
     * @return Object with the value of the deleted property or null if the property did not exist on the node.
     */
    public Object deletePropertyFromNetworkNode(NetworkNodeTransferObject transferNode, String key);

    /**
     * Retrieve an iterator for all nodes that match the given search key.
     * @param transferNode The node containing the nameSpace in which to search
     * @param key The key for the new property
     * @param value The value for the new property
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> getNetworkNodes(NetworkNodeTransferObject transferNode, String key, Object value);

    /**
     * Retrieve an iterator for all nodes that match the given search key.
     * @param nameSpace The nameSpace in which to search
     * @param key The key for the new property
     * @param value The value for the new property
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> getNetworkNodes(String nameSpace, String key, Object value);

    /**
     * Retrieve the node specified by the id parameter.
     * @param theId The string for which to return the node
     * @return The requested node
     */
    public NetworkNodeTransferObject getNetworkNode(String id);

    /**
     * Retrieve an iterator for all nodes in a given nameSpace
     * @param nameSpace The nameSpace in which to search
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> getNetworkNodesForNameSpace(String nameSpace);

    /**
     * Delete the given node
     * @param transferNode The node to delete
     * @return true on success, false on failure
     */
    public boolean deleteNetworkNode(NetworkNodeTransferObject transferNode);

    public static final String METADATA_NODE_KEY = "metaDataNodeKey";
}
