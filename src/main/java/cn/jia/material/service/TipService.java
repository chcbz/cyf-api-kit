package cn.jia.material.service;

import cn.jia.material.entity.Tip;
import com.github.pagehelper.Page;

public interface TipService {
	
	Tip create(Tip tip);

	Tip find(Integer id);

	Tip update(Tip tip);

	void delete(Integer id);
	
	Page<Tip> list(int pageNo, int pageSize, Tip example);
}
