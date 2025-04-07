package wang.jinjing.common.controller.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.pojo.DTO.BaseDTO;

import java.util.List;
import java.util.Map;

public interface RestfulAPIs<DTO extends BaseDTO> {
    @PostMapping
    ResponseEntity<?> addOne(@RequestBody DTO dto);

    @PutMapping("/{id}")
    ResponseEntity<?> updateOne(@PathVariable Long id, @RequestBody DTO dto);

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteOne(@PathVariable Long id);

    @GetMapping("/{id}")
    ResponseEntity<?> findById(@PathVariable Long id);

    @PostMapping("/batch")
    ResponseEntity<?> addBatch(@RequestBody List<DTO> dtos);

    @DeleteMapping("/batch")
    ResponseEntity<?> deleteBatch(@RequestBody List<Long> ids);

    @GetMapping("/search")
    ResponseEntity<?> search(@ModelAttribute DTO dto,
                             @RequestParam(required = false) Map<String, Object> args,
                             @RequestParam(required = false) String sorts,
                             @RequestParam(required = false) Integer page,
                             @RequestParam(required = false) Integer size);
}
