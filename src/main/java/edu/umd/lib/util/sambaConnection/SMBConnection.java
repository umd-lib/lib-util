package edu.umd.lib.util.sambaConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Set up a SMB connection to fedoradev server.
 * and access to Excel files in specific location.
 * 
 * @author Allan Shaoxiong Jiang
 *
 */
public class SMBConnection{
	
	public String urlString;
	NtlmPasswordAuthentication auth;
	SmbFile smbFile;
	String fileName;
	
	private String username;
	private String password;
	String path;
	String domain;
	
	SMBAuthenticator authenticator;
	
	/**
	 * Access file with username, path
	 * (e.g. site.umd.edu/pub$/path/file.xls)
	 * password and workgroup
	 * (e.g. LIBLAN)
	 * in parameter.
	 * @param username
	 * @param password
	 * @param sharePath
	 * @param domain
	 * @throws MalformedURLException
	 */
	public SMBConnection
	(String username, String password, String sharePath, String domain)
			throws MalformedURLException{
		
		this.username = username;
		this.password = password;
		this.domain = domain;
		path = sharePath;
		
		//make sure path syntax is legal.
		if(path.endsWith("/") == false){
			path = path + "/";
		}		
				
		authenticator = new SMBAuthenticator(username, password, domain);	
		auth = authenticator.getNtlmPasswordAuthentication();	
		smbFile = new SmbFile("smb://" + username + ":" + password + "@"
			     + path, auth);
	}

	/**
	 * returns the URL of the file in smb format.
	 * (e.g. smb://username:password@domain/path/to/file.xls)
	 * @return
	 */
	public String getFileURL(){
		
		String filePath = "smb://" + username + ":" + password + "@"
			     + path + fileName;
		
		return filePath;
		
	}
	
	/**
	 * Call the method to focus a file under current directory.
	 * parameter should be full name of the file.
	 * (e.g. sample.xls)
	 * @param fileName
	 */
	public void setFile(String fileName){
		
		this.fileName = fileName;
		
	}
	
	/**
	 * Return the InputStream of the setted file.
	 * CALL AFTER SETFILE(filename).
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException{
		
		InputStream is;
		
		smbFile = new SmbFile(this.getFileURL(),auth);
		is = smbFile.getInputStream();
		
		return is;
		
	}
		
	/**
	 * Go to the sub-directory of given name.
	 * @param directoryName
	 * @return
	 * @throws MalformedURLException
	 * @throws SmbException
	 */
	public List<SmbFile> gotoDirectory(String directoryName) throws MalformedURLException, SmbException{
		
		if(directoryName.endsWith("/") == false){
			path = path + directoryName + "/";
		}
		else{
			path = path + directoryName;
		}
		
		smbFile = new SmbFile("smb://" + username + ":" + password + "@"
			     + path, auth);
		return this.listFile();		
		
	}
	
	/**
	 * Return a List of all files and directories under current path.
	 * @return
	 * @throws SmbException
	 */
	public List<SmbFile> listFile() throws SmbException{
		
		SmbFile [] fileArray = this.smbFile.listFiles();
		List <SmbFile> fileList = new ArrayList<SmbFile>();
		
		for(int i = 0; i < fileArray.length; i++){
		fileList.add(fileArray[i]);
		}
		
		return fileList;
		
	}
}
