package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleDTO;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.vo.ArticleVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/public/{id}")
    public Result<ArticleVO> getDetail(@PathVariable Long id) {
        ArticleVO vo = articleService.getDetail(id);
        articleService.incrementViewCount(id);
        return Result.success(vo);
    }

    @GetMapping("/edit/{id}")
    public Result<ArticleVO> getArticleForEdit(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success(articleService.getArticleForEdit(id, userId));
    }

    @GetMapping("/public/list")
    public Result<PageResult<ArticleVO>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(articleService.getList(categoryId, tagId, keyword, pageNum, pageSize));
    }

    @GetMapping("/public/dates")
    public Result<List<LocalDate>> getArticleDates(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(articleService.getArticleDates(year, month));
    }

    @GetMapping("/my")
    public Result<PageResult<ArticleVO>> getMyArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        return Result.success(articleService.getMyArticles(userId, pageNum, pageSize));
    }

    @GetMapping("/drafts")
    public Result<PageResult<ArticleVO>> getDrafts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        return Result.success(articleService.getDrafts(userId, pageNum, pageSize));
    }

    @PostMapping
    public Result<Long> createArticle(@RequestBody ArticleDTO dto) {
        Long userId = getCurrentUserId();
        return Result.success(articleService.createArticle(dto, userId));
    }

    @PutMapping
    public Result<Void> updateArticle(@RequestBody ArticleDTO dto) {
        Long userId = getCurrentUserId();
        articleService.updateArticle(dto, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        articleService.deleteArticle(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeArticle(@PathVariable Long id) {
        articleService.incrementLikeCount(id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        return null;
    }
}
