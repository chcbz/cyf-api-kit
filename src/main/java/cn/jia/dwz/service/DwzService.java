package cn.jia.dwz.service;

import cn.jia.dwz.entity.DwzRecord;
import com.github.pagehelper.Page;

public interface DwzService {
	
	DwzRecord view(String uri) throws Exception;

	String gen(String jiacn, String orgi, Long expireTime);

	DwzRecord find(Integer id) throws Exception;

	DwzRecord update(DwzRecord record);

	void delete(Integer id);

	Page<DwzRecord> list(DwzRecord example, int pageNo, int pageSize);
}
