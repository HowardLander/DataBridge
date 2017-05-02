package org.renci.databridge.message;

/**
 This class is used to both status and results from the NetworkRPC Server to the
 RPC client. At the moment this client is our DataBridge web app, but this may
 wll be used by other clients in the future.
 */
public class NetworkRPCResultsMessage {
   public static final String STATUS_OK = "DataBridge_OK"; 
   public static final String STATUS_ERROR = "DataBridge_Error"; 
   public String       status; 
   public String       results;  // If error, will contain error message
   
   /**
    * Get status.
    *
    * @return status as String.
    */
   public String getStatus()
   {
       return status;
   }
   
   /**
    * Set status.
    *
    * @param status the value to set.
    */
   public void setStatus(String status)
   {
       this.status = status;
   }
   
   /**
    * Get results.
    *
    * @return results as String.
    */
   public String getResults()
   {
       return results;
   }
   
   /**
    * Set results.
    *
    * @param results the value to set.
    */
   public void setResults(String results)
   {
       this.results = results;
   }
}
