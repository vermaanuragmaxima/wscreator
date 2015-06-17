package com.maximaconsulting.webservices.rest;

/**
 * Response Pojo to be used as response for all the rest services.
 * 
 * @author anurag.verma
 */
public class Response {
	private String requestUri;
	private String status = "ok";
	private Object result;
	private String error;

	/**
	 * 
	 */
	public Response() {}

	/**
	 * @param requestUri
	 */
	public Response(String requestUri) {
		this.requestUri = requestUri;
	}

	/**
	 * @return error String
	 */
	public String getError() {
		return error;
	}

	/**
	 * @return request URI
	 */
	public String getRequestUri() {
		return requestUri;
	}

	/**
	 * @return the result object
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @return status of execution
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param error
	 */
	public void setError(String error) {
		status = "error";
		this.error = error;
	}

	/**
	 * @param requestUri
	 */
	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	/**
	 * @param result
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(byte result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(char result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(double result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(float result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(int result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(long result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(Object result) {
		this.result = result;
	}

	/**
	 * @param result
	 */
	public void setResult(short result) {
		this.result = result;
	}

	/**
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
