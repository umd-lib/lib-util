package edu.umd.lib.util.sambaConnection;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;

/**
 * Extending the NtlmAuthticator Class
 * 
 * @author Allan Shaoxiong Jiang
 *
 */
public class SMBAuthenticator extends NtlmAuthenticator{
	
	private String username;
	private String password;
	private String domain;
	
	/**
	 * User need not to call this function.
	 * @param username
	 * @param password
	 * @param domain
	 */
	public SMBAuthenticator(String username, String password, String domain){
		this.username = username;
		this.password = password;
		this.domain = domain;
	}

	protected NtlmPasswordAuthentication getNtlmPasswordAuthentication(){
		
		return new NtlmPasswordAuthentication(domain, username, password);	
		
	}
	
	
}
