/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge;
import java.util.List;

/**
 * A Cloud is a collection of Nodes (and relations that are also nodes).
 * A Cloud is part of a CloudContexts.
 *
 * @author Rob Vermeulen
 * @author Pierre van Rooden
 * @author Jaco de Groot
 */
public interface Cloud {

    /**
     * Returns the node with the specified number from this cloud. The returned
     * node is a new instance of <code>Node</code> with a reference to this
     * instance of <code>Cloud</code>.
     *
     * @param number                  the number of the requested node
     * @return                        the requested node
     * @throws NodeNotFoundException  if the specified node could not be found
     */
    public Node getNode(int number);

    /**
     * Returns the node with the specified number from this cloud. The returned
     * node is a new instance of <code>Node</code> with a reference to this
     * instance of <code>Cloud</code>.
     *
     * @param number                  the number of the requested node
     * @return                        the requested node
     * @throws NodeNotFoundException  if the specified node could not be found
     */
    public Node getNode(String number);
	

    /**
     * Returns the node with the specified alias from this cloud. The returned
     * node is a new instance of <code>Node</code> with a reference to this
     * instance of <code>Cloud</code>.
     *
     * @param alias                   the alias of the requested node
     * @return                        the requested node
     * @throws NodeNotFoundException  if the specified node could not be found
     */
    public Node getNodeByAlias(String alias);

    /**
     * Returns all node managers available in this cloud.
     *
     * @return  a <code>NodeManagerList</code> containing all node managers
     *          available in this cloud.
     */
    public NodeManagerList getNodeManagers();

    /**
     * Returns the specified node manager.
     *
     * @param name                           the name of the requested node
     *                                       manager
     * @return                               the requested node manager
     * @throws NodeManagerNotFoundException  if the specified node manager
     *                                       could not be found
     */
    public NodeManager getNodeManager(String name);

    /**
     * Returns the specified relation manager.
     *
     * @param sourceManagerName                  name of the node manager of the
     *                                           source node
     * @param destinationManagerName             name of the node manager of the
     *                                           destination node
     * @param roleName                           name of the role
     * @return                                   the requested relation manager
     * @throws RelationManagerNotFoundException  if the specified relation
     *                                           manager could not be found
     */
    public RelationManager getRelationManager(String sourceManagerName, String destinationManagerName, String roleName);

    /**
     * Returns all relation managers available in this cloud.
     *
     * @return  a <code>RelationManagerList</code> containing all relation
     *          managers available in this cloud
     */
    public RelationManagerList getRelationManagers();
	
    /**
     * Returns the context to which this cloud belongs.
     *
     * @return  a <code>CloudContext</code> to which this cloud belongs
     */
    public CloudContext getCloudContext();

    /**
     * Creates a transaction on this cloud. The returned
     * <code>Transaction</code> will have a generic ID.
     *
     * @return a <code>Transaction</code> on this cloud
     */
    public Transaction createTransaction();

    /**
     * Creates a transaction on this cloud with a specified name.
     *
     * @param name                                 an unique name to use for the
     *                                             transaction
     * @return                                     a <code>Transaction</code> on
     *                                             this cloud
     * @throws TransactionAllreadyExistsException  if a transaction with the
     *                                             specified name allready
     *                                             exists
     */
    public Transaction createTransaction(String name);

    /**
     * Returnes the transaction with the specified name.
     * If no active transaction exists, a new transaction is craeted.
     *
     * @param name  the name of the requested transaction
     * @return      the requested transaction
     */
    public Transaction getTransaction(String name);
   	
    /**
     * Logs on a user. This will associate the user with this cloud instance.
     *
     * @param authenticatorName  name of the authentication method to use
     * @param parameters         parameters for the authentication
     * @return                   <code>true</code> if succesfull,
     *                           <code>false</code otherwise
     */
    public boolean logon(String authenticatorName, Object[] parameters);
  	
    /**
     * Logs off a user. Resets the user's context to 'anonymous'.
     */
    // public void logoff();

    /**
     * Returns the name of this cloud.
     *
     * @return the name of this cloud
     */
    public String getName();

    /**
     * Returns the description of the cloud.
     *
     * @return the description of this cloud
     */
    public String getDescription();
	
