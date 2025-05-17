package wang.jinjing.editor.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.editor.pojo.VO.OssFileMetadataVO;
import wang.jinjing.editor.pojo.VO.OssRecycleMetadataVO;
import wang.jinjing.editor.service.file.UserFileService;

import java.util.List;

import static wang.jinjing.common.controller.AbstractRestfulAPIsController.getSortFromMap;

@RestController
@RequestMapping("/api/recycle")
public class UserRecycleController {

    @Autowired
    private UserFileService userFileService;

    @GetMapping("")
    public ResponseEntity<?> listRecycleFiles(
            @RequestParam(required = false) String sorts
    ){
        Sort sort = Sort.unsorted();
        if (sorts != null) {
            sort = getSortFromMap(sorts);
        } else {
            Sort.Order order1 = Sort.Order.desc("delete_at");
            Sort.Order order2 = Sort.Order.desc("is_dir");
            Sort.Order order3 = Sort.Order.asc("real_file_name");
            sort = Sort.by(order1, order2, order3);
        }
        List<OssRecycleMetadataVO> ossRecycleMetadataVOS = userFileService.listRecycleFiles(sort);
        return ResponseEntity.ok().body(ossRecycleMetadataVOS);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> recoveryRecycleFile(@PathVariable Long id) {
        OssFileMetadataVO ossFileMetadataVO = userFileService.recoveryRecycleFile(id);
        return ResponseEntity.ok().body(ossFileMetadataVO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecycleFile(@PathVariable Long id) {
        userFileService.deleteRecycleFile(id);
        return ResponseEntity.ok().build();
    }


}
