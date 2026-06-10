package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Tag;
import com.blog.vo.TagVO;

import java.util.List;

public interface TagService extends IService<Tag> {
    List<TagVO> getAllTags();

    void createTag(String name, String link);

    void updateTag(Long id, String name, String link);

    void deleteTag(Long id);
}
