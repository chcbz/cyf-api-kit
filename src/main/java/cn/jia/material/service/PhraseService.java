package cn.jia.material.service;

import cn.jia.material.entity.Phrase;
import cn.jia.material.entity.PhraseVote;

public interface PhraseService {
	
	Phrase create(Phrase phrase) throws Exception;

	Phrase find(Integer id);

	Phrase update(Phrase phrase);

	void delete(Integer id);
	
	Phrase findRandom(Phrase example);

	void vote(PhraseVote vote) throws Exception;

	void read(Integer id) throws Exception;
}
