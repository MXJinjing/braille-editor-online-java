package wang.jinjing.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.pojo.DTO.BaseDTO;

import java.util.List;

public abstract class RestfulAPIsController<DTO extends BaseDTO> {

    @PostMapping
    public abstract ResponseEntity<?> addOne(@RequestBody DTO dto);

    @PutMapping("/{id}")
    public abstract ResponseEntity<?> updateOne(@PathVariable Long id, @RequestBody DTO dto);

    @DeleteMapping("/{id}")
    public abstract ResponseEntity<?> deleteOne(@PathVariable Long id);

    @GetMapping("/{id}")
    public abstract ResponseEntity<?> findById(@PathVariable Long id);

    @PostMapping("/batch")
    public abstract ResponseEntity<?> addBatch(@RequestBody List<DTO> dtos);

    @DeleteMapping("/batch")
    public abstract ResponseEntity<?> deleteBatch(@RequestBody List<Long> ids);

    @GetMapping
    public abstract ResponseEntity<?> listPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/search")
    public abstract ResponseEntity<?> search(@ModelAttribute DTO dto);

}
