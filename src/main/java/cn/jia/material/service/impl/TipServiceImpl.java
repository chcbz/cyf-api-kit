package cn.jia.material.service.impl;

import cn.jia.core.util.DateUtil;
import cn.jia.material.dao.TipMapper;
import cn.jia.material.entity.Tip;
import cn.jia.material.service.TipService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TipServiceImpl implements TipService {
	
	@Autowired
	private TipMapper tipMapper;
	
	@Override
	public Tip create(Tip tip) {
		Long now = DateUtil.genTime(new Date());
		tip.setTime(now);
		tipMapper.insertSelective(tip);
		return tip;
	}

	@Override
	public Tip find(Integer id) {
		return tipMapper.selectByPrimaryKey(id);
	}

	@Override
	public Tip update(Tip tip) {
		Long now = DateUtil.genTime(new Date());
		tip.setTime(now);
		tipMapper.updateByPrimaryKeySelective(tip);
		return tip;
	}

	@Override
	public void delete(Integer id) {
		tipMapper.deleteByPrimaryKey(id);
	}

	@Override
	public Page<Tip> list(int pageNo, int pageSize, Tip example) {
		PageHelper.startPage(pageNo, pageSize);
		return tipMapper.selectByExample(example);
	}

}
