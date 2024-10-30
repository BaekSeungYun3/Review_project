package com.hk.board.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartRequest;

import com.hk.board.command.InsertBoardCommand;
import com.hk.board.command.UpdateBoardCommand;
import com.hk.board.dtos.BoardDto;
import com.hk.board.dtos.FileBoardDto;
import com.hk.board.mapper.BoardMapper;
import com.hk.board.mapper.FileMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class BoardService {

    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileService fileService;

    // 글목록 조회
    public List<BoardDto> getAllList() {
        return boardMapper.getAllList();
    }

    // 글 추가, 파일 업로드 및 파일 정보 추가
    @Transactional(rollbackFor = Exception.class)
    public void insertBoard(InsertBoardCommand insertBoardCommand,
                            MultipartRequest multipartRequest,
                            HttpServletRequest request) 
                            throws IllegalStateException, IOException {

        // 1. Command -> DTO로 데이터 옮겨 담기
        BoardDto boardDto = new BoardDto();
        boardDto.setId(insertBoardCommand.getId());
        boardDto.setTitle(insertBoardCommand.getTitle());
        boardDto.setContent(insertBoardCommand.getContent());

        // 2. 새 글 추가 (board_seq 값이 자동 증가하여 DTO에 반영됨)
        boardMapper.insertBoard(boardDto);

        // 3. 파일 첨부 여부 확인
        System.out.println("파일첨부여부: " + multipartRequest.getFiles("filename").get(0).isEmpty());

        // 4. 첨부된 파일이 있는 경우에만 처리
        if (!multipartRequest.getFiles("filename").get(0).isEmpty()) {
            // 5. 파일 저장 경로 설정 (절대경로)
            String filepath = request.getSession().getServletContext().getRealPath("upload");
            System.out.println("파일 저장 경로: " + filepath);

            // **경로가 없으면 디렉터리 생성**
            File dir = new File(filepath);
            if (!dir.exists()) {
                dir.mkdirs(); // 경로가 없으면 생성
                System.out.println("경로가 존재하지 않아 새로 생성했습니다.");
            }

            // 6. 파일 업로드 수행 (FileService에서 처리) 및 파일 정보 반환
            List<FileBoardDto> uploadFileList = fileService.uploadFiles(filepath, multipartRequest);

            // 7. 업로드된 파일 정보를 DB에 저장
            for (FileBoardDto fDto : uploadFileList) {
                fileMapper.insertFileBoard(
                    new FileBoardDto(0, boardDto.getBoard_seq(),  // 증가된 board_seq 사용
                                     fDto.getOrigin_filename(), 
                                     fDto.getStored_filename())
                );
            }
        }
    }

    // 상세 내용 조회
    public BoardDto getBoard(int board_seq) {
        return boardMapper.getBoard(board_seq);
    }

    // 게시글 수정
    public boolean updateBoard(UpdateBoardCommand updateBoardCommand) {
        BoardDto dto = new BoardDto();
        dto.setBoard_seq(updateBoardCommand.getBoard_seq());
        dto.setTitle(updateBoardCommand.getTitle());
        dto.setContent(updateBoardCommand.getContent());
        return boardMapper.updateBoard(dto);
    }

    // 여러 게시글 삭제
    public boolean mulDel(String[] seqs) {
        return boardMapper.mulDel(seqs);
    }
//    public List<FileBoardDto> getAllUploadedPhotos() {
//        return fileBoardRepository.findAll(); // 업로드된 파일 전체 조회
//    }
}
