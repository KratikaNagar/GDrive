package com.google.api.services.samples.drive.cmdline.quickstart;

// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START drive_quickstart]
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DriveQuickstart {
  private static final String APPLICATION_NAME = "My Drive";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String CREDENTIALS_FOLDER = "credentials";

  /**
   * Global instance of the scopes required by this quickstart. If modifying these scopes, delete
   * your previously saved credentials/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
  // private static final String CLIENT_SECRET_DIR = "./client_secret.json";

  /**
   * Creates an authorized Credential object.
   * 
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If there is no client_secret.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = DriveQuickstart.class.getResourceAsStream("/client_secrets.json");
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
            .setAccessType("offline").build();
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("webproxy401", 8080));

    final NetHttpTransport HTTP_TRANSPORT =
        new NetHttpTransport.Builder().doNotValidateCertificate().setProxy(proxy).build();

    // final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME).build();

    // Print the names and IDs for up to 10 files.
    /*
     * FileList result = service.files().list().setPageSize(10) .setFields(
     * "nextPageToken, files(id, name)").execute();
     */

    /*
     * FileList result = drive.files().list().execute(); List<File> files = result.getItems(); if
     * (files == null || files.isEmpty()) { System.out.println("No files found."); } else {
     * System.out.println("Files:"); for (File file : files) { System.out.printf("%s (%s)\n",
     * file.getTitle(), file.getId()); } }
     */

    Scanner sc = new Scanner(System.in);



    do {
      System.out.println(
          " 1:List \n 2:Download \n 3:Upload \n 4:Delete \n 5:Folder create & upload a file in it");
      switch (sc.nextInt()) {
        case 1:
          fileList(drive);

          break;

        case 2:
          fileDownload(drive);
          break;

        case 3:
          fileUpload(drive);
          break;

        case 4:
          fileDelete(drive);
          break;

        case 5:
          folderCreate(drive);
          break;

        default:
          System.out.println("No such case");

      }
      System.out.println("Want to stop: 1:yes 2:no");
    } while (sc.nextInt() == 2);


  }


  /**
   * @param drive
   */


  public static void fileList(Drive drive) throws IOException {
    FileList result = drive.files().list().execute();
    List<File> files = result.getItems();
    if (files == null || files.isEmpty()) {
      System.out.println("No files found.");
    } else {
      System.out.println("Files:");
      for (File file : files) {
        System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
      }
    }
  }

  public static void fileDownload(Drive drive) throws IOException {
    String fileId = "Enter fileId";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);

    FileOutputStream fileName = new FileOutputStream("abc.doc");
    outputStream.writeTo(fileName);

    fileName.flush();
    fileName.close();

    outputStream.flush();
    outputStream.close();
  }

  public static void fileUpload(Drive drive) throws IOException {
    File fileMetadata = new File();
    fileMetadata.setTitle("abcd.png");

    java.io.File filePath = new java.io.File("abcd.png");
    FileContent mediaContent = new FileContent("image/jpeg", filePath);
    File file = drive.files().insert(fileMetadata, mediaContent).setFields("id").execute();
    System.out.println("File ID: " + file.getId());
  }

  private static void fileDelete(Drive drive) throws IOException {
    String fileId = "Enter fileId";
    drive.files().delete(fileId).execute();
    fileList(drive);

  }

  private static void folderCreate(Drive drive) throws IOException {
    File fileMetadata = new File();
    fileMetadata.setTitle("MyGDriveApi");
    fileMetadata.setMimeType("application/vnd.google-apps.folder");

    File file = drive.files().insert(fileMetadata).setFields("id").execute();
    System.out.println("Folder ID: " + file.getId());
    fileInFolderCreate(drive, file.getId());

  }

  private static void fileInFolderCreate(Drive drive, String folderId) throws IOException {

    File fileMetadata = new File();
    fileMetadata.setTitle("abcd.png");
    ParentReference parentReference = new ParentReference();
    parentReference.setId(folderId);
    fileMetadata.setParents(Collections.singletonList(parentReference));
    java.io.File filePath = new java.io.File("abcd.png");
    FileContent mediaContent = new FileContent("image/jpeg", filePath);
    File file = drive.files().insert(fileMetadata, mediaContent).setFields("id, parents").execute();
    System.out.println("File ID: " + file.getId());
  }

}
// [END drive_quickstart]
