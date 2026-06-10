package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.PageResult;
import com.blog.dto.ArticleDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.ArticleVersion;
import com.blog.entity.Tag;
import com.blog.exception.BusinessException;
import com.blog.util.XssUtil;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.ArticleVersionMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleService;
import com.blog.vo.ArticleVO;
import com.blog.vo.TagVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleVersionMapper articleVersionMapper;
    private final TagMapper tagMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper, ArticleTagMapper articleTagMapper,
                              ArticleVersionMapper articleVersionMapper, TagMapper tagMapper) {
        this.articleMapper = articleMapper;
        this.articleTagMapper = articleTagMapper;
        this.articleVersionMapper = articleVersionMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public ArticleVO getDetail(Long id) {
        ArticleVO vo = articleMapper.getArticleDetail(id);
        if (vo == null) {
            throw new BusinessException("文章不存在");
        }
        return vo;
    }

    @Override
    public ArticleVO getArticleForEdit(Long id, Long userId) {
        ArticleVO vo = articleMapper.getArticleForEdit(id, userId);
        if (vo == null) {
            throw new BusinessException("文章不存在或无权编辑");
        }
        return vo;
    }

    @Override
    public PageResult<ArticleVO> getList(Long categoryId, Long tagId, String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleVO> list = articleMapper.getArticleList(categoryId, tagId, keyword, null);
        PageInfo<ArticleVO> pageInfo = new PageInfo<>(list);
        return PageResult.of(list, pageInfo.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<ArticleVO> getMyArticles(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleVO> list = articleMapper.getArticleList(null, null, null, userId);
        PageInfo<ArticleVO> pageInfo = new PageInfo<>(list);
        return PageResult.of(list, pageInfo.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<ArticleVO> getDrafts(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ArticleVO> list = articleMapper.getDraftList(userId);
        PageInfo<ArticleVO> pageInfo = new PageInfo<>(list);
        return PageResult.of(list, pageInfo.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public Long createArticle(ArticleDTO dto, Long userId) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(XssUtil.sanitize(dto.getSummary()));
        article.setContent(XssUtil.sanitize(dto.getContent()));
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        article.setIsPrivate(dto.getIsPrivate() != null ? dto.getIsPrivate() : 0);
        article.setCategoryId(dto.getCategoryId());
        article.setUserId(userId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        if (article.getStatus() == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.insert(article);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), dto.getTagIds());
        }

        return article.getId();
    }

    @Override
    @Transactional
    public void updateArticle(ArticleDTO dto, Long userId) {
        Article article = articleMapper.selectById(dto.getId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此文章");
        }

        saveArticleVersion(article);

        article.setTitle(dto.getTitle());
        article.setSummary(XssUtil.sanitize(dto.getSummary()));
        article.setContent(XssUtil.sanitize(dto.getContent()));
        article.setStatus(dto.getStatus());
        article.setIsPrivate(dto.getIsPrivate());
        article.setCategoryId(dto.getCategoryId());
        if (article.getStatus() == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.updateById(article);

        articleTagMapper.deleteByArticleId(article.getId());
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), dto.getTagIds());
        }
    }

    @Override
    @Transactional
    public void deleteArticle(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此文章");
        }
        articleMapper.deleteById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        articleMapper.incrementViewCount(id);
    }

    @Override
    public void incrementLikeCount(Long id) {
        articleMapper.incrementLikeCount(id);
    }

    @Override
    public List<LocalDate> getArticleDates(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Article::getCreateTime, start)
               .le(Article::getCreateTime, end)
               .eq(Article::getStatus, 1)
               .eq(Article::getDeleted, 0)
               .select(Article::getCreateTime, Article::getUpdateTime);
        
        List<Article> articles = articleMapper.selectList(wrapper);
        
        Set<LocalDate> dates = new HashSet<>();
        for (Article article : articles) {
            dates.add(article.getCreateTime().toLocalDate());
            if (article.getUpdateTime() != null) {
                dates.add(article.getUpdateTime().toLocalDate());
            }
        }
        
        return new ArrayList<>(dates);
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }

    private void saveArticleVersion(Article article) {
        Integer maxVersion = getMaxVersion(article.getId());
        ArticleVersion version = new ArticleVersion();
        version.setArticleId(article.getId());
        version.setTitle(article.getTitle());
        version.setContent(article.getContent());
        version.setVersionNo(maxVersion + 1);
        articleVersionMapper.insert(version);
    }

    private Integer getMaxVersion(Long articleId) {
        LambdaQueryWrapper<ArticleVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleVersion::getArticleId, articleId)
               .orderByDesc(ArticleVersion::getVersionNo)
               .last("LIMIT 1");
        ArticleVersion version = articleVersionMapper.selectOne(wrapper);
        return version != null ? version.getVersionNo() : 0;
    }
}
