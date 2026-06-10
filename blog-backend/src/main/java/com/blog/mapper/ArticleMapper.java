package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;
import com.blog.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    ArticleVO getArticleDetail(@Param("id") Long id);

    ArticleVO getArticleForEdit(@Param("id") Long id, @Param("userId") Long userId);

    List<ArticleVO> getArticleList(@Param("categoryId") Long categoryId,
                                   @Param("tagId") Long tagId,
                                   @Param("keyword") String keyword,
                                   @Param("userId") Long userId);

    List<ArticleVO> getDraftList(@Param("userId") Long userId);

    void incrementViewCount(@Param("id") Long id);

    void incrementLikeCount(@Param("id") Long id);

    void incrementCommentCount(@Param("id") Long id);
}
