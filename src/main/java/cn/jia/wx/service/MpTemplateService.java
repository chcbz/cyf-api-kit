package cn.jia.wx.service;

import cn.jia.wx.entity.MpTemplate;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MpTemplateService {
	
	@Autowired
	private IMpTemplateService mpTemplateService;

	public Boolean create(MpTemplate mpTemplate) {
		return mpTemplateService.save(mpTemplate);
	}

	public MpTemplate find(String templateId) {
		return mpTemplateService.getById(templateId);
	}

	public PageInfo<MpTemplate> list(MpTemplate example, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<MpTemplate> payOrderList = list(example);
		return new PageInfo<>(payOrderList);
	}

	public List<MpTemplate> list(MpTemplate example) {
		return mpTemplateService.listByEntity(example);
	}

	public Boolean update(MpTemplate mpTemplate) {
		return mpTemplateService.updateById(mpTemplate);
	}

	public Boolean delete(Integer id) {
		return mpTemplateService.removeById(id);
	}

	public List<MpTemplate> selectAll() {
		return mpTemplateService.list();
	}

}
