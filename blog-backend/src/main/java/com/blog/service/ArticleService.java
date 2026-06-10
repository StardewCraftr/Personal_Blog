package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.ArticleDTO;
import com.blog.vo.ArticleVO;
import com.blog.common.PageResult;

import java.util.List;
import java.time.LocalDate;

public interface ArticleService {
    ArticleVO getDetail(Long id);

    ArticleVO getArticleForEdit(Long id, Long userId);

    PageResult<ArticleVO> getList(Long categoryId, Long tagId, String keyword, Integer pageNum, Integer pageSize);

    PageResult<ArticleVO> getMyArticles(Long userId, Integer pageNum, Integer pageSize);

    PageResult<ArticleVO> getDrafts(Long userId, Integer pageNum, Integer pageSize);

    Long createArticle(ArticleDTO dto, Long userId);

    void updateArticle(ArticleDTO dto, Long userId);

    void deleteArticle(Long id, Long userId);

    void incrementViewCount(Long id);

    void incrementLikeCount(Long id);

    List<LocalDate> getArticleDates(int year, int month);
}
