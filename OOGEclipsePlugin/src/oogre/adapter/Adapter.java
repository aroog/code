package oogre.adapter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import oogre.web.PluginRequest;

/**
 * Entry point for the Eclipse Plugin:
 * - install the Eclipse plugin as a subscriber on the server
 * - start a loop
 * - if there are new jobs, process them by calling the Eclipse analyses 
 * - return the results to the server (JSON objects, etc.)
 * 
 * TODO: Rename this: Mediator or Orchestrator or Conductor
 */
public class Adapter {
	
	private static final int STATUS_JOB_SENT_TO_PLUGIN = 1;
	
	static PluginRequest request = null;

	/**
	 * Switch on the job name and call the appropriate method on the WebFacade.
	 * Call updateJob on the server to update the JSON result and set the status
	 *
	 * @param job
	 */
	private static void processJob(JSONObject job){
		String jobName = job.getString("name");
		String jobId = job.getString("id");
		
		switch(jobName){
			case "GEN_GRAPH":
				String oogTree = WebFacade.getInstance().getOOGFromFile();
				if(oogTree == null){
					JSONObject t = new JSONObject();
					t.put("success", false);	
					request.updateJob(jobId, t.toString(), false);
				}
				else{
					request.updateJob(jobId, oogTree, false);
				}				
				break;
			
			case "SHOW_REFINEMENT":
				String result = WebFacade.getInstance().getRefinements();				
				if(result == null){
					JSONObject t = new JSONObject();
					t.put("success", false);	
					request.updateJob(jobId, t.toString(), false);
				}
				else{
					request.updateJob(jobId, result, false);
				}
				break;
				
			case "ADD_REFINEMENT":
				if(job.has("params")){
					JSONObject params = job.getJSONObject("params");
					String refType = params.getString("type");
					String srcObject = params.getString("srcObject");
					String dstObject = params.getString("dstObject");
					String dstDomain = params.getString("dstDomain");
					if(WebFacade.getInstance().addRefinement(refType, srcObject, dstObject, dstDomain) != null){
						request.updateJob(jobId, WebFacade.getInstance().getRefinements(), false);
						break;
					};
					
				}
				JSONObject t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);								
				break;
				
			case "REMOVE_REFINEMENT":
				// We don't need any parameters. We just need to remove all pending refinements
				if(WebFacade.getInstance().removePendingRefinement() != null){
					// XXX. Do we have to return list of refinements?
					request.updateJob(jobId, WebFacade.getInstance().getRefinements(), false);
					break;
				};
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);								
				break;
				
			case "REFINE_GRAPH":
				t = new JSONObject();
				t.put("success", WebFacade.getInstance().doRefinement());				
				request.updateJob(jobId, t.toString(), false);				
				break;
					
			case "SHOW_CODE":
				if(job.has("params")){
					JSONObject params = job.getJSONObject("params");
					String resource = null;
					if(params.has("resource"))
						resource = params.getString("resource");
					
					String source;
					if(resource == null) {
						// DONE. Get default file name (root class) from OOGRE config (stored on Facade)
						String rootClass = WebFacade.getInstance().getRootClass();
						if (rootClass != null) {
							source = WebFacade.getInstance().getJava2(rootClass);
							// Load contents of file, encode and return
							result = Base64.encodeBase64String(source.getBytes());
						}
						// If root class is null, what to do?
						else {
							result = "";
						}
					}
					else {
						source = WebFacade.getInstance().getJava(resource);
						// XXX. getJava already does encoding; maybe remove it from there
						result = source;
					}				
					request.updateJob(jobId, result, false);
					break;
				}
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);								
				break;
				
			case "SHOW_GRAPH": // Display OOG (nested boxes): OOG.dot -> SVG
				byte[] svgData;
				try {
					String svgFileName = WebFacade.getInstance().getSVG();
					if(svgFileName != null){
						// XXX. Check that filename exists and read it
						// Load contents of file, encode and return
						svgData = Files.readAllBytes(Paths.get(svgFileName));
						result = Base64.encodeBase64String(svgData);
						request.updateJob(jobId, result, false);
						break;
					}
					
				} catch (IOException e1) {				
					e1.printStackTrace();					
				}
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);		
				break;		
			case "SHOW_GRAPH2": // Display OGraph: dfOGraph.dot -> SVG
				try {
					String svgFileName = WebFacade.getInstance().getSVG2();
					if(svgFileName != null){
						// XXX. Check that filename exists and read it
						// Load contents of file, encode and return
						svgData = Files.readAllBytes(Paths.get(svgFileName));
						result = Base64.encodeBase64String(svgData);
						request.updateJob(jobId, result, false);
						break;
					}
					
				} catch (IOException e1) {				
					e1.printStackTrace();					
				}
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);		
				break;	
				
			case "LOAD_STATE":
				String state = WebFacade.getInstance().loadState();
				if(state != null){
					request.updateJob(jobId, state, false);
					break;
				};
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);								
				break;
				
			case "SAVE_STATE":
				if(job.has("params")){
					JSONObject params = job.getJSONObject("params");
					System.out.println(params);
					result = WebFacade.getInstance().saveState(params);
					if(result != null){
						request.updateJob(jobId, result, false);
						break;
					}							
					
				}
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);								
				break;
				
			case "ANALYZE_SEC":
				if(job.has("params")){
					JSONObject params = job.getJSONObject("params");
					System.out.println(params);
					result = WebFacade.getInstance().analyzeSec(params);
					if(result != null){
						request.updateJob(jobId, result, false);
						break;
					}							
					
				}
				t = new JSONObject();
				t.put("success", false);	
				request.updateJob(jobId, t.toString(), false);								
				break;
		}
	}

	/**
	 * Entry point for the loop of the Eclipse Plugin.
	 * @param args
	 */
	public static void main(String[] args) {
		// Server loop
		while (true) {
			// XXX. Hard-coded URL and port number
			if (request == null) {
				// Initiate HTTP connection
				request = new PluginRequest("http://localhost:8080");
			}

			try {
				// Value of timer of how often we check if there is a new job on the server
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JSONObject response = request.fetchJobs();
			if (response == null) {
				// Reset the request, so we can try again to connect at the next iteration of the loop
				request = null;
			}
			else if (response.has("error")) {
				// XXX. Fix hard-coded project name. Set/get from facade?
				
				// Eclipse plugin attempt to subscribe itself to the server
				JSONObject status = request.subscribe("ProjectName");
				System.out.println(status);
			}
			else if (response.has("jobs")) {
				JSONArray jobs = response.getJSONArray("jobs");
				// Iterate through all the jobs
				for (int i = 0; i < jobs.length(); i++) {
					JSONObject job = (JSONObject) jobs.get(i);
					int status = job.getInt("status");
					// Process only jobs that are still pending
					if (status == STATUS_JOB_SENT_TO_PLUGIN) {
						processJob(job);
					}
				}
			}
		}
	}
}
