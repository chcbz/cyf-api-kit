package cn.jia.kefu.service;

import cn.jia.kefu.entity.KefuFAQ;
import cn.jia.kefu.entity.KefuMessage;
import com.github.pagehelper.Page;

public interface KefuService {

	Page<KefuFAQ> listFAQ(KefuFAQ example, int pageNo, int pageSize);

	KefuFAQ createFAQ(KefuFAQ record);

	KefuFAQ findFAQ(Integer id) throws Exception;

	KefuFAQ updateFAQ(KefuFAQ record);

	void deleteFAQ(Integer id);

	Page<KefuMessage> listMessage(KefuMessage example, int pageNo, int pageSize);

	KefuMessage createMessage(KefuMessage record);

	KefuMessage findMessage(Integer id) throws Exception;

	KefuMessage updateMessage(KefuMessage record);

	void deleteMessage(Integer id);

}
