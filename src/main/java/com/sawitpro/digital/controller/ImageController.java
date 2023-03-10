package com.sawitpro.digital.controller;

import com.sawitpro.digital.constant.ImageWords;
import com.sawitpro.digital.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Slf4j
@RestController
public class ImageController {

    private final ImageService imageService;

    @GetMapping(value = "/image/extract-text")
    public void doExtractText(@RequestParam ImageWords images, HttpServletResponse response) throws Exception {
        String zipFileName = images.name()+"_result.zip";
        List<File> result = imageService.doExtractText(images);
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + zipFileName + "\"");
        for (var file : result) {
            FileSystemResource resource = new FileSystemResource(file);
            ZipEntry zipEntry = new ZipEntry(resource.getFilename());
            zipEntry.setSize(resource.contentLength());
            zipOut.putNextEntry(zipEntry);
            StreamUtils.copy(resource.getInputStream(), zipOut);
            zipOut.closeEntry();

        }
        zipOut.finish();
        zipOut.close();
    }

    @PostMapping(value = "/image/upload")
    public ResponseEntity<String> upload(@RequestBody MultipartFile file) {
        log.info("ImageController::upload File: " + file.getOriginalFilename());
        String fileId = imageService.uploadFile(file);
        if(fileId == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok("Success, FileId: "+ fileId);
    }
}
