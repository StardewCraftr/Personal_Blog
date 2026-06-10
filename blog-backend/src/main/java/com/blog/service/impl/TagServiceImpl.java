package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Tag;
import com.blog.exception.BusinessException;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<TagVO> getAllTags() {
        List<Tag> tags = list();
        return tags.stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setLink(t.getLink());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void createTag(String name, String link) {
        Tag exist = getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (exist != null) {
            throw new BusinessException("标签名称已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setLink(link);
        save(tag);
    }

    @Override
    public void updateTag(Long id, String name, String link) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        Tag exist = getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name).ne(Tag::getId, id));
        if (exist != null) {
            throw new BusinessException("标签名称已存在");
        }
        tag.setName(name);
        tag.setLink(link);
        updateById(tag);
    }

    @Override
    public void deleteTag(Long id) {
        removeById(id);
    }
}
