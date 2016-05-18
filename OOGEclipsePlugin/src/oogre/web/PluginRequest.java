package oogre.web;

import java.util.UUID;

import org.json.JSONObject;

/**
 * HTTP-based plugin specific to our Eclipse Plugin. Wraps the general HTTP client.
 */
public class PluginRequest {
	private String instanceId;
	private String endpoint;
	private String projectName;
	private Request request;
	
	/**
	 * Generate a random GUID as an instance ID.
	 * @param endpoint The base address (domain + port)
	 */
	public PluginRequest(String endpoint){
		this.endpoint = endpoint;
		request = new Request(endpoint);
		instanceId = UUID.randomUUID().toString();
	}
	
	public PluginRequest(String endpoint, String id){
		request = new Request(endpoint);
		instanceId = id;
	}
	
	public String getInstanceId(){
		return instanceId;
	}
	
	public void setInstanceId(String id){
		instanceId = id;
	}
	
	/**
	 * Subsribe by doing a post request to the "/plugin/subscribe"
	 * @param projectName
	 * @return
	 */
	public JSONObject subscribe(String projectName){
		this.projectName = projectName;
		JSONObject bodyParam = new JSONObject();
		bodyParam.put("instanceId", instanceId);
		bodyParam.put("projectName", projectName);		
		return request.post("/plugin/subscribe", bodyParam);
	}

	/**
	 * Get request 
	 * @return
	 */
	public JSONObject fetchJobs(){
		return request.get("/plugin/job", "instanceId=" + instanceId);
	}
	
	/**
	 * Update (process) the job by uploading the result (data) of processing the job. 
	 * 
	 * XXX. We don't use 'isFile' flag anymore. Always false.
	 * 
	 * @param jobId
	 * @param filePathOrContent
	 * @param isFile
	 * @return
	 */
	public JSONObject updateJob(String jobId, String filePathOrContent, Boolean isFile){
		String params = "instanceId=" + instanceId + "&jobId=" + jobId;
		if(isFile){
			return request.upload("/plugin/job", params, filePathOrContent);
		}else{
			byte[] b = filePathOrContent.getBytes();
			return request.upload("/plugin/job", params, b);
		}		
	}
}
