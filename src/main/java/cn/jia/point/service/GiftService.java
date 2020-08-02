package cn.jia.point.service;

import cn.jia.point.entity.Gift;
import cn.jia.point.entity.GiftExample;
import cn.jia.point.entity.GiftUsage;
import com.github.pagehelper.Page;

public interface GiftService {
	
	Gift create(Gift gift);

	Gift find(Integer id) throws Exception;

	Gift update(Gift gift);

	void delete(Integer id);
	
	Page<Gift> list(int pageNo, int pageSize, GiftExample example);
	
	void usage(GiftUsage record) throws Exception;

	void usageCancel(Integer giftUsageId) throws Exception;

	void usageDelete(Integer giftUsageId) throws Exception;
	
	Page<GiftUsage> usageListByGift(int pageNum, int pageSize, Integer giftId);
	
	Page<GiftUsage> usageListByUser(int pageNum, int pageSize, String jiacn);
}
