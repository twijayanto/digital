package com.sawitpro.digital.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.sawitpro.digital.constant.ImageWords;
import com.sawitpro.digital.dto.OcrSpaceResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Service
public class ImageService {

    @Value("${resources.static.img1-path}")
    private String image1Path;

    @Value("${resources.static.img2-path}")
    private String image2Path;

    @Value("${resources.static.img3-path}")
    private String image3Path;

    @Value("${resources.static.img4-path}")
    private String image4Path;

    private final OcrSpaceService ocrSpaceService;

    private final GDriveManager gDriveManager;

    private static final String FILE_PATH = "sawitpro-file";

    public List<File> doExtractText(ImageWords iw) throws Exception {
        ClassPathResource resource = new ClassPathResource(chooseImages(iw));
        if (resource.exists() && resource.isFile()) {
            OcrSpaceResponseDTO ocrSpaceResponseDTO = ocrSpaceService.getParseImage(resource.getFile());
            if (ocrSpaceResponseDTO.isErroredOnProcessing()) {
                throw new Exception("error while extract");
            }
            if (ocrSpaceResponseDTO.getParsedResults().isEmpty()) {
                throw new Exception("error parsed text is empty");
            }
            Stream<String> text = ocrSpaceResponseDTO.getParsedResults().get(0).getParsedText().lines();
            List<String> eng = new ArrayList<>();
            List<String> chn = new ArrayList<>();
            text.forEach(s -> {
                boolean isContainsO = false;
                for (int i=0; i<s.length(); i++) {
                    if (s.charAt(i) == 'o') {
                        isContainsO = true;
                        break;
                    }
                    if ((i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z')) {

                    }
                }
                if (isContainsO) {
                    String[] words = s.split(" ");
                    if (words.length > 1) {
                        boolean isAlphabet = false;
                        for (String word : words) {
                            if (word.matches("^[a-zA-Z]*$")) {
                                isAlphabet = true;
                            }
                        }
                        if (isAlphabet) {
                            eng.add("<blue>"+s+"</blue>");
                        } else {
                            chn.add("<blue>"+s+"</blue>");
                        }
                    } else {
                        if (s.matches("^[a-zA-Z]*$")) {
                            eng.add("<blue>"+s+"</blue>");
                        } else {
                            chn.add("<blue>"+s+"</blue>");
                        }
                    }

                } else {
                    String[] words = s.split(" ");
                    if (words.length > 1) {
                        boolean isAlphabet = false;
                        for (String word : words) {
                            if (word.matches("^[a-zA-Z]*$")) {
                                isAlphabet = true;
                            }
                        }
                        if (isAlphabet) {
                            eng.add(s);
                        } else {
                            chn.add(s);
                        }
                    } else {
                        if (s.matches("^[a-zA-Z]*$")) {
                            eng.add(s);
                        } else {
                            chn.add(s);
                        }
                    }
                }
            });
            List<File> files = new ArrayList<>();
            if (!eng.isEmpty()) {
                File fileEng = new File("eng.txt");
                FileUtils.writeStringToFile(fileEng,
                        eng.stream().collect(Collectors.joining("\r\n")),
                        StandardCharsets.UTF_8);
                files.add(fileEng);
            }
            if (!chn.isEmpty()) {
                File fileChn = new File("chn.txt");
                FileUtils.writeStringToFile(fileChn,
                        chn.stream().collect(Collectors.joining("\r\n")),
                        StandardCharsets.UTF_8);
                files.add(fileChn);
            }
            return files;
        } else {
            throw new Exception("file not found");
        }
    }

    private String chooseImages(ImageWords iw) {
        String img = null;
        switch (iw) {
            case IMAGE_WORDS_1:
                img = image1Path;
                break;
            case IMAGE_WORDS_2:
                img = image2Path;
                break;
            case IMAGE_WORDS_3:
                img = image3Path;
                break;
            case IMAGE_WORDS_4:
                img = image4Path;
                break;

        }
        return img;
    }

    public String uploadFile(MultipartFile file) {
        try {
            String folderId = getFolderId(FILE_PATH);
            if (Objects.nonNull(file)) {
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setName(file.getOriginalFilename());
                com.google.api.services.drive.model.File uploadFile = gDriveManager.getInstance()
                        .files()
                        .create(fileMetadata, new InputStreamContent(
                                file.getContentType(),
                                new ByteArrayInputStream(file.getBytes()))
                        )
                        .setFields("id").execute();
                log.info("uploadFile getId: {}", uploadFile.getId());
                return uploadFile.getId();
            }
        } catch (Exception e) {
            log.error("ImageService::uploadFile Error {}", e.getMessage());
        }
        return null;
    }

    private String getFolderId(String path) throws Exception {
        String parentId = null;
        String[] folderNames = path.split("/");

        Drive driveInstance = gDriveManager.getInstance();
        for (String name : folderNames) {
            parentId = findOrCreateFolder(parentId, name, driveInstance);
        }
        return parentId;
    }

    private String findOrCreateFolder(String parentId, String folderName, Drive driveInstance) throws Exception {
        String folderId = searchFolderId(parentId, folderName, driveInstance);
        // Folder already exists, so return id
        if (folderId != null) {
            return folderId;
        }
        //Folder dont exists, create it and return folderId
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setName(folderName);

        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }
        return driveInstance.files().create(fileMetadata)
                .setFields("id")
                .execute()
                .getId();
    }

    private String searchFolderId(String parentId, String folderName, Drive service) throws Exception {
        String folderId = null;
        String pageToken = null;
        FileList result = null;

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setName(folderName);

        do {
            String query = " mimeType = 'application/vnd.google-apps.folder' ";
            if (parentId == null) {
                query = query + " and 'root' in parents";
            } else {
                query = query + " and '" + parentId + "' in parents";
            }
            result = service.files().list().setQ(query)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                if (file.getName().equalsIgnoreCase(folderName)) {
                    folderId = file.getId();
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null && folderId == null);

        return folderId;
    }

}
