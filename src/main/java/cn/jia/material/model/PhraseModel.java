package cn.jia.material.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PhraseModel implements Serializable {

    private Integer id;

    private String content;
}
