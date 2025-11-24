package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.PaintingImage;
import com.example.talkingCanvas.repository.PaintingImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final PaintingImageRepository paintingImageRepository;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        PaintingImage image = paintingImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaintingImage", "id", id));

        if (image.getData() == null || image.getData().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String contentType = image.getContentType() != null ? image.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        if (image.getFileName() != null && !image.getFileName().isEmpty()) {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFileName() + "\"");
        }
        return new ResponseEntity<>(image.getData(), headers, HttpStatus.OK);
    }
}
