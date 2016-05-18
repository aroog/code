package oogre.web;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;


/**
 * General HTTP client that connects to the node.js server.
 *
 */
public class Request {
	private String endpoint;
	private HttpClient client;
	
	public Request(String endpoint){
		this.endpoint = endpoint;
		client = HttpClientBuilder.create().build();
	}
	
	/**
	 * Process a response and convert it to JSON object
	 * @param response
	 * @return
	 */
	private JSONObject processResponse(HttpResponse response){
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent())
			);
		} catch (UnsupportedOperationException e1) {
			System.out.println(e1.getMessage());
			return null;
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
			return null;
		}
		
		String output, result = "";
		try {
			while ((output = br.readLine()) != null) {
				result += output;
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}		
		return new JSONObject(result);
	}
	
	/**
	 * Makes the HTTP request and calls processResponse
	 * 
	 * @param request
	 * @return
	 */
	private JSONObject processRequest(HttpRequestBase request){
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		return processResponse(response);
	}

	/**
	 * Get HTTP request
	 * @param path destination
	 * @param params query string params
	 * @return
	 */
	public JSONObject get(String path, String params){
		String uri = this.endpoint + path;
		if(params != null || params != ""){
			uri += "?" + params;
		}
		HttpGet request = new HttpGet(uri);
		return processRequest(request);
	}
	
	/**
	 * Overload without empty params
	 * @param path
	 * @return
	 */
	public JSONObject get(String path){
		return get(path, "");
	}
	
	/**
	 * HTTP post, that takes a JSONObject and converts it to a string
	 * @param path
	 * @param params
	 * @param body
	 * @return
	 */
	public JSONObject post(String path, String params, JSONObject body){
		String uri = this.endpoint + path;
		if(params != null || params != ""){
			uri += "?" + params;
		}
		HttpPost request = new HttpPost(uri);
		
		StringEntity input = null;
		try {
			input = new StringEntity(body.toString());
		} catch (UnsupportedEncodingException e2) {
			System.out.println(e2.getMessage());
			return null;
		}
		input.setContentType("application/json");
		request.setEntity(input);
				
		return processRequest(request);
	}
	
	/**
	 * Post with empty params
	 * @param path
	 * @param body
	 * @return
	 */
	public JSONObject post(String path, JSONObject body){
		return post(path, "", body);
	}
	
	/**
	 * Upload a binary file (using a post request).
	 * 
	 * @param path
	 * @param params
	 * @param filePath
	 * @return
	 */
	public JSONObject upload(String path, String params, String filePath){
		String uri = this.endpoint + path;
		if(params != null || params != ""){
			uri += "?" + params;
		}
		HttpPost request = new HttpPost(uri);
		
		HttpEntity entity = (HttpEntity) MultipartEntityBuilder
				.create()
				.addBinaryBody("result", new File(filePath), 
						ContentType.create("application/octet-stream"), 
						"filename"
				)
				.build();
		
		request.setEntity(entity);	
		return processRequest(request);
	}
	
	/**
	 * Uploads an array of bytes
	 * @param path
	 * @param params
	 * @param binary
	 * @return
	 */
	public JSONObject upload(String path, String params, byte[] binary){
		String uri = this.endpoint + path;
		if(params != null || params != ""){
			uri += "?" + params;
		}
		HttpPost request = new HttpPost(uri);		
		HttpEntity entity = (HttpEntity) MultipartEntityBuilder
				.create()
				.addBinaryBody("result", binary, 
						ContentType.create("application/octet-stream"), 
						"filename"
				)
				.build();
		
		request.setEntity(entity);	
		return processRequest(request);
	}
	
}
