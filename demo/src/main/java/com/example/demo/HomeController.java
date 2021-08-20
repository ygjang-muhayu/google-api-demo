package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Controller
public class HomeController {
	private static HttpTransport 	HTTP_TRANSPORT 	= new NetHttpTransport();
	private static JsonFactory 		JSON_FACTORY	= JacksonFactory.getDefaultInstance();
	
	private static final List<String>	SCOPES_DRIVE = Collections.singletonList(DriveScopes.DRIVE);
	private static final List<String>	SCOPES_SHEETS = Arrays.asList(SheetsScopes.SPREADSHEETS);
	
	private static final String USER_IDENTIFIER_KEY = "USER";
	
	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;

	@Value("${google.secret.key.path}")
	private Resource secretKeys;
	
	@Value("${google.credentials.folder.path}")
	private Resource credentialsFolder;
	
	private GoogleAuthorizationCodeFlow	flow;
	
	@PostConstruct
	public void init() throws Exception{
		GoogleClientSecrets	secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(secretKeys.getInputStream()));
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES_DRIVE)
				.setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile()))
				.setAccessType("offline").build();
	}
	
	@GetMapping(value={"/home"})
	public String showHome() throws Exception{
		boolean isUserAuthenticated = false;
		
		Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
		if(credential != null) {
			//boolean tokenValid = credential.refreshToken();
			//if(tokenValid) {
				isUserAuthenticated = true;
			//}
		}
		
		return isUserAuthenticated ? "dashboard.html" : "index.html";
	}
	
	@GetMapping(value= {"/googlesignin"})
	public void doGoogleSignIn(HttpServletResponse response) throws Exception{
		System.out.println("TEST");
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectUrl = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
		response.sendRedirect(redirectUrl);
	} 
	
	@GetMapping(value= {"/callback"})
	public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
		String code = request.getParameter("code");
		if(code != null) {
			saveToken(code);
			
			return "dashboard.html";
		}
		
		return "index.html";
	}
	
	private void saveToken(String code) throws Exception {
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
		flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
	}
	
	@GetMapping(value= {"/create"})
	public void createFile(HttpServletResponse response) throws Exception{
		Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
		
		// Drive 객체 생성 : setApplicationName 에 구글 프로젝트 이름 입력
		Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName("drive").build();
		
		File file = new File();
		String fileName = timeStamp();
		file.setName(fileName + ".png");
		
		// 파일 지정
		FileContent	content = new FileContent("image/png", new java.io.File(getClass().getResource("/img/logo.png").toURI()));
		File uploadedFile = drive.files().create(file, content).setFields("id").execute();
		
		String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
		response.getWriter().write(fileReference);
		
	}
	
	@GetMapping(value= {"/list"}, produces= {"application/json"})
	public @ResponseBody List<FileItemDTO> listFiles() throws Exception{
		Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
		
		// Drive 객체 생성 : setApplicationName 에 구글 프로젝트 이름 입력
		Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName("drive").build();
		
		List<FileItemDTO> responseList = new ArrayList<>();
		
		
		FileList fileList = drive.files().list().setFields("files(id, name, mimeType, shared, kind, webViewLink)").execute();
		for(File file : fileList.getFiles()) {
			FileItemDTO item = new FileItemDTO();
			item.setId(file.getId());
			item.setName(file.getName());
			item.setMimetype(file.getMimeType());
			item.setShared(file.getShared());
			item.setKind(file.getKind());
			item.setWebViewLink(file.getWebViewLink());
			
			responseList.add(item);
		}
		
		return responseList;
	}
	
	
	private String timeStamp() {
		Timestamp	timestamp	= new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		
		return sdf.format(timestamp);
	}
}