    /**
     * Returns a list of virtual nodes that are composed by fields of other
     * nodes.
     * Starting at one or more specified nodes traversals are made according to
     * a specified path. One traversal makes up one virtual node. All possible
     * traversals that can be made starting at one or more nodes of the same
     * type and following a specified path are stored in the returned list. 
     *
     * Suppose we have defined the following:
     *
     * <pre>
     * - A node manager recordcompany containing a field name.
     * - A node manager artist containing a field name.
     * - A node manager url containing a field description and url.
     * - A relation type related between recordcompany and artist.
     * - A relation type related between artist and url.
     * - A relation type homepagerel between artist and url.
     * </pre>
     *
     * If node 100 has a node manager called recordcompany we can do
     * the following to get a list of the record company's artists and all urls
     * belonging
     * to these artist (including nodes found through the related relation and
     * the homepagerel relation):
     * <pre>
     * getList("100", "recordcompany,artist,url",
     *         "artist.name,description,url", null, null, null, true);
     * </pre>
     * This call returns a list of virtual nodes with the fields artist.name,
     * description and url for every valid traversal.
     *
     * <p>
     * If we only want to list homepage urls of the artists we do:
     * <pre>
     * getList("100", "recordcompany,artist,url",
     *         "artist.name,description,homepagerel,url", null, null, null,
     *         true);
     * </pre>
     *
     * <p>
     * If we want to list all url's except the the homepage urls we do:
     * <pre>
     * getList("100", "recordcompany,artist,url",
     *         "artist.name,description,related,url", null, null, null, true);
     * </pre>
     *
     * <p>
     * If node 200 also has a node manager with name recordcompany we can get
     * the info from their artist together with the info of the artist from the
     * first company by also putting number 200 in the first parameter:
     * <pre>
     * getList("100,200", "recordcompany,artist,url",
     *         "artist.name,description,related,url", null, null, null, true);
     * </pre>
     *
     * For more information about the <code>constraints</code> parameter consult
     * {@link NodeManager#getList(String constraints, String orderby, String
     * directions)}. 
     *
     * @param startNodes    A comma separated list of node numbers that should
     *                      be used as a
     *                      starting point for zero or more traversals.
     *                      The nodes specified as start nodes have to be a
     *                      member of the first node manager
     *                      listed in the path parameter.
     * @param nodePath      A comma seperated list of node manager names
     *                      which specifies the path that should be followed.
     *                      It is possible to explicitly specify a relation
     *                      manager that should be used to go from one node to
     *                      an other. If no relation manager is specified
     *                      between two nodes, all possible relation managers
     *                      that can be used to go to the next specified node in
     *                      the path are followed.
     * @param fields        A comma separated list of field names that will make
     *                      up the returned virtual
     *                      nodes. A fieldname can be prefixed with the
     *                      original node manager name of the field and a dot
     *                      in cases where more than one node manager in the
     *                      path has a field with the same name.
     * @param constraints   Contraints to prevent nodes from being
     *                      included in the resulting list which would normally
     *                      by included or <code>null</code> if no contraints
     *                      should be applied.
     * @param orderby       A comma separated list of field names on which the
     *                      returned list should be sorted or <code>null</code>
     *                      if the order of the returned virtual nodes doesn't
     *                      matter.
     * @param directions    A comma separated list of the values UP and DOWN
     *                      indicating wether the sort on the
     *                      corresponding field in the <code>orderby</code>
     *                      parameter should be up (ascending) or down
     *                      (descending) or <code>null</code> if sorting for all
     *                      fields should be up. If less values are supplied
     *                      then there are fields in the <code>orderby</code>
     *                      parameter, the first value in the list is used for
     *                      the remainig fields.
     * @param distinct      <code>true</code> if nodes who allready exist in
     *                      the list should not be added to the list.
     *                      <code>false</code> if all nodes should be added to
     *                      the list even if a node with exactly the same field
     *                      values is allready present.
     * @return              a list of virtual nodes
     */
    public NodeList getList(String startNodes, String nodePath, String fields,
            String constraints, String orderby, String directions,
            boolean distinct);

}
