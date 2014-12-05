package org.renci.databridge.persistence.network;
import  java.util.*;

public interface NetworkNodeDAO {
    /**
     * Insert a node into the network.
     * @param transferNode The node to insert
     * @return 1 on success, -1 on failure, 0 if the node already exists.
     */
    public int insertNetworkNode(NetworkNodeTransferObject theNode);

    /**
     * Add a property to the given node.
     * @param transferNode The node for which to insert the property
     * @param key The key for the new property
     * @param value The value for the new property
     * @return true on success, false on failure
     */
    public boolean addPropertyToNetworkNode(NetworkNodeTransferObject theNode, String key, Object value);

    /**
     * Retrieve an iterator for all nodes that match the given search key.
     * @param transferNode The node containing the nameSpace in which to search
     * @param key The key for the new property
     * @param value The value for the new property
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> getNetworkNodes(NetworkNodeTransferObject theNode, String key, Object value);

    /**
     * Retrieve an iterator for all nodes that match the given search key.
     * @param nameSpace The nameSpace in which to search
     * @param key The key for the new property
     * @param value The value for the new property
     * @return The iterator
     */
    public Iterator<NetworkNodeTransferObject> getNetworkNodes(String nameSpace, String key, Object value);

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
    public boolean deleteNetworkNode(NetworkNodeTransferObject theNode);

    public static final String METADATA_NODE_KEY = "metaDataNodeKey";
}
