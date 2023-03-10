package com.sawitpro.digital.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sawitpro.digital.dto.OcrSpaceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OcrSpaceService {

    @Value("${ocr.space.url}")
    private String ocrSpaceUrl;

    @Value("${ocr.space.apikey}")
    private String getOcrSpaceApikey;

    public OcrSpaceResponseDTO getParseImage(File file) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request request = new Request.Builder()
                .url(ocrSpaceUrl)
                .method("POST", body)
                .addHeader("apikey", getOcrSpaceApikey)
                .build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        log.info("OcrSpaceService::getParseImage result {}", result);
        return new ObjectMapper().readValue(result, OcrSpaceResponseDTO.class);
    }
}
