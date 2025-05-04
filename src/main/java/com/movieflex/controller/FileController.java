package com.movieflex.controller;

import com.movieflex.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/file/")
public class FileController {

    @Autowired
    private FileService fileService;

    @Value("${project.poster}")
    private String Path;

    @PostMapping("/upload")
    public ResponseEntity<String > uploadFileHandler(@RequestParam MultipartFile file)throws IOException{

        String uploadFileName = fileService.uploadFile(Path,file);

        return ResponseEntity.ok("file Uploaded:"+ uploadFileName);
    }

    @GetMapping("/{fileName}")
    public void serveFileHandler(@PathVariable String fileName, HttpServletResponse httpServletResponse) throws IOException {
        InputStream responseFile = fileService.getResourceFile(Path,fileName);
        httpServletResponse.setContentType(MediaType.IMAGE_PNG_VALUE);
        StreamUtils.copy(responseFile,httpServletResponse.getOutputStream());
    }
}
