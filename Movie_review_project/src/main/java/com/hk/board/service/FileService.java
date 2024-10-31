package com.hk.board.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import com.hk.board.dtos.FileBoardDto;
import com.hk.board.mapper.FileMapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class FileService {

    @Autowired
    private FileMapper fileMapper;

    // file.upload-dir 설정을 가져옴
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 서버 시작 시 경로가 없으면 자동 생성
    @jakarta.annotation.PostConstruct
    public void init() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("파일 업로드 디렉토리를 생성했습니다: " + uploadDir);
        }
    }

    // 파일 업로드하기
    @Transactional
    public List<FileBoardDto> uploadFiles(MultipartRequest multipartRequest) 
            throws IllegalStateException, IOException {
        List<MultipartFile> multipartFiles = multipartRequest.getFiles("filename");
        List<FileBoardDto> uploadFileList = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            String origin_filename = multipartFile.getOriginalFilename();
            String stored_filename = UUID.randomUUID() + origin_filename.substring(origin_filename.indexOf("."));
            String fileuploadUrl = new File(uploadDir).getAbsolutePath() + "/" + stored_filename;

            multipartFile.transferTo(new File(fileuploadUrl));
            uploadFileList.add(new FileBoardDto(0, 0, origin_filename, stored_filename));
        }

        return uploadFileList;
    }

    // 파일 정보 가져오기
    public FileBoardDto getFileInfo(int file_seq) {
        return fileMapper.getFileInfo(file_seq);
    }

    // 파일 다운로드
    public void fileDownload(String origin_filename, String stored_filename, HttpServletRequest request,
                             HttpServletResponse response) throws UnsupportedEncodingException {

        String filePath = new File(uploadDir).getAbsolutePath();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; fileName=" + URLEncoder.encode(origin_filename, "UTF-8"));
        response.setHeader("Content-transfer", "binary");

        File file = new File(filePath + "/" + stored_filename);

        try (FileInputStream fs = new FileInputStream(file); ServletOutputStream out = response.getOutputStream()) {
            FileCopyUtils.copy(fs, out);
            response.flushBuffer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ee) {
            ee.printStackTrace();
        }
    }
}
