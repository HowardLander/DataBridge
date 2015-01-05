package org.renci.databridge.persistence.network;
import  java.util.*;

public interface NetworkRelationshipDAO {
    /**
     *  Insert a relationship into the network database.
     *
     *  @param theTransfer object containing the relationship type, and the
     *         attributes (if any) of the relationship
     *  @param node1 The first node of the relationship
     *  @param node2 The second node of the relationship
     *  @return true on success, false on failure
     */
    public boolean insertNetworkRelationship(NetworkRelationshipTransferObject theTransfer, 
        NetworkNodeTransferObject node1, NetworkNodeTransferObject node2);

    /**
     *  Add a property to an existing relationship. Note that if the property already exists, the old
     *  value is overwritten with the new value.
     *
     *  @param theRelationship object containing the existing relationship to which the property will be added.
     *  @param key The key for the property to add
     *  @param value The value for the property to add
     *  @return true on success, false on failure
     */
    public boolean addPropertyToNetworkRelationship(NetworkRelationshipTransferObject theRelationship, 
        String key, Object value);

    /**
     *  Retrieve an iterator for all the relationships of the the specified node.
     *  @param theTransfer object containing the nodes, the relationship type, and the
     *         attributes (if any) of the relationship
     *  @return the requested iterator
     */
    public Iterator<NetworkRelationshipTransferObject> 
        getNetworkRelationships(NetworkNodeTransferObject theTransfer);

    /**
     * Retrieve an iterator for all the relationships of the the specified node that
     * are of a type specified in the key param. In our data model, at this writing,
     * we will normally only expect one answer, but just in case that changes we are
     * returning an iterator.
     *  @param theTransfer object containing the nodes, the relationship type, and the
     *         attributes (if any) of the relationship
     *  @param key String containing the relationship type.
     *  @return the requested iterator
     */
    public Iterator<NetworkRelationshipTransferObject> 
        getNetworkRelationships(NetworkNodeTransferObject theTransfer, String key);

    public boolean deleteNetworkRelationship(NetworkRelationshipTransferObject theTransferNode);
    /**
     *  Delete a property from an existing relationship.
     *
     *  @param theTransferNode object containing the relationship from which the property will be deleted.
     *  @param key The key for the property to delete
     *  @return true on success, false on failure
     */
    public boolean deletePropertyFromNetworkRelationship(NetworkRelationshipTransferObject theTransferNode, 
       String key);

    /**
     *  Retrieve a property from an existing relationship.
     *
     *  @param theTransferNode object containing the relationship from which the property will be retrieved.
     *  @param key The key for the property to retrieve
     *  @return An object containing the value of the specified property of the given relationship.  At the
     *          moment that will always be a string, but that may change in the future.
     */
    public Object getPropertyFromNetworkRelationship(NetworkRelationshipTransferObject theTransferNode, 
        String key);

    public static final String METADATA_SIMILARITY_PROPERTY_NAME = "value";
}
