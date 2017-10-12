package org.renci.databridge.message;

/**
 This class is used to both status and results from the RPC Servers to the
 RPC client. At the moment this client is our DataBridge web app, but this may
 wll be used by other clients in the future.
 */
public class DatabridgeResultsMessage {
   public static final String STATUS_OK = "DataBridge_OK"; 
   public static final String STATUS_ERROR = "DataBridge_Error"; 
   public String       status; 
   public String       results;  // If error, will contain error message

   /**
    *  Default constructor
    */
   public DatabridgeResultsMessage() {
      this.status = STATUS_OK;
      this.results = "";
   }
  

   /**
    *  Constructor that takes a boolean and a result string
    */
   public DatabridgeResultsMessage(boolean status, String results) {
      if (status == false) {
         this.status = STATUS_ERROR;
      } else {
         this.status = STATUS_OK;
      }
      this.results = results;
   }
  
   /**
    *  Set this object to a success state.
    *
    */
   public void setSuccess()
   {
      status = STATUS_OK;
   }
  
   /**
    *  Set this object to an error state.
    *
    */
   public void setError()
   {
      status = STATUS_ERROR;
   }
   
   
   /**
    * Return a booloean indicating whether or not the this result represents success (true)
    * or failure (false)
    *
    * @return boolean
    */
   public boolean isSuccess()
   {
       boolean returnStatus = false;
       if (status.compareTo(STATUS_OK) == 0) {
          returnStatus = true;
       }
       return returnStatus;
   }
   
   
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
