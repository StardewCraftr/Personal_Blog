package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/public/list")
    public Result<List<TagVO>> getAllTags() {
        return Result.success(tagService.getAllTags());
    }

    @PostMapping
    public Result<Void> createTag(@RequestParam String name, @RequestParam(required = false) String link) {
        tagService.createTag(name, link);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) String link) {
        tagService.updateTag(id, name, link);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
